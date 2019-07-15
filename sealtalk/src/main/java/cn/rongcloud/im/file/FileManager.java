package cn.rongcloud.im.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;

import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UploadTokenResult;
import cn.rongcloud.im.net.HttpClientManager;
import cn.rongcloud.im.net.RetrofitClient;
import cn.rongcloud.im.net.service.AppService;
import cn.rongcloud.im.net.service.UserService;
import cn.rongcloud.im.utils.FileUtils;
import cn.rongcloud.im.utils.NetworkOnlyResource;
import cn.rongcloud.im.utils.log.SLog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileManager {
    private Context context;
    private UserService userService;
    private AppService appService;

    public FileManager(Context context){
        this.context = context.getApplicationContext();
        RetrofitClient client = HttpClientManager.getInstance(context).getClient();
        userService = client.createService(UserService.class);
        appService = client.createService(AppService.class);
    }

    /**
     * 保存图片至公共下载下载中
     *
     * @param bitmap
     * @return
     */
    public LiveData<Resource<String>> saveBitmapToPictures(Bitmap bitmap, String fileName){
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        ThreadManager.getInstance().runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                String path = FileUtils.saveBitmapToPublicPictures(bitmap, fileName);
                result.postValue(Resource.success(path));
            }
        });
        return result;
    }

    /**
     * 保存图片至公共下载下载中,使用时间作为文件名
     *
     * @param bitmap
     * @return
     */
    public LiveData<Resource<String>> saveBitmapToPictures(Bitmap bitmap){
        String fileName = System.currentTimeMillis() + ".png";
        return saveBitmapToPictures(bitmap, fileName);
    }

    /**
     * 上传图片
     * @param imageUri
     * @return Resource 中 data 为上传成功后的 url
     */
    public LiveData<Resource<String>> uploadImage(Uri imageUri){
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        LiveData<Resource<UploadTokenResult>> imageUploadTokenResource = getUploadToken();
        result.addSource(imageUploadTokenResource, tokenResultResource -> {
            // 当有结果时移除数据源
            if(tokenResultResource.status != Status.LOADING){
                result.removeSource(imageUploadTokenResource);
            }

            // 获取 token 失败时返回错误
            if(tokenResultResource.status == Status.ERROR){
                result.setValue(Resource.error(tokenResultResource.code, null));
                return;
            }

            if(tokenResultResource.status == Status.SUCCESS){
                UploadTokenResult tokenResult = tokenResultResource.data;
                // 当获取 token 成功时上传服务器至七牛，目前没有其他云服务器所以不做类型判断
                LiveData<Resource<String>> uploadResource = uploadFileByQiNiu(imageUri, tokenResult.getToken());
                result.addSource(uploadResource, uploadResultResource -> {
                    // 当有结果时移除数据源
                    if(uploadResultResource.status != Status.LOADING){
                        result.removeSource(uploadResource);
                    }

                    // 获取上传失败时返回错误
                    if(uploadResultResource.status == Status.ERROR){
                        result.setValue(Resource.error(uploadResultResource.code, null));
                        return;
                    }

                    if(uploadResultResource.status == Status.SUCCESS){
                        // 返回上传后结果 url
                        String resultUrl = "http://" + tokenResult.getDomain() + "/" + uploadResultResource.data;
                        result.setValue(Resource.success(resultUrl));
                    }
                });
            }
        });
        return result;
    }

    /**
     * 获取上传文件 token
     *
     * @return
     */
    private LiveData<Resource<UploadTokenResult>> getUploadToken(){
        // 请求服务器获取上传 token
        return new NetworkOnlyResource<UploadTokenResult, Result<UploadTokenResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<UploadTokenResult>> createCall() {
                return  userService.getImageUploadToken();
            }
        }.asLiveData();
    }

    /**
     * 使用七牛上传文件
     *
     * @param fileUri
     * @param uploadToken
     * @return
     */
    private LiveData<Resource<String>> uploadFileByQiNiu(Uri fileUri, String uploadToken){
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        File uploadFile = new File(fileUri.getPath());
        UploadManager uploadManager = new UploadManager();
        uploadManager.put(uploadFile, null, uploadToken, new UpCompletionHandler() {
            @Override
            public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                if(responseInfo.isOK()){
                    try {
                        String key = (String) jsonObject.get("key");
                        result.postValue(Resource.success(key));
                    } catch (JSONException e) {
                        SLog.e(LogTag.API, "qiniu upload success,but cannot get key");
                        result.postValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
                    }
                }else{
                    int statusCode = responseInfo.statusCode;
                    SLog.e(LogTag.API, "qiniu upload failed, status code:" + statusCode);
                    result.postValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
                }
            }
        }, null);

        return result;
    }

    /**
     * 下载文件
     * 
     * @param downloadFilePath
     * @param saveFilePath
     * @return
     */
    public LiveData<Resource<String>> downloadFile(String downloadFilePath, String saveFilePath){
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        appService.downloadFile(downloadFilePath).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                InputStream inputStream = response.body().byteStream();
                File saveFile = new File(saveFilePath);

                //TODO input 写进 file
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
        return result;
    }
}
