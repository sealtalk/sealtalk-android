package cn.rongcloud.im.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;

import cn.rongcloud.im.R;


/**
 * Created by AMing on 15/11/2.
 * Company RongCloud
 */
@SuppressLint("AppCompatCustomView")
public class ClearWriteEditText extends EditText implements View.OnFocusChangeListener, TextWatcher {

    /**
     * 删除按钮的引用
     */
    private Drawable mClearDrawable;

    private boolean neverShowClearDrawable;
    private boolean showClearDrawableNoFocus;
    private TextWatcher watcher;

    public ClearWriteEditText(Context context) {
        this(context, null);
    }

    public ClearWriteEditText(Context context, AttributeSet attrs) {
        //这里构造方法也很重要，不加这个很多属性不能再XML里面定义
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearWriteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        this.setOnFocusChangeListener(this);
        this.addTextChangedListener(this);


        TypedArray ta = attrs == null ? null : getContext().obtainStyledAttributes(attrs, R.styleable.ClearWriteEditText);
        if (ta != null) {
            Drawable drawable = null;
            final int N = ta.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = ta.getIndex(i);
                switch (attr) {
                    case R.styleable.ClearWriteEditText_et_right_image:
                        drawable = ta.getDrawable(R.styleable.ClearWriteEditText_et_right_image);
                        mClearDrawable = drawable;
//                        mClearDrawable = getResources().getDrawable(R.drawable.seal_ic_search_clear_pressed_write);
                        mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
                        setClearIconVisible(false);
                        break;
                    case R.styleable.ClearWriteEditText_et_left_image:
                        drawable = ta.getDrawable(R.styleable.ClearWriteEditText_et_left_image);
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        setCompoundDrawables(drawable, getCompoundDrawables()[1],
                                getCompoundDrawables()[2], getCompoundDrawables()[3]);
                        break;
                    default:
                        break;
                }
            }
        }

    }

    /**
     * 当输入框里面内容发生变化的时候回调的方法
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        if (!neverShowClearDrawable) {
            setClearIconVisible(s.length() > 0);
        }


        if (watcher == null) {
            return;
        }
        watcher.onTextChanged(s, start, count, after);
    }


    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     *
     * @param visible
     */
    protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    /**
     * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件
     * 当我们按下的位置 在  EditText的宽度 - 图标到控件右边的间距 - 图标的宽度  和
     * EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标，竖直方向没有考虑
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getCompoundDrawables()[2] != null) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                boolean touchable = event.getX() > (getWidth()
                        - getPaddingRight() - mClearDrawable.getIntrinsicWidth())
                        && (event.getX() < ((getWidth() - getPaddingRight())));
                if (touchable) {
                    this.setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus && !neverShowClearDrawable) {
            setClearIconVisible(getText().length() > 0);
        } else {
            if (!showClearDrawableNoFocus) {
                setClearIconVisible(false);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (watcher == null) {
            return;
        }

        watcher.beforeTextChanged(s, start, count, after);

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (watcher == null) {
            return;
        }

        watcher.afterTextChanged(s);

    }

    /**
     * 设置晃动动画
     */
    public void setShakeAnimation() {
        this.startAnimation(shakeAnimation(3));


    }


    /**
     * 晃动动画
     *
     * @param counts 半秒钟晃动多少下
     * @return
     */
    public static Animation shakeAnimation(int counts) {
        Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(counts));
        translateAnimation.setDuration(500);
        return translateAnimation;
    }

    public Drawable getClearDrawable() {
        return mClearDrawable;
    }

    public void setClearDrawable(Drawable mClearDrawable) {
        this.mClearDrawable = mClearDrawable;
        this.mClearDrawable.setBounds(0, 0, this.mClearDrawable.getIntrinsicWidth(), this.mClearDrawable.getIntrinsicHeight());
        setClearIconVisible(false);
    }

    public void setClearDrawableNeverShow(boolean neverShow) {
        neverShowClearDrawable = neverShow;
    }

    public void setShowClearDrawableNoFocus(boolean needShow) {
        showClearDrawableNoFocus = needShow;
    }

    public void addCommonTextChangedListener(TextWatcher watcher) {
        this.watcher = watcher;
    }
}
