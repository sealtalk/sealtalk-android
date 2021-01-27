package cn.rongcloud.im.ui.test;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.TitleBaseActivity;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.discussion.message.DiscussionNotificationMessage;
import io.rong.imlib.discussion.model.Discussion;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

public class DiscussionActivity extends TitleBaseActivity implements View.OnClickListener {

    private ListView lvContent;
    private MyAdapter mAdapter;
    private ArrayList<String> contentList = new ArrayList<>();
    private Handler handler = new Handler();
    private String currentDiscussionId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discussion);
        initView();
    }

    private void initView() {
        getTitleBar().setTitle("讨论组");
        lvContent = findViewById(R.id.lv_content);
        mAdapter = new MyAdapter();
        lvContent.setAdapter(mAdapter);
        findViewById(R.id.btn_create_discussion).setOnClickListener(this);
        findViewById(R.id.btn_add_member).setOnClickListener(this);
        findViewById(R.id.btn_remove_member).setOnClickListener(this);
        findViewById(R.id.btn_rename).setOnClickListener(this);
        findViewById(R.id.btn_quit).setOnClickListener(this);
        findViewById(R.id.btn_get_info).setOnClickListener(this);
        findViewById(R.id.btn_set_permission).setOnClickListener(this);
        findViewById(R.id.btn_send_message).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create_discussion:
                createDiscussion();
                break;
            case R.id.btn_add_member:
                addMemberToDiscussion();
                break;
            case R.id.btn_remove_member:
                removeMemberFromDiscussion();
                break;
            case R.id.btn_rename:
                renameDiscussion();
                break;
            case R.id.btn_quit:
                quitDiscussion();
                break;
            case R.id.btn_get_info:
                getDiscussionInfo();
                break;
            case R.id.btn_set_permission:
                setDiscussionInviteStatus();
                break;
            case R.id.btn_send_message:
                sendDiscussionMessage();
                break;
        }
    }

    private void createDiscussion() {
        List<String> userIdList = new ArrayList<>();
        userIdList.add("1");
        userIdList.add("2");
        userIdList.add("3");
        RongIMClient.getInstance().createDiscussion("讨论组", userIdList, new RongIMClient.CreateDiscussionCallback() {
            @Override
            public void onSuccess(String s) {
                currentDiscussionId = s;
                addToList("create success ===" + s);
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                addToList("create discussion failed === " + e.toString());
            }
        });
    }

    private void addMemberToDiscussion() {
        if (TextUtils.isEmpty(currentDiscussionId)) {
            Toast.makeText(this, "讨论组未创建", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> userIdList = new ArrayList<>();
        userIdList.add("4");
        userIdList.add("5");
        RongIMClient.getInstance().addMemberToDiscussion(currentDiscussionId, userIdList, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList("add Member success ===");
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                addToList("add Member failed === " + errorCode.toString());
            }
        });
    }

    private RongIMClient.DiscussionInviteStatus discussionInviteStatus = RongIMClient.DiscussionInviteStatus.OPENED;

    private void setDiscussionInviteStatus() {
        if (TextUtils.isEmpty(currentDiscussionId)) {
            Toast.makeText(this, "讨论组未创建", Toast.LENGTH_SHORT).show();
            return;
        }
        if (discussionInviteStatus.equals(RongIMClient.DiscussionInviteStatus.OPENED)) {
            discussionInviteStatus = RongIMClient.DiscussionInviteStatus.CLOSED;
        } else {
            discussionInviteStatus = RongIMClient.DiscussionInviteStatus.OPENED;
        }
        RongIMClient.getInstance().setDiscussionInviteStatus(currentDiscussionId, discussionInviteStatus, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList("set Discussion InviteStatus success ===");
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                addToList("set Discussion InviteStatus failed ===" + errorCode.toString());
            }
        });
    }

    private void removeMemberFromDiscussion() {
        if (TextUtils.isEmpty(currentDiscussionId)) {
            Toast.makeText(this, "讨论组未创建", Toast.LENGTH_SHORT).show();
            return;
        }
        RongIMClient.getInstance().removeMemberFromDiscussion(currentDiscussionId, "5", new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList("remove Member success ===");
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                addToList("remove Member failed === " + errorCode.toString());
            }
        });
    }

    private void renameDiscussion() {
        if (TextUtils.isEmpty(currentDiscussionId)) {
            Toast.makeText(this, "讨论组未创建", Toast.LENGTH_SHORT).show();
            return;
        }
        RongIMClient.getInstance().setDiscussionName(currentDiscussionId, "讨论组 1", new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList("rename Discussion success ===");
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                addToList("rename Discussion failed === " + errorCode.toString());
            }
        });
    }

    private void quitDiscussion() {
        if (TextUtils.isEmpty(currentDiscussionId)) {
            Toast.makeText(this, "讨论组未创建", Toast.LENGTH_SHORT).show();
            return;
        }
        RongIMClient.getInstance().quitDiscussion(currentDiscussionId, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                addToList("quit Discussion success ===");
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                addToList("quit Discussion failed === " + errorCode.toString());
            }
        });
    }

    private void getDiscussionInfo() {
        if (TextUtils.isEmpty(currentDiscussionId)) {
            Toast.makeText(this, "讨论组未创建", Toast.LENGTH_SHORT).show();
            return;
        }
        RongIMClient.getInstance().getDiscussion(currentDiscussionId, new RongIMClient.ResultCallback<Discussion>() {
            @Override
            public void onSuccess(Discussion discussion) {
                addToList("get Discussion success === " + "Discussion{" +
                        "id='" + discussion.getId() + '\'' +
                        ", name='" + discussion.getName() + '\'' +
                        ", creatorId='" + discussion.getCreatorId() + '\'' +
                        ", isOpen=" + discussion.isOpen() +
                        ", memberIdList=" + discussion.getMemberIdList() +
                        '}');
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                addToList("get Discussion failed === " + e.toString());
            }
        });
    }

    private void sendDiscussionMessage(){
        if (TextUtils.isEmpty(currentDiscussionId)) {
            Toast.makeText(this, "讨论组未创建", Toast.LENGTH_SHORT).show();
            return;
        }
        DiscussionNotificationMessage message = new DiscussionNotificationMessage();
        message.setType(1);
        message.setOperator(RongIMClient.getInstance().getCurrentUserId());
        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.DISCUSSION, currentDiscussionId, message, "", "", new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                addToList("send Discussion Message Success === ");
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                addToList("send Discussion Message === " + errorCode.toString());
            }
        });
    }

    public void addToList(String str) {
        contentList.add(str);
        handler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (lvContent != null && mAdapter != null) {
                    lvContent.setSelection(mAdapter.getCount() - 1);
                    Log.e("addToList", "**" + mAdapter.getCount() + "**" + contentList.size());
                }
            }
        }, 300);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return contentList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test_status, null);
            }
            TextView tvCotent = convertView.findViewById(R.id.tv_content);
            tvCotent.setText(contentList.get(position));
            return convertView;
        }
    }
}
