package cn.rongcloud.im.im.plugin;

import android.content.Context;
import androidx.fragment.app.Fragment;
import io.rong.imkit.conversation.extension.IExtensionModule;
import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.component.emoticon.IEmoticonTab;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import java.util.ArrayList;
import java.util.List;

public class InsertMessageModule implements IExtensionModule {
    @Override
    public void onInit(Context context, String appKey) {}

    @Override
    public void onAttachedToExtension(Fragment fragment, RongExtension extension) {}

    @Override
    public void onDetachedFromExtension() {}

    @Override
    public void onReceivedMessage(Message message) {}

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        if (conversationType == Conversation.ConversationType.ULTRA_GROUP) {
            List<IPluginModule> pluginModuleList = new ArrayList<>();
            pluginModuleList.add(new InsertMessagePlugin());
            return pluginModuleList;
        }
        return null;
    }

    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        return null;
    }

    @Override
    public void onDisconnect() {}
}
