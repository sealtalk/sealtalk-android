package cn.rongcloud.im.im.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import androidx.fragment.app.Fragment;
import cn.rongcloud.im.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class SendKVMessagePlugin implements IPluginModule {

    private static final String TAG = "SendKVMessagePlugin";

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_ext_plugin_poke_selector);
    }

    @Override
    public String obtainTitle(Context context) {
        return "发送KV消息";
    }

    @Override
    public void onClick(Fragment currentFragment, RongExtension extension, int index) {
        Map<String, String> map = new HashMap<>();
        map.put("key1", "key1_ext");
        map.put("key2", "key2_ext");
        sendTextMsg(extension.getConversationIdentifier(), map);
    }

    private void sendTextMsg(
            ConversationIdentifier conversationIdentifier, Map<String, String> mapSend) {
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String timeString = sdf.format(new Date(System.currentTimeMillis()));
        TextMessage textMessage = TextMessage.obtain("这是一条KV消息，当前时间：" + timeString);
        Message message = Message.obtain(conversationIdentifier, textMessage);
        message.setCanIncludeExpansion(true);
        if (mapSend != null && mapSend.size() > 0) {
            message.setExpansion((HashMap<String, String>) mapSend);
        }
        RongIM.getInstance()
                .sendMessage(
                        message,
                        null,
                        null,
                        new IRongCallback.ISendMessageCallback() {
                            @Override
                            public void onAttached(io.rong.imlib.model.Message message) {}

                            @Override
                            public void onSuccess(io.rong.imlib.model.Message message) {
                                Log.e(TAG, "发送消息成功: ");
                            }

                            @Override
                            public void onError(
                                    io.rong.imlib.model.Message message,
                                    RongIMClient.ErrorCode errorCode) {
                                Log.e(TAG, "onError: " + errorCode);
                            }
                        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {}
}
