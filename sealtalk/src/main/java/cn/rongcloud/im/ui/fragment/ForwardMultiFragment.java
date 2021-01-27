package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.ui.activity.ForwardSelectedDetailActivity;
import cn.rongcloud.im.ui.adapter.CommonListAdapter;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.ui.interfaces.OnContactItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnForwardComfirmListener;
import cn.rongcloud.im.ui.interfaces.OnGroupItemClickListener;
import cn.rongcloud.im.ui.interfaces.SearchableInterface;

import static android.app.Activity.RESULT_OK;

/**
 * 转发 "多选" 页面
 * 包含两个 Fragment,一层不变
 * 一层 forwardCheckRecendFragment: 最近聊天
 * 二层 forwardCheckSearchFragment: 搜索
 */
public class ForwardMultiFragment extends BaseFragment implements SearchableInterface {

    private static final int REQUEST_SELECT_DETAIL = 1000;
    private ArrayList<Parcelable> messageList;

    /**
     * Fragment 的类型。
     */
    private enum Type {
        RECENT_LIST(0),
        SEARCH(1),
        CONTACTS(2),
        GROUPS(3);

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

    private TextView selectedCountTv;
    private TextView selectedConfirmTv;
    private ArrayList<FriendShipInfo> selectedFriends = new ArrayList<>();
    private ArrayList<GroupEntity> selectedGroups = new ArrayList<>();

    private int currentFragmentIndex = Type.RECENT_LIST.getValue();
    private int preFragmentIndex = currentFragmentIndex;

    private OnForwardComfirmListener listener;

    @Override
    protected int getLayoutResId() {
        return R.layout.forward_fragment_mutils;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        messageList = getActivity().getIntent().getParcelableArrayListExtra(IntentExtra.FORWARD_MESSAGE_LIST);
        showFragment(Type.RECENT_LIST.getValue());
        // 初始化底部布局
        initBottomView();
    }

