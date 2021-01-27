package cn.rongcloud.im.im.provider;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.im.message.PokeMessage;
import io.rong.imkit.conversation.messgelist.provider.BaseMessageItemProvider;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.adapter.IViewProviderListener;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imlib.model.MessageContent;

/**
 * 戳一下消息模版
 */
public class PokeMessageItemProvider extends BaseMessageItemProvider<PokeMessage> {
    private final float POKE_ICON_WIDTH_DP = 15f;
    private final float POKE_ICON_HEIGHT_DP = 18.6f;

    @Override
    protected ViewHolder onCreateMessageContentViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_poke_message, parent, false);
        return new ViewHolder(view.getContext(),view);
    }

    @Override
    protected void bindMessageContentViewHolder(ViewHolder holder,ViewHolder parentHolder, PokeMessage pokeMessage, UiMessage uiMessage, int position, List<UiMessage> list, IViewProviderListener<UiMessage> listener) {
        Context context = holder.getContext();
        String content = pokeMessage.getContent();
        if (TextUtils.isEmpty(content)) {
            content = context.getString(R.string.im_plugin_poke_message_default);
        }
        String pokeTitle = context.getString(R.string.im_plugin_poke_title);
        String itemContent = "  " + pokeTitle + " " + content;
        SpannableString contentSpan = new SpannableString(itemContent);

        // 设置"戳一下"文字的颜色
        ForegroundColorSpan pokeTitleSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.default_clickable_text));
        int pokeTitleStarIndex = itemContent.indexOf(pokeTitle);
        int pokeTitleEndIndex = pokeTitleStarIndex + pokeTitle.length();
        contentSpan.setSpan(pokeTitleSpan, pokeTitleStarIndex, pokeTitleEndIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        // 设置戳一下图标
        Drawable pokeImg = context.getResources().getDrawable(R.drawable.im_plugin_img_dialog_send_poke);
        float densityDpi = context.getResources().getDisplayMetrics().density;
        int pokeImgWidth = (int) (POKE_ICON_WIDTH_DP * densityDpi);
        int pokeImgHeight = (int) (POKE_ICON_HEIGHT_DP * densityDpi);
        pokeImg.setBounds(0, 0, pokeImgWidth, pokeImgHeight);
        ImageSpan imageSpan = new ImageSpan(pokeImg);
        contentSpan.setSpan(imageSpan, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        holder.setText(R.id.item_tv_poke_message,contentSpan);
    }

    @Override
    protected boolean onItemClick(ViewHolder holder, PokeMessage pokeMessage, UiMessage uiMessage, int position,List<UiMessage> list, IViewProviderListener<UiMessage> listener) {
        return false;
    }

    @Override
    protected boolean isMessageViewType(MessageContent messageContent) {
        return messageContent instanceof PokeMessage;
    }

    @Override
    public Spannable getSummarySpannable(Context context, PokeMessage pokeMessage) {
        return new SpannableString(context.getString(R.string.im_message_content_poke));
    }
}
