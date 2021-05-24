package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.AddFriendResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.SearchFriendInfo;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.dialog.SimpleInputDialog;
import cn.rongcloud.im.ui.fragment.SearchFriendNetFragment;
import cn.rongcloud.im.ui.fragment.SearchFriendResultFragment;
import cn.rongcloud.im.ui.interfaces.OnSearchFriendClickListener;
import cn.rongcloud.im.ui.interfaces.OnSearchFriendItemClickListener;
import cn.rongcloud.im.viewmodel.SearchFriendNetViewModel;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

public class SearchFriendActivity extends TitleBaseActivity implements OnSearchFriendClickListener,
        OnSearchFriendItemClickListener {
    private SearchFriendNetFragment searchFriendFragment;
    private SearchFriendResultFragment searchFriendResultFragment;
    private SearchFriendNetViewModel viewModel;
    private boolean isFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle((R.string.seal_main_title_add_friends));
        getTitleBar().setOnBtnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setContentView(R.layout.activity_friend_search_content);
        searchFriendFragment = new SearchFriendNetFragment();
        searchFriendFragment.setOnSearchFriendClickListener(this);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_fragment_container, searchFriendFragment).commit();
        viewModel = ViewModelProviders.of(this).get(SearchFriendNetViewModel.class);
        viewModel.getSearchFriend().observe(this, new Observer<Resource<SearchFriendInfo>>() {
            @Override
            public void onChanged(Resource<SearchFriendInfo> searchFriendInfoResource) {
                if (searchFriendInfoResource.status == Status.SUCCESS && searchFriendInfoResource.data != null) {
                    SearchFriendInfo friendInfo = searchFriendInfoResource.data;
                    searchFriendResultFragment = new SearchFriendResultFragment();
                    searchFriendResultFragment.setData(SearchFriendActivity.this, searchFriendInfoResource.data);
                    pushFragment(searchFriendResultFragment);
                    viewModel.isFriend(friendInfo.getId());
                } else if (searchFriendInfoResource.status == Status.ERROR
                        || searchFriendInfoResource.data == null) {
                    Toast.makeText(SearchFriendActivity.this, R.string.seal_account_not_exist, Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getIsFriend().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                isFriend = aBoolean;
            }
        });
        viewModel.getAddFriend().observe(this, new Observer<Resource<AddFriendResult>>() {
            @Override
            public void onChanged(Resource<AddFriendResult> addFriendResultResource) {
                if (addFriendResultResource.status == Status.SUCCESS) {
                    Toast.makeText(SearchFriendActivity.this, R.string.common_request_success, Toast.LENGTH_SHORT).show();
                } else if (addFriendResultResource.status == Status.ERROR) {
                    Toast.makeText(SearchFriendActivity.this,
                            String.format(getString(R.string.seal_quest_failed_error_code), addFriendResultResource.code),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void pushFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fl_fragment_container, searchFriendResultFragment);
        transaction.addToBackStack(fragment.getClass().getSimpleName());
        transaction.commit();
    }

    @Override
    public void onSearchClick(String region, String searchContent) {
        if (TextUtils.isDigitsOnly(searchContent)) {
            viewModel.searchFriendFromServer(null, region, searchContent);
        } else {
            viewModel.searchFriendFromServer(searchContent, null, null);
        }
    }

    @Override
    public void onSearchFriendItemClick(SearchFriendInfo searchFriendInfo) {
        if (isFriend || searchFriendInfo.getId().equals(RongIMClient.getInstance().getCurrentUserId())) {
            toDetailActivity(searchFriendInfo.getId());
        } else {
            showAddFriendDialog(searchFriendInfo.getId());
        }
    }

    private void toDetailActivity(String userId) {
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra(IntentExtra.STR_TARGET_ID, userId);
        startActivity(intent);
    }

    private void showAddFriendDialog(String userId) {
        final EditText et = new EditText(this);
        SimpleInputDialog dialog = new SimpleInputDialog();
        dialog.setInputHint(getString(R.string.profile_add_friend_hint));
        dialog.setInputDialogListener(new SimpleInputDialog.InputDialogListener() {
            @Override
            public boolean onConfirmClicked(EditText input) {
                String inviteMsg = input.getText().toString();
                UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(IMManager.getInstance().getCurrentId());
                // 如果邀请信息为空则使用默认邀请语
                if (TextUtils.isEmpty(inviteMsg) && userInfo != null) {
                    // 当有附带群组名时显示来自哪个群组，没有时仅带自己的昵称
                    inviteMsg = getString(R.string.profile_invite_friend_description_format, userInfo.getName());
                }
                viewModel.inviteFriend(userId, inviteMsg);
                return true;
            }
        });
        dialog.show(getSupportFragmentManager(), null);

    }


}
