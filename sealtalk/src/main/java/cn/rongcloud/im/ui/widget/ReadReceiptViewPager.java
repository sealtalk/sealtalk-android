package cn.rongcloud.im.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class ReadReceiptViewPager extends ViewPager {

    private boolean unscrollable = true;

    public ReadReceiptViewPager(Context context) {
        super(context);
    }

    public ReadReceiptViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = 0;
        //下面遍历所有child的高度
        for (int i = 0; i < getChildCount(); i++) {
            if (getCurrentItem() == i) {
                View child = getChildAt(i);
                child.measure(widthMeasureSpec,
                              View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                if (h > height) //采用最大的view的高度。
                    height = h;
            }
        }

        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height,
                            View.MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public boolean isUnscrollable() {
        return unscrollable;
    }

    public void setUnscrollable(boolean scrollable) {
        this.unscrollable = scrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (unscrollable)
            return false;
        else
            return super.onTouchEvent(arg0);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (unscrollable)
            return false;
        else
            return super.onInterceptTouchEvent(arg0);
    }
}
