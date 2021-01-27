package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.FriendStatus;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.ui.adapter.NewFriendListAdapter;
import cn.rongcloud.im.viewmodel.NewFriendViewModel;


public class NewFriendListActivity extends TitleBaseActivity {

    private NewFriendListAdapter adapter;
    private NewFriendViewModel newFriendViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friendlist);
        initView();
        initViewModel();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        // title 设置
        getTitleBar().setTitle(R.string.new_friends);
        //左侧按钮监听
        getTitleBar().setOnBtnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 右侧按钮监听
        getTitleBar().getBtnRight().setImageDrawable(getResources().getDrawable(R.drawable.seal_ic_new_friend_title_right));
        getTitleBar().setOnBtnRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewFriendListActivity.this, SearchFriendActivity.class);
                startActivity(intent);
            }
        });

        //新朋友列表
        ListView newFriendsList = findViewById(R.id.lv_new_friends_list);
        TextView isNull = findViewById(R.id.tv_is_null);
        newFriendsList.setEmptyView(isNull);
        adapter = new NewFriendListAdapter();
        // 点击监听
        adapter.setOnItemButtonClick(new NewFriendListAdapter.OnItemButtonClick() {
            @Override
            public boolean onButtonClick(View view, int position, FriendShipInfo info) {
                switch (FriendStatus.getStatus(info.getStatus())) {
                    case RECEIVE_REQUEST: //收到了好友邀请
                        String friendId = info.getUser().getId();
                        agreeFriends(friendId);
                        break;
                    case SEND_REQUEST: // 发出了好友邀请
                        break;
                    case IGNORE_REQUEST: // 忽略好友邀请
                        break;
                    case IS_FRIEND: // 已是好友
                        break;
                    case DELETE_FRIEND: // 删除了好友关系
                        break;
                }
                return false;
            }

            @Override
            public boolean onIgnore(View view, int position, FriendShipInfo info) {
                String friendId = info.getUser().getId();
                ignoreFriends(friendId);
                return false;
            }
        });
        newFriendsList.setAdapter(adapter);
    }

    /**
     * ViewModel 初始化
     */
    private void initViewModel() {
        newFriendViewModel = ViewModelProviders.of(this).get(NewFriendViewModel.class);
        /**
         * 朋友列表， 进入页面或返回此相关数据
         */
        newFriendViewModel.getFriendsAll().observe(this, new Observer<Resource<List<FriendShipInfo>>>() {
            @Override
            public void onChanged(Resource<List<FriendShipInfo>> listResource) {
                if (listResource != null) {
                    adapter.updateList(listResource.data);
                }
            }
        });

        /**
         * 添加好友点击操作
         */
        newFriendViewModel.getAgreeResult().observe(this, new Observer<Resource<Boolean>>() {
            @Override
            public void onChanged(Resource<Boolean> resource) {
                
            }
        });

        newFriendViewModel.getIngoreResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {

            }
        });
    }


    /**
     * 通知添加好友
     *
     * @param friendId
     */
    private void agreeFriends(String friendId) {
        if (newFriendViewModel != null) {
            newFriendViewModel.agree(friendId);
        }
    }

    /**
     * 忽略好友请求
     * @param friendId
     */
    private void ignoreFriends(String friendId) {
        if (newFriendViewModel != null) {
            newFriendViewModel.ingore(friendId);
        }
    }

}
