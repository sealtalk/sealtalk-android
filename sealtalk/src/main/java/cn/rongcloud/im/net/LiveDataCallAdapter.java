package cn.rongcloud.im.net;

import static cn.rongcloud.im.common.NetConstant.REQUEST_SUCCESS_CODE;

import androidx.lifecycle.LiveData;
import cn.rongcloud.im.common.ApiErrorCodeMap;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.utils.log.SLog;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicBoolean;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveDataCallAdapter<R> implements CallAdapter<R, LiveData<R>> {
    private final Type responseType;
    private static final String TAG = "LiveDataCallAdapter";

    public LiveDataCallAdapter(Type responseType) {
        this.responseType = responseType;
    }

    @Override
    public Type responseType() {
        return responseType;
    }

    @Override
    public LiveData<R> adapt(Call<R> call) {
        return new LiveData<R>() {
            AtomicBoolean started = new AtomicBoolean(false);

            @Override
            protected void onActive() {
                super.onActive();
                if (started.compareAndSet(false, true)) {
                    call.enqueue(
                            new Callback<R>() {
                                @Override
                                public void onResponse(Call<R> call, Response<R> response) {
                                    R body = response.body();
                                    String path = call.request().url().encodedPath();

                                    // 当没有信息体时通过 http code 判断业务错误
                                    if (body == null && !response.isSuccessful()) {
                                        if (responseType instanceof Class
                                                && ((Class) responseType)
                                                        .isAssignableFrom(Result.class)) {
                                            Result result = new Result();
                                            int errorCode =
                                                    ApiErrorCodeMap.getApiErrorCode(
                                                            path, response.code());
                                            result.setCode(errorCode);
                                            try {
                                                body = (R) result;
                                            } catch (Exception e) {
                                                // 可能部分接口并不是由 result 包裹，此时无法获取错误码
                                            }
                                        } else {
                                            body = null;
                                        }
                                    } else if (body instanceof Result) {
                                        Result result = (Result) body;
                                        // 当请求失败时，转义API错误码到全局错误码
                                        if (result.code != REQUEST_SUCCESS_CODE) {
                                            int errorCode =
                                                    ApiErrorCodeMap.getApiErrorCode(
                                                            path, result.code);
                                            result.setCode(errorCode);
                                        }
                                    }
                                    postValue(body);
                                }

                                @Override
                                public void onFailure(Call<R> call, Throwable throwable) {
                                    SLog.d(
                                            LogTag.API,
                                            "onFailure:"
                                                    + call.request().url().toString()
                                                    + ", error:"
                                                    + throwable.getMessage());
                                    if (throwable instanceof ConnectException) {
                                        R body = null;
                                        if (responseType instanceof Class
                                                && ((Class) responseType)
                                                        .isAssignableFrom(Result.class)) {
                                            Result result = new Result();
                                            result.setCode(ErrorCode.NETWORK_ERROR.getCode());
                                            try {
                                                body = (R) result;
                                            } catch (Exception e) {
                                                // 可能部分接口并不是由 result 包裹，此时无法获取错误码
                                            }
                                            postValue(body);
                                        } else {
                                            postValue(null);
                                        }
                                    } else {
                                        postValue(null);
                                    }
                                }
                            });
                }
            }
        };
    }
}
