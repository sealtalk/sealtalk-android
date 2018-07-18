package io.rong.recognizer;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtension;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;


public class RecognizeExtensionModule implements IExtensionModule {
    @Override
    public void onInit(String appKey) {

    }

    @Override
    public void onConnect(String token) {

    }

    @Override
    public void onAttachedToExtension(RongExtension extension) {

    }

    @Override
    public void onDetachedFromExtension() {

    }

    @Override
    public void onReceivedMessage(Message message) {

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

    }
}
