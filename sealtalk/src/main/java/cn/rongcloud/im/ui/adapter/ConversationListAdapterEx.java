package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.GroupNoticeInfo;
import cn.rongcloud.im.im.message.GroupApplyMessage;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.model.Conversation;

/**
 * 自定义会话列表的 Adapter。继承 IMKit 里面的 ConversationListAdapter。
 * 通过重写 newView 或者 bindView 来进行自定义显示逻辑
 */
public class ConversationListAdapterEx extends ConversationListAdapter {

    private int mGroupNotifyUnReadCount;
    private String mContent;
    private Context mContext;
    private View groupView;
    private UIConversation groupAllyData;
    //是否为第一次网络请求获取数据
    private boolean isFirstRefresh = true;
    private GroupApplyMessageListener messageListener;

    public ConversationListAdapterEx(Context context) {
        super(context);
        mGroupNotifyUnReadCount = 0;
        mContext = context;
    }

    public void setGroupApplyMessageListener(GroupApplyMessageListener listener) {
        this.messageListener = listener;
    }

    @Override
    protected View newView(Context context, int position, ViewGroup group) {
        return super.newView(context, position, group);
    }

    @Override
    protected void bindView(View v, int position, UIConversation data) {
        if (data != null) {
            if (data.getConversationType().equals(Conversation.ConversationType.DISCUSSION)) {
                data.setUnreadType(UIConversation.UnreadRemindType.REMIND_ONLY);
            }
            //将缓存消息内容放入刷新数据中，防止队列刷新时内容丢失
            if (isGroupApplyMessage(data) && !TextUtils.isEmpty(mContent)) {
                data.setConversationContent(new SpannableString(mContent));
            }
        }
        super.bindView(v, position, data);
        if (isGroupApplyMessage(data)) {
            groupView = v;
            groupAllyData = data;
            //设置头像
            ImageView leftImg = v.findViewById(R.id.rc_left);
            leftImg.setImageDrawable(v.getContext().getResources().getDrawable(R.drawable.group_notice));
            //更新未读消息数
            updateGroupApplyView(groupView);
        }
    }

    private boolean isGroupApplyMessage(UIConversation data) {
        if (data.getMessageContent() != null) {
            return data.getMessageContent() instanceof GroupApplyMessage;
        }
        return false;
    }

    /**
     * 刷新未读消息
     *
     * @param v
     */
    private void updateGroupApplyView(View v) {
        if (v != null) {
            ViewHolder holder = (ViewHolder) v.getTag();
            if (mGroupNotifyUnReadCount == 0) {
                holder.unReadMsgCountIcon.setVisibility(View.GONE);
                holder.unReadMsgCount.setVisibility(View.GONE);
            } else {
                if (holder.unReadMsgCountIcon.getVisibility() == View.GONE) {
                    holder.unReadMsgCountIcon.setVisibility(View.VISIBLE);
                    holder.unReadMsgCountIcon.setImageResource(io.rong.imkit.R.drawable.rc_unread_count_bg);
                    setUnReadViewLayoutParams(holder.leftUnReadView, UIConversation.UnreadRemindType.REMIND_WITH_COUNTING);
                }
                if (holder.unReadMsgCount.getVisibility() == View.GONE) {
                    holder.unReadMsgCount.setVisibility(View.VISIBLE);
                }
                holder.unReadMsgCount.setText(String.valueOf(mGroupNotifyUnReadCount));
            }
            if (mContent != null) {
                View view = holder.contentView.getCurrentInflateView();
                TextView tvCotent = view.findViewById(R.id.rc_conversation_content);
                tvCotent.setText(new SpannableString(mContent));
            }
        }
    }

    /**
     * 请求获得通知消息数据进行更新处理
     *
     * @param data
     */
    public void updateNoticeInfoData(List<GroupNoticeInfo> data) {
        if (data != null && data.size() > 0) {
            int newCount = 0;
            String content = "";
            for (GroupNoticeInfo info : data) {
                // 2 为没有处理过的消息
                if (info.getStatus() == 2) {
                    newCount++;
                }
            }
            //取第一条数据更新通知消息内容
            GroupNoticeInfo info = data.get(0);
            if (info.getType() == 2) {
                content = mContext.getString(R.string.seal_conversation_notification_group_tips_add) + " " + info.getGroupNickName();
            } else if (info.getType() == 1) {
                content = info.getRequesterNickName() + " " + mContext.getString(R.string.seal_conversation_notification_group_tips);
            }
            mContent = content;
            //刷新通知消息逻辑，有新消息或者第一次请求网络获得数据的时候刷新
            if (mGroupNotifyUnReadCount != newCount || isFirstRefresh) {
                //监听回调更新群消息未读数量
                if (messageListener != null) {
                    messageListener.updateGroupUnReadCount(newCount);
                }
                isFirstRefresh = false;
                mGroupNotifyUnReadCount = newCount;
                if (groupView != null) {
                    updateGroupApplyView(groupView);
                }
            }
        } else {
            //没有消息通知时清除在对话列表中清除
            if (messageListener != null) {
                messageListener.updateGroupUnReadCount(0);
            }
            mGroupNotifyUnReadCount = 0;
            mContent = "";
            if (groupAllyData != null) {
                RongIM.getInstance().removeConversation(groupAllyData.getConversationType(),
                        groupAllyData.getConversationTargetId(), null);
            }
        }
    }

    public interface GroupApplyMessageListener {
        void updateGroupUnReadCount(int count);
    }
}
