package cn.rongcloud.im.im.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.fragment.app.Fragment;
import cn.rongcloud.im.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

class MentionAllPlugin implements IPluginModule {
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_ext_plugin_poke_selector);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.rc_message_content_mentioned);
    }

    @Override
    public void onClick(Fragment currentFragment, RongExtension extension, int index) {
        TextMessage textMessage = TextMessage.obtain("全部人消息");
        MentionedInfo mentionedInfo = new MentionedInfo();
        mentionedInfo.setType(MentionedInfo.MentionedType.ALL);
        textMessage.setMentionedInfo(mentionedInfo);
        Message message = Message.obtain(extension.getConversationIdentifier(), textMessage);
        RongIM.getInstance().sendMessage(message, null, null, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {}
}
