package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.view.SealTitleBar;

public abstract class TitleBaseActivity extends BaseActivity {
    private ViewFlipper contentContainer;
    private SealTitleBar titleBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);
        titleBar = findViewById(R.id.title_bar);
        contentContainer = findViewById(R.id.layout_container);
        setTitleBarType(SealTitleBar.Type.NORMAL);
        getTitleBar().setOnBtnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void setContentView(View view) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        contentContainer.addView(view, lp);
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = LayoutInflater.from(this).inflate(layoutResID, null);
        setContentView(view);
    }

    public SealTitleBar getTitleBar() {
        return titleBar;
    }

    public void setTitleBarType(SealTitleBar.Type type) {
        titleBar.setType(type);
    }

    @Override
    public void finish() {
        super.finish();
        hideInputKeyboard();
    }
}
