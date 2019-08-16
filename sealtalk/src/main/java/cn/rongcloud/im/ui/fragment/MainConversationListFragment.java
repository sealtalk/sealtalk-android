package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import cn.rongcloud.im.db.model.GroupNoticeInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.activity.MainActivity;
import cn.rongcloud.im.ui.adapter.ConversationListAdapterEx;
import cn.rongcloud.im.viewmodel.GroupNoticeInfoViewModel;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.model.Conversation;

public class MainConversationListFragment extends ConversationListFragment {

    private ConversationListAdapterEx conversationListAdapterEx;
    private GroupNoticeInfoViewModel groupNoticeInfoViewModel;
    private MainActivity mainActivity;

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
        initViewModel();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
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
        if (conversationListAdapterEx == null) {
            conversationListAdapterEx = new ConversationListAdapterEx(context);
            conversationListAdapterEx.setGroupApplyMessageListener(new ConversationListAdapterEx.GroupApplyMessageListener() {
                @Override
                public void updateGroupUnReadCount(int count) {
                    updateGroupNotifyUnReadCount(count);
                }
            });
        }
        return conversationListAdapterEx;
    }

    /**
     * 更新群通知未读消息的数量
     *
     * @param num
     */
    public void updateGroupNotifyUnReadCount(int num) {
        if (mainActivity != null) {
            mainActivity.mainViewModel.setGroupNotifyUnReadNum(num);
        }
    }

    private void initViewModel() {
        groupNoticeInfoViewModel = ViewModelProviders.of(this).get(GroupNoticeInfoViewModel.class);
        groupNoticeInfoViewModel.getGroupNoticeInfo().observe(this, new Observer<Resource<List<GroupNoticeInfo>>>() {
            @Override
            public void onChanged(Resource<List<GroupNoticeInfo>> listResource) {
                if (listResource.status != Status.LOADING) {
                    if (conversationListAdapterEx != null) {
                        conversationListAdapterEx.updateNoticeInfoData(listResource.data);
                    }
                }
            }
        });
    }

}
