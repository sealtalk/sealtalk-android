package cn.rongcloud.im.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;

public class AnnouceView extends RelativeLayout {
    private TextView msgTv;
    private ImageView announceIv;
    private ImageView arrowIv;
    private OnAnnounceClickListener listener;
    private String url;

    public AnnouceView(Context context) {
        super(context);
        initView();
    }

    public AnnouceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AnnouceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.conversation_view_annouce, this);
        msgTv = view.findViewById(R.id.tv_announce_msg);
        announceIv = view.findViewById(R.id.iv_announce);
        arrowIv = view.findViewById(R.id.iv_announce_arrow);
        view.findViewById(R.id.ll_annouce).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && !TextUtils.isEmpty(url)) {
                    listener.onClick(v, url);
                }
            }
        });
    }

    /**
     * 设置通知信息
     * @param content
     * @param url
     */
    public void setAnnounce(String content, String url) {
        this.url = url;
        content = TextUtils.isEmpty(content) ? "" : content;
        msgTv.setText(content);
        if (TextUtils.isEmpty(url)) {
            setClickable(false);
            arrowIv.setVisibility(View.GONE);
        } else {
            setFocusable(true);
            setClickable(true);
            arrowIv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置点击监听
     * @param listener
     */
    public void setOnAnnounceClickListener(OnAnnounceClickListener listener) {
        this.listener = listener;
    }

    /**
     * 通知点击监听接口
     */
    public interface OnAnnounceClickListener {
        /**
         * 点击
         * @param v
         * @param url 具体通知内容的路径
         */
        void onClick(View v, String url);
    }
}
