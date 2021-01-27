package cn.rongcloud.im.ui.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.widget.DragPointView;
import cn.rongcloud.im.ui.widget.TabItem;

public class MainBottomTabItem extends RelativeLayout {
    private ImageView tabImage;
    private TextView tabText;
    private ImageView redIv;
    private DragPointView numDpv;
    private TabItem.AnimationDrawableBean mAnimationDrawable;

    public MainBottomTabItem(Context context) {
        super(context);
        initView();
    }

    public MainBottomTabItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MainBottomTabItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.item_tab, this);
        tabImage = view.findViewById(R.id.iv_tab_img);
        tabText = view.findViewById(R.id.tv_tab_text);
        redIv = view.findViewById(R.id.iv_red);
        numDpv = view.findViewById(R.id.dpv_num);
    }

    public void setName(String name) {
        tabText.setText(name);
    }

    public void setAnimationDrawable(TabItem.AnimationDrawableBean animationDrawableBean) {
        this.mAnimationDrawable = animationDrawableBean;
        setDrawable(mAnimationDrawable.drawableNormal);
    }

    public void setDrawable(int drawable) {
        tabImage.setImageResource(drawable);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (mAnimationDrawable != null) {
            if (selected) {
                AnimationDrawable animationDrawable = (AnimationDrawable) getResources().getDrawable(mAnimationDrawable.drawableAnimation);
                tabImage.setImageDrawable(animationDrawable);
                animationDrawable.setOneShot(true);
                animationDrawable.start();
            } else {
                // 防止点击过快动画还未结束
//                AnimationDrawable animationDrawable = (AnimationDrawable) tabImage.getBackground();
//                if (animationDrawable!=null && animationDrawable.isRunning()){
//                    animationDrawable.stop();
//                }
                tabImage.setImageDrawable(getResources().getDrawable(mAnimationDrawable.drawableNormal));
            }
        }
        tabImage.setSelected(selected);
        tabText.setSelected(selected);
    }

    /**
     * 红点
     *
     * @param visibility
     */
    public void setRedVisibility(int visibility) {
        redIv.setVisibility(visibility);
    }

    /**
     * 数量
     *
     * @param visibility
     */
    public void setNumVisibility(int visibility) {
        numDpv.setVisibility(visibility);
    }

    /**
     * 消息数
     *
     * @param num
     */
    public void setNum(String num) {
        numDpv.setText(num);
    }

    /**
     * 设置未读书多拽监听
     *
     * @param listener
     */
    public void setTabUnReadNumDragListener(DragPointView.OnDragListencer listener) {
        numDpv.setDragListencer(listener);
    }
}
