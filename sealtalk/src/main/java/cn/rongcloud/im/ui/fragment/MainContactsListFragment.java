package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.ui.activity.GroupListActivity;
import cn.rongcloud.im.ui.activity.NewFriendListActivity;
import cn.rongcloud.im.ui.activity.PublicServiceActivity;
import cn.rongcloud.im.ui.activity.UserDetailActivity;
import cn.rongcloud.im.ui.adapter.CommonListAdapter;
import cn.rongcloud.im.ui.adapter.models.FunctionInfo;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.viewmodel.CommonListBaseViewModel;
import cn.rongcloud.im.viewmodel.MainContactsListViewModel;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

import static cn.rongcloud.im.common.IntentExtra.STR_TARGET_ID;

public class MainContactsListFragment extends CommonListBaseFragment {
    private static final String TAG = "MainContactsListFragment";

//    private ContactsAdapter adapter;
    private MainContactsListViewModel viewModel;

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        super.onInitView(savedInstanceState, intent);

        // Adapter 的点击监听
        setOnItemClickListener(new CommonListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position, ListItemModel data) {
                final ListItemModel.ItemView.Type type = data.getItemView().getType();
                switch (type) {
                    case FUN:
                        FunctionInfo functionInfo = (FunctionInfo)data.getData();
                        handleFunItemClick(functionInfo);
                        break;
                    case FRIEND:
                        FriendShipInfo friendShipInfo = (FriendShipInfo)data.getData();
                        handleFriendItemClick(friendShipInfo);
                        break;
                    default:
                        //Do nothing
                        break;
                }
            }
        });
    }


    @Override
    protected boolean isUseSideBar() {
        return true;
    }

    @Override
    protected CommonListBaseViewModel createViewModel() {
        viewModel = ViewModelProviders.of(MainContactsListFragment.this).get(MainContactsListViewModel.class);
        viewModel.getRefreshItem().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                getAdapter().notifyItemChanged(integer);
            }
        });
        return viewModel;
    }


    /**
     * 处理 好友 item 点击事件
     * @param friendShipInfo
     */
    private void handleFriendItemClick(FriendShipInfo friendShipInfo) {
        if (friendShipInfo.getUser().getId().equals(IMManager.getInstance().getCurrentId())) {
            String title = TextUtils.isEmpty(friendShipInfo.getDisplayName())? friendShipInfo.getUser().getNickname() : friendShipInfo.getDisplayName();
            RongIM.getInstance().startConversation(getActivity(), Conversation.ConversationType.PRIVATE, friendShipInfo.getUser().getId(),title);
        } else {
            Intent intent = new Intent(getContext(), UserDetailActivity.class);
            intent.putExtra(STR_TARGET_ID, friendShipInfo.getUser().getId());
            startActivity(intent);
        }
    }


    /**
     * 处理功能事件
     * @param functionInfo
     */
    private void handleFunItemClick(FunctionInfo functionInfo) {
        final String id = functionInfo.getId();
        switch (id) {
            case "1":
                //新的朋友
                viewModel.setFunRedDotShowStatus(id, false);
                Intent intent = new Intent(getActivity(), NewFriendListActivity.class);
                startActivity(intent);
                break;
            case "2":
                //群组
                intent = new Intent(getActivity(), GroupListActivity.class);
                startActivity(intent);
                break;
            case "3":
                //公众号
                intent = new Intent(getActivity(), PublicServiceActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
