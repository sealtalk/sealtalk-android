package cn.rongcloud.im.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

import io.rong.common.RLog;

/**
 * Created by Yuejunhong on 16/10/11.
 */
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
                              MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                if (h > height) //采用最大的view的高度。
                    height = h;
            }
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                            MeasureSpec.EXACTLY);

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
