package cn.rongcloud.im.ui.dialog;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

import cn.rongcloud.im.R;

/**
 * 用户详情更多弹窗
 */
public class UserDetailMorePopWindow extends PopupWindow {
    private OnPopWindowItemClickListener listener;
    private View contentView;

    public interface OnPopWindowItemClickListener {
        void onClickedBlackList(boolean isToBlackList);
    }

    @SuppressLint("InflateParams")
    public UserDetailMorePopWindow(final Activity context, boolean isInBlackList, OnPopWindowItemClickListener listener) {
        this.listener = listener;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.profile_popup_user_detail_title_more, null);

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

        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimationMainTitleMore);

        Button blackListBtn = contentView.findViewById(R.id.profile_btn_detail_black_list);
        blackListBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClickedBlackList(!isInBlackList);
                }
                dismiss();
            }
        });
        if (isInBlackList) {
            blackListBtn.setText(R.string.profile_detail_remove_from_blacklist);
        } else {
            blackListBtn.setText(R.string.profile_detail_join_the_blacklist);
        }

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
}
