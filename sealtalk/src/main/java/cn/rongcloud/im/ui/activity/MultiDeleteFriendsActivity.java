package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.DeleteFriendViewModel;

/**
 * 批量删除好友
 */
public class MultiDeleteFriendsActivity extends SelectMultiFriendsActivity {
    private DeleteFriendViewModel deleteFriendViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(getString(R.string.contact_multi_delete_friend));

        initViewModel();
    }

    private void initViewModel() {
        deleteFriendViewModel = ViewModelProviders.of(this).get(DeleteFriendViewModel.class);
        deleteFriendViewModel.getDeleteFriendsResult().observe(this, new Observer<Resource<Object>>() {
            @Override
            public void onChanged(Resource<Object> resource) {
                if (resource.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.common_delete_successful);
                    finish();
                } else if (resource.status == Status.ERROR) {
                    ToastUtils.showToast(resource.message);
                }
            }
        });
    }

    @Override
    protected void onConfirmClicked(ArrayList<String> selectIds, ArrayList<String> selectGroups) {
        CommonDialog confirmDialog = new CommonDialog.Builder()
                .setContentMessage(getString(R.string.contact_multi_delete_friend_conf))
                .setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClick(View v, Bundle bundle) {
                        deleteFriendViewModel.deleteFriends(selectIds);
                    }

                    @Override
                    public void onNegativeClick(View v, Bundle bundle) {
                    }
                })
                .build();
        confirmDialog.show(getSupportFragmentManager(), null);
    }
}
