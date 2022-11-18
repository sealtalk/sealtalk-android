package io.rong.recognizer;

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

public class RecognizeExtensionModule implements IExtensionModule {
    @Override
    public void onInit(Context context, String appKey) {
        // default implementation ignored
    }

    @Override
    public void onAttachedToExtension(Fragment fragment, RongExtension extension) {
        // default implementation ignored
    }

    @Override
    public void onDetachedFromExtension() {
        // default implementation ignored
    }

    @Override
    public void onReceivedMessage(Message message) {
        // default implementation ignored
    }

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        List<IPluginModule> pluginModules = new ArrayList<>();
        RecognizePlugin recognizePlugin = new RecognizePlugin();
        pluginModules.add(recognizePlugin);
        return pluginModules;
    }

    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        return null;
    }

    @Override
    public void onDisconnect() {
        // default implementation ignored
    }
}
