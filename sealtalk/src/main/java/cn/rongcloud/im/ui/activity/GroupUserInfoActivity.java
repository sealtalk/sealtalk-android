package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.GroupMemberInfoDes;
import cn.rongcloud.im.model.CountryInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.adapter.GroupUserInfoDesAdapter;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.GroupUserInfoViewModel;

/**
 * 群组个人信息
 */
public class GroupUserInfoActivity extends TitleBaseActivity {

    private static final int REQUEST_CODE_SELECT_COUNTRY = 1040;
    private RecyclerView rvDes;
    private GroupUserInfoDesAdapter mAdapter;
    private TextView tvRegion;
    private TextView tvNameTitle;
    private EditText etNickName;
    private EditText etPhone;
    private EditText etVchat;
    private EditText etAliPay;
    private GroupUserInfoViewModel groupUserInfoViewModel;
    private String groupId;
    private String memberId;
    private int mType;
    public final static int FROM_USER_DETAIL = 0x786;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);
        groupId = getIntent().getStringExtra(IntentExtra.GROUP_ID);
        memberId = getIntent().getStringExtra(IntentExtra.STR_TARGET_ID);
        mType = getIntent().getIntExtra(IntentExtra.START_FROM_ID, 0);
        initView();
        initViewModel();
    }

    private void initView() {
        getTitleBar().setTitle(getString(R.string.seal_group_user_info_title));
        if (mType != FROM_USER_DETAIL) {
            getTitleBar().setOnBtnRightClickListener(getString(R.string.seal_group_user_info_des_confirm)
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setMemberInfoDes();
                        }
                    });
        }
        etNickName = findViewById(R.id.et_nick_name);
        etPhone = findViewById(R.id.et_phone);
        etVchat = findViewById(R.id.et_vchat);
        etAliPay = findViewById(R.id.et_alipay);
        tvNameTitle = findViewById(R.id.tv_name_title);
        rvDes = findViewById(R.id.rv_des);
        tvRegion = findViewById(R.id.tv_region);
        tvRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(GroupUserInfoActivity.this, SelectCountryActivity.class), REQUEST_CODE_SELECT_COUNTRY);
            }
        });
        if (mType == FROM_USER_DETAIL) {
            //来自用户详情页，只展示，不可编辑
            mAdapter = new GroupUserInfoDesAdapter(this, FROM_USER_DETAIL);
            tvNameTitle.setText(R.string.seal_group_user_info_name);
            etNickName.setText(R.string.seal_group_user_info_des_no_set, TextView.BufferType.EDITABLE);
            etPhone.setText(R.string.seal_group_user_info_des_no_set, TextView.BufferType.EDITABLE);
            etVchat.setText(R.string.seal_group_user_info_des_no_set, TextView.BufferType.EDITABLE);
            etAliPay.setText(R.string.seal_group_user_info_des_no_set, TextView.BufferType.EDITABLE);
            etNickName.setEnabled(false);
            etPhone.setEnabled(false);
            etVchat.setEnabled(false);
            etAliPay.setEnabled(false);
            tvRegion.setClickable(false);
        } else {
            mAdapter = new GroupUserInfoDesAdapter(this);
        }
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        rvDes.setLayoutManager(mLayoutManager);
        rvDes.setItemAnimator(null);
        rvDes.setAdapter(mAdapter);


    }

    private void initViewModel() {
        groupUserInfoViewModel = ViewModelProviders.of(this).get(GroupUserInfoViewModel.class);
        groupUserInfoViewModel.requestMemberInfoDes(groupId, memberId);
        groupUserInfoViewModel.getGroupMemberInfoDes().observe(this, new Observer<Resource<GroupMemberInfoDes>>() {
            @Override
            public void onChanged(Resource<GroupMemberInfoDes> groupMemberInfoDesResource) {
                if (groupMemberInfoDesResource.status != Status.LOADING && groupMemberInfoDesResource.data != null) {
                    updateView(groupMemberInfoDesResource.data);
                }
            }
        });
        groupUserInfoViewModel.setMemberInfoDesResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                if (voidResource.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.seal_group_user_info_des_confirm_success);
                    finish();
                } else if (voidResource.status == Status.ERROR) {
                    if (!TextUtils.isEmpty(voidResource.message)) {
                        ToastUtils.showToast(voidResource.message);
                    }
                }
            }
        });
    }

    private void updateView(GroupMemberInfoDes data) {
        if (!TextUtils.isEmpty(data.getGroupNickname())) {
            etNickName.setText(data.getGroupNickname(), TextView.BufferType.EDITABLE);
        }
        if (!TextUtils.isEmpty(data.getRegion())) {
            tvRegion.setText("+" + data.getRegion());
        }
        if (!TextUtils.isEmpty(data.getPhone())) {
            etPhone.setText(data.getPhone());
        }
        if (!TextUtils.isEmpty(data.getWeChat())) {
            etVchat.setText(data.getWeChat());
        }
        if (!TextUtils.isEmpty(data.getAlipay())) {
            etAliPay.setText(data.getAlipay());
        }
        if (data.getMemberDesc() != null && data.getMemberDesc().size() > 0) {
            mAdapter.setData(data.getMemberDesc());
        }
    }

    private void setMemberInfoDes() {
        //国家码需去掉 '+' 号
        groupUserInfoViewModel.setMemberInfoDes(groupId, memberId, etNickName.getText().toString()
                , tvRegion.getText().toString().replace("+", ""), etPhone.getText().toString(), etVchat.getText().toString()
                , etAliPay.getText().toString(), mAdapter.getData());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_COUNTRY) {
            CountryInfo info = data.getParcelableExtra(SelectCountryActivity.RESULT_PARAMS_COUNTRY_INFO);
            tvRegion.setText(info.getZipCode());
        }
    }
}
