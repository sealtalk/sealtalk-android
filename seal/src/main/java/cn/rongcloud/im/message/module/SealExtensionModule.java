package cn.rongcloud.im.message.module;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.RongExtension;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.DefaultLocationPlugin;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imkit.widget.provider.FilePlugin;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;


public class SealExtensionModule extends DefaultExtensionModule {

    @Override
    public void onInit(String appKey) {
        super.onInit(appKey);
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
    }

    @Override
    public void onConnect(String token) {
        super.onConnect(token);
    }

    @Override
    public void onAttachedToExtension(RongExtension extension) {
        super.onAttachedToExtension(extension);
    }

    @Override
    public void onDetachedFromExtension() {
        super.onDetachedFromExtension();
    }

    @Override
    public void onReceivedMessage(Message message) {
        super.onReceivedMessage(message);
    }

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        if (conversationType.equals(Conversation.ConversationType.PUBLIC_SERVICE)) {
            List<IPluginModule> pluginModuleList = new ArrayList<>();
            IPluginModule image = new ImagePlugin();
            IPluginModule locationPlugin = new DefaultLocationPlugin();
            pluginModuleList.add(image);
            pluginModuleList.add(locationPlugin);
            try {
                String clsName = "com.iflytek.cloud.SpeechUtility";
                Class<?> cls = Class.forName(clsName);
                if (cls != null) {
                    cls = Class.forName("io.rong.recognizer.RecognizePlugin");
                    Constructor<?> constructor = cls.getConstructor();
                    IPluginModule recognizer = (IPluginModule) constructor.newInstance();
                    pluginModuleList.add(recognizer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return pluginModuleList;
        } else if (conversationType == Conversation.ConversationType.CUSTOMER_SERVICE) {
            List<IPluginModule> pluginModules = super.getPluginModules(conversationType);
            if (conversationType == Conversation.ConversationType.CUSTOMER_SERVICE) {
                if (pluginModules != null) {
                    for (IPluginModule module : pluginModules) {
                        if (module instanceof FilePlugin) {
                            pluginModules.remove(module);
                            break;
                        }
                    }
                }
            }
            return pluginModules;
        } else {
            return super.getPluginModules(conversationType);
        }
    }

    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        return super.getEmoticonTabs();
    }
}
