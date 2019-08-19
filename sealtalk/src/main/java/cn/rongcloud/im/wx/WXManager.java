package cn.rongcloud.im.wx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.File;

import cn.rongcloud.im.R;
import cn.rongcloud.im.qrcode.QRCodeManager;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.RongIM;

public class WXManager {
    private static final String TAG = "WXManager";
    private static final String APP_ID = "替换为您的微信开放平台 AppId";

    /**
     * 分享图片缩略图大小
     */
    private static final int THUMB_SIZE = 150;

    private static volatile WXManager instance;
    private Context context;
    // IWXAPI 是第三方app和微信通信的openApi接口
    private IWXAPI api;

    private WXManager() {
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        regToWx();
    }

    public static WXManager getInstance() {
        if (instance == null) {
            synchronized (WXManager.class) {
                if (instance == null) {
                    instance = new WXManager();
                }
            }
        }

        return instance;
    }

    /**
     * 注册到微信
     */
    private void regToWx() {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(context, APP_ID, true);

        // 将应用的appId注册到微信
        api.registerApp(APP_ID);

        // 建议动态监听微信启动广播进行注册到微信
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // 将该app注册到微信
                api.registerApp(APP_ID);
            }
        }, new IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP));
    }

    /**
     * 分享图片
     * @param picPath
     */
    public void sharePicture(String picPath) {
        File file = new File(picPath);
        if (!file.exists()) {
            SLog.e(TAG, "share picture but " + picPath + " is not exist.");
            return;
        }

        //初始化 WXImageObject 和 WXMediaMessage 对象
        WXImageObject imgObj = new WXImageObject();
        imgObj.setImagePath(picPath);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        //设置缩略图,暂时取消
//        Bitmap thumbBmp = BitmapFactory.decodeFile(picPath);
//        msg.thumbData = WXUtils.bmpToByteArray(thumbBmp, true);

        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = WXUtils.buildTransaction("img");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;

        //调用api接口，发送数据到微信
        api.sendReq(req);
    }

    /**
     * 发送邀请至 SealTalk
     */
    public void inviteToSealTalk(){
        WXWebpageObject webpage = new WXWebpageObject();
        QRCodeManager qrCodeManager = new QRCodeManager(context);
        webpage.webpageUrl = qrCodeManager.generateUserQRCodeContent(RongIM.getInstance().getCurrentUserId());
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = context.getString(R.string.wx_share_invite_friend_title);
        msg.description = context.getString(R.string.wx_share_invite_friend_content);
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.seal_app_logo);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
        bmp.recycle();
        msg.thumbData = WXUtils.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = WXUtils.buildTransaction("webpage");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    public IWXAPI getWXAPI() {
        return api;
    }


}
