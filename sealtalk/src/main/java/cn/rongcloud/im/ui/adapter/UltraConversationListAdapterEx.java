package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import cn.rongcloud.im.R;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imkit.conversationlist.model.GroupConversation;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.adapter.ViewHolder;
import java.util.List;

public class UltraConversationListAdapterEx extends BaseAdapter<BaseUiConversation> {

    private static final String COLON_SPLIT = ": ";

    Context mContext;

    public UltraConversationListAdapterEx() {
        super();
        mProviderManager = RongConfigCenter.conversationListConfig().getProviderManager();
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }

    // 后需要修改这种处理方式
    private String getMentionedMessage(
            Context context, int mentionedMeCount, int mentionedAllCount) {
        if (mentionedMeCount > 0 || mentionedAllCount > 0) {
            return context.getString(
                    R.string.ultra_group_conversation_mentioned_me_and_all,
                    mentionedMeCount,
                    mentionedAllCount);
        }
        return "";
    }

    // 后需要修改这种处理方式
    private void changeMentionedMessage(BaseUiConversation data) {
        if (!(data instanceof GroupConversation)) {
            return;
        }

        GroupConversation gc = (GroupConversation) data;
        int total = gc.mCore.getUnreadMentionedCount();
        int me = gc.mCore.getUnreadMentionedMeCount();
        int all = total - me;

        String mentionedMessage = getMentionedMessage(mContext, me, all);
        if (TextUtils.isEmpty(mentionedMessage)) {
            return;
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        String senderName =
                TextUtils.isEmpty(gc.mCore.getSenderUserName()) ? "" : gc.mCore.getSenderUserName();
        boolean isShowName =
                RongConfigCenter.conversationConfig()
                        .showSummaryWithName(gc.mCore.getLatestMessage());
        SpannableString preString = new SpannableString(mentionedMessage);
        preString.setSpan(
                new ForegroundColorSpan(
                        mContext.getResources().getColor(io.rong.imkit.R.color.rc_warning_color)),
                0,
                preString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        Spannable messageSummary =
                RongConfigCenter.conversationConfig()
                        .getMessageSummary(mContext, gc.mCore.getLatestMessage());
        builder.append(preString);
        if (!TextUtils.isEmpty(senderName) && isShowName) {
            builder.append(senderName).append(COLON_SPLIT);
        }
        builder.append(messageSummary);
        gc.mConversationContent = builder;
    }

    @Override
    public void setDataCollection(final List<BaseUiConversation> data) {
        for (BaseUiConversation d : data) {
            changeMentionedMessage(d);
        }
        if (mDataList != null && mDataList.size() != 0) {
            DiffUtil.calculateDiff(
                    new DiffUtil.Callback() {
                        @Override
                        public int getOldListSize() {
                            return mDataList.size();
                        }

                        @Override
                        public int getNewListSize() {
                            return data.size();
                        }

                        @Override
                        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                            BaseUiConversation oldItem = mDataList.get(oldItemPosition);
                            BaseUiConversation newItem = data.get(newItemPosition);
                            return oldItem.mCore.getTargetId().equals(newItem.mCore.getTargetId())
                                    && oldItem.mCore
                                            .getConversationType()
                                            .equals(newItem.mCore.getConversationType());
                        }

                        @Override
                        public boolean areContentsTheSame(
                                int oldItemPosition, int newItemPosition) {
                            return false;
                        }
                    });
        }
        mDataList = data;
        notifyDataSetChanged();
    }
}
