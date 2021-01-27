package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import cn.rongcloud.im.R;

import static cn.rongcloud.im.common.IntentExtra.BOOLEAN_CONFIRM_FORWARD;
import static cn.rongcloud.im.common.IntentExtra.LIST_ALREADY_CHECKED_USER_ID_LIST;
import static cn.rongcloud.im.common.IntentExtra.LIST_ALREADY_CHECKED_GROUP_ID_LIST;

/**
 * 转发多选选择更多
 */
public class SelectForwardMoreContactActivity extends SelectMultiFriendsActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(getString(R.string.seal_select_friend));
    }

    @Override
    protected void onConfirmClicked(ArrayList<String> selectIds, ArrayList<String> selectGroups) {
        setResult(selectIds, selectGroups, true);
    }

    @Override
    public void onBackPressed() {
        setResult(getCheckedFriendIds(), getCheckedGroupIds(), false);
    }

    @Override
    public void onSelectCountChange(int groupCount, int userCount) {
        updateBottomCount(groupCount, userCount);
    }

    private void setResult(ArrayList<String> fiends, ArrayList<String> groups, boolean isConfirmForward) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(LIST_ALREADY_CHECKED_USER_ID_LIST, fiends);
        intent.putStringArrayListExtra(LIST_ALREADY_CHECKED_GROUP_ID_LIST, groups);
        intent.putExtra(BOOLEAN_CONFIRM_FORWARD, isConfirmForward);//已经确认转发
        setResult(RESULT_OK, intent);
        finish();
    }
}
