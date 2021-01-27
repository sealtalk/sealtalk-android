package cn.rongcloud.im.ui.view;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.widget.ClearWriteEditText;

public class SealTitleBar extends RelativeLayout {
    private TextView btnLeft;
    private ImageButton btnRight;
    private TextView tvTitle;
    private ClearWriteEditText etSearch;
    private TextView tvClear;
    private LinearLayout llSearch;
    private TextView tvRight;
    private TextView tvTyping;
    private View flContent;
    private Type type;

    private OnSearchClearTextClickedListener searchClearTextClickedListener;

    public SealTitleBar(Context context) {
        super(context);
        initView();
    }

    public SealTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SealTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.layout_title, this);
        btnLeft = view.findViewById(R.id.btn_left);
        btnRight = view.findViewById(R.id.btn_right);
        tvTitle = view.findViewById(R.id.tv_title);
        tvTyping = view.findViewById(R.id.tv_typing);
        etSearch = view.findViewById(R.id.et_search);
        llSearch = view.findViewById(R.id.ll_search);
        tvRight = view.findViewById(R.id.tv_right);
        flContent = view.findViewById(R.id.fl_content);
        tvClear = view.findViewById(R.id.tv_clear);
        tvClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
                etSearch.clearFocus();
                if (searchClearTextClickedListener != null) {
                    searchClearTextClickedListener.onSearchClearTextClicked();
                }
            }
        });
//        etSearch.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                final int DRAWABLE_RIGHT = 2;
//                if (event.getAction() == MotionEvent.ACTION_UP) {
////                    if (event.getRawX() >= (etSearch.getRight() - 2 * etSearch.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
////                        etSearch.setText("");
////                        etSearch.clearFocus();
////                        if(searchClearTextClickedListener != null){
////                            searchClearTextClickedListener.onSearchClearTextClicked();
////                        }
////                        return true;
////                    }
//                }
//                return false;
//            }
//        });

        setType(Type.NORMAL);
    }

    public TextView getBtnLeft() {
        return btnLeft;
    }

    public ImageButton getBtnRight() {
        return btnRight;
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public EditText getEtSearch() {
        return etSearch;
    }

    public TextView getTvRight() {
        return tvRight;
    }

    public enum Type {
        /**
         * 正常的模式， 有左右按钮和 title
         */
        NORMAL,
        /**
         * 搜索框， 有左边按钮和搜索框
         */
        SEARCH,
        /**
         * 正在输入
         */
        TYPING
    }

    public void setType(Type type) {
        switch (type) {
            case NORMAL:
                btnLeft.setVisibility(View.VISIBLE);
                btnRight.setVisibility(View.VISIBLE);
                tvTitle.setVisibility(View.VISIBLE);
                tvRight.setVisibility(View.VISIBLE);
                llSearch.setVisibility(View.GONE);
                tvTyping.setVisibility(View.GONE);
                flContent.setVisibility(View.VISIBLE);
                break;
            case SEARCH:
                btnLeft.setVisibility(View.GONE);
                btnRight.setVisibility(View.GONE);
                tvTitle.setVisibility(View.GONE);
                tvRight.setVisibility(View.GONE);
                llSearch.setVisibility(View.VISIBLE);
                tvTyping.setVisibility(View.GONE);
                flContent.setVisibility(View.GONE);
                break;
            case TYPING:
                btnLeft.setVisibility(View.VISIBLE);
                btnRight.setVisibility(View.VISIBLE);
                tvTitle.setVisibility(View.GONE);
                tvRight.setVisibility(View.VISIBLE);
                llSearch.setVisibility(View.GONE);
                tvTyping.setVisibility(View.VISIBLE);
                flContent.setVisibility(View.VISIBLE);

                break;
            default:
                // Do nothing
                break;
        }
        this.type = type;
    }

    /**
     * 返回当前标题类型
     *
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * 正在输入内容
     *
     * @param resId
     */
    public void setTyping(int resId) {
        tvTyping.setText(resId);
    }

    /**
     * 正在输入内容
     *
     * @param text
     */
    public void setTyping(String text) {
        tvTyping.setText(text);
    }

    /**
     * 做按钮监听
     *
     * @param leftClickListener
     */
    public void setOnBtnLeftClickListener(OnClickListener leftClickListener) {
        btnLeft.setOnClickListener(leftClickListener);
    }

    /**
     * 右按钮监听
     *
     * @param rightClickListener
     */
    public void setOnBtnRightClickListener(OnClickListener rightClickListener) {
        btnRight.setOnClickListener(rightClickListener);
        btnRight.setVisibility(View.VISIBLE);
    }


    /**
     * 右按钮监听
     *
     * @param rightClickListener
     */
    public void setOnBtnRightClickListener(String text, OnClickListener rightClickListener) {
        tvRight.setText(text);
        tvRight.setOnClickListener(rightClickListener);
        tvRight.setVisibility(View.VISIBLE);
    }

    /**
     * 设置有按钮文本
     *
     * @param text
     */
    public void setRightText(String text) {
        tvRight.setText(text);
    }

    /**
     * 设置有按钮文本
     *
     * @param resId
     */
    public void setRightText(int resId) {
        tvRight.setText(resId);
    }

    /**
     * 设置 title
     *
     * @param textResId
     */
    public void setTitle(int textResId) {
        tvTitle.setText(textResId);
    }

    /**
     * 设置title
     *
     * @param title
     */
    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    /**
     * 添加edit 内容变化监听
     *
     * @param watcher
     */
    public void addSeachTextChangedListener(TextWatcher watcher) {
        etSearch.addCommonTextChangedListener(watcher);
    }

    /**
     * 设置当清除搜索内容点击事件
     *
     * @param listener
     */
    public void setOnSearchClearTextClickedListener(OnSearchClearTextClickedListener listener) {
        searchClearTextClickedListener = listener;
    }

    /**
     * 当搜索模式时点击清除搜索监听
     */
    public interface OnSearchClearTextClickedListener {
        void onSearchClearTextClicked();
    }
}
