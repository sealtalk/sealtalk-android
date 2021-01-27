package cn.rongcloud.im.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;

public class SettingItemView extends LinearLayout {
    private ImageView ivTagImage;
    private SwitchButton sbSwitch;
    private TextView tvValue;
    private TextView tvContent;
    private ImageView ivImage;
    private ImageView ivSelectImage;
    private boolean isShowSelected = false;
    private ImageView ivRightImage;

    private CompoundButton.OnCheckedChangeListener checkedListener;

    public SettingItemView(Context context) {
        super(context);
        init(null);
    }

    public SettingItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public SettingItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        View view = View.inflate(getContext(), R.layout.item_setting, this);
        ivImage = findViewById(R.id.iv_image);
        tvContent = findViewById(R.id.tv_content);
        tvValue = findViewById(R.id.tv_value);
        View vDivider = findViewById(R.id.v_divider);
        ivTagImage = findViewById(R.id.iv_tag_image);
        sbSwitch = findViewById(R.id.sb_switch);
        ivSelectImage = findViewById(R.id.iv_select_image);
        ivRightImage = findViewById(R.id.iv_right_image);

        ivImage.setVisibility(GONE);
        tvValue.setVisibility(GONE);
        ivTagImage.setVisibility(GONE);
        sbSwitch.setVisibility(GONE);
        vDivider.setVisibility(GONE);
        ivSelectImage.setVisibility(GONE);
        ivRightImage.setVisibility(GONE);

        view.setBackgroundResource(R.drawable.seal_mine_setting_item_selector);

        TypedArray ta = attrs == null ? null : getContext().obtainStyledAttributes(attrs, R.styleable.SettingItemView);
        if (ta != null) {
            final int N = ta.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = ta.getIndex(i);
                switch (attr) {
                    case R.styleable.SettingItemView_item_image:
                        Drawable drawable = ta.getDrawable(attr);
                        ivImage.setVisibility(VISIBLE);
                        ivImage.setImageDrawable(drawable);
                        break;
                    case R.styleable.SettingItemView_item_image_height:
                        float imageHeight = ta.getDimension(attr, 0);
                        if (imageHeight > 0) {
                            ViewGroup.LayoutParams layoutParamsHeight = ivImage.getLayoutParams();
                            layoutParamsHeight.height = Math.round(imageHeight);
                            ivImage.setLayoutParams(layoutParamsHeight);
                        }
                        break;
                    case R.styleable.SettingItemView_item_image_width:
                        float imageWidth = ta.getDimension(attr, 0);
                        if (imageWidth > 0) {
                            ViewGroup.LayoutParams layoutParamsWidth = ivImage.getLayoutParams();
                            layoutParamsWidth.width = Math.round(imageWidth);
                            ivImage.setLayoutParams(layoutParamsWidth);
                        }
                        break;
                    case R.styleable.SettingItemView_item_content:
                        String content = ta.getString(attr);
                        tvContent.setText(content == null ? "" : content);
                        break;
                    case R.styleable.SettingItemView_item_content_text_size:
                        float contentSize = ta.getDimension(attr, 0);
                        if (contentSize > 0) {
                            tvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.round(contentSize));
                        }
                        break;
                    case R.styleable.SettingItemView_item_content_text_color:
                        int color = ta.getColor(attr, -1);
                        if (color > 0) {
                            tvContent.setTextColor(color);
                        }
                        break;
                    case R.styleable.SettingItemView_item_value:
                        String value = ta.getString(attr);
                        tvValue.setVisibility(VISIBLE);
                        tvValue.setText(value);
                        break;
                    case R.styleable.SettingItemView_item_value_text_size:
                        float valueSize = ta.getDimension(attr, 0);
                        if (valueSize > 0) {
                            tvValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.round(valueSize));
                        }
                        break;
                    case R.styleable.SettingItemView_item_value_text_color:
                        int valueColor = ta.getColor(attr, -1);
                        if (valueColor > 0) {
                            tvValue.setTextColor(valueColor);
                        }
                        break;

