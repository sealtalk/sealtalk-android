package cn.rongcloud.im.im.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.fragment.app.Fragment;
import cn.rongcloud.im.R;
import io.rong.imkit.IMCenter;
import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

class InsertMessagePlugin implements IPluginModule {
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_ext_plugin_poke_selector);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.rc_insert_message);
    }

    @Override
    public void onClick(Fragment currentFragment, RongExtension extension, int index) {
        IMCenter.getInstance()
                .insertOutgoingMessage(
                        extension.getConversationIdentifier(),
                        Message.SentStatus.SENT,
                        TextMessage.obtain("插入的消息"),
                        System.currentTimeMillis(),
                        new RongIMClient.ResultCallback<Message>() {
                            @Override
                            public void onSuccess(Message message) {}

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {}
                        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {}
}
