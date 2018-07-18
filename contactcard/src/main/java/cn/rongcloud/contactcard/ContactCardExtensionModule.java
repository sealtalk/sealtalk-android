package cn.rongcloud.contactcard;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.contactcard.message.ContactMessage;
import cn.rongcloud.contactcard.message.ContactMessageItemProvider;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 * Created by Beyond on 2017/4/14.
 */

public class ContactCardExtensionModule implements IExtensionModule {

    private IContactCardClickListener iContactCardClickListener;

    public ContactCardExtensionModule() {
    }

    public ContactCardExtensionModule(IContactCardSelectListProvider iContactCardSelectListProvider
            , IContactCardInfoProvider iContactCardInfoProvider
            , IContactCardClickListener iContactCardClickListener) {
        ContactCardContext.getInstance().setContactCardSelectListProvider(iContactCardSelectListProvider);
        ContactCardContext.getInstance().setContactCardInfoProvider(iContactCardInfoProvider);
        this.iContactCardClickListener = iContactCardClickListener;
    }

    public ContactCardExtensionModule(IContactCardInfoProvider iContactCardInfoProvider
            , IContactCardClickListener iContactCardClickListener) {
        ContactCardContext.getInstance().setContactCardInfoProvider(iContactCardInfoProvider);
        this.iContactCardClickListener = iContactCardClickListener;
    }

    @Override
    public void onInit(String appKey) {
        RongIM.registerMessageType(ContactMessage.class); //注册名片消息
        RongIM.registerMessageTemplate(new ContactMessageItemProvider(iContactCardClickListener));
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
        if (conversationType.equals(Conversation.ConversationType.PRIVATE)
                || conversationType.equals(Conversation.ConversationType.GROUP)) {
            pluginModules.add(new ContactCardPlugin());
        }
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
