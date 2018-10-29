package io.rong.callkit;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bailingcloud.bailingvideo.engine.binstack.util.FinLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.rong.callkit.util.BluetoothUtil;
import io.rong.callkit.util.CallKitUtils;
import io.rong.callkit.util.GlideUtils;
import io.rong.callkit.util.HeadsetInfo;
import io.rong.callkit.util.SPUtils;
import io.rong.calllib.CallUserProfile;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.calllib.message.CallSTerminateMessage;
import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

public class SingleCallActivity extends BaseCallActivity implements Handler.Callback {
    private static final String TAG = "VoIPSingleActivity";
    private LayoutInflater inflater;
    private RongCallSession callSession;
    private FrameLayout mLPreviewContainer;
    private FrameLayout mSPreviewContainer;
    private FrameLayout mButtonContainer;
    private LinearLayout mUserInfoContainer;
    private Boolean isInformationShow = false;
    private SurfaceView mLocalVideo = null;
    private boolean muted = false;
    private boolean handFree = false;
    private boolean startForCheckPermissions = false;

    private int EVENT_FULL_SCREEN = 1;

    private String targetId = null;
    private RongCallCommon.CallMediaType mediaType;

    @Override
    final public boolean handleMessage(Message msg) {
        if (msg.what == EVENT_FULL_SCREEN) {
            hideVideoCallInformation();
            return true;
        }
        return false;
    }