                    case R.styleable.SettingItemView_item_tag_image:
                        Drawable tagImage = ta.getDrawable(R.styleable.SettingItemView_item_tag_image);
                        if (tagImage != null) {
                            ivTagImage.setImageDrawable(tagImage);
                        }
                        break;
                    case R.styleable.SettingItemView_item_tag_image_height:
                        float tagImageHeight = ta.getDimension(R.styleable.SettingItemView_item_tag_image_height, 0);
                        if (tagImageHeight > 0) {
                            ViewGroup.LayoutParams layoutParamsTagHeight = ivTagImage.getLayoutParams();
                            layoutParamsTagHeight.height = Math.round(tagImageHeight);
                            ivTagImage.setLayoutParams(layoutParamsTagHeight);
                        }
                        break;
                    case R.styleable.SettingItemView_item_tag_image_width:
                        float tagImageWidth = ta.getDimension(R.styleable.SettingItemView_item_tag_image_width, 0);
                        if (tagImageWidth > 0) {
                            ViewGroup.LayoutParams layoutParamsTagWidth = ivTagImage.getLayoutParams();
                            layoutParamsTagWidth.width = Math.round(tagImageWidth);
                            ivTagImage.setLayoutParams(layoutParamsTagWidth);
                        }
                        break;
                    case R.styleable.SettingItemView_item_divider:
                        boolean divider = ta.getBoolean(attr, false);
                        vDivider.setVisibility(divider ? VISIBLE : GONE);
                        break;
                    case R.styleable.SettingItemView_item_switch:
                        boolean switchCheck = ta.getBoolean(attr, false);
                        if (switchCheck) {
                            sbSwitch.setVisibility(VISIBLE);
                        } else {
                            sbSwitch.setVisibility(GONE);
                        }
                        break;
                    case R.styleable.SettingItemView_item_null_background:
                        Boolean bgNull = ta.getBoolean(attr, false);
                        if (bgNull) {
                            setBackground(null);
                        }
                        break;
                    case R.styleable.SettingItemView_item_background:
                        Drawable bg = ta.getDrawable(attr);
                        setBackground(bg);
                        break;
                    case R.styleable.SettingItemView_item_show_selected:
                        isShowSelected = ta.getBoolean(attr, false);
                        break;
                    case R.styleable.SettingItemView_item_selected_image:
                        Drawable selectedImage = ta.getDrawable(attr);
                        ivSelectImage.setImageDrawable(selectedImage);
                        break;
                    case R.styleable.SettingItemView_item_right_image:
                        Drawable rightImage = ta.getDrawable(attr);
                        ivRightImage.setImageDrawable(rightImage);
                        ivRightImage.setVisibility(VISIBLE);
                        break;
                    case R.styleable.SettingItemView_item_right_image_height:
                        float rightImageHeight = ta.getDimension(attr, 0);
                        if (rightImageHeight > 0) {
                            ViewGroup.LayoutParams layoutParamsHeight = ivRightImage.getLayoutParams();
                            layoutParamsHeight.height = Math.round(rightImageHeight);
                            ivRightImage.setLayoutParams(layoutParamsHeight);
                        }
                        break;
                    case R.styleable.SettingItemView_item_right_image_width:
                        float rightImageWidth = ta.getDimension(attr, 0);
                        if (rightImageWidth > 0) {
                            ViewGroup.LayoutParams layoutParamsWidth = ivRightImage.getLayoutParams();
                            layoutParamsWidth.width = Math.round(rightImageWidth);
                            ivRightImage.setLayoutParams(layoutParamsWidth);
                        }
                        break;
                }
            }
        }

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float defHeight = getResources().getDimension(R.dimen.seal_setting_item_def_height);
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
     * 设置右侧图片的隐藏
     *
     * @param visibility
     */
    public void setRightImageVisibility(int visibility) {
        ivRightImage.setVisibility(visibility);
    }

    /**
     * 设置标示图片显示隐藏
     *
     * @param visibility
     */
    public void setTagImageVisibility(int visibility) {
        ivTagImage.setVisibility(visibility);
    }

    /**
     * 设置标示图片
     *
     * @param resId
     */
    public void setTagImage(int resId) {
        ivTagImage.setImageResource(resId);
    }


    /**
     * 设置switch 按钮显示隐藏
     *
     * @param visibility
     */
    public void setSwitchButtonVisibility(int visibility) {
        sbSwitch.setVisibility(visibility);
    }

    /**
     * 设置 switch 按钮选择监听
     *
     * @param listener
     */
    public void setSwitchCheckListener(CompoundButton.OnCheckedChangeListener listener) {
        checkedListener = listener;
        sbSwitch.setOnCheckedChangeListener(checkedListener);
    }

    public void setSwitchTouchListener(OnTouchListener listener) {
        sbSwitch.setOnTouchListener(listener);
    }


    /**
     * 设置 value 值 显示隐藏
     *
     * @param visibility
     */
    public void setValueVisibility(int visibility) {
        tvValue.setVisibility(visibility);
    }

    /**
     * 设置 value 值
     *
     * @param resId
     */
    public void setValue(int resId) {
        tvValue.setText(resId);
        tvValue.setVisibility(VISIBLE);
    }

    /**
     * 设置 value 值
     *
     * @param value
     */
    public void setValue(String value) {
        tvValue.setText(value);
        tvValue.setVisibility(VISIBLE);
    }

    /**
     * 设置 value 颜色
     */
    public TextView getValueView() {
        return tvValue;
    }

    /**
     * 获取value
     *
     * @return
     */
    public String getValue() {
        if (tvValue.getText() == null) {
            return "";
        }
        return tvValue.getText().toString();
    }

    /**
     * 设置内容
     *
     * @param resId
     */
    public void setContent(int resId) {
        tvContent.setText(resId);
    }

    /**
     * 设置内容
     *
     * @param content
     */
    public void setContent(String content) {
        tvContent.setText(content);
    }

    /**
     * 设置左侧图片显示隐藏
     *
     * @param visibility
     */
    public void setImageVisibility(int visibility) {
        ivImage.setVisibility(visibility);
    }

    /**
     * 设置左侧图片
     *
     * @param resId
     */
    public void setImage(int resId) {
        ivImage.setImageResource(resId);
    }

    /**
     * 设置 switch 按钮选择状态
     *
     * @param isChecked
     */
    public void setChecked(boolean isChecked) {
        sbSwitch.setChecked(isChecked);
    }

    /**
     * 设置 switch 按钮选择状态,不触发选中事件
     *
     * @param isChecked
     */
    public void setCheckedWithOutEvent(boolean isChecked) {
        sbSwitch.setOnCheckedChangeListener(null);
        sbSwitch.setChecked(isChecked);
        sbSwitch.setOnCheckedChangeListener(checkedListener);
    }

    /**
     * 立即设置 switch 按钮选择状态，没有动画
     *
     * @param isChecked
     */
    public void setCheckedImmediately(boolean isChecked) {
        sbSwitch.setCheckedImmediately(isChecked);
    }

    /**
     * 立即设置 switch 按钮选择状态，没有动画,不触发选中事件
     *
     * @param isChecked
     */
    public void setCheckedImmediatelyWithOutEvent(boolean isChecked) {
        sbSwitch.setOnCheckedChangeListener(null);
        sbSwitch.setCheckedImmediately(isChecked);
        sbSwitch.setOnCheckedChangeListener(checkedListener);
    }

    /**
     * 获取当前 switch 状态
     */
    public boolean isChecked() {
        return sbSwitch.isChecked();
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected && isShowSelected) {
            ivSelectImage.setVisibility(View.VISIBLE);
        } else {
            ivSelectImage.setVisibility(View.GONE);
        }
    }

    public ImageView getRightImageView() {
        return ivRightImage;
    }
}
