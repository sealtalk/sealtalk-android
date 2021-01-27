package cn.rongcloud.im.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import cn.rongcloud.im.R;

public class StarsView extends LinearLayout {

    private int starIndex = 0;
    private OnSelectStatusListener listener;
    private int maxStar = 0;

    public StarsView(Context context) {
        super(context);
    }

    public StarsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StarsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化
     * @param num
     */
    public void init (int num) {
        this.maxStar = num;
        setOrientation(HORIZONTAL);
        for (int i=0; i<num; i++) {
            ImageView imageView = new ImageView(getContext());
            imageView.setTag(Integer.valueOf(i + 1));
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object tag = v.getTag();
                    if (tag != null && tag instanceof Integer) {
                        updateView((int)tag);
                    }
                }
            });
            imageView.setImageResource(R.drawable.seal_dialog_evaluate_star_selector);
            addView(imageView, getStarLayoutParams());
        }
    }

    /**
     * 显示star数
     * @param index
     */
    private void updateView(int index) {
        starIndex = index;
        int childCount = getChildCount();
        for (int i=0; i<childCount; i++) {
            View view = getChildAt(i);
            if (i+1 <= index) {
                view.setSelected(true);
            } else {
                view.setSelected(false);
            }
        }

        if (listener != null) {
            listener.onSelectStatus(this, starIndex);
        }

    }

    private LayoutParams getStarLayoutParams() {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = dip2px(6);
        layoutParams.rightMargin = dip2px(6);
        return layoutParams;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 获取当前几星
     * @return
     */
    public int getStars() {
        return starIndex;
    }


    /**
     * 设置选择监听
     * @param listener
     */
    public void setOnSelectStatusListener (OnSelectStatusListener listener) {
        this.listener = listener;
    }

    public int getMaxStar() {
        return maxStar;
    }

    public interface OnSelectStatusListener {
        void onSelectStatus(View view, int stars);
    }
}
