package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import cn.rongcloud.im.ui.fragment.ForwardMultiFragment;
import cn.rongcloud.im.ui.fragment.ForwardSingleFragment;
import cn.rongcloud.im.ui.interfaces.OnForwardComfirmListener;
import cn.rongcloud.im.viewmodel.ForwardActivityViewModel;
import io.rong.imlib.model.Message;

import static cn.rongcloud.im.ui.view.SealTitleBar.Type.SEARCH;

/**
 * 此界面有三个 fragment ：
 * 上部搜索：
 * 还有单选界面
 * 多选界面
 */
public class ForwardActivity extends SelectBaseActivity {

    private static final String TAG = "ForwardActivity";
    private ForwardActivityViewModel forwardActivityViewModel;
    private ArrayList<Message> messageList;

    /**
     * Fragment 的类型。
     */
    private enum Type {
        SINGLE(0),
        MULTI(1);

        int value;
        Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    // fragment 集合
    private Fragment[] fragments = new Fragment[Type.values().length];

    /**
     * 选择页面的index 。 单选页面还是多选页面
     */
    private int selectPageIndex = Type.SINGLE.getValue();

    /**
     * 是否在转发成功后弹出提示
     */
    private boolean enableResultToast = true;

    /**
     * 使用融云自带的转发功能
     */
    private boolean enableSDKForward = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_forward);
        Intent intent = getIntent();
        messageList = intent.getParcelableArrayListExtra(IntentExtra.FORWARD_MESSAGE_LIST);
        enableResultToast = intent.getBooleanExtra(IntentExtra.BOOLEAN_ENABLE_TOAST, true);
        enableSDKForward = intent.getBooleanExtra(IntentExtra.BOOLEAN_FORWARD_USE_SDK, true);
        initView();
        initViewModel();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_select_forward_title);
        getTitleBar().addSeachTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });

        getTitleBar().setOnBtnRightClickListener(getString(R.string.seal_select_forward_contact_multi), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectPageIndex == Type.SINGLE.getValue()) {
                    changeSelectPage(Type.MULTI);
                } else {
                    changeSelectPage(Type.SINGLE);
                }

            }
        });

        getTitleBar().setOnBtnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!onBackKey()) {
                    finish();
                }
            }
        });

        //默认页面是多选
        showFragment(selectPageIndex);
    }

    @Override
    protected boolean isSearchable() {
        return true;
    }

    /**
     * 改变title 右侧按钮文本
     */
    private void updateTitleRightText(Type type) {
        if (type == Type.MULTI) {
            getTitleBar().setRightText(R.string.seal_select_forward_contact_single);
        } else {
            getTitleBar().setRightText(R.string.seal_select_forward_contact_multi);
        }
    }

    /**
     * 单选页面和多选页面的切换
     *
     * @param type
     */
    private void changeSelectPage(Type type) {
        if (selectPageIndex == type.getValue()) {
            return;
        }

        selectPageIndex = type.getValue();
        showFragment(type.getValue());
        // 改变title
        updateTitleRightText(type);
        /**
         * 执行此句是因为有时在搜索时， 则会切换单选和多选
         */
        search(getTitleBar().getEtSearch().getText().toString());

    }


    /**
     * 显示framgne
     *
     * @param index 下标, 可通过 Type 的getValue() 获取对应 fragment 的 index
     */
    private void showFragment(int index) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;
        for (int i = 0; i < fragments.length; i++) {
            fragment = fragments[i];
            if (index == i) {
                // 创建或者显示
                if (fragment == null) {
                    fragment = createFragment(i);
                    fragments[i] = fragment;
                    if (fragment != null) {
                        fragmentTransaction.add(R.id.fragment_container, fragment);
                        fragmentTransaction.show(fragment);
                    }
                } else {
                    fragmentTransaction.show(fragment);
                }
            } else {
                if (fragment != null) {
                    fragmentTransaction.hide(fragment);
                }
            }
        }
        fragmentTransaction.commit();
    }


    /**
     * 创建fragment
     *
     * @param index
     * @return
     */
    private Fragment createFragment(int index) {
        Fragment fragment = null;
        if (index == Type.SINGLE.getValue()) {
            ForwardSingleFragment singleFragment = new ForwardSingleFragment();
            singleFragment.setOnForwardComfirmListener(new OnForwardComfirmListener() {
                @Override
                public void onForward(List<GroupEntity> groups, List<FriendShipInfo> friendShipInfos) {
                    showForwardDialog(groups, friendShipInfos, messageList);
                }

                @Override
                public void onForwardNoDialog(List<GroupEntity> groups, List<FriendShipInfo> friendShipInfos) {
                    forwardMessage(groups, friendShipInfos, messageList);
                }
            });
            fragment = singleFragment;
        } else if (index == Type.MULTI.getValue()) {
            ForwardMultiFragment multiFragment = new ForwardMultiFragment();
            multiFragment.setOnForwardComfirmListener(new OnForwardComfirmListener() {
                @Override
                public void onForward(List<GroupEntity> groups, List<FriendShipInfo> friends) {
                    showForwardDialog(groups, friends, messageList);
                }

                @Override
                public void onForwardNoDialog(List<GroupEntity> groups, List<FriendShipInfo> friendShipInfos) {
                    forwardMessage(groups, friendShipInfos, messageList);
                }
            });
            fragment = multiFragment;
        }
        return fragment;
    }

    /**
     * 转发给多人的 dialog
     *
     * @param groups
     * @param friendShipInfos
     */
    private void showForwardDialog(List<GroupEntity> groups, List<FriendShipInfo> friendShipInfos, List<Message> messageList) {
        ForwardDialog.Builder builder = new ForwardDialog.Builder();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(IntentExtra.GROUP_LIST, groups == null ? null : (ArrayList<GroupEntity>) groups);
        bundle.putParcelableArrayList(IntentExtra.FRIEND_LIST, friendShipInfos == null ? null : (ArrayList<FriendShipInfo>) friendShipInfos);
        bundle.putParcelableArrayList(IntentExtra.FORWARD_MESSAGE_LIST, (ArrayList<Message>) messageList);
        if (enableSDKForward) {
            Intent intent = getIntent();
            if (intent != null) {
                bundle.putIntegerArrayList(IntentExtra.FORWARD_MESSAGE_ID_LIST, intent.getIntegerArrayListExtra(IntentExtra.FORWARD_MESSAGE_ID_LIST));
            }
        }
        builder.setExpandParams(bundle);
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                final ArrayList<GroupEntity> groupEntities = bundle.getParcelableArrayList(IntentExtra.GROUP_LIST);
                final ArrayList<FriendShipInfo> friendShipInfos = bundle.getParcelableArrayList(IntentExtra.FRIEND_LIST);
                final ArrayList<Message> messages = bundle.getParcelableArrayList(IntentExtra.FORWARD_MESSAGE_LIST);
                forwardMessage(groupEntities, friendShipInfos, messages);
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {
            }
        });
        final CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "forward_dialog");
    }


    /**
     * 搜索
     *
     * @param filterText
     */
    private void search(String filterText) {
        if (TextUtils.isEmpty(filterText)) {
            if (selectPageIndex == Type.SINGLE.getValue()) {
                ForwardSingleFragment fragment = (ForwardSingleFragment) fragments[Type.SINGLE.getValue()];
                if (fragment != null) {
                    fragment.clear();
                }
                changeSelectPage(Type.SINGLE);
            } else {
                ForwardMultiFragment fragment = (ForwardMultiFragment) fragments[Type.MULTI.getValue()];
                if (fragment != null) {
                    fragment.clear();
                }
                changeSelectPage(Type.MULTI);
            }

        } else {
            if (selectPageIndex == Type.SINGLE.getValue()) {
                showFragment(Type.SINGLE.getValue());
                ((ForwardSingleFragment) fragments[Type.SINGLE.getValue()]).search(filterText);
            } else {
                showFragment(Type.MULTI.getValue());
                ((ForwardMultiFragment) fragments[Type.MULTI.getValue()]).search(filterText);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
            if (onBackKey()) return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected boolean onBackKey() {
        if (selectPageIndex == Type.SINGLE.getValue()) {
            if (((ForwardSingleFragment) fragments[Type.SINGLE.getValue()]).onBackKey()) {
                if (!TextUtils.isEmpty(getTitleBar().getEtSearch().getText())) {
                    getTitleBar().getEtSearch().setText("");
                }
                return true;
            }
        } else if (selectPageIndex == Type.MULTI.getValue()) {
            if (((ForwardMultiFragment) fragments[Type.MULTI.getValue()]).onBackKey()) {
                if (!TextUtils.isEmpty(getTitleBar().getEtSearch().getText())) {
                    getTitleBar().getEtSearch().setText("");
                }
                return true;
            }
        }
        return false;
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
                    if (enableResultToast) {
                        showToast(R.string.seal_forward__message_success);
                    }
                    setResult(RESULT_OK);
                } else {
                    if (enableResultToast) {
                        if (resource.code == ErrorCode.NETWORK_ERROR.getCode()) {
                            showToast(resource.message);
                        } else {
                            showToast(R.string.seal_select_forward_message_defeat);
                        }
                    }
                    setResult(RESULT_FIRST_USER);
                }
                finish();
            }
        });
    }

    /**
     * 转发消息
     */
    private void forwardMessage(List<GroupEntity> groups, List<FriendShipInfo> friends, List<Message> messageList) {
        if (forwardActivityViewModel != null) {
            forwardActivityViewModel.ForwardMessage(ForwardActivity.this, groups, friends, messageList, enableSDKForward);
        }
    }

}
