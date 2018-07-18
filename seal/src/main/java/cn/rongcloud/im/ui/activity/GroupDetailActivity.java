package cn.rongcloud.im.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.db.GroupMember;
import cn.rongcloud.im.db.Groups;
import cn.rongcloud.im.db.GroupsDao;
import cn.rongcloud.im.model.SealSearchConversationResult;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.response.DismissGroupResponse;
import cn.rongcloud.im.server.response.GetGroupInfoResponse;
import cn.rongcloud.im.server.response.QiNiuTokenResponse;
import cn.rongcloud.im.server.response.QuitGroupResponse;
import cn.rongcloud.im.server.response.SetGroupDisplayNameResponse;
import cn.rongcloud.im.server.response.SetGroupNameResponse;
import cn.rongcloud.im.server.response.SetGroupPortraitResponse;
import cn.rongcloud.im.server.utils.CommonUtils;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.OperationRong;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.utils.json.JsonMananger;
import cn.rongcloud.im.server.utils.photo.PhotoUtils;
import cn.rongcloud.im.server.widget.BottomMenuDialog;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.server.widget.SelectableRoundedImageView;
import cn.rongcloud.im.ui.widget.DemoGridView;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.utilities.PromptPopupDialog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

/**
 * Created by AMing on 16/1/27.
 * Company RongCloud
 */
