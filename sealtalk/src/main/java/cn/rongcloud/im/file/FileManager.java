package cn.rongcloud.im.file;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.rongcloud.im.SealApp;
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
import io.rong.imlib.NativeClient;
import io.rong.message.utils.BitmapUtil;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileManager {
    private Context context;
    private UserService userService;
    private AppService appService;

    public FileManager(Context context) {
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
    public LiveData<Resource<String>> saveBitmapToPictures(Bitmap bitmap, String fileName) {
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
     * 保存图片至缓存文件中
     *
     * @param bitmap
     * @return
     */
    public LiveData<Resource<String>> saveBitmapToCache(Bitmap bitmap, String fileName) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        ThreadManager.getInstance().runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                String path = FileUtils.saveBitmapToCache(bitmap, fileName);
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
    public LiveData<Resource<String>> saveBitmapToPictures(Bitmap bitmap) {
        String fileName = System.currentTimeMillis() + ".png";
        return saveBitmapToPictures(bitmap, fileName);
    }

    /**
     * 保存图片至缓存文件中,使用时间作为文件名
     *
     * @param bitmap
     * @return
     */
    public LiveData<Resource<String>> saveBitmapToCache(Bitmap bitmap) {
        String fileName = System.currentTimeMillis() + ".png";
        return saveBitmapToCache(bitmap, fileName);
    }

    /**
     * 上传图片
     *
     * @param imageUri
     * @return Resource 中 data 为上传成功后的 url
     */
    public LiveData<Resource<String>> uploadImage(Uri imageUri) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        LiveData<Resource<UploadTokenResult>> imageUploadTokenResource = getUploadToken();
        result.addSource(imageUploadTokenResource, tokenResultResource -> {
            // 当有结果时移除数据源
            if (tokenResultResource.status != Status.LOADING) {
                result.removeSource(imageUploadTokenResource);
            }

            // 获取 token 失败时返回错误
            if (tokenResultResource.status == Status.ERROR) {
                result.setValue(Resource.error(tokenResultResource.code, null));
                return;
            }

            if (tokenResultResource.status == Status.SUCCESS) {
                UploadTokenResult tokenResult = tokenResultResource.data;
                // 当获取 token 成功时上传服务器至七牛，目前没有其他云服务器所以不做类型判断
                LiveData<Resource<String>> uploadResource = uploadFileByQiNiu(imageUri, tokenResult.getToken());
                result.addSource(uploadResource, uploadResultResource -> {
                    // 当有结果时移除数据源
                    if (uploadResultResource.status != Status.LOADING) {
                        result.removeSource(uploadResource);
                    }

                    // 获取上传失败时返回错误
                    if (uploadResultResource.status == Status.ERROR) {
                        result.setValue(Resource.error(uploadResultResource.code, null));
                        return;
                    }

                    if (uploadResultResource.status == Status.SUCCESS) {
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
    private LiveData<Resource<UploadTokenResult>> getUploadToken() {
        // 请求服务器获取上传 token
        return new NetworkOnlyResource<UploadTokenResult, Result<UploadTokenResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<UploadTokenResult>> createCall() {
                return userService.getImageUploadToken();
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
    private LiveData<Resource<String>> uploadFileByQiNiu(Uri fileUri, String uploadToken) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        File uploadFile = new File(fileUri.getPath());
        if (!uploadFile.exists()) {
            uploadFile = new File(getRealPathFromUri(fileUri));
        }
        UploadManager uploadManager = new UploadManager();
        uploadManager.put(uploadFile, null, uploadToken, new UpCompletionHandler() {
            @Override
            public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                if (responseInfo.isOK()) {
                    try {
                        String key = (String) jsonObject.get("key");
                        result.postValue(Resource.success(key));
                    } catch (JSONException e) {
                        SLog.e(LogTag.API, "qiniu upload success,but cannot get key");
                        result.postValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
                    }
                } else {
                    int statusCode = responseInfo.statusCode;
                    SLog.e(LogTag.API, "qiniu upload failed, status code:" + statusCode);
                    result.postValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
                }
            }
        }, null);

        return result;
    }

    /**
     * 获取本地文件真实 uri
     *
     * @param contentUri
     * @return
     */
    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static int COMPRESSED_SIZE = 1080;
    private final static String IMAGE_LOCAL_PATH = "/image/local/seal/";
    private static int MAX_ORIGINAL_IMAGE_SIZE = 500;
    private static int COMPRESSED_FULL_QUALITY = 100;
    private static int COMPRESSED_QUALITY = 70;

    public LiveData<Resource<String>> uploadCompressImage(Uri contentUri) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        String localPath = "";
        Uri uri = Uri.parse(getSavePath());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (contentUri.getScheme().equals("file")) {
            localPath = contentUri.toString().substring(5);
        } else if (contentUri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(contentUri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            cursor.moveToFirst();
            localPath = cursor.getString(0);
            cursor.close();
        }
        BitmapFactory.decodeFile(localPath, options);
        File file = new File(localPath);
        long fileSize = file.length() / 1024;
        Bitmap bitmap = null;
        try {
            Log.e("uploadCompressImage","localPath***" + localPath);
            bitmap = BitmapUtil.getNewResizedBitmap(context,
                    Uri.parse("file://"+localPath), COMPRESSED_SIZE);
            if (bitmap != null) {
                String dir = uri.toString() + IMAGE_LOCAL_PATH;
                Log.e("uploadCompressImage","dir***" + dir);
                file = new File(dir);
                if (!file.exists())
                    file.mkdirs();
                file = new File(dir + file.getName());
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                boolean success;
                int quality;
                if (fileSize > MAX_ORIGINAL_IMAGE_SIZE) {
                    quality = COMPRESSED_QUALITY;
                } else {
                    quality = COMPRESSED_FULL_QUALITY;
                }
                success = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                // 在部分机型调用系统压缩转换png会有异常情况，修改后先进行判断是否压缩成功，如果压缩不成功则使用png方式进行二次压缩
                if (!success) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, quality, bos);
                }
                bos.close();
//                model.setLocalUri(Uri.parse("file://" + dir + name));
                Log.e("uploadCompressImage","file://" + dir + file.getName());
                if (!bitmap.isRecycled())
                    bitmap.recycle();
                return uploadImage(Uri.parse("file://" + dir + file.getName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getSavePath() {
        File saveFileDirectory = SealApp.getApplication().getExternalCacheDir();
        if(saveFileDirectory == null){
            saveFileDirectory = SealApp.getApplication().getCacheDir();
        }
        if (!saveFileDirectory.exists()) {
            saveFileDirectory.mkdirs();
        }

        return saveFileDirectory.getAbsolutePath();
    }

    /**
     * 下载文件
     *
     * @param downloadFilePath
     * @param saveFilePath
     * @return
     */
    public LiveData<Resource<String>> downloadFile(String downloadFilePath, String saveFilePath) {
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
