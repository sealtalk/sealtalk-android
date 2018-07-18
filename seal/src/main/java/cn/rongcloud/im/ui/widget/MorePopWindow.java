package cn.rongcloud.im.ui.widget;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.SearchFriendActivity;
import cn.rongcloud.im.ui.activity.SelectFriendsActivity;


public class MorePopWindow extends PopupWindow {

    @SuppressLint("InflateParams")
    public MorePopWindow(final Activity context) {
        LayoutInflater inflater = (LayoutInflater) context
                                  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.popupwindow_add, null);

        // 设置SelectPicPopupWindow的View
        this.setContentView(content);
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
        this.setAnimationStyle(R.style.AnimationPreview);


        RelativeLayout re_addfriends = (RelativeLayout) content.findViewById(R.id.re_addfriends);
        RelativeLayout re_chatroom = (RelativeLayout) content.findViewById(R.id.re_chatroom);
        RelativeLayout re_scanner = (RelativeLayout) content.findViewById(R.id.re_scanner);
        re_addfriends.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(new Intent(context, SelectFriendsActivity.class));
                intent.putExtra("createGroup", true);
                context.startActivity(intent);
                MorePopWindow.this.dismiss();

            }

        });
        re_chatroom.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, SelectFriendsActivity.class));
                MorePopWindow.this.dismiss();

            }

        });
        re_scanner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, SearchFriendActivity.class));
                MorePopWindow.this.dismiss();
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
}
