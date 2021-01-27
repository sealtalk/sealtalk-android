package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.ui.fragment.SelectSingleFragment;
import cn.rongcloud.im.ui.view.SealTitleBar;

public class SelectSingleFriendActivity extends SelectBaseActivity implements View.OnClickListener {
    private SelectSingleFragment selectSingleFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SealTitleBar sealTitleBar = getTitleBar();
        sealTitleBar.setTitle(getString(R.string.seal_select_friend));
        sealTitleBar.getTvRight().setText(R.string.seal_select_confirm);
        sealTitleBar.getTvRight().setOnClickListener(this);
        setContentView(R.layout.activity_select_content);
        selectSingleFragment = new SelectSingleFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_fragment_container, selectSingleFragment);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        onConfirmClicked(selectSingleFragment.getCheckedList(),selectSingleFragment.getCheckedInitGroupList());
    }

    @Override
    protected void onConfirmClick() {
        onConfirmClicked(selectSingleFragment.getCheckedList(),selectSingleFragment.getCheckedInitGroupList());
    }

    @Override
    protected void onConfirmClicked(ArrayList<String> selectIds, ArrayList<String> selectGroups) {
        super.onConfirmClicked(selectIds, selectGroups);
        if (selectIds != null && selectIds.size() == 1) {
            Intent intent = new Intent();
            intent.putExtra(IntentExtra.STR_TARGET_ID, selectIds.get(0));
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    @Override
    protected boolean isSearchable() {
        return true;
    }

    @Override
    public void onSearch(String keyword) {
        selectSingleFragment.searchFriend(keyword);
    }
}
