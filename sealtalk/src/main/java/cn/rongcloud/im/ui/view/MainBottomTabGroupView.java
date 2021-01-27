package cn.rongcloud.im.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import cn.rongcloud.im.ui.widget.TabGroupView;
import cn.rongcloud.im.ui.widget.TabItem;

public class MainBottomTabGroupView extends TabGroupView {
    public MainBottomTabGroupView(Context context) {
        super(context);
    }

    public MainBottomTabGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainBottomTabGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View createView(TabItem item) {
        MainBottomTabItem mainBottomTabItem = new MainBottomTabItem(getContext());
        if (item.animationDrawable != null) {
            mainBottomTabItem.setAnimationDrawable(item.animationDrawable);
        } else {
            mainBottomTabItem.setDrawable(item.drawable);
        }
        mainBottomTabItem.setName(item.text);
        return mainBottomTabItem;
    }

    private long firstClick = 0;
    private OnTabDoubleClickListener doubleListener;

    @Override
    protected void onItemClick(TabItem item, OnTabSelectedListener listener, View view) {
        if (getSelectedItemId() == item.id) {
            if (firstClick == 0) {
                firstClick = System.currentTimeMillis();
            } else {
                long secondClick = System.currentTimeMillis();
                if (secondClick - firstClick > 0 && secondClick - firstClick <= 800) {
                    if (doubleListener != null) {
                        doubleListener.onDoubleClick(item, view);
                    }
                    firstClick = 0;
                } else {
                    firstClick = secondClick;
                }
            }

        } else {
            super.onItemClick(item, listener, view);
        }
    }

    /**
     * 设置双击监听
     *
     * @param listener
     */
    public void setOnTabDoubleClickListener(OnTabDoubleClickListener listener) {
        this.doubleListener = listener;
    }


    /**
     * tab 双击监听
     */
    public interface OnTabDoubleClickListener {
        void onDoubleClick(TabItem item, View view);
    }
}
