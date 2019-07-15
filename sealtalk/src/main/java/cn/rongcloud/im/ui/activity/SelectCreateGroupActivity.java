package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;

public class SelectCreateGroupActivity extends SelectMultiFriendsActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(getString(R.string.seal_select_group_member));
    }

    @Override
    protected void onConfirmClicked(ArrayList<String> selectIds,ArrayList<String> selectGroups) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST, selectIds);
        setResult(RESULT_OK, intent);
        finish();
    }
}
