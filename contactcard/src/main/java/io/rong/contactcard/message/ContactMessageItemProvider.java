package io.rong.contactcard.message;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import io.rong.contactcard.ContactCardContext;
import io.rong.contactcard.IContactCardClickListener;
import io.rong.contactcard.IContactCardInfoProvider;
import io.rong.contactcard.R;
import io.rong.imkit.IMCenter;
import io.rong.imkit.RongIM;
import io.rong.imkit.conversation.extension.component.emoticon.AndroidEmoji;
import io.rong.imkit.conversation.messgelist.provider.BaseMessageItemProvider;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.picture.tools.ScreenUtils;
import io.rong.imkit.widget.adapter.IViewProviderListener;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Beyond on 2016/12/5.
 */

//Todo
public class ContactMessageItemProvider extends BaseMessageItemProvider<ContactMessage> {
    private final static String TAG = "ContactMessageItemProvider";
    private IContactCardClickListener iContactCardClickListener;


    public ContactMessageItemProvider(IContactCardClickListener iContactCardClickListener) {
        this.iContactCardClickListener = iContactCardClickListener;
        mConfig.showContentBubble = false;
    }

    @Override
    protected ViewHolder onCreateMessageContentViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rc_message_contact_card, parent, false);
        return new ViewHolder(parent.getContext(), view);
    }

    @Override
    protected void bindMessageContentViewHolder(final ViewHolder holder, ViewHolder parentHolder, final ContactMessage contactMessage, final UiMessage uiMessage, int position, List<UiMessage> list, IViewProviderListener<UiMessage> listener) {

        final ImageView imageView = holder.getView(R.id.rc_img);
        final RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(ScreenUtils.dip2px(IMCenter.getInstance().getContext(), 6))).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        Glide.with(imageView).load(contactMessage.getImgUrl())
                .apply(options)
                .placeholder(R.drawable.rc_default_portrait)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(imageView);

        if (!TextUtils.isEmpty(contactMessage.getName())) {
            SpannableStringBuilder spannable = new SpannableStringBuilder(contactMessage.getName());
            AndroidEmoji.ensure(spannable);
            holder.setText(R.id.rc_name, spannable);
        }

        IContactCardInfoProvider iContactCardInfoProvider
                = ContactCardContext.getInstance().getContactCardInfoProvider();
        if (iContactCardInfoProvider != null) {
            iContactCardInfoProvider.getContactAppointedInfoProvider(
                    contactMessage.getId(), contactMessage.getName(), contactMessage.getImgUrl(),
                    new IContactCardInfoProvider.IContactCardInfoCallback() {
                        @Override
                        public void getContactCardInfoCallback(List<? extends UserInfo> list) {
                            if (list != null && list.size() > 0) {
                                UserInfo userInfo = list.get(0);
                                if (userInfo != null && userInfo.getPortraitUri() != null) {
                                    /* 如果名片发送的推荐人头像信息，与本地数据库的对应头像信息不一致，
                                       则优先显示本地数据库的对应头像信息 */
                                    if (TextUtils.isEmpty(contactMessage.getImgUrl()) ||
                                            !contactMessage.getImgUrl().equals(userInfo.getPortraitUri().toString())) {
                                        Glide.with(imageView).load(userInfo.getPortraitUri())
                                                .apply(options)
                                                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                                .into(imageView);
                                        ((ContactMessage) (uiMessage.getMessage().getContent()))
                                                .setImgUrl(userInfo.getPortraitUri().toString());
                                    }
                                    // 如果本端设置了该用户信息的别名(备注、昵称)，优先显示这个别名
                                    if (!TextUtils.isEmpty(contactMessage.getName()) && !contactMessage.getName().equals(userInfo.getName())) {
                                        holder.setText(R.id.rc_name, userInfo.getName());
                                    }
                                }
                            }
                        }
                    });
        }

        if (uiMessage.getMessage().getMessageDirection() == Message.MessageDirection.RECEIVE) {
            holder.setBackgroundRes(R.id.rc_layout, R.drawable.rc_contact_bg_receive);
        } else {
            holder.setBackgroundRes(R.id.rc_layout, R.drawable.rc_contact_bg_send);
        }
    }

    @Override
    protected boolean onItemClick(ViewHolder holder, ContactMessage contactMessage, UiMessage uiMessage, int position, List<UiMessage> list, IViewProviderListener<UiMessage> listener) {
        if (iContactCardClickListener != null) {
            iContactCardClickListener.onContactCardClick(holder.getConvertView(), contactMessage);
            return true;
        }
        return false;
    }

    @Override
    protected boolean isMessageViewType(MessageContent messageContent) {
        return messageContent instanceof ContactMessage && !messageContent.isDestruct();
    }

    @Override
    public Spannable getSummarySpannable(Context context, ContactMessage contactMessage) {
        if (contactMessage != null && !TextUtils.isEmpty(contactMessage.getSendUserId())
                && !TextUtils.isEmpty(contactMessage.getSendUserName())) {
            if (contactMessage.getSendUserId().equals(RongIM.getInstance().getCurrentUserId())) {
                String str_RecommendClause = context.getResources().getString(R.string.rc_recommend_clause_to_others);
                return new SpannableString(String.format(str_RecommendClause, contactMessage.getName()));
            } else {
                String str_RecommendClause = context.getResources().getString(R.string.rc_recommend_clause_to_me);
                return new SpannableString(String.format(str_RecommendClause, "", contactMessage.getName()));
            }
        }
        return new SpannableString(context.getResources().getString(R.string.rc_plugins_contact));
    }
}
