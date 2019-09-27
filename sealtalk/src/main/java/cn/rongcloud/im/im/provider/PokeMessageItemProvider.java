package cn.rongcloud.im.im.provider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.im.message.PokeMessage;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Message;


/**
 * 戳一下消息模版
 */
@ProviderTag(messageContent = PokeMessage.class, showProgress = false, showReadState = true)
public class PokeMessageItemProvider extends IContainerItemProvider.MessageProvider<PokeMessage> {
    private final float POKE_ICON_WIDTH_DP = 15f;
    private final float POKE_ICON_HEIGHT_DP = 18.6f;

    @Override
    public void bindView(View view, int i, PokeMessage pokeMessage, UIMessage uiMessage) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        Context context = view.getContext();

        if (uiMessage.getMessageDirection() == Message.MessageDirection.SEND) {
            viewHolder.contentTv.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_right);
        } else {
            viewHolder.contentTv.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_left);
        }

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
        Drawable pokeImg = view.getContext().getResources().getDrawable(R.drawable.im_plugin_img_dialog_send_poke);
        float densityDpi = context.getResources().getDisplayMetrics().density;
        int pokeImgWidth = (int) (POKE_ICON_WIDTH_DP * densityDpi);
        int pokeImgHeight = (int) (POKE_ICON_HEIGHT_DP * densityDpi);
        pokeImg.setBounds(0, 0, pokeImgWidth, pokeImgHeight);
        ImageSpan imageSpan = new ImageSpan(pokeImg);
        contentSpan.setSpan(imageSpan, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        viewHolder.contentTv.setText(contentSpan);
    }

    @Override
    public Spannable getContentSummary(PokeMessage pokeMessage) {
        return null;
    }

    @Override
    public Spannable getContentSummary(Context context, PokeMessage pokeMessage) {
        return new SpannableString(context.getString(R.string.im_message_content_poke));
    }

    @Override
    public void onItemClick(View view, int i, PokeMessage pokeMessage, UIMessage uiMessage) {

    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.message_item_poke_message, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.contentTv = contentView.findViewById(R.id.item_tv_poke_message);
        contentView.setTag(viewHolder);
        return contentView;
    }

    private class ViewHolder {
        TextView contentTv;
    }

    /**
     * 图片与文字对齐时使用
     */
    private class TextAlignImageSpan extends ImageSpan {
        private Drawable image;

        public TextAlignImageSpan(@NonNull Drawable drawable) {
            super(drawable);
            image = drawable;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text,
                         int start, int end, float x,
                         int top, int y, int bottom, Paint paint) {
            Drawable b = image;
            canvas.save();
            canvas.translate(x, paint.getFontMetricsInt().descent);
            b.draw(canvas);
            canvas.restore();
        }
    }

}
