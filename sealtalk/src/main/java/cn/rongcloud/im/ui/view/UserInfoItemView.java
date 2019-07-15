package cn.rongcloud.im.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;

public class UserInfoItemView extends FrameLayout {

    private SelectableRoundedImageView rightHeaderIv;
    private SelectableRoundedImageView leftHeaderIv;
    private TextView nameTv;
    private View vDivider;
    private View.OnClickListener portraitListener;

    /**
     * 在左侧显示头像
     */
    public static final int SHOW_LEFT = 0;
    /**
     * 在右侧显示头像
     */
    public static final int SHOW_RIGHT = 1;

    private int currentShow = SHOW_LEFT;


    public UserInfoItemView(@NonNull Context context) {
        super(context);
        initView(null);
    }

    public UserInfoItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public UserInfoItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        final View view = View.inflate(getContext(), R.layout.item_view_userinfo, this);
        nameTv = findViewById(R.id.tv_name);
        rightHeaderIv = findViewById(R.id.iv_right_header);
        leftHeaderIv = findViewById(R.id.iv_left_header);
        leftHeaderIv.setVisibility(VISIBLE);
        rightHeaderIv.setVisibility(GONE);

        if(portraitListener != null){
            rightHeaderIv.setOnClickListener(portraitListener);
            leftHeaderIv.setOnClickListener(portraitListener);
        }
        vDivider = findViewById(R.id.v_divider);
        vDivider.setVisibility(View.GONE);
        view.setBackgroundResource(R.drawable.seal_mine_setting_item_selector);


        TypedArray ta = attrs == null ? null : getContext().obtainStyledAttributes(attrs, R.styleable.UserInfoItemView);
        if (ta != null) {
            Drawable drawable = null;
            final int N = ta.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = ta.getIndex(i);
                switch (attr) {
                    case R.styleable.UserInfoItemView_item_userinfo_portrait:
                        drawable = ta.getDrawable(R.styleable.UserInfoItemView_item_userinfo_portrait);
                        break;
                    case R.styleable.UserInfoItemView_item_right_show:
                        boolean showRight = ta.getBoolean(attr, false);
                        currentShow = showRight ? SHOW_RIGHT : SHOW_LEFT;
                        break;
                    case R.styleable.UserInfoItemView_item_userinfo_portrait_width:
                        float width = ta.getDimension(R.styleable.UserInfoItemView_item_userinfo_portrait_width, 0);
                        ViewGroup.LayoutParams leftLayoutParams = leftHeaderIv.getLayoutParams();
                        ViewGroup.LayoutParams rightLayoutParams = rightHeaderIv.getLayoutParams();
                        if (width > 0) {
                            leftLayoutParams.width = Math.round(width);
                            rightLayoutParams.width = Math.round(width);

                        }
                        rightHeaderIv.setLayoutParams(rightLayoutParams);
                        leftHeaderIv.setLayoutParams(leftLayoutParams);

                        break;

                    case R.styleable.UserInfoItemView_item_userinfo_portrait_height:
                        float height = ta.getDimension(attr, 0);
                        ViewGroup.LayoutParams leftLayoutParamsHeight = leftHeaderIv.getLayoutParams();
                        ViewGroup.LayoutParams rightLayoutParamsHeight = rightHeaderIv.getLayoutParams();
                        if (height > 0) {
                            leftLayoutParamsHeight.height = Math.round(height);
                            rightLayoutParamsHeight.height = Math.round(height);
                        }
                        rightHeaderIv.setLayoutParams(rightLayoutParamsHeight);
                        leftHeaderIv.setLayoutParams(leftLayoutParamsHeight);
                        break;
                    case R.styleable.UserInfoItemView_item_userinfo_name:
                        String content = ta.getString(attr);
                        if (content != null) {
                            nameTv.setText(content);
                        }
                        break;
                    case R.styleable.UserInfoItemView_item_userinfo_divider:
                        boolean divider = ta.getBoolean(attr, false);
                        vDivider.setVisibility(divider ? VISIBLE : GONE);
                        break;
                    case R.styleable.UserInfoItemView_item_userifo_null_background:
                        Boolean bgNull = ta.getBoolean(attr, false);
                        if (bgNull) {
                            setBackground(null);
                        }
                        break;
                    default:
                        break;
                }
            }

            if (currentShow == SHOW_RIGHT) {
                leftHeaderIv.setVisibility(GONE);
                rightHeaderIv.setVisibility(VISIBLE);
                if (drawable != null) {
                    rightHeaderIv.setImageDrawable(drawable);
                }

            } else {
                leftHeaderIv.setVisibility(VISIBLE);
                rightHeaderIv.setVisibility(GONE);
                if (drawable != null) {
                    leftHeaderIv.setImageDrawable(drawable);
                }
            }

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float defHeight = getResources().getDimension(R.dimen.seal_mine_user_height);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int specMode = MeasureSpec.getMode(heightMeasureSpec);

        if (specMode != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) defHeight, MeasureSpec.EXACTLY);
        } else {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) height, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 设置用户名字
     * @param resId
     */
    public void setName(int resId) {
        nameTv.setText(resId);
    }

    /**
     * 设置用户名字
     * @param name
     */
    public void setName(String name) {
        nameTv.setText(name);
    }


    /**
     * 获取name 控件
     * @return
     */
    public TextView getNameTv() {
        return nameTv;
    }

    /**
     * 获得头像控件
     * @return
     */
    public ImageView getHeaderImageView() {
        if (currentShow == SHOW_LEFT) {
            return leftHeaderIv;
        } else if (currentShow == SHOW_RIGHT) {
            return rightHeaderIv;
        }
        return leftHeaderIv;
    }

    /**
     * 设置用户头像
     * @param portraitResId
     */
    public void setPortrait(int portraitResId) {
        setPortrait(portraitResId, currentShow);
    }

    /**
     * 设置用户头像， 可设置像是位置
     * @param portraitResId
     * @param showLocation 显示位置， 可设置 {@link SHOW_LEFT } 或 {@link SHOW_RIGHT }
     */
    public void setPortrait(int portraitResId, int showLocation) {
        if (showLocation == SHOW_LEFT) {
            leftHeaderIv.setVisibility(VISIBLE);
            rightHeaderIv.setVisibility(GONE);
            leftHeaderIv.setImageResource(portraitResId);
        } else if (showLocation == SHOW_RIGHT) {
            leftHeaderIv.setVisibility(GONE);
            rightHeaderIv.setVisibility(VISIBLE);
            rightHeaderIv.setImageResource(portraitResId);
        }
    }

    /**
     * 设置用户头像点击事件
     *
     * @param listener
     */
    public void setPortraitOnClickedListener(View.OnClickListener listener){
        portraitListener = listener;
        if(leftHeaderIv != null){
            leftHeaderIv.setOnClickListener(listener);
        }

        if(rightHeaderIv != null){
            rightHeaderIv.setOnClickListener(listener);
        }
    }

    /**
     * 名字颜色
     * @param colorRes
     */
    public void setNameTextColor(int colorRes) {
        nameTv.setTextColor(colorRes);
    }


    public void setDividerVisibility(int visibility) {
        vDivider.setVisibility(visibility);
    }
}
