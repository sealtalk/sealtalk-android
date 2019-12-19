package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.dialog.ForwardDialog;
import cn.rongcloud.im.ui.fragment.ForwardSelectedDetailFragment;
import cn.rongcloud.im.viewmodel.ForwardActivityViewModel;
import io.rong.imlib.model.Message;

public class ForwardSelectedDetailActivity extends TitleBaseActivity {

    private ArrayList<GroupEntity> seletedGroup;
    private ArrayList<FriendShipInfo>  selectedFriends;
    private ForwardActivityViewModel forwardActivityViewModel;
    private List<Message> messageList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forward_activity_selected_detail);
        messageList = getIntent().getParcelableArrayListExtra(IntentExtra.FORWARD_MESSAGE_LIST);
        seletedGroup = getIntent().getParcelableArrayListExtra(IntentExtra.GROUP_LIST);
        selectedFriends = getIntent().getParcelableArrayListExtra(IntentExtra.FRIEND_LIST);
        initView();
        initViewModel();
    }

    /**
     * 初始化viewmodel
     */
    private void initViewModel() {
        forwardActivityViewModel = ViewModelProviders.of(this).get(ForwardActivityViewModel.class);
        forwardActivityViewModel.getForwardSuccessLiveData().observe(this, new Observer<Resource>() {
            @Override
            public void onChanged(Resource resource) {
                if (resource.status == Status.SUCCESS) {
                    showToast(R.string.seal_forward__message_success);
                } else {
                    if (resource.code == ErrorCode.NETWORK_ERROR.getCode()) {
                        showToast(resource.message);
                    } else {
                        showToast(R.string.seal_select_forward_message_defeat);
                    }
                }
                if (resource.status == Status.SUCCESS) {
                    Intent intent = new Intent();
                    intent.putExtra(IntentExtra.FORWARD_FINISH, true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    /**
     * 转发消息
     */
    private void forwardMessage(List<GroupEntity> groups, List<FriendShipInfo> friends, List<Message> messages) {
        if (forwardActivityViewModel != null) {
            forwardActivityViewModel.ForwardMessage(ForwardSelectedDetailActivity.this,groups, friends, messages);
        }
    }

    /**
     * 初始化 View
     */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_forward_selected_detail_title);
        getTitleBar().setOnBtnRightClickListener(getString(R.string.seal_forward_selected_detail_comfirm), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((seletedGroup == null || seletedGroup.size() <=0) && (selectedFriends == null || selectedFriends.size() <= 0) ) {
                    return;
                }
                showForwardDialog(seletedGroup, selectedFriends, messageList);
            }
        });

        getTitleBar().setOnBtnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(IntentExtra.GROUP_LIST, seletedGroup);
                intent.putParcelableArrayListExtra(IntentExtra.FRIEND_LIST, selectedFriends);
                intent.putExtra(IntentExtra.FORWARD_FINISH, false);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        updateTitle(selectedFriends, seletedGroup);

        final ForwardSelectedDetailFragment forwardSelectedDetailFragment = new ForwardSelectedDetailFragment();

        forwardSelectedDetailFragment.setOnLeftSelectedListener(new ForwardSelectedDetailFragment.OnLeftSelectedListener() {
            @Override
            public void onLeftSelected(ArrayList<FriendShipInfo> friendShipInfos, ArrayList<GroupEntity> groupEntities) {
                selectedFriends = friendShipInfos;
                seletedGroup = groupEntities;
                updateTitle(selectedFriends, seletedGroup);
            }
        });
        getSupportFragmentManager().beginTransaction().add(R.id.ll_selected_detail_container, forwardSelectedDetailFragment)
                .show(forwardSelectedDetailFragment).commit();
    }

    /**
     * 初始化数据
     */
    private void updateTitle(List<FriendShipInfo> selectedFriends, List<GroupEntity> seletedGroup) {
        if ((seletedGroup == null || seletedGroup.size() <= 0) && (selectedFriends == null || selectedFriends.size() <= 0)) {
            getTitleBar().getTvRight().setTextColor(getResources().getColor(R.color.text_gray));
            getTitleBar().getTvRight().setClickable(false);
        }
    }

    /**
     * 转发给多人的 dialog
     * @param groups
     * @param friendShipInfos
     */
    private void showForwardDialog(List<GroupEntity> groups, List<FriendShipInfo> friendShipInfos, List<Message> messageList) {
        ForwardDialog.Builder builder = new ForwardDialog.Builder();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(IntentExtra.GROUP_LIST, groups == null? null : (ArrayList<GroupEntity>) groups);
        bundle.putParcelableArrayList(IntentExtra.FRIEND_LIST, friendShipInfos == null? null : (ArrayList<FriendShipInfo>) friendShipInfos);
        bundle.putParcelableArrayList(IntentExtra.FORWARD_MESSAGE_LIST, (ArrayList<Message>)messageList);
        builder.setExpandParams(bundle);
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(IntentExtra.GROUP_LIST, seletedGroup);
                intent.putParcelableArrayListExtra(IntentExtra.FRIEND_LIST, selectedFriends);
                intent.putExtra(IntentExtra.CONFIRM_SEND,true);
                setResult(RESULT_OK,intent);
                finish();
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {
            }
        });
        final CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "forward_dialog");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(IntentExtra.GROUP_LIST, seletedGroup);
            intent.putParcelableArrayListExtra(IntentExtra.FRIEND_LIST, selectedFriends);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
