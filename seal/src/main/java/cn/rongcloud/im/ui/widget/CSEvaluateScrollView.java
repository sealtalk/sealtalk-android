package cn.rongcloud.im.ui.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;

import cn.rongcloud.im.utils.CommonUtils;

public class CSEvaluateScrollView extends ScrollView {
    Context mContext;

    public CSEvaluateScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

/*    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View child = getChildAt(0);
        child.measure(widthMeasureSpec, heightMeasureSpec);
        int width = child.getMeasuredWidth();
        int height = Math.min(child.getMeasuredHeight(), getWindowHeight()
                - CommonUtils.dip2pix(mContext, 55)
                - getStatusBarHeight());
        setMeasuredDimension(width, height);
    }*/

    private int getWindowHeight() {
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);

        int height = wm.getDefaultDisplay().getHeight();
        return height;
    }

    private int getStatusBarHeight() {
        int statusBarHeight1 = -1;
//获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
        }
        Log.e("WangJ", "状态栏-方法1:" + statusBarHeight1);
        return statusBarHeight1;
    }
}