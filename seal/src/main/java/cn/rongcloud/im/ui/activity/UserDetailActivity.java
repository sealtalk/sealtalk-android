package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.response.FriendInvitationResponse;
import cn.rongcloud.im.server.response.GetFriendInfoByIDResponse;
import cn.rongcloud.im.server.response.GetUserInfoByIdResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.utils.RongGenerate;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import cn.rongcloud.im.server.widget.LoadDialog;
import cn.rongcloud.im.ui.widget.SinglePopWindow;

//CallKit start 1
import io.rong.callkit.RongCallAction;
import io.rong.callkit.RongVoIPIntent;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
//CallKit end 1

import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;
import io.rong.imlib.model.UserOnlineStatusInfo;

/**
 * Created by tiankui on 16/11/2.
 */

public class UserDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final int SYNC_FRIEND_INFO = 129;
    private ImageView mUserPortrait;
    private TextView mUserNickName;
    private TextView mUserDisplayName;
    private TextView mUserPhone;
    private TextView mUserLineStatus;
    private LinearLayout mChatButtonGroupLinearLayout;
    private Button mAddFriendButton;
    private LinearLayout mNoteNameLinearLayout;

    private static final int ADD_FRIEND = 10086;
    private static final int SYN_USER_INFO = 10087;
    private Friend mFriend;
    private String addMessage;
    private String mGroupName;
    private String mPhoneString;
    private boolean mIsFriendsRelationship;

    private int mType;
    private static final int CLICK_CONVERSATION_USER_PORTRAIT = 1;
    private static final int CLICK_CONTACT_FRAGMENT_FRIEND = 2;


    private UserDetailActivityHandler mHandler = new UserDetailActivityHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        initView();
        initData();
        initBlackListStatusView();
    }

    private void initView() {
        setTitle(R.string.user_details);
        mUserNickName = (TextView) findViewById(R.id.contact_below);
        mUserDisplayName = (TextView) findViewById(R.id.contact_top);
        mUserPhone = (TextView) findViewById(R.id.contact_phone);
        mUserLineStatus = (TextView) findViewById(R.id.user_online_status);
        mUserPortrait = (ImageView) findViewById(R.id.ac_iv_user_portrait);
        mChatButtonGroupLinearLayout = (LinearLayout) findViewById(R.id.ac_ll_chat_button_group);
        mAddFriendButton = (Button) findViewById(R.id.ac_bt_add_friend);
        mNoteNameLinearLayout = (LinearLayout) findViewById(R.id.ac_ll_note_name);

        mAddFriendButton.setOnClickListener(this);
        mUserPhone.setOnClickListener(this);
    }

    private void initData() {
        mType = getIntent().getIntExtra("type", 0);
        if (mType == CLICK_CONVERSATION_USER_PORTRAIT) {
            SealAppContext.getInstance().pushActivity(this);
        }
        mGroupName = getIntent().getStringExtra("groupName");
        mFriend = getIntent().getParcelableExtra("friend");

        if (mFriend != null) {
            if (mFriend.isExitsDisplayName()) {
                mUserNickName.setVisibility(View.VISIBLE);
                mUserNickName.setText(getString(R.string.ac_contact_nick_name) + " " + mFriend.getName());
                mUserDisplayName.setText(mFriend.getDisplayName());
            } else {
                mUserDisplayName.setText(mFriend.getName());
            }
            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(mFriend);
            ImageLoader.getInstance().displayImage(portraitUri, mUserPortrait, App.getOptions());
        }
        if (getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false)) {
            RongIMClient.getInstance().getUserOnlineStatus(mFriend.getUserId(), new IRongCallback.IGetUserOnlineStatusCallback() {
                @Override
                public void onSuccess(final ArrayList<UserOnlineStatusInfo> userOnlineStatusInfoList) {
                    if (userOnlineStatusInfoList != null && userOnlineStatusInfoList.size() > 0) {
                        UserOnlineStatusInfo userOnlineStatusInfo = null;
                        for (int i = 0; i < userOnlineStatusInfoList.size(); ++i) {
                            if (i == 0) {
                                userOnlineStatusInfo = userOnlineStatusInfoList.get(i);
                            } else {
                                if (userOnlineStatusInfoList.get(i).getPlatform().getValue() > userOnlineStatusInfo.getPlatform().getValue()) {
                                    userOnlineStatusInfo = userOnlineStatusInfoList.get(i);
                                }
                            }
                        }
                        Message message = mHandler.obtainMessage();
                        message.obj = userOnlineStatusInfo;
                        mHandler.sendMessage(message);
                    } else {
                        Message message = mHandler.obtainMessage();
                        message.obj = null;
                        mHandler.sendMessage(message);
                    }

                }

                @Override
                public void onError(int errorCode) {

                }
            });
        }
        syncPersonalInfo();

        if (!TextUtils.isEmpty(mFriend.getUserId())) {
            String mySelf = getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, "");
            if (mySelf.equals(mFriend.getUserId())) {
                mChatButtonGroupLinearLayout.setVisibility(View.VISIBLE);
                mAddFriendButton.setVisibility(View.GONE);
                return;
            }
            if (mIsFriendsRelationship) {
                mChatButtonGroupLinearLayout.setVisibility(View.VISIBLE);
                mAddFriendButton.setVisibility(View.GONE);
            } else {
                mAddFriendButton.setVisibility(View.VISIBLE);
                mChatButtonGroupLinearLayout.setVisibility(View.GONE);
                mNoteNameLinearLayout.setVisibility(View.GONE);
            }
        }
    }


    private void syncPersonalInfo() {
        mIsFriendsRelationship = SealUserInfoManager.getInstance().isFriendsRelationship(mFriend.getUserId());
        if (mIsFriendsRelationship) {
            String userId = mFriend.getUserId();
            mFriend = SealUserInfoManager.getInstance().getFriendByID(userId);
            request(SYNC_FRIEND_INFO, true);
        } else {
            request(SYN_USER_INFO, true);
        }
    }

    private void initBlackListStatusView() {
        if (mIsFriendsRelationship) {
            Button rightButton = getHeadRightButton();
            rightButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.main_activity_contact_more));
            rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    RongIM.getInstance().getBlacklistStatus(mFriend.getUserId(), new RongIMClient.ResultCallback<RongIMClient.BlacklistStatus>() {
                        @Override
                        public void onSuccess(RongIMClient.BlacklistStatus blacklistStatus) {
                            SinglePopWindow morePopWindow = new SinglePopWindow(UserDetailActivity.this, mFriend, blacklistStatus);
                            morePopWindow.showPopupWindow(v);
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode e) {

                        }
                    });
                }
            });
        }
    }

    public void startChat(View view) {
        String displayName = mFriend.getDisplayName();
        if (!TextUtils.isEmpty(displayName)) {
            RongIM.getInstance().startPrivateChat(mContext, mFriend.getUserId(), displayName);
        } else {
            RongIM.getInstance().startPrivateChat(mContext, mFriend.getUserId(), mFriend.getName());
        }
        finish();
    }

    //CallKit start 2
    public void startVoice(View view) {
        RongCallSession profile = RongCallClient.getInstance().getCallSession();
        if (profile != null && profile.getActiveTime() > 0) {
            Toast.makeText(mContext,
                    profile.getMediaType() == RongCallCommon.CallMediaType.AUDIO ?
                            getString(io.rong.callkit.R.string.rc_voip_call_audio_start_fail) :
                            getString(io.rong.callkit.R.string.rc_voip_call_video_start_fail),
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            Toast.makeText(mContext, getString(io.rong.callkit.R.string.rc_voip_call_network_error), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO);
        intent.putExtra("conversationType", Conversation.ConversationType.PRIVATE.getName().toLowerCase(Locale.US));
        intent.putExtra("targetId", mFriend.getUserId());
        intent.putExtra("callAction", RongCallAction.ACTION_OUTGOING_CALL.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getPackageName());
        getApplicationContext().startActivity(intent);
    }

    public void startVideo(View view) {
        RongCallSession profile = RongCallClient.getInstance().getCallSession();
        if (profile != null && profile.getActiveTime() > 0) {
            Toast.makeText(mContext,
                    profile.getMediaType() == RongCallCommon.CallMediaType.AUDIO ?
                            getString(io.rong.callkit.R.string.rc_voip_call_audio_start_fail) :
                            getString(io.rong.callkit.R.string.rc_voip_call_video_start_fail),
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            Toast.makeText(mContext, getString(io.rong.callkit.R.string.rc_voip_call_network_error), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEVIDEO);
        intent.putExtra("conversationType", Conversation.ConversationType.PRIVATE.getName().toLowerCase(Locale.US));
        intent.putExtra("targetId", mFriend.getUserId());
        intent.putExtra("callAction", RongCallAction.ACTION_OUTGOING_CALL.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getPackageName());
        getApplicationContext().startActivity(intent);
    }
    //CallKit end 2

    public void finishPage(View view) {
        this.finish();
    }

    public void setDisplayName(View view) {
        Intent intent = new Intent(mContext, NoteInformationActivity.class);
        intent.putExtra("friend", mFriend);
        startActivityForResult(intent, 99);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ac_bt_add_friend:
                DialogWithYesOrNoUtils.getInstance().showEditDialog(mContext, getString(R.string.add_text), getString(R.string.confirm), new DialogWithYesOrNoUtils.DialogCallBack() {
                    @Override
                    public void executeEvent() {

                    }

                    @Override
                    public void executeEditEvent(String editText) {
                        if (TextUtils.isEmpty(editText)) {
                            if (mGroupName != null && !TextUtils.isEmpty(mGroupName)) {
                                addMessage = "我是" + mGroupName + "群的" + getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_NAME, "");
                            } else {
                                addMessage = "我是" + getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_NAME, "");
                            }
                        } else {
                            addMessage = editText;
                        }
                        LoadDialog.show(mContext);
                        request(ADD_FRIEND, true);
                    }

                    @Override
                    public void updatePassword(String oldPassword, String newPassword) {

                    }
                });
                break;
            case R.id.contact_phone:
                if (!TextUtils.isEmpty(mPhoneString)) {
                    Uri telUri = Uri.parse("tel:"+mPhoneString);
                    Intent intent = new Intent(Intent.ACTION_DIAL, telUri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 155 && data != null) {
            String displayName = data.getStringExtra("displayName");
            if (!TextUtils.isEmpty(displayName)) {
                mUserNickName.setVisibility(View.VISIBLE);
                mUserNickName.setText(getString(R.string.ac_contact_nick_name) + " " + mFriend.getName());
                mUserDisplayName.setText(displayName);
                mFriend.setDisplayName(displayName);
            } else {
                mUserNickName.setVisibility(View.GONE);
                mUserDisplayName.setText(mFriend.getName());
                mUserDisplayName.setVisibility(View.VISIBLE);
                mFriend.setDisplayName("");
            }
        }
    }

    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        switch (requestCode) {
            case ADD_FRIEND:
                return action.sendFriendInvitation(mFriend.getUserId(), addMessage);
            case SYN_USER_INFO:
                return action.getUserInfoById(mFriend.getUserId());
            case SYNC_FRIEND_INFO:
                return action.getFriendInfoByID(mFriend.getUserId());
        }
        return super.doInBackground(requestCode, id);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            switch (requestCode) {
                case ADD_FRIEND:
                    FriendInvitationResponse response = (FriendInvitationResponse) result;
                    if (response.getCode() == 200) {
                        LoadDialog.dismiss(mContext);
                        NToast.shortToast(mContext, getString(R.string.request_success));
                        this.finish();
                    }
                    break;
                case SYN_USER_INFO:
                    //TODO:群组里的好友备注功能，还没有实现；
                    GetUserInfoByIdResponse userInfoByIdResponse = (GetUserInfoByIdResponse) result;
                    if (userInfoByIdResponse.getCode() == 200 && userInfoByIdResponse.getResult() != null &&
                            mFriend.getUserId().equals(userInfoByIdResponse.getResult().getId())) {
                        String nickName = userInfoByIdResponse.getResult().getNickname();
                        String portraitUri = userInfoByIdResponse.getResult().getPortraitUri();
                        if (hasNickNameChanged(nickName) || hasPortraitUriChanged(portraitUri)) {
                            if (hasNickNameChanged(nickName)) {
                                mUserNickName.setText(nickName);
                            }
                            if (hasPortraitUriChanged(portraitUri)) {
                                ImageLoader.getInstance().displayImage(portraitUri, mUserPortrait, App.getOptions());
                            } else {
                                portraitUri = mFriend.getPortraitUri().toString();
                            }

                            UserInfo userInfo = new UserInfo(userInfoByIdResponse.getResult().getId(), nickName, Uri.parse(portraitUri));
                            RongIM.getInstance().refreshUserInfoCache(userInfo);
                        }
                    }
                    break;
                case SYNC_FRIEND_INFO:
                    GetFriendInfoByIDResponse friendInfoByIDResponse = (GetFriendInfoByIDResponse) result;
                    if (friendInfoByIDResponse.getCode() == 200) {
                        mUserPhone.setVisibility(View.VISIBLE);
                        mPhoneString = friendInfoByIDResponse.getResult().getUser().getPhone();
                        mUserPhone.setText("手机号:" + friendInfoByIDResponse.getResult().getUser().getPhone());
                        GetFriendInfoByIDResponse.ResultEntity resultEntity = friendInfoByIDResponse.getResult();
                        GetFriendInfoByIDResponse.ResultEntity.UserEntity userEntity = resultEntity.getUser();
                        if (mFriend.getUserId().equals(userEntity.getId())) {
                            if (hasFriendInfoChanged(resultEntity)) {
                                String nickName = userEntity.getNickname();
                                String portraitUri = userEntity.getPortraitUri();
                                //当前app server返回的displayName为空,先不使用
                                String displayName = resultEntity.getdisplayName();
                                //如果没有设置头像,好友数据库的头像地址和用户信息提供者的头像处理不一致,这个不一致是seal app代码处理的问题,未来应该矫正回来
                                String userInfoPortraitUri = mFriend.getPortraitUri().toString();
                                //更新UI
                                //if (TextUtils.isEmpty(displayName) && hasDisplayNameChanged(displayName)) {
                                if (!TextUtils.isEmpty(mFriend.getDisplayName())) {
                                    mUserNickName.setVisibility(View.VISIBLE);
                                    mUserNickName.setText(getString(R.string.ac_contact_nick_name) + " " + nickName);
                                    mUserDisplayName.setText(mFriend.getDisplayName());
                                } else if (hasNickNameChanged(nickName)) {
                                    if (mFriend.isExitsDisplayName()) {
                                        mUserNickName.setText(getString(R.string.ac_contact_nick_name) + " " + nickName);
                                    } else {
                                        mUserDisplayName.setText(nickName);
                                    }
                                }
                                if (hasPortraitUriChanged(portraitUri)) {
                                    ImageLoader.getInstance().displayImage(portraitUri, mUserPortrait, App.getOptions());
                                    userInfoPortraitUri = portraitUri;
                                }
                                //更新好友数据库
                                SealUserInfoManager.getInstance().addFriend(
                                        new Friend(mFriend.getUserId(),
                                                nickName,
                                                Uri.parse(portraitUri),
                                                mFriend.getDisplayName(),
                                                null, null, null, null,
                                                CharacterParser.getInstance().getSpelling(nickName),
                                                TextUtils.isEmpty(mFriend.getDisplayName()) ?
                                                        null : CharacterParser.getInstance().getSpelling(mFriend.getDisplayName())));
                                //更新好友列表
                                BroadcastManager.getInstance(mContext).sendBroadcast(SealAppContext.UPDATE_FRIEND);
                                //更新用户信息提供者
                                if ((!mFriend.isExitsDisplayName() && hasNickNameChanged(nickName)) ||
                                        hasPortraitUriChanged(portraitUri)) {
                                    //如果备注存在,UserInfo设置备注
                                    if (mFriend.isExitsDisplayName())
                                        nickName = mFriend.getDisplayName();
                                    if (TextUtils.isEmpty(userInfoPortraitUri)) {
                                        userInfoPortraitUri = RongGenerate.generateDefaultAvatar(nickName, mFriend.getUserId());
                                    }
                                    UserInfo newUserInfo = new UserInfo(mFriend.getUserId(),
                                            nickName,
                                            Uri.parse(userInfoPortraitUri));
                                    RongIM.getInstance().refreshUserInfoCache(newUserInfo);
                                }
                            }
                        }

                    }

                    break;
            }
        }
    }

    private boolean hasNickNameChanged(String nickName) {
        if (mFriend.getName() == null) {
            return nickName != null;
        } else {
            return !mFriend.getName().equals(nickName);
        }
    }

    private boolean hasPortraitUriChanged(String portraitUri) {
        if (mFriend.getPortraitUri() == null) {
            return portraitUri != null;
        } else {
            if (mFriend.getPortraitUri().equals(portraitUri)) {
                return false;
            } else {
                return !TextUtils.isEmpty(portraitUri);
            }
        }
    }

    private boolean hasDisplayNameChanged(String displayName) {
        if (mFriend.getDisplayName() == null) {
            return displayName != null;
        } else {
            return !mFriend.getDisplayName().equals(displayName);
        }
    }

    private boolean hasFriendInfoChanged(GetFriendInfoByIDResponse.ResultEntity resultEntity) {
        GetFriendInfoByIDResponse.ResultEntity.UserEntity userEntity = resultEntity.getUser();
        String nickName = userEntity.getNickname();
        String portraitUri = userEntity.getPortraitUri();
        String displayName = resultEntity.getdisplayName();
        return hasNickNameChanged(nickName) ||
                hasPortraitUriChanged(portraitUri) ||
                hasDisplayNameChanged(displayName);
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        if (requestCode == ADD_FRIEND)//添加好友时报网络异常,其余操作不需要
            super.onFailure(requestCode, state, result);
    }

    @Override
    public void onBackPressed() {
        if (mType == CLICK_CONVERSATION_USER_PORTRAIT) {
            SealAppContext.getInstance().popActivity(this);
        }
        super.onBackPressed();
    }

    @Override
    public void onHeadLeftButtonClick(View v) {
        if (mType == CLICK_CONVERSATION_USER_PORTRAIT) {
            SealAppContext.getInstance().popActivity(this);
        }
        super.onHeadLeftButtonClick(v);
    }

    private static class UserDetailActivityHandler extends Handler {
        private final WeakReference<UserDetailActivity> mActivity;

        public UserDetailActivityHandler(UserDetailActivity activity) {
            mActivity = new WeakReference<UserDetailActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg != null) {
                UserDetailActivity activity = mActivity.get();
                if (activity != null) {
                    activity.mUserLineStatus.setVisibility(View.VISIBLE);
                    UserOnlineStatusInfo userOnlineStatusInfo = (UserOnlineStatusInfo) msg.obj;
                    if (userOnlineStatusInfo.getCustomerStatus() > 1) {
                        if (userOnlineStatusInfo.getCustomerStatus() == 5) {
                            activity.mUserLineStatus.setText(activity.getString(R.string.ipad_online));
                            activity.mUserLineStatus.setTextColor(Color.parseColor("#60E23F"));
                        } else if (userOnlineStatusInfo.getCustomerStatus() == 6) {
                            activity.mUserLineStatus.setText(activity.getString(R.string.imac_online));
                            activity.mUserLineStatus.setTextColor(Color.parseColor("#60E23F"));
                        }
                    } else if (userOnlineStatusInfo.getServiceStatus() == 0) {
                        activity.mUserLineStatus.setTextColor(Color.parseColor("#666666"));
                        activity.mUserLineStatus.setText(R.string.offline);
                    } else if (userOnlineStatusInfo != null){
                        switch (userOnlineStatusInfo.getPlatform()) {
                            case Platform_PC:
                                activity.mUserLineStatus.setText(R.string.pc_online);
                                activity.mUserLineStatus.setTextColor(Color.parseColor("#60E23F"));
                                break; //PC
                            case Platform_Android:
                            case Platform_iOS:
                                activity.mUserLineStatus.setText(R.string.phone_online);
                                activity.mUserLineStatus.setTextColor(Color.parseColor("#60E23F"));
                                break; //phone
                            case Platform_Web:
                                activity.mUserLineStatus.setText(R.string.pc_online);
                                activity.mUserLineStatus.setTextColor(Color.parseColor("#60E23F"));
                                break; //web
                            case Platform_Other:
                            default:
                                activity.mUserLineStatus.setTextColor(Color.parseColor("#666666"));
                                activity.mUserLineStatus.setText(R.string.offline);
                                break; // offline
                        }
                    } else {
                        activity.mUserLineStatus.setTextColor(Color.parseColor("#666666"));
                        activity.mUserLineStatus.setText(R.string.offline);
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
