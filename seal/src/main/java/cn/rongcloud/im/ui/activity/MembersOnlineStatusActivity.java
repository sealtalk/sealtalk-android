package cn.rongcloud.im.ui.activity;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.GroupMember;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserOnlineStatusInfo;

public class MembersOnlineStatusActivity extends BaseActivity implements IRongCallback.ISetSubscribeStatusCallback {

    private ListView listView;
    private String groupId;

    private List<UserOnlineStatus> datas = new ArrayList<>();
    private GroupMemberAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_online_status);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new GroupMemberAdapter(this);
        listView.setAdapter(adapter);
        groupId = getIntent().getStringExtra("targetId");
        datas.clear();
        RongIMClient.getInstance().setSubscribeStatusListener(this);
        initData();
    }


    private void initData() {
        SealUserInfoManager.getInstance().getGroupMembers(groupId, new SealUserInfoManager.ResultCallback<List<GroupMember>>() {

            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                List<String> users = new ArrayList<>();
                for (GroupMember member : groupMembers) {
                    users.add(member.getUserId());
                }
                RongIMClient.getInstance().subscribeUserOnlineStatus(users);
            }

            @Override
            public void onError(String errString) {

            }
        });
    }

    @Override
    public void onStatusReceived(final String userId, final ArrayList<UserOnlineStatusInfo> userOnlineStatusInfos) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (userOnlineStatusInfos != null && userOnlineStatusInfos.size() > 0) {
                    datas.add(new UserOnlineStatus(userId, userOnlineStatusInfos));
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RongIMClient.getInstance().setSubscribeStatusListener(null);
    }


    class UserOnlineStatus {

        private String id;
        private List<UserOnlineStatusInfo> userOnlineStatusInfo;


        public UserOnlineStatus(String id, List<UserOnlineStatusInfo> userOnlineStatusInfo) {
            this.id = id;
            this.userOnlineStatusInfo = userOnlineStatusInfo;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<UserOnlineStatusInfo> getUserOnlineStatusInfo() {
            return userOnlineStatusInfo;
        }

        public void setUserOnlineStatusInfo(List<UserOnlineStatusInfo> userOnlineStatusInfo) {
            this.userOnlineStatusInfo = userOnlineStatusInfo;
        }
    }

    class GroupMemberAdapter extends BaseAdapter {

        private Context context;

        public GroupMemberAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            if (datas != null && datas.size() > 0) {
                return datas.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_online_status, null);
            }
            TextView name = (TextView) convertView.findViewById(R.id.user_name);
            TextView onlineStatus = (TextView) convertView.findViewById(R.id.tv_online_status);
            name.setText(datas.get(position).getId());
            String status = "";
            List<UserOnlineStatusInfo> userOnlineStatusInfo = datas.get(position).getUserOnlineStatusInfo();
            for (int i = 0; i < userOnlineStatusInfo.size(); ++i) {

                if (userOnlineStatusInfo.get(i).getServiceStatus() == 0) {
                    status = getString(R.string.offline);
                } else {
                    UserOnlineStatusInfo.PlatformInfo platformInfo = userOnlineStatusInfo.get(i).getPlatform();
                    switch (platformInfo) {
                        case Platform_PC:
                            status += getString(R.string.pc_online);
                            break; //PC
                        case Platform_Android:
                        case Platform_iOS:
                            status += getString(R.string.phone_online);
                            break; //phone
                        case Platform_Web:
                            status += getString(R.string.pc_online);
                            break; //web
                        case Platform_Other:
                        default:
                            status = getString(R.string.offline);
                            break; // offline
                    }
                    if (i != userOnlineStatusInfo.size() - 1 && platformInfo != UserOnlineStatusInfo.PlatformInfo.Platform_Other) {
                        status += "/";
                    }
                }
                if (userOnlineStatusInfo.get(i).getCustomerStatus() > 1 && i == userOnlineStatusInfo.size() - 1) {
                    if (userOnlineStatusInfo.get(i).getCustomerStatus() == 5) {
                        status += "(" + getString(R.string.ipad_online) + ")";
                    } else if (userOnlineStatusInfo.get(i).getCustomerStatus() == 6) {
                        status += "(" + getString(R.string.imac_online) + ")";
                    }
                }
            }

            onlineStatus.setText(status);
            return convertView;
        }
    }
}
