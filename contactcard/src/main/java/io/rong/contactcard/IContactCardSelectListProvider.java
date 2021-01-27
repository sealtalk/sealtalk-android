package io.rong.contactcard;

import androidx.fragment.app.Fragment;

import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;

/**
 * 用于自定义联系人列表界面(取代 ContactListActivity)，需要实现的接口
 * Created by Beyond on 2017/5/11.
 */

public interface IContactCardSelectListProvider {
    void onContactPluginClick(int requestCode, Fragment currentFragment,
                              RongExtension extension, IPluginModule pluginModule);
}
