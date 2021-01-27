package cn.rongcloud.im.ui.dialog;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

import cn.rongcloud.im.R;


public class MorePopWindow extends PopupWindow implements PopupWindow.OnDismissListener {
    private Activity context;
    private OnPopWindowItemClickListener listener;
    private View contentView;
    private static final float ALPHA_TRANSPARENT_COMPLETE = 1.0f;


    public interface OnPopWindowItemClickListener {
        void onStartChartClick();

        void onCreateGroupClick();

        void onAddFriendClick();

        void onScanClick();
    }

    @SuppressLint("InflateParams")
    public MorePopWindow(final Activity context, OnPopWindowItemClickListener listener) {
        this.listener = listener;
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.main_popup_title_more, null);

        // 设置SelectPicPopupWindow的View
        this.setContentView(contentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);

        setOnDismissListener(this);

        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimationMainTitleMore);
        contentView.findViewById(R.id.btn_start_chat).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onStartChartClick();
                }
                dismiss();
            }
        });
        contentView.findViewById(R.id.btn_create_group).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCreateGroupClick();
                }
                dismiss();
            }
        });
        contentView.findViewById(R.id.btn_add_friends).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAddFriendClick();
                }
                dismiss();
            }
        });
        contentView.findViewById(R.id.btn_scan).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onScanClick();
                }
                dismiss();
            }
        });

    }


    /**
     * 显示popupWindow
     *
     * @param parent
     */
    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAsDropDown(parent, 0, 0);
        } else {
            this.dismiss();
        }
    }

    /**
     * @param parent
     * @param alpha
     */
    public void showPopupWindow(View parent, float alpha, int xoff, int yoff) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAsDropDown(parent, xoff, yoff);
            setAlpha(alpha);
        } else {
            this.dismiss();
            setAlpha(ALPHA_TRANSPARENT_COMPLETE);
        }
    }

    private void setAlpha(float bgAlpha) {
        if (context == null || context.getWindow() == null) {
            return;
        }
        Window window = context.getWindow();
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        // 0.0-1.0
        lp.alpha = bgAlpha;
        window.setAttributes(lp);
        // everything behind this window will be dimmed.
        // 此方法用来设置浮动层，防止部分手机变暗无效
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    public void onDismiss() {
        super.dismiss();
        setAlpha(ALPHA_TRANSPARENT_COMPLETE);

    }
}
