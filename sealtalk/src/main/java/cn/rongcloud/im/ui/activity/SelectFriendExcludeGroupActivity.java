package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.ui.fragment.SelectFriendsExcludeGroupFragment;
import cn.rongcloud.im.ui.fragment.SelectMultiFriendFragment;

import static cn.rongcloud.im.common.IntentExtra.STR_TARGET_ID;

/**
 * 除了当前群组 groupId 之外的人
 */
public class SelectFriendExcludeGroupActivity extends SelectMultiFriendsActivity {
    private String groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        groupId = getIntent().getStringExtra(STR_TARGET_ID);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected SelectMultiFriendFragment getSelectMultiFriendFragment() {
        SelectFriendsExcludeGroupFragment fragment = new SelectFriendsExcludeGroupFragment();
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
