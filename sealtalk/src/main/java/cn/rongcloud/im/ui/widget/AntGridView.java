package cn.rongcloud.im.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class AntGridView extends GridView {

    public AntGridView(Context context) {
        super(context);
    }

    public AntGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AntGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    /*重点在这里重写onMeasure()*/
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
