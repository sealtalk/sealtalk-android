package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.interfaces.SearchableInterface;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.utils.log.SLog;

import static cn.rongcloud.im.ui.view.SealTitleBar.Type.SEARCH;

public class SealSearchBaseActivity extends TitleBaseActivity implements TextWatcher, SearchableInterface {
    private static final String TAG = "SealSearchBaseActivity";
    protected String search; //当前关键字

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setType(SEARCH);
        getTitleBar().addSeachTextChangedListener(this);
        setContentView(R.layout.activity_select_content);
        getTitleBar().setOnSearchClearTextClickedListener(new SealTitleBar.OnSearchClearTextClickedListener() {
            @Override
            public void onSearchClearTextClicked() {
                onBackPressed();
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        SLog.i(TAG, "afterTextChanged Editable = " + s);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                search = s.toString();
                if (TextUtils.isEmpty(search)) {
                    clear();
                } else {
                    search(search);
                }
            }
        }, 300);
    }

    @Override
    public void search(String match) {
        //子类实现自己搜索
    }

    @Override
    public void clear() {
        //子类实现清空搜索

    }

}
