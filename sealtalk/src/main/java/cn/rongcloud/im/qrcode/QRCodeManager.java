package cn.rongcloud.im.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cn.rongcloud.im.common.QRCodeConstant;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.qrcode.QRCodeResult;
import cn.rongcloud.im.model.qrcode.QRCodeType;
import cn.rongcloud.im.model.qrcode.QRGroupInfo;
import cn.rongcloud.im.model.qrcode.QRUserInfo;
import cn.rongcloud.im.utils.qrcode.QRCodeUtils;

public class QRCodeManager {
    private Context context;

    public QRCodeManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 生成群组二维码内容
     *
     * @param groupId
     * @param sharedUserId
     * @return
     */
    public String generateGroupQRCodeContent(String groupId, String sharedUserId) {
        Uri baseUri = Uri.parse(QRCodeConstant.BASE_URL);
        Uri sealGroupUri = new Uri.Builder()
                .scheme(QRCodeConstant.SealTalk.SCHEME)
                .authority(QRCodeConstant.SealTalk.AUTHORITY_GROUP)
                .appendPath(QRCodeConstant.SealTalk.GROUP_PATH_JOIN)
                .appendQueryParameter(QRCodeConstant.SealTalk.GROUP_QUERY_GROUP_ID, groupId)
                .appendQueryParameter(QRCodeConstant.SealTalk.GROUP_QUERY_SHARED_USER_ID, sharedUserId)
                .build();
        Uri fullUri = baseUri.buildUpon().appendQueryParameter(QRCodeConstant.BASE_URL_QUERY_CONTENT, sealGroupUri.toString()).build();
        String url = fullUri.toString();
        try {
            url = URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
        }
        return url;
    }

    /**
     * 生成用户二维码内容
     *
     * @param userId
     * @return
     */
    public String generateUserQRCodeContent(String userId) {
        Uri baseUri = Uri.parse(QRCodeConstant.BASE_URL);
        Uri sealGroupUri = new Uri.Builder()
                .scheme(QRCodeConstant.SealTalk.SCHEME)
                .authority(QRCodeConstant.SealTalk.AUTHORITY_USER)
                .appendPath(QRCodeConstant.SealTalk.USER_PATH_INFO)
                .appendQueryParameter(QRCodeConstant.SealTalk.USER_QUERY_USER_ID, userId)
                .build();
        Uri fullUri = baseUri.buildUpon().appendQueryParameter(QRCodeConstant.BASE_URL_QUERY_CONTENT, sealGroupUri.toString()).build();
        String url = fullUri.toString();
        try {
            url = URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
        }
        return url;
    }

    /**
     * 获取群组二维码
     *
     * @param groupId
     * @param sharedUserId
     * @param width
     * @param height
     * @return
     */
    public LiveData<Resource<Bitmap>> getGroupQRCode(String groupId, String sharedUserId, int width, int height) {
        MutableLiveData<Resource<Bitmap>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        ThreadManager.getInstance().runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                String qrCodeContent = generateGroupQRCodeContent(groupId, sharedUserId);
                Bitmap bitmap = QRCodeUtils.generateImage(qrCodeContent, width, height, null);
                result.postValue(Resource.success(bitmap));
            }
        });
        return result;
    }

    /**
     * 获取群组二维码
     *
     * @param userId
     * @param width
     * @param height
     * @return
     */
    public LiveData<Resource<Bitmap>> getUserQRCode(String userId, int width, int height) {
        MutableLiveData<Resource<Bitmap>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        ThreadManager.getInstance().runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                String qrCodeContent = generateUserQRCodeContent(userId);
                Bitmap bitmap = QRCodeUtils.generateImage(qrCodeContent, width, height, null);
                result.postValue(Resource.success(bitmap));
            }
        });
        return result;
    }

    /**
     * 获取 QR 二维码结果
     * @param qrCodeStr
     * @return
     */
    public QRCodeResult getQRCodeType(String qrCodeStr){
        Uri uri = Uri.parse(qrCodeStr);
        Uri sealUri = null;

        // 获取 sealtalk 的 uri
        if(uri.getScheme() != null && QRCodeConstant.SealTalk.SCHEME.equals(uri.getScheme().toLowerCase())){
            sealUri = uri;
        } else {
            // 若从其他跳转至 sealtalk 判断请求参数中是否包含 sealtalk 的 uri
            String content = uri.getQueryParameter(QRCodeConstant.BASE_URL_QUERY_CONTENT);
            if (!TextUtils.isEmpty(content)) {
                Uri contentUri = Uri.parse(content);
                if(contentUri.getScheme() != null && QRCodeConstant.SealTalk.SCHEME.equals(contentUri.getScheme().toLowerCase())) {
                    sealUri = contentUri;
                }
            }
        }

        if(sealUri != null){
            String authority = sealUri.getAuthority();
            String path = sealUri.getPath();
            if(path != null && path.startsWith("/")){
                path = path.substring(1);
            }
            if(QRCodeConstant.SealTalk.AUTHORITY_GROUP.equals(authority)){
                // 群组信息结果
                if (QRCodeConstant.SealTalk.GROUP_PATH_JOIN.equals(path)){
                    String groupId = sealUri.getQueryParameter(QRCodeConstant.SealTalk.GROUP_QUERY_GROUP_ID);
                    String sharedId = sealUri.getQueryParameter(QRCodeConstant.SealTalk.GROUP_QUERY_SHARED_USER_ID);
                    QRGroupInfo qrGroupInfo = new QRGroupInfo();
                    qrGroupInfo.setGroupId(groupId);
                    qrGroupInfo.setSharedUserId(sharedId);
                    return new QRCodeResult(QRCodeType.GROUP_INFO, qrGroupInfo);
                }else {
                    return new QRCodeResult(QRCodeType.OTHER, qrCodeStr);
                }
            } else if(QRCodeConstant.SealTalk.AUTHORITY_USER.equals(authority)){
                // 用户信息结果
                if (QRCodeConstant.SealTalk.USER_PATH_INFO.equals(path)){
                    String userId = sealUri.getQueryParameter(QRCodeConstant.SealTalk.USER_QUERY_USER_ID);
                    QRUserInfo qrUserInfo = new QRUserInfo();
                    qrUserInfo.setUserId(userId);
                    return new QRCodeResult(QRCodeType.USER_INFO, qrUserInfo);
                }else {
                    return new QRCodeResult(QRCodeType.OTHER, qrCodeStr);
                }
            } else {
                return new QRCodeResult(QRCodeType.OTHER, qrCodeStr);
            }
        } else {
            return new QRCodeResult(QRCodeType.OTHER, qrCodeStr);
        }
    }


}
