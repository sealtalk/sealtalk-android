package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import cn.rongcloud.im.ui.adapter.ConversationListAdapterEx;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.model.Conversation;

public class MainConversationListFragment extends ConversationListFragment {

    Conversation.ConversationType[] mConversationsTypes = new Conversation.ConversationType[]{Conversation.ConversationType.PRIVATE,
            Conversation.ConversationType.GROUP,
            Conversation.ConversationType.PUBLIC_SERVICE,
            Conversation.ConversationType.APP_PUBLIC_SERVICE,
            Conversation.ConversationType.SYSTEM,
            Conversation.ConversationType.DISCUSSION
    };

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUri();
    }

    private void setUri() {
        // 此处是因为适配 Androidx 库， 所以才会有红色警告， 但是不影响编译运行。
        // 设置会话列表所要支持的会话类型。 并且会话类型后面的  boolean 值表示此会话类型是否内举。
        Uri uri = Uri.parse("rong://" +
                getActivity().getApplicationInfo().packageName).buildUpon()
                .appendPath("conversationlist")
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//群组
                .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//订阅号
                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")//系统
                .build();

        setUri(uri);
    }


    @Override
    public ConversationListAdapter onResolveAdapter(Context context) {
        return new ConversationListAdapterEx(context);
    }

}