    @Override
    @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rc_voip_activity_single_call);
		Log.i("AudioPlugin","savedInstanceState != null="+(savedInstanceState != null)+",,,RongCallClient.getInstance() == null"+(RongCallClient.getInstance() == null));
        if (savedInstanceState != null && RongCallClient.getInstance() == null) {
            // 音视频请求权限时，用户在设置页面取消权限，导致应用重启，退出当前activity.
			Log.i("AudioPlugin","音视频请求权限时，用户在设置页面取消权限，导致应用重启，退出当前activity");
            finish();
            return;
        }
        Intent intent = getIntent();
        mLPreviewContainer = (FrameLayout) findViewById(R.id.rc_voip_call_large_preview);
        mSPreviewContainer = (FrameLayout) findViewById(R.id.rc_voip_call_small_preview);
        mButtonContainer = (FrameLayout) findViewById(R.id.rc_voip_btn);
        mUserInfoContainer = (LinearLayout) findViewById(R.id.rc_voip_user_info);

        startForCheckPermissions = intent.getBooleanExtra("checkPermissions", false);
        RongCallAction callAction = RongCallAction.valueOf(intent.getStringExtra("callAction"));

        if (callAction.equals(RongCallAction.ACTION_OUTGOING_CALL)) {
            if (intent.getAction().equals(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO)) {
                mediaType = RongCallCommon.CallMediaType.AUDIO;
            } else {
                mediaType = RongCallCommon.CallMediaType.VIDEO;
            }
        } else if (callAction.equals(RongCallAction.ACTION_INCOMING_CALL)) {
            callSession = intent.getParcelableExtra("callSession");
            mediaType = callSession.getMediaType();
        } else {
            callSession = RongCallClient.getInstance().getCallSession();
            if (callSession != null) {
                mediaType = callSession.getMediaType();
            }
        }
        if (mediaType != null) {
            inflater = LayoutInflater.from(this);
            initView(mediaType, callAction);

            if (requestCallPermissions(mediaType, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)) {
                setupIntent();
            }
        } else {
            RLog.w(TAG, "恢复的瞬间，对方已挂断");
            setShouldShowFloat(false);
            CallFloatBoxView.hideFloatBox();
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        startForCheckPermissions = intent.getBooleanExtra("checkPermissions", false);
        RongCallAction callAction = RongCallAction.valueOf(intent.getStringExtra("callAction"));
        if (callAction == null) {
            return;
        }
        if (callAction.equals(RongCallAction.ACTION_OUTGOING_CALL)) {
            if (intent.getAction().equals(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO)) {
                mediaType = RongCallCommon.CallMediaType.AUDIO;
            } else {
                mediaType = RongCallCommon.CallMediaType.VIDEO;
            }
        } else if (callAction.equals(RongCallAction.ACTION_INCOMING_CALL)) {
            callSession = intent.getParcelableExtra("callSession");
            mediaType = callSession.getMediaType();
        } else {
            callSession = RongCallClient.getInstance().getCallSession();
            mediaType = callSession.getMediaType();
        }
        super.onNewIntent(intent);

        if (requestCallPermissions(mediaType, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)) {
            setupIntent();
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                boolean permissionGranted;
                if (mediaType == RongCallCommon.CallMediaType.AUDIO) {
                    permissionGranted = PermissionCheckUtil.checkPermissions(this, AUDIO_CALL_PERMISSIONS);
                } else {
                    permissionGranted = PermissionCheckUtil.checkPermissions(this, VIDEO_CALL_PERMISSIONS);

                }
                if (permissionGranted) {
                    if (startForCheckPermissions) {
                        startForCheckPermissions = false;
                        RongCallClient.getInstance().onPermissionGranted();
                    } else {
                        setupIntent();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.rc_permission_grant_needed), Toast.LENGTH_SHORT).show();
                    if (startForCheckPermissions) {
                        startForCheckPermissions = false;
                        RongCallClient.getInstance().onPermissionDenied();
                    } else {
						Log.i("AudioPlugin","--onRequestPermissionsResult--finish");
                        finish();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {

            String[] permissions;
            if (mediaType == RongCallCommon.CallMediaType.AUDIO) {
                permissions = AUDIO_CALL_PERMISSIONS;
            } else {
                permissions = VIDEO_CALL_PERMISSIONS;
            }
            if (PermissionCheckUtil.checkPermissions(this, permissions)) {
                if (startForCheckPermissions) {
                    RongCallClient.getInstance().onPermissionGranted();
                } else {
                    setupIntent();
                }
            } else {
                if (startForCheckPermissions) {
                    RongCallClient.getInstance().onPermissionDenied();
                } else {
					Log.i("AudioPlugin","onActivityResult finish");
                    finish();
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupIntent() {
        RongCallCommon.CallMediaType mediaType;
        Intent intent = getIntent();
        RongCallAction callAction = RongCallAction.valueOf(intent.getStringExtra("callAction"));
//        if (callAction.equals(RongCallAction.ACTION_RESUME_CALL)) {
//            return;
//        }
        if (callAction.equals(RongCallAction.ACTION_INCOMING_CALL)) {
            callSession = intent.getParcelableExtra("callSession");
            mediaType = callSession.getMediaType();
            targetId = callSession.getInviterUserId();
        } else if (callAction.equals(RongCallAction.ACTION_OUTGOING_CALL)) {
            if (intent.getAction().equals(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO)) {
                mediaType = RongCallCommon.CallMediaType.AUDIO;
            } else {
                mediaType = RongCallCommon.CallMediaType.VIDEO;
            }
            Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(intent.getStringExtra("conversationType").toUpperCase(Locale.US));
            targetId = intent.getStringExtra("targetId");

            List<String> userIds = new ArrayList<>();
            userIds.add(targetId);
            RongCallClient.getInstance().startCall(conversationType, targetId, userIds, null, mediaType, null);
        } else { // resume call
            callSession = RongCallClient.getInstance().getCallSession();
            mediaType = callSession.getMediaType();
        }

        if (mediaType.equals(RongCallCommon.CallMediaType.AUDIO)) {
            handFree = false;
        } else if (mediaType.equals(RongCallCommon.CallMediaType.VIDEO)) {
            handFree = true;
        }

        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(targetId);
        if (userInfo != null) {
            if (mediaType.equals(RongCallCommon.CallMediaType.AUDIO) || callAction.equals(RongCallAction.ACTION_INCOMING_CALL)) {
                AsyncImageView userPortrait = (AsyncImageView) mUserInfoContainer.findViewById(R.id.rc_voip_user_portrait);
                if (userPortrait != null && userInfo.getPortraitUri() != null) {
                    userPortrait.setResource(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
                }
                TextView userName = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
                userName.setText(userInfo.getName());
            }
        }
        if(callAction.equals(RongCallAction.ACTION_INCOMING_CALL) && userInfo!=null){
            ImageView iv_icoming_backgroud=(ImageView)mUserInfoContainer.findViewById(R.id.iv_icoming_backgroud);
            iv_icoming_backgroud.setVisibility(View.VISIBLE);
            GlideUtils.showBlurTransformation(SingleCallActivity.this, iv_icoming_backgroud, null != userInfo ? userInfo.getPortraitUri() : null);
        }

        createPowerManager();
        createPickupDetector();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FinLog.i("AudioPlugin","---single activity onResume---");
        if (pickupDetector != null && mediaType.equals(RongCallCommon.CallMediaType.AUDIO)) {
            pickupDetector.register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pickupDetector != null) {
            pickupDetector.unRegister();
        }
    }

    private void initView(RongCallCommon.CallMediaType mediaType, RongCallAction callAction) {
        RelativeLayout buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
        RelativeLayout userInfoLayout = null;
        if (mediaType.equals(RongCallCommon.CallMediaType.AUDIO)|| callAction.equals(RongCallAction.ACTION_INCOMING_CALL)) {
            userInfoLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_audio_call_user_info_incoming, null);
            userInfoLayout.findViewById(R.id.iv_large_preview_Mask).setVisibility(View.VISIBLE);
        } else {
            //单人视频 or 拨打 界面
            userInfoLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_audio_call_user_info, null);
            TextView callInfo = (TextView) userInfoLayout.findViewById(R.id.rc_voip_call_remind_info);
            CallKitUtils.textViewShadowLayer(callInfo,SingleCallActivity.this);
        }

        if (callAction.equals(RongCallAction.ACTION_RESUME_CALL) && CallKitUtils.isDial) {
            try {
                ImageView button = buttonLayout.findViewById(R.id.rc_voip_call_mute_btn);
                button.setEnabled(false);
                userInfoLayout.findViewById(R.id.rc_voip_call_minimize).setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (callAction.equals(RongCallAction.ACTION_OUTGOING_CALL)) {
            userInfoLayout.findViewById(R.id.rc_voip_call_minimize).setVisibility(View.VISIBLE);
            RelativeLayout layout = buttonLayout.findViewById(R.id.rc_voip_call_mute);
            layout.setVisibility(View.VISIBLE);
            ImageView button = buttonLayout.findViewById(R.id.rc_voip_call_mute_btn);
            button.setEnabled(false);
            buttonLayout.findViewById(R.id.rc_voip_handfree).setVisibility(View.VISIBLE);
        }

        if (mediaType.equals(RongCallCommon.CallMediaType.AUDIO)) {
            findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(R.color.rc_voip_background_color));
            mLPreviewContainer.setVisibility(View.GONE);
            mSPreviewContainer.setVisibility(View.GONE);

            if (callAction.equals(RongCallAction.ACTION_INCOMING_CALL)) {
                buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_incoming_button_layout, null);
                ImageView iv_answerBtn = (ImageView) buttonLayout.findViewById(R.id.rc_voip_call_answer_btn);
                iv_answerBtn.setBackground(CallKitUtils.BackgroundDrawable(R.drawable.rc_voip_audio_answer_selector_new, SingleCallActivity.this));

                TextView callInfo = (TextView) userInfoLayout.findViewById(R.id.rc_voip_call_remind_info);
                CallKitUtils.textViewShadowLayer(callInfo,SingleCallActivity.this);
                callInfo.setText(R.string.rc_voip_audio_call_inviting);
                onIncomingCallRinging();
            }
        } else if (mediaType.equals(RongCallCommon.CallMediaType.VIDEO)){
            if (callAction.equals(RongCallAction.ACTION_INCOMING_CALL)) {
                findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(R.color.rc_voip_background_color));
                buttonLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_incoming_button_layout, null);
                ImageView iv_answerBtn = (ImageView) buttonLayout.findViewById(R.id.rc_voip_call_answer_btn);
                iv_answerBtn.setBackground(CallKitUtils.BackgroundDrawable(R.drawable.rc_voip_vedio_answer_selector_new, SingleCallActivity.this));

                TextView callInfo = (TextView) userInfoLayout.findViewById(R.id.rc_voip_call_remind_info);
                CallKitUtils.textViewShadowLayer(callInfo,SingleCallActivity.this);
                callInfo.setText(R.string.rc_voip_video_call_inviting);
                onIncomingCallRinging();
            }
        }
        mButtonContainer.removeAllViews();
        mButtonContainer.addView(buttonLayout);
        mUserInfoContainer.removeAllViews();
        mUserInfoContainer.addView(userInfoLayout);

        if (callAction.equals(RongCallAction.ACTION_INCOMING_CALL)) {
            regisHeadsetPlugReceiver();
            if(BluetoothUtil.hasBluetoothA2dpConnected() || BluetoothUtil.isWiredHeadsetOn(SingleCallActivity.this)){
                HeadsetInfo headsetInfo=new HeadsetInfo(true,HeadsetInfo.HeadsetType.BluetoothA2dp);
                onEventMainThread(headsetInfo);
            }
        }
    }


    @Override
    public void onCallOutgoing(RongCallSession callSession, SurfaceView localVideo) {
        super.onCallOutgoing(callSession, localVideo);
        this.callSession = callSession;
        try {
            UserInfo InviterUserIdInfo = RongContext.getInstance().getUserInfoFromCache(targetId);
            UserInfo SelfUserInfo = RongContext.getInstance().getUserInfoFromCache(callSession.getSelfUserId());
            if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                mLPreviewContainer.setVisibility(View.VISIBLE);
                localVideo.setTag(callSession.getSelfUserId());
                mLPreviewContainer.addView(localVideo);
                if (null!=SelfUserInfo && null!=SelfUserInfo.getName()) {
                    //单人视频
                    TextView callkit_voip_user_name_signleVideo = (TextView) mUserInfoContainer.findViewById(R.id.callkit_voip_user_name_signleVideo);
                    callkit_voip_user_name_signleVideo.setText(SelfUserInfo.getName());
                }
            }else if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)){
                if (null!=InviterUserIdInfo && null!=InviterUserIdInfo.getPortraitUri()) {
                    ImageView iv_icoming_backgroud = mUserInfoContainer.findViewById(R.id.iv_icoming_backgroud);
                    GlideUtils.showBlurTransformation(SingleCallActivity.this, iv_icoming_backgroud, null != InviterUserIdInfo ? InviterUserIdInfo.getPortraitUri() : null);
                    iv_icoming_backgroud.setVisibility(View.VISIBLE);
                    mUserInfoContainer.findViewById(R.id.iv_large_preview_Mask).setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        onOutgoingCallRinging();

        regisHeadsetPlugReceiver();
        if(BluetoothUtil.hasBluetoothA2dpConnected() || BluetoothUtil.isWiredHeadsetOn(this)){
            HeadsetInfo headsetInfo=new HeadsetInfo(true,HeadsetInfo.HeadsetType.BluetoothA2dp);
            onEventMainThread(headsetInfo);
        }
    }

    @Override
    public void onCallConnected(RongCallSession callSession, SurfaceView localVideo) {
        super.onCallConnected(callSession, localVideo);
        this.callSession = callSession;
        FinLog.i(TAG,"onCallConnected----mediaType="+callSession.getMediaType().getValue());
        if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
            findViewById(R.id.rc_voip_call_minimize).setVisibility(View.VISIBLE);
            RelativeLayout btnLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
            ImageView button = btnLayout.findViewById(R.id.rc_voip_call_mute_btn);
            button.setEnabled(true);
            mButtonContainer.removeAllViews();
            mButtonContainer.addView(btnLayout);
        } else {
            // 二人视频通话接通后 mUserInfoContainer 中更换为无头像的布局
            mUserInfoContainer.removeAllViews();
            inflater.inflate(R.layout.rc_voip_video_call_user_info, mUserInfoContainer);
            UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(targetId);
            if (userInfo != null) {
                TextView userName = mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
                userName.setText(userInfo.getName());
//                userName.setShadowLayer(16F, 0F, 2F, getResources().getColor(R.color.rc_voip_reminder_shadow));//callkit_shadowcolor
                CallKitUtils.textViewShadowLayer(userName,SingleCallActivity.this);
            }
            mLocalVideo = localVideo;
            mLocalVideo.setTag(callSession.getSelfUserId());
        }
        TextView tv_rc_voip_call_remind_info = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_call_remind_info);
        CallKitUtils.textViewShadowLayer(tv_rc_voip_call_remind_info,SingleCallActivity.this);
        tv_rc_voip_call_remind_info.setVisibility(View.GONE);
        TextView remindInfo = null;
        if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
            remindInfo = mUserInfoContainer.findViewById(R.id.tv_setupTime);
        } else {
            remindInfo = mUserInfoContainer.findViewById(R.id.tv_setupTime_video);
        }
        if(remindInfo==null){
            remindInfo=tv_rc_voip_call_remind_info;
        }
        setupTime(remindInfo);

        RongCallClient.getInstance().setEnableLocalAudio(!muted);
        View muteV = mButtonContainer.findViewById(R.id.rc_voip_call_mute);
        if (muteV != null) {
            muteV.setSelected(muted);
        }

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager.isWiredHeadsetOn() || BluetoothUtil.hasBluetoothA2dpConnected()) {
            handFree=false;
            RongCallClient.getInstance().setEnableSpeakerphone(false);
            ImageView handFreeV=null;
            if(null!=mButtonContainer){
                handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree_btn);
            }
            if (handFreeV != null) {
                handFreeV.setSelected(false);
                handFreeV.setEnabled(false);
                handFreeV.setClickable(false);
            }
        } else {
            RongCallClient.getInstance().setEnableSpeakerphone(handFree);
            View handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree);
            if (handFreeV != null) {
                handFreeV.setSelected(handFree);
            }
        }
        stopRing();
    }

    @Override
    protected void onDestroy() {
        stopRing();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.setReferenceCounted(false);
            wakeLock.release();
        }
        super.onDestroy();
    }

    @Override
    public void onRemoteUserJoined(final String userId, RongCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
        super.onRemoteUserJoined(userId, mediaType, userType, remoteVideo);
        FinLog.i(TAG,"onRemoteUserJoined userID="+userId+",mediaType="+mediaType.getValue()+",userType="+userId);
        if (mediaType.equals(RongCallCommon.CallMediaType.VIDEO)) {
            findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(android.R.color.transparent));
            mLPreviewContainer.setVisibility(View.VISIBLE);
            mLPreviewContainer.removeAllViews();
            remoteVideo.setTag(userId);

            FinLog.i(TAG,"onRemoteUserJoined mLPreviewContainer.addView(remoteVideo)");
            mLPreviewContainer.addView(remoteVideo);
            mLPreviewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isInformationShow) {
                        hideVideoCallInformation();
                    } else {
                        showVideoCallInformation();
                        handler.sendEmptyMessageDelayed(EVENT_FULL_SCREEN, 5 * 1000);
                    }
                }
            });
            mSPreviewContainer.setVisibility(View.VISIBLE);
            mSPreviewContainer.removeAllViews();
            FinLog.i(TAG,"onRemoteUserJoined mLocalVideo != null="+(mLocalVideo != null));
            if (mLocalVideo != null) {
                mLocalVideo.setZOrderMediaOverlay(true);
                mLocalVideo.setZOrderOnTop(true);
                mSPreviewContainer.addView(mLocalVideo);
            }
            /** 小窗口点击事件 **/
            mSPreviewContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        SurfaceView fromView = (SurfaceView) mSPreviewContainer.getChildAt(0);
                        SurfaceView toView = (SurfaceView) mLPreviewContainer.getChildAt(0);

                        mLPreviewContainer.removeAllViews();
                        mSPreviewContainer.removeAllViews();
                        fromView.setZOrderOnTop(false);
                        fromView.setZOrderMediaOverlay(false);
                        mLPreviewContainer.addView(fromView);
                        toView.setZOrderOnTop(true);
                        toView.setZOrderMediaOverlay(true);
                        mSPreviewContainer.addView(toView);
                        if(null!= fromView.getTag() && !TextUtils.isEmpty(fromView.getTag().toString())){
                            UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(fromView.getTag().toString());
                            TextView userName = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
                            userName.setText(userInfo.getName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mButtonContainer.setVisibility(View.GONE);
            mUserInfoContainer.setVisibility(View.GONE);
        }
    }

    /**
     * 当通话中的某一个参与者切换通话类型，例如由 audio 切换至 video，回调 onMediaTypeChanged。
     *
     * @param userId    切换者的 userId。
     * @param mediaType 切换者，切换后的媒体类型。
     * @param video     切换着，切换后的 camera 信息，如果由 video 切换至 audio，则为 null。
     */
    @Override
    public void onMediaTypeChanged(String userId, RongCallCommon.CallMediaType mediaType, SurfaceView video) {
        if (callSession.getSelfUserId().equals(userId)) {
            showShortToast(getString(R.string.rc_voip_switched_to_audio));
        } else {
            if (callSession.getMediaType() != RongCallCommon.CallMediaType.AUDIO) {
                RongCallClient.getInstance().changeCallMediaType(RongCallCommon.CallMediaType.AUDIO);
                callSession.setMediaType(RongCallCommon.CallMediaType.AUDIO);
                showShortToast(getString(R.string.rc_voip_remote_switched_to_audio));
            }
        }
        initAudioCallView();
        handler.removeMessages(EVENT_FULL_SCREEN);
        mButtonContainer.findViewById(R.id.rc_voip_call_mute).setSelected(muted);
    }

    /** 视频转语音 **/
    private void initAudioCallView() {
        mLPreviewContainer.removeAllViews();
        mLPreviewContainer.setVisibility(View.GONE);
        mSPreviewContainer.removeAllViews();
        mSPreviewContainer.setVisibility(View.GONE);
        //显示全屏底色
        findViewById(R.id.rc_voip_call_information).setBackgroundColor(getResources().getColor(R.color.rc_voip_background_color));
        findViewById(R.id.rc_voip_audio_chat).setVisibility(View.GONE);//隐藏语音聊天按钮

        View userInfoView = inflater.inflate(R.layout.rc_voip_audio_call_user_info_incoming, null);
        TextView tv_rc_voip_call_remind_info = (TextView) userInfoView.findViewById(R.id.rc_voip_call_remind_info);
        tv_rc_voip_call_remind_info.setVisibility(View.GONE);

        TextView timeView = (TextView) userInfoView.findViewById(R.id.tv_setupTime);
        setupTime(timeView);

        mUserInfoContainer.removeAllViews();
        mUserInfoContainer.addView(userInfoView);
        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(targetId);
        if (userInfo != null) {
            TextView userName = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
            userName.setText(userInfo.getName());
            if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)) {
                AsyncImageView userPortrait = (AsyncImageView) mUserInfoContainer.findViewById(R.id.rc_voip_user_portrait);
                if (userPortrait != null) {
                    userPortrait.setAvatar(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
                }
            } else {//单人视频接听layout
                ImageView iv_large_preview = mUserInfoContainer.findViewById(R.id.iv_large_preview);
                iv_large_preview.setVisibility(View.VISIBLE);
                GlideUtils.showBlurTransformation(SingleCallActivity.this, iv_large_preview, null != userInfo ? userInfo.getPortraitUri() : null);
            }
        }
        mUserInfoContainer.setVisibility(View.VISIBLE);
        mUserInfoContainer.findViewById(R.id.rc_voip_call_minimize).setVisibility(View.VISIBLE);

        View button = inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
        mButtonContainer.removeAllViews();
        mButtonContainer.addView(button);
        mButtonContainer.setVisibility(View.VISIBLE);
        // 视频转音频时默认不开启免提
        handFree = false;
        RongCallClient.getInstance().setEnableSpeakerphone(false);
        View handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree);
        handFreeV.setSelected(handFree);

        ImageView iv_large_preview_Mask=(ImageView)userInfoView.findViewById(R.id.iv_large_preview_Mask);
        iv_large_preview_Mask.setVisibility(View.VISIBLE);

        /**视频切换成语音 全是语音界面的ui**/
        ImageView iv_large_preview = mUserInfoContainer.findViewById(R.id.iv_icoming_backgroud);

        if(null!=userInfo && callSession.getMediaType().equals(RongCallCommon.CallMediaType.AUDIO)){
            GlideUtils.showBlurTransformation(SingleCallActivity.this, iv_large_preview, null != userInfo ? userInfo.getPortraitUri() : null);
            iv_large_preview.setVisibility(View.VISIBLE);
        }

        if (pickupDetector != null) {
            pickupDetector.register(this);
        }
    }

    public void onHangupBtnClick(View view) {
        unRegisterHeadsetplugReceiver();
        RongCallSession session = RongCallClient.getInstance().getCallSession();
        if (session == null || isFinishing) {
            finish();
            FinLog.e(TAG+"_挂断单人视频出错 callSession="+(callSession == null)+",isFinishing="+isFinishing);
            return;
        }
        RongCallClient.getInstance().hangUpCall(session.getCallId());
        stopRing();
    }

    public void onReceiveBtnClick(View view) {
        RongCallSession session = RongCallClient.getInstance().getCallSession();
        if (session == null || isFinishing) {
            FinLog.e(TAG+"_接听单人视频出错 callSession="+(callSession == null)+",isFinishing="+isFinishing);
            finish();
            return;
        }
        RongCallClient.getInstance().acceptCall(session.getCallId());
    }

    public void hideVideoCallInformation() {
        isInformationShow = false;
        mUserInfoContainer.setVisibility(View.GONE);
        mButtonContainer.setVisibility(View.GONE);
        findViewById(R.id.rc_voip_audio_chat).setVisibility(View.GONE);
    }

    public void showVideoCallInformation() {
        isInformationShow = true;
        mUserInfoContainer.setVisibility(View.VISIBLE);

        mUserInfoContainer.findViewById(R.id.rc_voip_call_minimize).setVisibility(View.VISIBLE);
        mButtonContainer.setVisibility(View.VISIBLE);
        RelativeLayout btnLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_call_bottom_connected_button_layout, null);
        btnLayout.findViewById(R.id.rc_voip_call_mute).setSelected(muted);
        btnLayout.findViewById(R.id.rc_voip_handfree).setVisibility(View.GONE);
        btnLayout.findViewById(R.id.rc_voip_camera).setVisibility(View.VISIBLE);
        mButtonContainer.removeAllViews();
        mButtonContainer.addView(btnLayout);
        View view = findViewById(R.id.rc_voip_audio_chat);
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RongCallClient.getInstance().changeCallMediaType(RongCallCommon.CallMediaType.AUDIO);
                callSession.setMediaType(RongCallCommon.CallMediaType.AUDIO);
                initAudioCallView();
            }
        });
    }

    public void onHandFreeButtonClick(View view) {
        CallKitUtils.speakerphoneState=!view.isSelected();
        RongCallClient.getInstance().setEnableSpeakerphone(!view.isSelected());//true:打开免提 false:关闭免提
        view.setSelected(!view.isSelected());
        handFree = view.isSelected();
    }

    public void onMuteButtonClick(View view) {
        RongCallClient.getInstance().setEnableLocalAudio(view.isSelected());
        view.setSelected(!view.isSelected());
        muted = view.isSelected();
    }

    @Override
    public void onCallDisconnected(RongCallSession callSession, RongCallCommon.CallDisconnectedReason reason) {
        super.onCallDisconnected(callSession, reason);

        String senderId;
        String extra = "";

        isFinishing = true;
        if (callSession == null) {
            RLog.e(TAG, "onCallDisconnected. callSession is null!");
            postRunnableDelay(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
            return;
        }
        senderId = callSession.getInviterUserId();
        switch (reason) {
            case HANGUP:
            case REMOTE_HANGUP:
                long time = getTime();
                if (time >= 3600) {
                    extra = String.format("%d:%02d:%02d", time / 3600, (time % 3600) / 60, (time % 60));
                } else {
                    extra = String.format("%02d:%02d", (time % 3600) / 60, (time % 60));
                }
                break;
        }

        if (!TextUtils.isEmpty(senderId)) {
            CallSTerminateMessage message = new CallSTerminateMessage();
            message.setReason(reason);
            message.setMediaType(callSession.getMediaType());
            message.setExtra(extra);
            if (senderId.equals(callSession.getSelfUserId())) {
                message.setDirection("MO");
                RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.PRIVATE, callSession.getTargetId(), io.rong.imlib.model.Message.SentStatus.SENT, message, null);
            } else {
                message.setDirection("MT");
                io.rong.imlib.model.Message.ReceivedStatus receivedStatus = new io.rong.imlib.model.Message.ReceivedStatus(0);
                receivedStatus.setRead();
                RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.PRIVATE, callSession.getTargetId(), senderId, receivedStatus, message, null);
            }
        }
        postRunnableDelay(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

    @Override
    public void onRestoreFloatBox(Bundle bundle) {
        super.onRestoreFloatBox(bundle);
        if (bundle == null)
            return;
        muted = bundle.getBoolean("muted");
        handFree = bundle.getBoolean("handFree");

        setShouldShowFloat(true);
        callSession = RongCallClient.getInstance().getCallSession();
        if (callSession == null) {
            setShouldShowFloat(false);
            finish();
            return;
        }
        RongCallCommon.CallMediaType mediaType = callSession.getMediaType();
        RongCallAction callAction = RongCallAction.valueOf(getIntent().getStringExtra("callAction"));
        inflater = LayoutInflater.from(this);
        initView(mediaType, callAction);
        targetId = callSession.getTargetId();
        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(targetId);
        if (userInfo != null) {
//            TextView userName = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
//            userName.setText(userInfo.getName());
            if (mediaType.equals(RongCallCommon.CallMediaType.AUDIO)) {
                AsyncImageView userPortrait = (AsyncImageView) mUserInfoContainer.findViewById(R.id.rc_voip_user_portrait);
                if (userPortrait != null) {
                    userPortrait.setAvatar(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
                }
            } else if (mediaType.equals(RongCallCommon.CallMediaType.VIDEO)){
                if(null!=callAction && callAction.equals(RongCallAction.ACTION_INCOMING_CALL)){
                    ImageView iv_large_preview = mUserInfoContainer.findViewById(R.id.iv_large_preview);
                    iv_large_preview.setVisibility(View.VISIBLE);
                    GlideUtils.showBlurTransformation(SingleCallActivity.this, iv_large_preview, null != userInfo ? userInfo.getPortraitUri() : null);
                }
            }
        }
        SurfaceView localVideo = null;
        SurfaceView remoteVideo = null;
        String remoteUserId = null;
        for (CallUserProfile profile : callSession.getParticipantProfileList()) {
            if (profile.getUserId().equals(RongIMClient.getInstance().getCurrentUserId())) {
                localVideo = profile.getVideoView();
            } else {
                remoteVideo = profile.getVideoView();
                remoteUserId = profile.getUserId();
            }
        }
        if (localVideo != null && localVideo.getParent() != null) {
            ((ViewGroup) localVideo.getParent()).removeView(localVideo);
        }
        onCallOutgoing(callSession, localVideo);
        if(!(boolean)bundle.get("isDial")){
            onCallConnected(callSession, localVideo);
        }
        if (remoteVideo != null && remoteVideo.getParent() != null) {
            ((ViewGroup) remoteVideo.getParent()).removeView(remoteVideo);
            onRemoteUserJoined(remoteUserId, mediaType, 1, remoteVideo);
        }
    }

    @Override
    public String onSaveFloatBoxState(Bundle bundle) {
        super.onSaveFloatBoxState(bundle);
        callSession = RongCallClient.getInstance().getCallSession();
        if (callSession == null) {
            return null;
        }
        bundle.putBoolean("muted", muted);
        bundle.putBoolean("handFree", handFree);
        bundle.putInt("mediaType", callSession.getMediaType().getValue());

        return getIntent().getAction();
    }

    public void onMinimizeClick(View view) {
        super.onMinimizeClick(view);
    }

    public void onSwitchCameraClick(View view) {
        RongCallClient.getInstance().switchCamera();
    }

    @Override
    public void onBackPressed() {
        return;
//        List<CallUserProfile> participantProfiles = callSession.getParticipantProfileList();
//        RongCallCommon.CallStatus callStatus = null;
//        for (CallUserProfile item : participantProfiles) {
//            if (item.getUserId().equals(callSession.getSelfUserId())) {
//                callStatus = item.getCallStatus();
//                break;
//            }
//        }
//        if (callStatus != null && callStatus.equals(RongCallCommon.CallStatus.CONNECTED)) {
//            super.onBackPressed();
//        } else {
//            RongCallClient.getInstance().hangUpCall(callSession.getCallId());
//        }
    }

    public void onEventMainThread(UserInfo userInfo) {
        if (isFinishing()) {
            return;
        }
        if (targetId != null && targetId.equals(userInfo.getUserId())) {
            TextView userName = (TextView) mUserInfoContainer.findViewById(R.id.rc_voip_user_name);
            if (userInfo.getName() != null)
                userName.setText(userInfo.getName());
//            AsyncImageView userPortrait = (AsyncImageView) mUserInfoContainer.findViewById(R.id.rc_voip_user_portrait);
//            if (userPortrait != null && userInfo.getPortraitUri() != null) {
//                userPortrait.setResource(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
//            }
//            ImageView iv_large_preview=mUserInfoContainer.findViewById(R.id.iv_large_preview);
//            GlideUtils.blurTransformation(SingleCallActivity.this,iv_large_preview,null!=userInfo?userInfo.getPortraitUri():null);
        }
    }

//    @Override
//    public void showOnGoingNotification() {
//        Intent intent = new Intent(getIntent().getAction());
//        Bundle bundle = new Bundle();
//        onSaveFloatBoxState(bundle);
//        intent.putExtra("floatbox", bundle);
//        intent.putExtra("callAction", RongCallAction.ACTION_RESUME_CALL.getName());
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        NotificationUtil.showNotification(this, "todo", "coontent", pendingIntent, CALL_NOTIFICATION_ID);
//    }

    public void onEventMainThread(HeadsetInfo headsetInfo) {
        if(headsetInfo==null || !BluetoothUtil.isForground(SingleCallActivity.this)){
            FinLog.i("bugtags","SingleCallActivity 不在前台！");
            return;
        }
        FinLog.i("bugtags","Insert="+headsetInfo.isInsert()+",headsetInfo.getType="+headsetInfo.getType().getValue());
        try {
            if(headsetInfo.isInsert()){
                RongCallClient.getInstance().setEnableSpeakerphone(false);
                ImageView handFreeV=null;
                if(null!=mButtonContainer){
                    handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree_btn);
                }
                if (handFreeV != null) {
                    handFreeV.setSelected(false);
                    handFreeV.setEnabled(false);
                    handFreeV.setClickable(false);
                }
                if(headsetInfo.getType()==HeadsetInfo.HeadsetType.BluetoothA2dp){
                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    am.startBluetoothSco();
                    am.setBluetoothScoOn(true);
                    am.setSpeakerphoneOn(false);
                }
            }else{
                if(headsetInfo.getType()==HeadsetInfo.HeadsetType.WiredHeadset &&
                        BluetoothUtil.hasBluetoothA2dpConnected()){
                    return;
                }
                RongCallClient.getInstance().setEnableSpeakerphone(false);
                ImageView handFreeV = mButtonContainer.findViewById(R.id.rc_voip_handfree_btn);
                if (handFreeV != null) {
                    handFreeV.setSelected(false);
                    handFreeV.setEnabled(true);
                    handFreeV.setClickable(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            FinLog.i("bugtags","SingleCallActivity->onEventMainThread Error="+e.getMessage());
        }
    }
}
