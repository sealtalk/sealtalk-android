package cn.rongcloud.im.sms;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.qrcode.QRCodeManager;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.RongIM;

/**
 * 短信相关管理
 */
public class SmsManager {
    /**
     * 邀请指定的电话号到 SealTalk ，跳转到发送短信界面
     *
     * @param context
     * @param phoneNumberList
     */
    public static void sendInviteSMS(Context context, List<String> phoneNumberList) {
        String smsContent = "";
        try {
            StringBuilder phoneBuilder = new StringBuilder();
            for (String number : phoneNumberList) {
                phoneBuilder.append(number).append(";");
            }

            QRCodeManager qrCodeManager = new QRCodeManager(context);
            String myUrl = qrCodeManager.generateUserQRCodeContent(RongIM.getInstance().getCurrentUserId());
            smsContent = context.getString(R.string.sms_share_invite_friend_content_format, myUrl);

            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + Uri.encode(phoneBuilder.toString())));
            // 兼容旧版发送短信加入 address 字段信息
            intent.putExtra("address", phoneBuilder.toString());
            intent.putExtra("sms_body", smsContent);
            context.startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            try {
                //获取剪贴板管理器
                ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("sms", smsContent);
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
            } catch (Exception e) {
            }
            ToastUtils.showToast(R.string.new_friend_invite_from_phone_contact_error, Toast.LENGTH_LONG);
        }
    }
}
