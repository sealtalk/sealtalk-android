package io.rong.contactcard.message;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.rong.contactcard.ContactCardContext;
import io.rong.contactcard.IContactCardClickListener;
import io.rong.contactcard.IContactCardInfoProvider;
import io.rong.contactcard.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Beyond on 2016/12/5.
 */

@ProviderTag(messageContent = ContactMessage.class, showProgress = false, showReadState = true)
public class ContactMessageItemProvider extends IContainerItemProvider.MessageProvider<ContactMessage> {
    private final static String TAG = "ContactMessageItemProvider";
    private IContactCardClickListener iContactCardClickListener;

    public ContactMessageItemProvider(IContactCardClickListener iContactCardClickListener) {
        this.iContactCardClickListener = iContactCardClickListener;
    }

    private static class ViewHolder {
        AsyncImageView mImage;
        TextView mName;
        LinearLayout mLayout;
    }

    @Override
    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_message_contact_card, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.mImage = (AsyncImageView) view.findViewById(R.id.rc_img);
        viewHolder.mName = (TextView) view.findViewById(R.id.rc_name);
        viewHolder.mLayout = (LinearLayout) view.findViewById(R.id.rc_layout);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View v, int position, final ContactMessage content, final UIMessage message) {
        final ViewHolder viewHolder = (ViewHolder) v.getTag();

        if (!TextUtils.isEmpty(content.getImgUrl())) {
            viewHolder.mImage.setAvatar(content.getImgUrl(), R.drawable.rc_default_portrait);
        }
        if (!TextUtils.isEmpty(content.getName())) {
            SpannableStringBuilder spannable = new SpannableStringBuilder(content.getName());
            AndroidEmoji.ensure(spannable);
            viewHolder.mName.setText(spannable);
        }

        IContactCardInfoProvider iContactCardInfoProvider
                = ContactCardContext.getInstance().getContactCardInfoProvider();
        if (iContactCardInfoProvider != null) {
            iContactCardInfoProvider.getContactAppointedInfoProvider(
                    content.getId(), content.getName(), content.getImgUrl(),
                    new IContactCardInfoProvider.IContactCardInfoCallback() {
                        @Override
                        public void getContactCardInfoCallback(List<? extends UserInfo> list) {
                            if (list != null && list.size() > 0) {
                                UserInfo userInfo = list.get(0);
                                if (userInfo != null && userInfo.getPortraitUri() != null) {
                                    /* 如果名片发送的推荐人头像信息，与本地数据库的对应头像信息不一致，
                                       则优先显示本地数据库的对应头像信息 */
                                    if (TextUtils.isEmpty(content.getImgUrl()) ||
                                            !content.getImgUrl().equals(userInfo.getPortraitUri().toString())) {
                                        viewHolder.mImage.setAvatar(userInfo.getPortraitUri());
                                        ((ContactMessage) (message.getContent()))
                                                .setImgUrl(userInfo.getPortraitUri().toString());
                                    }
                                    // 如果本端设置了该用户信息的别名(备注、昵称)，优先显示这个别名
                                    if (!TextUtils.isEmpty(content.getName()) && !content.getName().equals(userInfo.getName())) {
                                        viewHolder.mName.setText(userInfo.getName());
                                    }
                                }
                            }
                        }
                    });
        }

        if (message.getMessageDirection() == Message.MessageDirection.RECEIVE)
            viewHolder.mLayout.setBackgroundResource(R.drawable.rc_ic_bubble_left_file);
        else
            viewHolder.mLayout.setBackgroundResource(R.drawable.rc_ic_bubble_right_file);
    }

    @Override
    public Spannable getContentSummary(final ContactMessage contactMessage) {
        return null;
    }

    @Override
    public Spannable getContentSummary(Context context, final ContactMessage contactMessage) {
        if (contactMessage != null && !TextUtils.isEmpty(contactMessage.getSendUserId())
                && !TextUtils.isEmpty(contactMessage.getSendUserName())) {
            if (contactMessage.getSendUserId().equals(RongIM.getInstance().getCurrentUserId())) {
                String str_RecommendClause = context.getResources().getString(R.string.rc_recommend_clause_to_others);
                return new SpannableString(String.format(str_RecommendClause, contactMessage.getName()));
            } else {
                String str_RecommendClause = context.getResources().getString(R.string.rc_recommend_clause_to_me);
                return new SpannableString(String.format(str_RecommendClause, contactMessage.getSendUserName(), contactMessage.getName()));
            }
        }
        return new SpannableString("[" + context.getResources().getString(R.string.rc_plugins_contact) + "]");
    }

    @Override
    public void onItemClick(View view, int position, ContactMessage content, UIMessage message) {
        if (iContactCardClickListener != null) {
            iContactCardClickListener.onContactCardClick(view, content);
        }
    }
}
