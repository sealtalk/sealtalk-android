package cn.rongcloud.im.im;

import java.util.List;

import io.rong.callkit.AudioPlugin;
import io.rong.callkit.VideoPlugin;
import io.rong.imkit.IMCenter;
import io.rong.imkit.conversation.extension.DefaultExtensionConfig;
import io.rong.imkit.conversation.extension.component.plugin.FilePlugin;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imkit.feature.destruct.DestructPlugin;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.sight.SightPlugin;

public class SealExtensionConfig extends DefaultExtensionConfig {
    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType, String targetId) {
        List<IPluginModule> pluginList = super.getPluginModules(conversationType, targetId);
        IPluginModule sightPlugin = null, filePlugin = null, audioPlugin = null, videoPlugin = null, destructPlugin = null;
        for (IPluginModule pluginModule : pluginList) {
            if (pluginModule instanceof SightPlugin) {
                sightPlugin = pluginModule;
            } else if (pluginModule instanceof FilePlugin) {
                filePlugin = pluginModule;
            } else if (pluginModule instanceof AudioPlugin) {
                audioPlugin = pluginModule;
            } else if (pluginModule instanceof VideoPlugin) {
                videoPlugin = pluginModule;
            } else if (pluginModule instanceof DestructPlugin) {
                destructPlugin = pluginModule;
            }

        }
        if (sightPlugin != null && pluginList.size() > 1) {
            pluginList.remove(sightPlugin);
            pluginList.add(1, sightPlugin);
        }
        if (filePlugin != null && pluginList.size() > 4) {
            pluginList.remove(filePlugin);
            pluginList.add(3, filePlugin);
        }
        if (targetId.equals(RongIMClient.getInstance().getCurrentUserId())) {
            if (audioPlugin != null) {
                pluginList.remove(audioPlugin);
            }
            if (videoPlugin != null) {
                pluginList.remove(videoPlugin);
            }
            if (destructPlugin != null) {
                pluginList.remove(destructPlugin);
            }
        }
        return pluginList;
    }
}
