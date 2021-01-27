package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.ui.activity.CreateGroupActivity;
import cn.rongcloud.im.ui.activity.SelectForwardCreateChatActivity;
import cn.rongcloud.im.ui.adapter.CommonListAdapter;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.ui.interfaces.OnContactItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnForwardComfirmListener;
import cn.rongcloud.im.ui.interfaces.OnGroupItemClickListener;
import cn.rongcloud.im.ui.interfaces.SearchableInterface;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.ForwardTransferDialogViewModel;

import static android.app.Activity.RESULT_OK;

/**
 * 此页面是转发的单选页面。 具有单选并转发的功能
 */
public class ForwardSingleFragment extends BaseFragment implements SearchableInterface {


    private OnForwardComfirmListener listener;

    /**
     * Fragment 的类型。
     */
    private enum Type {
        RECENT_LIST(0),
        SEARCH(1),
        GROUPS(2);

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

    private static final String TAG = "ForwardSingleFragment";
    private static final int REQUEST_CREATE_CHAT = 100;
    private static final int REQUEST_CREATE_GROUP = 101;
    private ForwardTransferDialogViewModel viewModel;
    private int currentFragmentIndex = Type.RECENT_LIST.getValue();


    @Override
    protected int getLayoutResId() {
        return R.layout.forward_fragment_single;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        showFragment(Type.RECENT_LIST.getValue());
    }

    /**
     * 显示framgne
     * @param index 下标, 可通过 Type 的getValue() 获取对应 fragment 的 index
     */
    private void showFragment(int index) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = null;
        for (int i = 0; i < fragments.length; i++) {
            fragment = fragments[i];
            if (index == i) {
                // 创建或者显示
                if (fragment == null) {
                    fragment = createFragment(i);
                    fragments[i] = fragment;
                    if (fragment != null) {
                        fragmentTransaction.add(R.id.fl_signle_fragment_container, fragment);
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
        currentFragmentIndex = index;
    }


    /**
     * 创建fragment
     * @param index
     * @return
     */
    private Fragment createFragment(int index) {
        Fragment fragment = null;
        if (index == Type.RECENT_LIST.getValue()) {
            fragment  = createRecentListFragment();
        } else if (index == Type.SEARCH.getValue()) {
            fragment = createSearchFragment();
        } else {
            fragment = createGroupsFragment();
        }
        return fragment;
    }

    /**
     * 创建群组
     * @return
     */
    private Fragment createGroupsFragment() {
        ForwardGroupListFragment forwardGroupListFragment = new ForwardGroupListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IntentExtra.IS_SELECT, false);
        forwardGroupListFragment.setArguments(bundle);
        forwardGroupListFragment.setOnItemClickListener(new CommonListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position, ListItemModel data) {
                final ListItemModel.ItemView.Type type = data.getItemView().getType();
                switch (type) {
                    case GROUP:
                        final GroupEntity groupEntity = (GroupEntity) data.getData();
                        viewModel.showTransferToGroupDialog(groupEntity);
                        break;

                    default:
                        //DO Nothing
                        break;
                }
            }
        });

        return forwardGroupListFragment;
    }


    /**
     * 创建搜索查询页面
     * @return
     */
    private Fragment createSearchFragment() {
        ForwardSearchFragment fragment = new ForwardSearchFragment();
        fragment.setOnContactItemClickListener(new OnContactItemClickListener() {
            @Override
            public void onItemContactClick(FriendShipInfo friendShipInfo) {
                viewModel.showTransferToFriendDialog(friendShipInfo);
            }
        });

        fragment.setOnGroupItemClickListener(new OnGroupItemClickListener() {
            @Override
            public void onGroupClicked(GroupEntity groupEntity) {
                viewModel.showTransferToGroupDialog(groupEntity);
            }
        });
        return fragment;
    }

