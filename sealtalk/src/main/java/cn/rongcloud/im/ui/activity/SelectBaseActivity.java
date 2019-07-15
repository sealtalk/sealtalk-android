package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import cn.rongcloud.im.R;

public class SelectBaseActivity extends TitleBaseActivity {

    //    getIntent().getStringArrayListExtra(LIST_CAN_NOT_CHECK_ID_LIST); //不可选的列表
//    getIntent().getStringArrayListExtra(LIST_ALREADY_CHECKED_FRIEND_ID_LIST);    //已经选择的列表
    private TextView tvSelectCount;
    private TextView tvConfirm;
    private View bottomLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_base_layout);
        tvSelectCount = findViewById(R.id.tv_search_count);
        tvConfirm = findViewById(R.id.tv_search_confirm);
        bottomLayout = findViewById(R.id.select_bottom_layout);
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmClick();
            }
        });

        tvSelectCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectedDetail();
            }
        });

    }

    /**
     * 覆盖获取选择结果
     *
     * @param selectIds
     */
    protected void onConfirmClicked(ArrayList<String> selectIds, ArrayList<String> selectGroups) {

    }

    /**
     * 右下角点击确定
     */
    protected void onConfirmClick() {

    }

    /**
     * 点击选择禅看选择详情
     */
    protected void onSelectedDetail() {

    }

    protected void showBottomSelectedCount(boolean isShow){
        if(isShow){
            String shownText = tvSelectCount.getText().toString();
            if(!TextUtils.isEmpty(shownText)){
                bottomLayout.setVisibility(View.VISIBLE);
            } else {
                bottomLayout.setVisibility(View.GONE);
            }
        } else {
            bottomLayout.setVisibility(View.GONE);
        }
    }

    protected void updateBottomCount(int groupCount, int userCount) {
        String userOnly = getString(R.string.seal_selected_contacts_count);
        String groupOnly = getString(R.string.seal_selected_only_group);
        String both = getString(R.string.seal_selected_groups_count);

        String countString = "";
        if (groupCount == 0 && userCount == 0) {
            bottomLayout.setVisibility(View.GONE);
            return;
        } else {
            bottomLayout.setVisibility(View.VISIBLE);
        }

        if (groupCount == 0 && userCount > 0) {
            countString = String.format(userOnly, userCount);
        } else if (groupCount > 0 && userCount == 0) {
            countString = String.format(groupOnly, groupCount);
        } else {
            countString = String.format(both, userCount, groupCount);
        }
        tvSelectCount.setText(countString);
    }
}