public class GroupDetailActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int CLICK_CONVERSATION_USER_PORTRAIT = 1;

    private static final int DISMISS_GROUP = 26;
    private static final int QUIT_GROUP = 27;
    private static final int SET_GROUP_NAME = 29;
    private static final int GET_GROUP_INFO = 30;
    private static final int UPDATE_GROUP_NAME = 32;
    private static final int GET_QI_NIU_TOKEN = 133;
    private static final int UPDATE_GROUP_HEADER = 25;
    private static final int SEARCH_TYPE_FLAG = 1;
    private static final int CHECKGROUPURL = 39;


    private boolean isCreated = false;
    private DemoGridView mGridView;
    private List<GroupMember> mGroupMember;
    private TextView mTextViewMemberSize, mGroupDisplayNameText;
    private SelectableRoundedImageView mGroupHeader;
    private SwitchButton messageTop, messageNotification;
    private Groups mGroup;
    private String fromConversationId;
    private Conversation.ConversationType mConversationType;
    private boolean isFromConversation;
    private LinearLayout mGroupAnnouncementDividerLinearLayout;
    private TextView mGroupName;
    private PhotoUtils photoUtils;
    private BottomMenuDialog dialog;
    private UploadManager uploadManager;
    private String imageUrl;
    private Uri selectUri;
    private String newGroupName;
    private LinearLayout mGroupNotice;
    private LinearLayout mSearchMessagesLinearLayout;
    private Button mDismissBtn;
    private Button mQuitBtn;
    private SealSearchConversationResult mResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_group);
        initViews();
        setTitle(R.string.group_info);

        //群组会话界面点进群组详情
        fromConversationId = getIntent().getStringExtra("TargetId");
        mConversationType = (Conversation.ConversationType) getIntent().getSerializableExtra("conversationType");

        if (!TextUtils.isEmpty(fromConversationId)) {
            isFromConversation = true;
        }

        if (isFromConversation) {//群组会话页进入
            LoadDialog.show(mContext);
            getGroups();
            getGroupMembers();
        }
        setPortraitChangeListener();

        SealAppContext.getInstance().pushActivity(this);

        setGroupsInfoChangeListener();
    }

    private void getGroups() {
        SealUserInfoManager.getInstance().getGroupsByID(fromConversationId, new SealUserInfoManager.ResultCallback<Groups>() {

            @Override
            public void onSuccess(Groups groups) {
                if (groups != null) {
                    mGroup = groups;
                    initGroupData();
                }
            }

            @Override
            public void onError(String errString) {

            }
        });
    }

    private void getGroupMembers() {
        SealUserInfoManager.getInstance().getGroupMembers(fromConversationId, new SealUserInfoManager.ResultCallback<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                LoadDialog.dismiss(mContext);
                if (groupMembers != null && groupMembers.size() > 0) {
                    mGroupMember = groupMembers;
                    initGroupMemberData();
                }
            }

            @Override
            public void onError(String errString) {
                LoadDialog.dismiss(mContext);
            }
        });
    }

    @Override
    protected void onDestroy() {
        BroadcastManager.getInstance(this).destroy(SealAppContext.UPDATE_GROUP_NAME);
        BroadcastManager.getInstance(this).destroy(SealAppContext.UPDATE_GROUP_MEMBER);
        BroadcastManager.getInstance(this).destroy(SealAppContext.GROUP_DISMISS);
        super.onDestroy();
    }

    private void initGroupData() {
        String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(mGroup);
        ImageLoader.getInstance().displayImage(portraitUri, mGroupHeader, App.getOptions());
        mGroupName.setText(mGroup.getName());

        if (RongIM.getInstance() != null) {
            RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, mGroup.getGroupsId(), new RongIMClient.ResultCallback<Conversation>() {
                @Override
                public void onSuccess(Conversation conversation) {
                    if (conversation == null) {
                        return;
                    }
                    if (conversation.isTop()) {
                        messageTop.setChecked(true);
                    } else {
                        messageTop.setChecked(false);
                    }

                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });

            RongIM.getInstance().getConversationNotificationStatus(Conversation.ConversationType.GROUP, mGroup.getGroupsId(), new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
                @Override
                public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {

                    if (conversationNotificationStatus == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB) {
                        messageNotification.setChecked(true);
                    } else {
                        messageNotification.setChecked(false);
                    }
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
        }

        if (mGroup.getRole().equals("0"))
            isCreated = true;
        if (!isCreated) {
            mGroupAnnouncementDividerLinearLayout.setVisibility(View.VISIBLE);
            mGroupNotice.setVisibility(View.VISIBLE);
        } else {
            mGroupAnnouncementDividerLinearLayout.setVisibility(View.VISIBLE);
            mDismissBtn.setVisibility(View.VISIBLE);
            mQuitBtn.setVisibility(View.GONE);
            mGroupNotice.setVisibility(View.VISIBLE);
        }
        if (CommonUtils.isNetworkConnected(mContext)) {
            request(CHECKGROUPURL);
        }
    }

    private void initGroupMemberData() {
        if (mGroupMember != null && mGroupMember.size() > 0) {
            setTitle(getString(R.string.group_info) + "(" + mGroupMember.size() + ")");
            mTextViewMemberSize.setText(getString(R.string.group_member_size) + "(" + mGroupMember.size() + ")");
            mGridView.setAdapter(new GridAdapter(mContext, mGroupMember));
        } else {
            return;
        }

        for (GroupMember member : mGroupMember) {
            if (member.getUserId().equals(getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, ""))) {
                if (!TextUtils.isEmpty(member.getDisplayName())) {
                    mGroupDisplayNameText.setText(member.getDisplayName());
                } else {
                    mGroupDisplayNameText.setText("无");
                }
            }
        }
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case QUIT_GROUP:
                return action.quitGroup(fromConversationId);
            case DISMISS_GROUP:
                return action.dissmissGroup(fromConversationId);
            case SET_GROUP_NAME:
                return action.setGroupDisplayName(fromConversationId, newGroupName);
            case GET_GROUP_INFO:
                return action.getGroupInfo(fromConversationId);
            case UPDATE_GROUP_HEADER:
                return action.setGroupPortrait(fromConversationId, imageUrl);
            case GET_QI_NIU_TOKEN:
                return action.getQiNiuToken();
            case UPDATE_GROUP_NAME:
                return action.setGroupName(fromConversationId, newGroupName);
            case CHECKGROUPURL:
                return action.getGroupInfo(fromConversationId);
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case QUIT_GROUP:
                    QuitGroupResponse response = (QuitGroupResponse) result;
                    if (response.getCode() == 200) {

                        RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, fromConversationId, new RongIMClient.ResultCallback<Conversation>() {
                            @Override
                            public void onSuccess(Conversation conversation) {
                                RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, fromConversationId, new RongIMClient.ResultCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {
                                        RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, fromConversationId, null);
                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode e) {

                                    }
                                });
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {

                            }
                        });
                        SealUserInfoManager.getInstance().deleteGroups(new Groups(fromConversationId));
                        SealUserInfoManager.getInstance().deleteGroupMembers(fromConversationId);
                        BroadcastManager.getInstance(mContext).sendBroadcast(SealConst.GROUP_LIST_UPDATE);
                        setResult(501, new Intent());
                        NToast.shortToast(mContext, getString(R.string.quit_success));
                        LoadDialog.dismiss(mContext);
                        finish();
                    }
                    break;

                case DISMISS_GROUP:
                    DismissGroupResponse response1 = (DismissGroupResponse) result;
                    if (response1.getCode() == 200) {
                        RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, fromConversationId, new RongIMClient.ResultCallback<Conversation>() {
                            @Override
                            public void onSuccess(Conversation conversation) {
                                RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, fromConversationId, new RongIMClient.ResultCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {
                                        RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, fromConversationId, null);
                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode e) {

                                    }
                                });
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {

                            }
                        });
                        SealUserInfoManager.getInstance().deleteGroups(new Groups(fromConversationId));
                        SealUserInfoManager.getInstance().deleteGroupMembers(fromConversationId);
                        BroadcastManager.getInstance(mContext).sendBroadcast(SealConst.GROUP_LIST_UPDATE);
                        setResult(501, new Intent());
                        NToast.shortToast(mContext, getString(R.string.dismiss_success));
                        LoadDialog.dismiss(mContext);
                        finish();
                    }
                    break;

                case SET_GROUP_NAME:
                    SetGroupDisplayNameResponse response2 = (SetGroupDisplayNameResponse) result;
                    if (response2.getCode() == 200) {
                        request(GET_GROUP_INFO);
                    }
                    break;
                case GET_GROUP_INFO:
                    GetGroupInfoResponse response3 = (GetGroupInfoResponse) result;
                    if (response3.getCode() == 200) {
                        int i;
                        if (isCreated) {
                            i = 0;
                        } else {
                            i = 1;
                        }
                        GetGroupInfoResponse.ResultEntity bean = response3.getResult();
                        SealUserInfoManager.getInstance().addGroup(
                                new Groups(bean.getId(), bean.getName(), bean.getPortraitUri(), newGroupName, String.valueOf(i), null)
                        );
                        mGroupName.setText(newGroupName);
                        RongIM.getInstance().refreshGroupInfoCache(new Group(fromConversationId, newGroupName, Uri.parse(bean.getPortraitUri())));
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, getString(R.string.update_success));
                    }
                    break;
                case UPDATE_GROUP_HEADER:
                    SetGroupPortraitResponse response5 = (SetGroupPortraitResponse) result;
                    if (response5.getCode() == 200) {
                        ImageLoader.getInstance().displayImage(imageUrl, mGroupHeader, App.getOptions());
                        RongIM.getInstance().refreshGroupInfoCache(new Group(fromConversationId, mGroup.getName(), Uri.parse(imageUrl)));
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, getString(R.string.update_success));
                    }

                    break;
                case GET_QI_NIU_TOKEN:
                    QiNiuTokenResponse response6 = (QiNiuTokenResponse) result;
                    if (response6.getCode() == 200) {
                        uploadImage(response6.getResult().getDomain(), response6.getResult().getToken(), selectUri);
                    }
                    break;
                case UPDATE_GROUP_NAME:
                    SetGroupNameResponse response7 = (SetGroupNameResponse) result;
                    if (response7.getCode() == 200) {
                        SealUserInfoManager.getInstance().addGroup(
                                new Groups(mGroup.getGroupsId(), newGroupName, mGroup.getPortraitUri(), mGroup.getRole())
                        );
                        mGroupName.setText(newGroupName);
                        RongIM.getInstance().refreshGroupInfoCache(new Group(fromConversationId, newGroupName, TextUtils.isEmpty(mGroup.getPortraitUri()) ? Uri.parse(RongGenerate.generateDefaultAvatar(newGroupName, mGroup.getGroupsId())) : Uri.parse(mGroup.getPortraitUri())));
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, getString(R.string.update_success));
                    }
                    break;
                case CHECKGROUPURL:
                    GetGroupInfoResponse groupInfoResponse = (GetGroupInfoResponse) result;
                    if (groupInfoResponse.getCode() == 200) {
                        if (groupInfoResponse.getResult() != null) {
                            mGroupName.setText(groupInfoResponse.getResult().getName());
                            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(groupInfoResponse);
                            ImageLoader.getInstance().displayImage(portraitUri, mGroupHeader, App.getOptions());
                            RongIM.getInstance().refreshGroupInfoCache(new Group(fromConversationId, groupInfoResponse.getResult().getName(), TextUtils.isEmpty(groupInfoResponse.getResult().getPortraitUri()) ? Uri.parse(RongGenerate.generateDefaultAvatar(groupInfoResponse.getResult().getName(), groupInfoResponse.getResult().getId())) : Uri.parse(groupInfoResponse.getResult().getPortraitUri())));
                        }
                    }
                    break;
            }
        }
    }


    @Override
    public void onFailure(int requestCode, int state, Object result) {
        switch (requestCode) {
            case QUIT_GROUP:
                NToast.shortToast(mContext, "退出群组请求失败");
                LoadDialog.dismiss(mContext);
                break;
            case DISMISS_GROUP:
                NToast.shortToast(mContext, "解散群组请求失败");
                LoadDialog.dismiss(mContext);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.group_quit:
                DialogWithYesOrNoUtils.getInstance().showDialog(mContext, getString(R.string.confirm_quit_group), new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void executeEvent() {
                        LoadDialog.show(mContext);
                        request(QUIT_GROUP);
                    }

                    @Override
                    public void executeEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
                break;
            case R.id.group_dismiss:
                DialogWithYesOrNoUtils.getInstance().showDialog(mContext, getString(R.string.confirm_dismiss_group), new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void executeEvent() {
                        LoadDialog.show(mContext);
                        request(DISMISS_GROUP);
                    }

                    @Override
                    public void executeEditEvent(String editText) {

                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
                break;
            case R.id.ac_ll_search_chatting_records:
                Intent searchIntent = new Intent(GroupDetailActivity.this, SealSearchChattingDetailActivity.class);
                searchIntent.putExtra("filterString", "");
                ArrayList<Message> arrayList = new ArrayList<>();
                searchIntent.putParcelableArrayListExtra("filterMessages", arrayList);
                mResult = new SealSearchConversationResult();
                Conversation conversation = new Conversation();
                conversation.setTargetId(fromConversationId);
                conversation.setConversationType(mConversationType);
                mResult.setConversation(conversation);
                Groups groupInfo = DBManager.getInstance().getDaoSession().getGroupsDao().queryBuilder().where(GroupsDao.Properties.GroupsId.eq(fromConversationId)).unique();
                if (groupInfo != null) {
                    String portraitUri = groupInfo.getPortraitUri();
                    mResult.setId(groupInfo.getGroupsId());

                    if (!TextUtils.isEmpty(portraitUri)) {
                        mResult.setPortraitUri(portraitUri);
                    }
                    if (!TextUtils.isEmpty(groupInfo.getName())) {
                        mResult.setTitle(groupInfo.getName());
                    } else {
                        mResult.setTitle(groupInfo.getGroupsId());
                    }

                    searchIntent.putExtra("searchConversationResult", mResult);
                    searchIntent.putExtra("flag", SEARCH_TYPE_FLAG);
                    startActivity(searchIntent);
                }
                break;
            case R.id.group_clean:
                PromptPopupDialog.newInstance(mContext,
                        getString(R.string.clean_group_chat_history)).setLayoutRes(io.rong.imkit.R.layout.rc_dialog_popup_prompt_warning)
                        .setPromptButtonClickedListener(new PromptPopupDialog.OnPromptButtonClickedListener() {
                            @Override
                            public void onPositiveButtonClicked() {
                                if (RongIM.getInstance() != null) {
                                    if (mGroup != null) {
                                        RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, mGroup.getGroupsId(), new RongIMClient.ResultCallback<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean aBoolean) {
                                                NToast.shortToast(mContext, getString(R.string.clear_success));
                                            }

                                            @Override
                                            public void onError(RongIMClient.ErrorCode errorCode) {
                                                NToast.shortToast(mContext, getString(R.string.clear_failure));
                                            }
                                        });
                                        RongIMClient.getInstance().cleanRemoteHistoryMessages(Conversation.ConversationType.GROUP, mGroup.getGroupsId(), System.currentTimeMillis(), null);
                                    }
                                }
                            }
                        }).show();

                break;
            case R.id.group_member_size_item:
                Intent intent = new Intent(mContext, TotalGroupMemberActivity.class);
                intent.putExtra("targetId", fromConversationId);
                startActivity(intent);
                break;
            case R.id.group_member_online_status:
                intent = new Intent(mContext, MembersOnlineStatusActivity.class);
                intent.putExtra("targetId", fromConversationId);
                startActivity(intent);
                break;
            case R.id.ll_group_port:
                if (isCreated) {
                    showPhotoDialog();
                }
                break;
            case R.id.ll_group_name:
                if (isCreated) {
                    DialogWithYesOrNoUtils.getInstance().showEditDialog(mContext, getString(R.string.new_group_name), getString(R.string.confirm), new DialogWithYesOrNoUtils.DialogCallBack() {
                        @Override
                        public void executeEvent() {

                        }

                        @Override
                        public void executeEditEvent(String editText) {
                            if (TextUtils.isEmpty(editText)) {
                                return;
                            }
                            if (editText.length() < 2 && editText.length() > 10) {
                                NToast.shortToast(mContext, "群名称应为 2-10 字");
                                return;
                            }

                            if (AndroidEmoji.isEmoji(editText) && editText.length() < 4) {
                                NToast.shortToast(mContext, "群名称表情过短");
                                return;
                            }
                            newGroupName = editText;
                            LoadDialog.show(mContext);
                            request(UPDATE_GROUP_NAME);
                        }

                        @Override
                        public void updatePassword(String oldPassword, String newPassword) {

                        }
                    });
                }
                break;
            case R.id.group_announcement:
                Intent tempIntent = new Intent(mContext, GroupNoticeActivity.class);
                tempIntent.putExtra("conversationType", Conversation.ConversationType.GROUP.getValue());
                tempIntent.putExtra("targetId", fromConversationId);
                startActivity(tempIntent);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sw_group_top:
                if (isChecked) {
                    if (mGroup != null) {
                        OperationRong.setConversationTop(mContext, Conversation.ConversationType.GROUP, mGroup.getGroupsId(), true);
                    }
                } else {
                    if (mGroup != null) {
                        OperationRong.setConversationTop(mContext, Conversation.ConversationType.GROUP, mGroup.getGroupsId(), false);
                    }
                }
                break;
            case R.id.sw_group_notfaction:
                if (isChecked) {
                    if (mGroup != null) {
                        OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.GROUP, mGroup.getGroupsId(), true);
                    }
                } else {
                    if (mGroup != null) {
                        OperationRong.setConverstionNotif(mContext, Conversation.ConversationType.GROUP, mGroup.getGroupsId(), false);
                    }
                }

                break;
        }
    }


    private class GridAdapter extends BaseAdapter {

        private List<GroupMember> list;
        Context context;


        public GridAdapter(Context context, List<GroupMember> list) {
            if (list.size() >= 31) {
                this.list = list.subList(0, 30);
            } else {
                this.list = list;
            }

            this.context = context;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.social_chatsetting_gridview_item, parent, false);
            }
            SelectableRoundedImageView iv_avatar = (SelectableRoundedImageView) convertView.findViewById(R.id.iv_avatar);
            TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);
            ImageView badge_delete = (ImageView) convertView.findViewById(R.id.badge_delete);

            // 最后一个item，减人按钮
            if (position == getCount() - 1 && isCreated) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.icon_btn_deleteperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GroupDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("isDeleteGroupMember", true);
                        intent.putExtra("GroupId", mGroup.getGroupsId());
                        startActivityForResult(intent, 101);
                    }

                });
            } else if ((isCreated && position == getCount() - 2) || (!isCreated && position == getCount() - 1)) {
                tv_username.setText("");
                badge_delete.setVisibility(View.GONE);
                iv_avatar.setImageResource(R.drawable.jy_drltsz_btn_addperson);

                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GroupDetailActivity.this, SelectFriendsActivity.class);
                        intent.putExtra("isAddGroupMember", true);
                        intent.putExtra("GroupId", mGroup.getGroupsId());
                        startActivityForResult(intent, 100);

                    }
                });
            } else { // 普通成员
                final GroupMember bean = list.get(position);
                Friend friend = SealUserInfoManager.getInstance().getFriendByID(bean.getUserId());
                if (friend != null && !TextUtils.isEmpty(friend.getDisplayName())) {
                    tv_username.setText(friend.getDisplayName());
                } else {
                    tv_username.setText(bean.getName());
                }

                String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(bean);
                ImageLoader.getInstance().displayImage(portraitUri, iv_avatar, App.getOptions());
                iv_avatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserInfo userInfo = new UserInfo(bean.getUserId(), bean.getName(), TextUtils.isEmpty(bean.getPortraitUri().toString()) ? Uri.parse(RongGenerate.generateDefaultAvatar(bean.getName(), bean.getUserId())) : bean.getPortraitUri());
                        Intent intent = new Intent(context, UserDetailActivity.class);
                        Friend friend = CharacterParser.getInstance().generateFriendFromUserInfo(userInfo);
                        intent.putExtra("friend", friend);
                        intent.putExtra("conversationType", Conversation.ConversationType.GROUP.getValue());
                        //Groups not Serializable,just need group name
                        intent.putExtra("groupName", mGroup.getName());
                        intent.putExtra("type", CLICK_CONVERSATION_USER_PORTRAIT);
                        context.startActivity(intent);
                    }

                });

            }

            return convertView;
        }

        @Override
        public int getCount() {
            if (isCreated) {
                return list.size() + 2;
            } else {
                return list.size() + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 传入新的数据 刷新UI的方法
         */
        public void updateListView(List<GroupMember> list) {
            this.list = list;
            notifyDataSetChanged();
        }

    }


    // 拿到新增的成员刷新adapter
    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            List<Friend> newMemberData = (List<Friend>) data.getSerializableExtra("newAddMember");
            List<Friend> deleMember = (List<Friend>) data.getSerializableExtra("deleteMember");
            if (newMemberData != null && newMemberData.size() > 0) {
                for (Friend friend : newMemberData) {
                    GroupMember member = new GroupMember(fromConversationId,
                            friend.getUserId(),
                            friend.getName(),
                            friend.getPortraitUri(),
                            null);
                    mGroupMember.add(1, member);
                }
                initGroupMemberData();
            } else if (deleMember != null && deleMember.size() > 0) {
                for (Friend friend : deleMember) {
                    for (GroupMember member : mGroupMember) {
                        if (member.getUserId().equals(friend.getUserId())) {
                            mGroupMember.remove(member);
                            break;
                        }
                    }
                }
                initGroupMemberData();
            }

        }
        switch (requestCode) {
            case PhotoUtils.INTENT_CROP:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_SELECT:
                photoUtils.onActivityResult(GroupDetailActivity.this, requestCode, resultCode, data);
                break;
        }

    }

    private void setGroupsInfoChangeListener() {
        //有些权限只有群主有,比如修改群名称等,已经更新UI不需要再更新
        BroadcastManager.getInstance(this).addAction(SealAppContext.UPDATE_GROUP_NAME, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String result = intent.getStringExtra("result");
                    if (result != null) {
                        try {
                            List<String> nameList = JsonMananger.jsonToBean(result, List.class);
                            if (nameList.size() != 3)
                                return;
                            String groupID = nameList.get(0);
                            if (groupID != null && !groupID.equals(fromConversationId))
                                return;
                            if (mGroup != null && mGroup.getRole().equals("0"))
                                return;
                            String groupName = nameList.get(1);
                            String operationName = nameList.get(2);
                            mGroupName.setText(groupName);
                            newGroupName = groupName;
                            NToast.shortToast(mContext, operationName + context.getString(R.string.rc_item_change_group_name)
                                    + "\"" + groupName + "\"");
                            RongIM.getInstance().refreshGroupInfoCache(new Group(fromConversationId, newGroupName, TextUtils.isEmpty(mGroup.getPortraitUri()) ? Uri.parse(RongGenerate.generateDefaultAvatar(newGroupName, mGroup.getGroupsId())) : Uri.parse(mGroup.getPortraitUri())));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
        BroadcastManager.getInstance(this).addAction(SealAppContext.UPDATE_GROUP_MEMBER, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String groupID = intent.getStringExtra("String");
                    if (groupID != null && groupID.equals(fromConversationId))
                        getGroupMembers();
                }
            }
        });
        BroadcastManager.getInstance(this).addAction(SealAppContext.GROUP_DISMISS, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String groupID = intent.getStringExtra("String");
                    if (groupID != null && groupID.equals(fromConversationId)) {
                        if (mGroup.getRole().equals("1"))
                            backAsGroupDismiss();
                    }
                }
            }
        });
    }

    private void backAsGroupDismiss() {
        this.setResult(501, new Intent());
        finish();
    }

    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    selectUri = uri;
                    LoadDialog.show(mContext);
                    request(GET_QI_NIU_TOKEN);
                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });
    }


    /**
     * 弹出底部框
     */
    private void showPhotoDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new BottomMenuDialog(mContext);
        dialog.setConfirmListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.takePicture(GroupDetailActivity.this);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.selectPicture(GroupDetailActivity.this);
            }
        });
        dialog.show();
    }


    public void uploadImage(final String domain, String imageToken, Uri imagePath) {
        if (TextUtils.isEmpty(domain) && TextUtils.isEmpty(imageToken) && TextUtils.isEmpty(imagePath.toString())) {
            throw new RuntimeException("upload parameter is null!");
        }
        File imageFile = new File(imagePath.getPath());

        if (this.uploadManager == null) {
            this.uploadManager = new UploadManager();
        }
        this.uploadManager.put(imageFile, null, imageToken, new UpCompletionHandler() {

            @Override
            public void complete(String s, ResponseInfo responseInfo, JSONObject jsonObject) {
                if (responseInfo.isOK()) {
                    try {
                        String key = (String) jsonObject.get("key");
                        imageUrl = "http://" + domain + "/" + key;
                        Log.e("uploadImage", imageUrl);
                        if (!TextUtils.isEmpty(imageUrl)) {
                            request(UPDATE_GROUP_HEADER);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, null);
    }


    private void initViews() {
        messageTop = (SwitchButton) findViewById(R.id.sw_group_top);
        messageNotification = (SwitchButton) findViewById(R.id.sw_group_notfaction);
        messageTop.setOnCheckedChangeListener(this);
        messageNotification.setOnCheckedChangeListener(this);
        LinearLayout groupClean = (LinearLayout) findViewById(R.id.group_clean);
        mGridView = (DemoGridView) findViewById(R.id.gridview);
        mTextViewMemberSize = (TextView) findViewById(R.id.group_member_size);
        mGroupHeader = (SelectableRoundedImageView) findViewById(R.id.group_header);
        LinearLayout mGroupDisplayName = (LinearLayout) findViewById(R.id.group_displayname);
        mGroupDisplayNameText = (TextView) findViewById(R.id.group_displayname_text);
        mGroupName = (TextView) findViewById(R.id.group_name);
        mQuitBtn = (Button) findViewById(R.id.group_quit);
        mDismissBtn = (Button) findViewById(R.id.group_dismiss);
        RelativeLayout totalGroupMember = (RelativeLayout) findViewById(R.id.group_member_size_item);
        RelativeLayout memberOnlineStatus = (RelativeLayout) findViewById(R.id.group_member_online_status);
        LinearLayout mGroupPortL = (LinearLayout) findViewById(R.id.ll_group_port);
        LinearLayout mGroupNameL = (LinearLayout) findViewById(R.id.ll_group_name);
        mGroupAnnouncementDividerLinearLayout = (LinearLayout) findViewById(R.id.ac_ll_group_announcement_divider);
        mGroupNotice = (LinearLayout) findViewById(R.id.group_announcement);
        mSearchMessagesLinearLayout = (LinearLayout) findViewById(R.id.ac_ll_search_chatting_records);
        mGroupPortL.setOnClickListener(this);
        mGroupNameL.setOnClickListener(this);
        totalGroupMember.setOnClickListener(this);
        mGroupDisplayName.setOnClickListener(this);
        memberOnlineStatus.setOnClickListener(this);
        if (getSharedPreferences("config", Context.MODE_PRIVATE).getBoolean("isDebug", false)) {
            memberOnlineStatus.setVisibility(View.VISIBLE);
        }
        mQuitBtn.setOnClickListener(this);
        mDismissBtn.setOnClickListener(this);
        groupClean.setOnClickListener(this);
        mGroupNotice.setOnClickListener(this);
        mSearchMessagesLinearLayout.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        SealAppContext.getInstance().popActivity(this);
        super.onBackPressed();
    }
}