    /**
     * 初始化底部布局
     */
    private void initBottomView() {
        selectedCountTv = findView(R.id.tv_search_count);
        selectedConfirmTv = findView(R.id.tv_search_confirm);

        selectedConfirmTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickConfirm();
            }
        });

        selectedCountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickContent();
            }
        });

        updateBottomCount(0, 0);
    }


    /**
     * 显示framgne
     *
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
                        fragmentTransaction.add(R.id.fl_mutil_fragment_container, fragment);
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
        preFragmentIndex = currentFragmentIndex;
        currentFragmentIndex = index;
    }


    /**
     * 创建fragment
     *
     * @param index
     * @return
     */
    private Fragment createFragment(int index) {
        Fragment fragment = null;
        if (index == Type.RECENT_LIST.getValue()) {
            fragment = createRecentListFragment();
        } else if (index == Type.SEARCH.getValue()) {
            fragment = createMutilSearchFragment();
        } else if (index == Type.CONTACTS.getValue()) {
            fragment = createContactsFragment();
        } else if (index == Type.GROUPS.getValue()) {
            fragment = createCroupsFragment();
        }
        return fragment;
    }

    /**
     * 群组fragment
     *
     * @return
     */
    private Fragment createCroupsFragment() {
        ForwardGroupListFragment forwardGroupListFragment = new ForwardGroupListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IntentExtra.IS_SELECT, true);
        forwardGroupListFragment.setArguments(bundle);
        forwardGroupListFragment.setOnItemClickListener(new CommonListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position, ListItemModel data) {
                final ListItemModel.ItemView.Type type = data.getItemView().getType();
                switch (type) {
                    case GROUP:
                        final GroupEntity groupEntity = (GroupEntity) data.getData();
                        handleGroupClicked(groupEntity);
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
     * 选择联系人
     *
     * @return
     */
    private Fragment createContactsFragment() {
        ForwardSelectContactFragment selectContactFragment = new ForwardSelectContactFragment();
        selectContactFragment.setOnItemClickListener(new CommonListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position, ListItemModel data) {
                final ListItemModel.ItemView.Type type = data.getItemView().getType();
                switch (type) {
                    case FUN:
                        showFragment(Type.GROUPS.getValue());
                        ((ForwardGroupListFragment) fragments[Type.GROUPS.getValue()]).setSelectedIds(getSelectGourpIds(), getSelectFriendIds());
                        break;
                    case FRIEND:
                        final FriendShipInfo friendShipInfo = (FriendShipInfo) data.getData();
                        handleFriendClicked(friendShipInfo);
                        break;
                    default:
                        //DO Nothing
                        break;
                }
            }
        });

        return selectContactFragment;
    }

    /**
     * 多选搜索页面
     *
     * @return
     */
    private Fragment createMutilSearchFragment() {
        ForwardSearchFragment fragment = new ForwardSearchFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IntentExtra.IS_SELECT, true);
        fragment.setArguments(bundle);
        fragment.setOnGroupItemClickListener(new OnGroupItemClickListener() {
            @Override
            public void onGroupClicked(GroupEntity groupEntity) {
                handleGroupClicked(groupEntity);
            }
        });

        fragment.setOnContactItemClickListener(new OnContactItemClickListener() {
            @Override
            public void onItemContactClick(FriendShipInfo friendShipInfo) {
                handleFriendClicked(friendShipInfo);
            }
        });
        return fragment;
    }

    /**
     * 创建最近联系人的列表
     *
     * @return
     */
    private Fragment createRecentListFragment() {
        ForwordRecentMultiSelectListFragment recentListFragment = new ForwordRecentMultiSelectListFragment();

        recentListFragment.setOnItemClickListener(new CommonListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position, ListItemModel data) {
                final ListItemModel.ItemView.Type type = data.getItemView().getType();
                switch (type) {
                    case FUN:
                        showFragment(Type.CONTACTS.getValue());
                        ((ForwardSelectContactFragment) fragments[Type.CONTACTS.getValue()]).setSelectedIds(getSelectGourpIds(), getSelectFriendIds());
                        break;
                    case GROUP:
                        final GroupEntity groupEntity = (GroupEntity) data.getData();
                        handleGroupClicked(groupEntity);
                        break;
                    case FRIEND:
                        final FriendShipInfo friendShipInfo = (FriendShipInfo) data.getData();
                        handleFriendClicked(friendShipInfo);
                        break;
                    default:
                        //DO Nothing
                        break;
                }
            }
        });
        return recentListFragment;
    }


    /**
     * 修改选择数字文案
     *
     * @param groupCount
     * @param userCount
     */
    private void updateBottomCount(int groupCount, int userCount) {
        String userOnly = getString(R.string.seal_selected_contacts_count);
        String groupOnly = getString(R.string.seal_selected_only_group);
        String both = getString(R.string.seal_selected_groups_count);

        String countString = "";
        int colorResId = -1;

        if (groupCount == 0 && userCount == 0) {
            colorResId = R.color.text_gray;
            countString = String.format(userOnly, userCount);
            selectedConfirmTv.setClickable(false);
        } else {
            selectedConfirmTv.setClickable(true);
            colorResId = R.color.text_blue;
            if (groupCount == 0 && userCount > 0) {
                countString = String.format(userOnly, userCount);
            } else if (groupCount > 0 && userCount == 0) {
                countString = String.format(groupOnly, groupCount);
            } else {
                countString = String.format(both, userCount, groupCount);
            }
        }

        selectedCountTv.setText(countString);
        selectedCountTv.setTextColor(getResources().getColor(colorResId));
        selectedConfirmTv.setTextColor(getResources().getColor(colorResId));
    }

    /**
     * 底部布局点击按钮事件
     */
    private void onClickConfirm() {
        if (listener != null) {
            listener.onForward(selectedGroups, selectedFriends);
        }
    }

    /**
     * 底部布局点击选择人详情查看的事件
     */
    private void onClickContent() {
        // 跳转查看选择人的详情部分
        Intent intent = new Intent(getActivity(), ForwardSelectedDetailActivity.class);
        intent.putParcelableArrayListExtra(IntentExtra.GROUP_LIST, selectedGroups);
        intent.putParcelableArrayListExtra(IntentExtra.FRIEND_LIST, selectedFriends);
        intent.putParcelableArrayListExtra(IntentExtra.FORWARD_MESSAGE_LIST, messageList);
        startActivityForResult(intent, REQUEST_SELECT_DETAIL);
    }

    @Override
    public void search(String match) {
        if (currentFragmentIndex != Type.SEARCH.getValue()) {
            showFragment(Type.SEARCH.getValue());
        }
        ((ForwardSearchFragment) fragments[Type.SEARCH.getValue()]).setSelectedIds(getSelectGourpIds(), getSelectFriendIds());
        ((ForwardSearchFragment) fragments[Type.SEARCH.getValue()]).search(match);
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

        ForwardSearchFragment fragment = (ForwardSearchFragment) fragments[Type.SEARCH.getValue()];
        if (fragment != null) {
            fragment.clear();
            showFragment(preFragmentIndex);
            // 因为上面已经进行切换了
            if (currentFragmentIndex == Type.CONTACTS.getValue()) {
                ((ForwardSelectContactFragment) fragments[Type.CONTACTS.getValue()]).setSelectedIds(getSelectGourpIds(), getSelectFriendIds());
            } else if (currentFragmentIndex == Type.RECENT_LIST.getValue()) {
                ((ForwordRecentListFragment) fragments[Type.RECENT_LIST.getValue()]).setSelectedIds(getSelectGourpIds(), getSelectFriendIds());
            } else if (currentFragmentIndex == Type.GROUPS.getValue()) {
                ((ForwardSelectContactFragment) fragments[Type.CONTACTS.getValue()]).setSelectedIds(getSelectGourpIds(), getSelectFriendIds());
            }
        }
    }


    /**
     * 在选中的id 同比到选择界面
     *
     * @return
     */
    private List<String> getSelectGourpIds() {
        List<String> ids = new ArrayList<>();
        for (GroupEntity entity : selectedGroups) {
            ids.add(entity.getId());
        }
        return ids;
    }

    /**
     * 在选中的id 同比到选择界面
     *
     * @return
     */
    private List<String> getSelectFriendIds() {
        List<String> ids = new ArrayList<>();
        for (FriendShipInfo friend : selectedFriends) {
            ids.add(friend.getUser().getId());
        }
        return ids;
    }


    /**
     * 点击了好友事件处理
     *
     * @param friendShipInfo
     */
    private void handleFriendClicked(FriendShipInfo friendShipInfo) {
        if (!isContainsFriend(friendShipInfo)) {
            selectedFriends.add(friendShipInfo);
        }
        updateBottomCount(selectedGroups.size(), selectedFriends.size());
    }

    /**
     * 判断是否已经含有此好友， 如果有则删除， 并返回true； 没有返回 false
     *
     * @param friendShipInfo
     * @return
     */
    private boolean isContainsFriend(FriendShipInfo friendShipInfo) {
        for (FriendShipInfo info : selectedFriends) {
            if (friendShipInfo.getUser().getId().equals(info.getUser().getId())) {
                selectedFriends.remove(info);
                return true;
            }
        }
        return false;
    }


    /**
     * 点击了区组事件处理
     *
     * @param groupEntity
     */
    private void handleGroupClicked(GroupEntity groupEntity) {
        if (!isContainsGroup(groupEntity)) {
            selectedGroups.add(groupEntity);
        }
        updateBottomCount(selectedGroups.size(), selectedFriends.size());
    }


    /**
     * 判断是否已经含有此群， 如果有则删除， 并返回true； 没有返回 false
     *
     * @param groupEntity
     * @return
     */
    private boolean isContainsGroup(GroupEntity groupEntity) {
        for (GroupEntity group : selectedGroups) {
            if (groupEntity.getId().equals(group.getId())) {
                selectedGroups.remove(group);
                return true;
            }
        }
        return false;
    }

    /**
     * 返回按钮
     *
     * @return
     */
    public boolean onBackKey() {
        if (currentFragmentIndex == Type.SEARCH.getValue()) {
            clear();
            return true;
        } else if (currentFragmentIndex == Type.CONTACTS.getValue()) {
            showFragment(Type.RECENT_LIST.getValue());
            ((ForwordRecentMultiSelectListFragment) fragments[Type.RECENT_LIST.getValue()]).setSelectedIds(getSelectGourpIds(), getSelectFriendIds());
            return true;
        } else if (currentFragmentIndex == Type.GROUPS.getValue()) {
            showFragment(Type.CONTACTS.getValue());
            ((ForwardSelectContactFragment) fragments[Type.CONTACTS.getValue()]).setSelectedIds(getSelectGourpIds(), getSelectFriendIds());
            return true;
        }
        return false;
    }


    /**
     * 点击转发监听
     *
     * @param listener
     */
    public void setOnForwardComfirmListener(OnForwardComfirmListener listener) {
        this.listener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT_DETAIL) {
            boolean finish = data.getBooleanExtra(IntentExtra.FORWARD_FINISH, false);
            boolean confirm = data.getBooleanExtra(IntentExtra.CONFIRM_SEND, false);
            if (finish) {
                getActivity().finish();
            } else {
                selectedGroups = data.getParcelableArrayListExtra(IntentExtra.GROUP_LIST);
                selectedFriends = data.getParcelableArrayListExtra(IntentExtra.FRIEND_LIST);
                updateBottomCount(selectedGroups == null ? 0 : selectedGroups.size(), selectedFriends == null ? 0 : selectedFriends.size());
                if (currentFragmentIndex == Type.RECENT_LIST.getValue()) {
                    ((ForwordRecentMultiSelectListFragment) fragments[Type.RECENT_LIST.getValue()]).setSelectedIds(getSelectGourpIds(), getSelectFriendIds());
                } else if (currentFragmentIndex == Type.CONTACTS.getValue()) {
                    ((ForwardSelectContactFragment) fragments[Type.CONTACTS.getValue()]).setSelectedIds(getSelectGourpIds(), getSelectFriendIds());
                }
                if (confirm){
                    listener.onForwardNoDialog(selectedGroups, selectedFriends);
                }
            }

        }
    }
}
