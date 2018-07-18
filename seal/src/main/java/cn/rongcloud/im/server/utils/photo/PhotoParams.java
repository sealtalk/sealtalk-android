package cn.rongcloud.im.server.utils.photo;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

/**
 * [裁剪参数配置类]
 *
 * @author huxinwu
 * @version 1.0
 * @date 2015-1-7
 *
 **/
public class PhotoParams {

    public static final String CROP_FILE_NAME = "crop_file.jpg";
    public static final String OUTPUT_FORMAT = Bitmap.CompressFormat.JPEG.toString();

    public static final int DEFAULT_ASPECT = 1;
    public static final int DEFAULT_OUTPUT = 300;

    /** 临时地址 **/
    public Uri uri;
    /** 输出地址 **/
    public Uri outputUri;

    /** 输入类型，图片如jpg **/
    public String outputFormat;

    /** crop为true可以剪裁 **/
    public String crop;
    public boolean scale;
    public boolean returnData;
    public boolean noFaceDetection;
    public boolean scaleUpIfNeeded;

    /** aspectX aspectY 是宽高的比例 **/
    public int aspectX;
    public int aspectY;

    /** outputX,outputY 是剪裁图片的宽高 **/
    public int outputX;
    public int outputY;

    public PhotoParams() {
        crop = "true";
        uri = buildUri();
        outputUri = buildUri();
        scale = false;
        returnData = false;
        noFaceDetection = false;
        scaleUpIfNeeded = false;
        outputFormat = OUTPUT_FORMAT;
        aspectX = DEFAULT_ASPECT;
        aspectY = DEFAULT_ASPECT;
        outputX = DEFAULT_OUTPUT;
        outputY = DEFAULT_OUTPUT;
    }

    private Uri buildUri() {
        return Uri.fromFile(Environment.getExternalStorageDirectory()).buildUpon().appendPath(CROP_FILE_NAME).build();
    }
}
