package cn.rongcloud.im.ui.activity;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.ui.fragment.SelectGroupMemberMultiFragment;
import cn.rongcloud.im.ui.fragment.SelectMultiFriendFragment;

import static cn.rongcloud.im.common.IntentExtra.STR_TARGET_ID;
import static cn.rongcloud.im.common.IntentExtra.TITLE;

/**
 * 选择当前群组 groupId 内的人
 */
public class SelectGroupMemberActivity extends SelectMultiFriendsActivity {
    private String groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        groupId = getIntent().getStringExtra(STR_TARGET_ID);
        String title = getIntent().getStringExtra(TITLE);
        super.onCreate(savedInstanceState);
        if (title != null) {
            getTitleBar().setTitle(title);
        }
    }

    @Override
    protected SelectMultiFriendFragment getSelectMultiFriendFragment() {
        SelectGroupMemberMultiFragment fragment = new SelectGroupMemberMultiFragment();
        fragment.setGroupId(groupId);
        return fragment;
    }

    @Override
    protected void onConfirmClicked(ArrayList<String> selectIds, ArrayList<String> selectGroups) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST, selectIds);
        setResult(RESULT_OK, intent);
        finish();
    }

}
