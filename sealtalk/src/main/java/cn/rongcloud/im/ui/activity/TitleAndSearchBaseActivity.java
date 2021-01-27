package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.view.SealTitleBar;

/**
 * 带有标题和搜索输入框的基础类
 * 通过实现 {@link TitleAndSearchBaseActivity#onSearch} 方法来进行搜索操作
 */
public abstract class TitleAndSearchBaseActivity extends TitleBaseActivity {
    /**
     * 输入搜索文字相应延迟
     */
    public static final int SEARCH_TEXT_INPUT_DELAY_MILLIS = 500;
    private FrameLayout containerLayout;
    private RelativeLayout searchTv;
    private TextView tvSearch;
    private SealTitleBar titleBar;
    private Handler delayHandler;
    private LinearLayout llSelectContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = LayoutInflater.from(this).inflate(R.layout.common_activity_title_and_search_base, null);
        super.setContentView(contentView);

        containerLayout = findViewById(R.id.title_and_search_container);
        searchTv = findViewById(R.id.title_and_search_tv_search);
        tvSearch = findViewById(R.id.tv_search);
        llSelectContent = findViewById(R.id.ll_select_content);
        titleBar = getTitleBar();

        delayHandler = new Handler();

        initTitleAndSearchView();
    }

    private void initTitleAndSearchView(){
        // 设置搜索框的点击事件
        searchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchTitle();
            }
        });

        // 设置搜索框的清除文本点击事件
        titleBar.setOnSearchClearTextClickedListener(new SealTitleBar.OnSearchClearTextClickedListener() {
            @Override
            public void onSearchClearTextClicked() {
                showNormalTitle();
            }
        });

        // 设置后退键的点击事件
        getTitleBar().setOnBtnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeSearchOrExit();
            }
        });

        // 设置标题栏搜索输入框的文本变化监听
        titleBar.addSeachTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                delayHandler.removeCallbacks(searchKeywordRunnable);
                String keyword = s.toString();
                int delay = SEARCH_TEXT_INPUT_DELAY_MILLIS;
                // 当输入空白时立即显示结果
                if(TextUtils.isEmpty(keyword)){
                    delay = 0;
                }
                delayHandler.postDelayed(searchKeywordRunnable, delay);
            }
        });
    }

    /**
     * 触发搜索操作，因为配合 delayHandler 使用，所以单独实现
     */
    private Runnable searchKeywordRunnable = new Runnable() {
        @Override
        public void run() {
            if(titleBar != null) {
                String keyword = titleBar.getEtSearch().getText().toString();
                onSearch(keyword);
            }
        }
    };

    /**
     * 当搜索中输入内容时会调用此方法
     * 此方法会在输入后
     * @param keyword
     */
    abstract public void onSearch(String keyword);

    /**
     * 设置确认按钮的可用情况
     * @param enable
     */
    public void enableConfirmButton(boolean enable) {
        TextView titleConfirmTv = titleBar.getTvRight();
        if (enable) {
            titleConfirmTv.setClickable(true);
            titleConfirmTv.setTextColor(getResources().getColor(android.R.color.black));
        } else {
            titleConfirmTv.setClickable(false);
            titleConfirmTv.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    /**
     * 获取搜索文本框
     * @return
     */
    public TextView getSearchTextView(){
        return  tvSearch;
    }

    @Override
    public void setContentView(View view) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        containerLayout.addView(view, lp);
    }

    /**
     * 显示搜索标题
     */
    private void showSearchTitle(){
        setTitleBarType(SealTitleBar.Type.SEARCH);
        searchTv.setVisibility(View.GONE);
    }

    /**
     * 显示普通标题
     */
    private void showNormalTitle(){
        titleBar.getEtSearch().setText("");
        setTitleBarType(SealTitleBar.Type.NORMAL);
        searchTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        closeSearchOrExit();
    }

    /**
     * 判断当前是该关闭搜索还是退出界面
     */
    private void closeSearchOrExit(){
        if(titleBar.getType() == SealTitleBar.Type.SEARCH){
            showNormalTitle();
        } else {
            finish();
        }
    }
}
