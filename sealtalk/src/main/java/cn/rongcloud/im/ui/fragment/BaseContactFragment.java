package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.widget.SideBar;

public class BaseContactFragment extends BaseFragment implements SideBar.OnTouchingLetterChangedListener {
    private static String TAG = "BaseContactFragment";
    protected RecyclerView recyclerView;
    protected SideBar sideBar;
    private TextView textView;

    @Override
    protected int getLayoutResId() {
        return R.layout.main_fragment_contacts_list;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        recyclerView = findView(R.id.rv_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        sideBar = findView(R.id.sv_sidebar);
        sideBar.setOnTouchingLetterChangedListener(this);
        textView = findView(R.id.tv_group_dialog);
        sideBar.setTextView(textView);
    }

    /**
     * 右侧字母点击
     *
     * @param s
     */
    @Override
    public void onTouchingLetterChanged(String s) {

    }

}