    /**
     * 创建最近联系列表
     * @return
     */
    private Fragment createRecentListFragment() {
        ForwordRecentListFragment recentListFragment = new ForwordRecentListFragment();
        recentListFragment.setOnItemClickListener(new CommonListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position, ListItemModel data) {
                final ListItemModel.ItemView.Type type = data.getItemView().getType();
                switch (type) {
                    case FUN:
                        switch (data.getId()) {
                            case "1": //创建一个聊天
                                Intent intent = new Intent(getContext(), SelectForwardCreateChatActivity.class);
                                startActivityForResult(intent, REQUEST_CREATE_CHAT);
                                break;
                            case "2": //选择一个群
                                showFragment(Type.GROUPS.getValue());
                                break;
                            default:
                                break;
                        }
                        break;
                    case GROUP:
                        final GroupEntity groupEntity = (GroupEntity) data.getData();
                        viewModel.showTransferToGroupDialog(groupEntity);
                        break;
                    case FRIEND:
                        final FriendShipInfo friendShipInfo = (FriendShipInfo) data.getData();
                        viewModel.showTransferToFriendDialog(friendShipInfo);
                        break;
                    default:
                        //DO Nothing
                        break;
                }
            }
        });

        return recentListFragment;
    }

    @Override
    protected void onInitViewModel() {
        viewModel = ViewModelProviders.of(this).get(ForwardTransferDialogViewModel.class);

        /**
         * 显示转发给朋友的dialig 的监听
         */
        viewModel.getShowTransferToFriendDialog().observe(this, new Observer<FriendShipInfo>() {
            @Override
            public void onChanged(FriendShipInfo friendShipInfo) {
                if (listener != null && friendShipInfo != null) {
                    List<FriendShipInfo> friendShipInfos = new ArrayList<>();
                    friendShipInfos.add(friendShipInfo);
                    listener.onForward(null, friendShipInfos);
                }
            }
        });

        /**
         * 显示转发给群组的dialig 的监听
         */
        viewModel.getShowTransferToGroupDialog().observe(this, new Observer<GroupEntity>() {
            @Override
            public void onChanged(GroupEntity groupEntity) {
                SLog.d("ss_group", "group==" + groupEntity);
                if (listener != null && groupEntity != null) {
                    List<GroupEntity> groups = new ArrayList<>();
                    groups.add(groupEntity);
                    listener.onForward(groups, null);
                }
            }
        });
    }

    @Override
    public void search(String match) {
        if (currentFragmentIndex != Type.SEARCH.getValue()) {
            showFragment(Type.SEARCH.getValue());
        }
        showFragment(Type.SEARCH.getValue());
        ((ForwardSearchFragment)fragments[Type.SEARCH.getValue()]).search(match);
    }

    @Override
    public void clear() {

        // 如果不是搜索界面，则不需要执行
        // 因为当activity 接收到 back 按键时, fragment 的 backKey 方法返回.
        // 由于搜索输入是在 activity 中, 所以需要清理搜索栏上的文字.由于文字变化， 可能造成
        // 此方法的无用调用， 所以再次做一下拦截。
        if (currentFragmentIndex != Type.SEARCH.getValue()) {
            return;
        }

        ForwardSearchFragment fragment = (ForwardSearchFragment)fragments[Type.SEARCH.getValue()];
        if (fragment != null) {
            ((ForwardSearchFragment)fragments[Type.SEARCH.getValue()]).clear();
            showFragment(Type.RECENT_LIST.getValue());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CREATE_CHAT:
                    //选择列表返回
                    ArrayList<String> selectList = data.getStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST);
                    if (selectList == null || selectList.size()  <=  0) {
                        return;
                    }
                    if (selectList.size() == 1) {
                        viewModel.showTransferToFriendDialog(selectList.get(0));
                    } else {
                        Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                        intent.putExtra(IntentExtra.BOOLEAN_CREATE_GROUP_RETURN_RESULT, true);
                        intent.putStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST, selectList);
                        startActivityForResult(intent, REQUEST_CREATE_GROUP);
                    }

                    break;
                case REQUEST_CREATE_GROUP:
                    //创建群组返回
                    String groupId = data.getStringExtra(IntentExtra.GROUP_ID);
                    viewModel.showTransferToGroupDialog(groupId);

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 返回按钮
     * @return
     */
    public boolean onBackKey() {
        if (currentFragmentIndex == Type.SEARCH.getValue()) {
            clear();
            return true;
        } else  if (currentFragmentIndex == Type.GROUPS.getValue()) {
            showFragment(Type.RECENT_LIST.getValue());
            return true;
        }
        return false;
    }


    /**
     * 点击转发监听
     * @param listener
     */
    public void setOnForwardComfirmListener(OnForwardComfirmListener listener) {
        this.listener = listener;
    }

}
