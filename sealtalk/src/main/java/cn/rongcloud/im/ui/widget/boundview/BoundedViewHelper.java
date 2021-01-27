package cn.rongcloud.im.ui.widget.boundview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import cn.rongcloud.im.R;


/**
 * Created by baoleduc on 26/07/16.
 */
public class BoundedViewHelper {
    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;


    public BoundedViewHelper(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BoundedView);
            mMaxWidth = a.getDimensionPixelSize(R.styleable.BoundedView_boundedWidth, Integer.MAX_VALUE);
            mMaxHeight = a.getDimensionPixelSize(R.styleable.BoundedView_boundedHeight, Integer.MAX_VALUE);
            a.recycle();
        }
    }

    public int getBoundedMeasuredWidth(int width) {
        return Math.min(width, mMaxWidth);
    }

    public int getBoundedMeasuredHeight(int height) {
        return Math.min(height, mMaxHeight);
    }

}