package cn.rongcloud.im.utils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Objects;

import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.common.NetConstant;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.utils.log.SLog;

/**
 * 此类用于结合处理网络请求和数据库请求。
 * 返回结果始终来源于数据库，当有网络请求时将请求保存至数据库后返回数据库中结果。
 *
 * @param <ResultType>  网络请求结果
 * @param <RequestType> 数据库中数据结果
 */
public abstract class NetworkBoundResource<ResultType, RequestType> {
    private final ThreadManager threadManager;

    private final MediatorLiveData<Resource<ResultType>> result = new MediatorLiveData<>();

    @MainThread
    public NetworkBoundResource() {
        this.threadManager = ThreadManager.getInstance();

        if (threadManager.isInMainThread()) {
            init();
        } else {
            threadManager.runOnUIThread(() -> init());
        }
    }

    private void init() {
        result.setValue(Resource.loading(null));
        LiveData<ResultType> dbSource = safeLoadFromDb();
        result.addSource(dbSource, data -> {
            result.removeSource(dbSource);
            if (shouldFetch(data)) {
                fetchFromNetwork(dbSource);
            } else {
                result.addSource(dbSource, newData -> setValue(Resource.success(newData)));
            }
        });
    }

    @MainThread
    private void setValue(Resource<ResultType> newValue) {
        if (!Objects.equals(result.getValue(), newValue)) {
            result.setValue(newValue);
        }
    }

    private void fetchFromNetwork(final LiveData<ResultType> dbSource) {
        LiveData<RequestType> apiResponse = createCall();
        // 先从数据库中获取数据
        result.addSource(dbSource, newData -> setValue(Resource.loading(newData)));
        result.addSource(apiResponse, response -> {
            // 当网络请求有结果时移除数据库和网络请求的资源
            result.removeSource(apiResponse);
            result.removeSource(dbSource);

            if (response != null) {
                // 当数据结果属于 Result<> 结构，则判断当前的请求结果是否成功
                if (response instanceof Result) {
                    int code = ((Result) response).code;
                    if (code != NetConstant.REQUEST_SUCCESS_CODE) {
                        onFetchFailed();
                        result.addSource(dbSource,
                                newData -> setValue(Resource.error(code, newData)));
                        return;
                    } else {
                        // do nothing
                    }
                }
                threadManager.runOnWorkThread(() -> {
                    // 保存网络请求结果至数据库
                    try {
                        saveCallResult(processResponse(response));
                    } catch (Exception e) {
                        SLog.e(LogTag.DB, "saveCallResult failed:" + e.toString());
                    }

                    threadManager.runOnUIThread(() ->
                            // 重新从数据库中获取结果，防止从旧数据源中获取到加载网络前的数据

                            result.addSource(safeLoadFromDb(),
                                    newData -> setValue(Resource.success(newData)))
                    );
                });
            } else {
                onFetchFailed();
                result.addSource(dbSource,
                        newData -> setValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), newData)));
            }
        });
    }

    private LiveData<ResultType> safeLoadFromDb(){
        LiveData<ResultType> dbSource;
        try {
            dbSource = loadFromDb();
        } catch (Exception e){
            SLog.e(LogTag.DB, "loadFromDb failed:" + e.toString());
            dbSource = new MutableLiveData<>(null);
        }
        return dbSource;
    }

    protected void onFetchFailed() {

    }

    /**
     * 返回结果数据
     *
     * @return
     */
    public LiveData<Resource<ResultType>> asLiveData() {
        return result;
    }

    /**
     * 过滤处理网络请求
     *
     * @param response
     * @return
     */
    @WorkerThread
    protected RequestType processResponse(RequestType response) {
        return response;
    }

    /**
     * 保存网络请求结果
     *
     * @param item
     */
    @WorkerThread
    protected abstract void saveCallResult(@NonNull RequestType item);

    /**
     * 通过请求结果判断是否需要请求网络
     *
     * @param data
     * @return
     */
    @MainThread
    protected boolean shouldFetch(@Nullable ResultType data) {
        return true;
    }

    /**
     * 从数据库中取数据
     *
     * @return
     */
    @NonNull
    @MainThread
    protected abstract LiveData<ResultType> loadFromDb();

    /**
     * 创建网络请求
     *
     * @return
     */
    @NonNull
    @MainThread
    protected abstract LiveData<RequestType> createCall();
}