package io.rong.callkit;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import io.rong.callkit.util.CallKitUtils;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.calllib.message.CallSTerminateMessage;
import io.rong.common.RLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.utils.NotificationUtil;
import io.rong.imlib.model.Conversation;
import io.rong.message.InformationNotificationMessage;

import static io.rong.callkit.util.CallKitUtils.closeKeyBoard;
import static io.rong.callkit.util.CallKitUtils.isDial;

/**
 * Created by weiqinxiao on 16/3/17.
 */
public class CallFloatBoxView {
    private static Context mContext;
    private static Timer timer;
    private static long mTime;
    private static View mView;
    private static Boolean isShown = false;
    private static WindowManager wm;
    private static Bundle mBundle;
    private static final String TAG = "CallFloatBoxView";
    private static TextView showFBCallTime=null;

    public static void showFB(Context context, Bundle bundle){
        Log.i("audioTag","CallKitUtils.isDial="+CallKitUtils.isDial);
        if(CallKitUtils.isDial){
            CallFloatBoxView.showFloatBoxToCall(context,bundle);
        }else{
            CallFloatBoxView.showFloatBox(context, bundle);
        }
    }

    public static void showFloatBox(Context context, Bundle bundle) {
        if (isShown) {
            return;
        }
        mContext = context;
        isShown = true;
        RongCallSession session = RongCallClient.getInstance().getCallSession();
        long activeTime = session != null ? session.getActiveTime() : 0;
        mTime = activeTime == 0 ? 0 : (System.currentTimeMillis() - activeTime) / 1000;

        mBundle = bundle;
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < 24) {
            type = WindowManager.LayoutParams.TYPE_TOAST;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.type = type;
        params.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        params.format = PixelFormat.TRANSLUCENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        params.x = context.getResources().getDisplayMetrics().widthPixels;
        params.y = 0;

        mView = LayoutInflater.from(context).inflate(R.layout.rc_voip_float_box, null);
        mView.setOnTouchListener(new View.OnTouchListener() {
            float lastX, lastY;
            int oldOffsetX, oldOffsetY;
            int tag = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                float x = event.getX();
                float y = event.getY();
                if (tag == 0) {
                    oldOffsetX = params.x;
                    oldOffsetY = params.y;
                }
                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = x;
                    lastY = y;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    // 减小偏移量,防止过度抖动
                    params.x += (int) (x - lastX) / 3;
                    params.y += (int) (y - lastY) / 3;
                    tag = 1;
                    if (mView != null)
                        wm.updateViewLayout(mView, params);
                } else if (action == MotionEvent.ACTION_UP) {
                    int newOffsetX = params.x;
                    int newOffsetY = params.y;
                    if (Math.abs(oldOffsetX - newOffsetX) <= 20 && Math.abs(oldOffsetY - newOffsetY) <= 20) {
                        onClickToResume();
                    } else {
                        tag = 0;
                    }
                }
                return true;
            }
        });
        wm.addView(mView, params);
        TextView timeV = (TextView) mView.findViewById(R.id.rc_time);
        setupTime(timeV);
        ImageView mediaIconV = (ImageView) mView.findViewById(R.id.rc_voip_media_type);
        RongCallCommon.CallMediaType mediaType = RongCallCommon.CallMediaType.valueOf(bundle.getInt("mediaType"));
        if (mediaType.equals(RongCallCommon.CallMediaType.AUDIO)) {
            mediaIconV.setImageResource(R.drawable.rc_voip_float_audio);
        } else {
            mediaIconV.setImageResource(R.drawable.rc_voip_float_video);
        }
        RongCallClient.getInstance().setVoIPCallListener(new IRongCallListener() {
            @Override
            public void onCallOutgoing(RongCallSession callInfo, SurfaceView localVideo) {

            }

            @Override
            public void onRemoteUserRinging(String userId) {

            }

            @Override
            public void onCallDisconnected(RongCallSession callProfile, RongCallCommon.CallDisconnectedReason reason) {
                String senderId;
                String extra = "";
                senderId = callProfile.getInviterUserId();
                switch (reason) {
                    case HANGUP:
                    case REMOTE_HANGUP:
                        if (mTime >= 3600) {
                            extra = String.format("%d:%02d:%02d", mTime / 3600, (mTime % 3600) / 60, (mTime % 60));
                        } else {
                            extra = String.format("%02d:%02d", (mTime % 3600) / 60, (mTime % 60));
                        }
                        break;
                }

                if (!TextUtils.isEmpty(senderId)) {
                    switch (callProfile.getConversationType()) {
                        case PRIVATE:
                            CallSTerminateMessage callSTerminateMessage = new CallSTerminateMessage();
                            callSTerminateMessage.setReason(reason);
                            callSTerminateMessage.setMediaType(callProfile.getMediaType());
                            callSTerminateMessage.setExtra(extra);
                            if (senderId.equals(callProfile.getSelfUserId())) {
                                callSTerminateMessage.setDirection("MO");
                                RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.PRIVATE, callProfile.getTargetId(),
                                        io.rong.imlib.model.Message.SentStatus.SENT, callSTerminateMessage, null);
                            } else {
                                callSTerminateMessage.setDirection("MT");
                                io.rong.imlib.model.Message.ReceivedStatus receivedStatus = new io.rong.imlib.model.Message.ReceivedStatus(0);
                                RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.PRIVATE, callProfile.getTargetId(),
                                        senderId, receivedStatus, callSTerminateMessage, null);
                            }
                            break;
                        case GROUP:
                            InformationNotificationMessage informationNotificationMessage;
                            if (reason.equals(RongCallCommon.CallDisconnectedReason.NO_RESPONSE)) {
                                informationNotificationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_voip_audio_no_response));
                            } else {
                                informationNotificationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_voip_audio_ended));
                            }

                            if (senderId.equals(callProfile.getSelfUserId())) {
                                RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.GROUP, callProfile.getTargetId(),
                                        io.rong.imlib.model.Message.SentStatus.SENT, informationNotificationMessage, null);
                            } else {
                                io.rong.imlib.model.Message.ReceivedStatus receivedStatus = new io.rong.imlib.model.Message.ReceivedStatus(0);
                                RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.GROUP, callProfile.getTargetId(),
                                        senderId, receivedStatus, informationNotificationMessage, null);
                            }
                            break;
                        default:
                            break;
                    }
                }
                Toast.makeText(mContext, mContext.getString(R.string.rc_voip_call_terminalted), Toast.LENGTH_SHORT).show();

                if (wm != null && mView != null) {
                    wm.removeView(mView);
                    timer.cancel();
                    timer = null;
                    isShown = false;
                    mView = null;
                    mTime = 0;
                }
                NotificationUtil.clearNotification(mContext, BaseCallActivity.CALL_NOTIFICATION_ID);
                RongCallClient.getInstance().setVoIPCallListener(RongCallProxy.getInstance());
            }

            @Override
            public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
                CallKitUtils.isDial=false;
            }

            @Override
            public void onRemoteUserInvited(String userId, RongCallCommon.CallMediaType mediaType) {

            }

            @Override
            public void onRemoteUserLeft(String userId, RongCallCommon.CallDisconnectedReason reason) {

            }

            @Override
            public void onMediaTypeChanged(String userId, RongCallCommon.CallMediaType mediaType, SurfaceView video) {

            }

            @Override
            public void onError(RongCallCommon.CallErrorCode errorCode) {

            }

            @Override
            public void onCallConnected(RongCallSession callInfo, SurfaceView localVideo) {
                CallKitUtils.isDial=false;
            }

            @Override
            public void onRemoteCameraDisabled(String userId, boolean muted) {

            }

            @Override
            public void onWhiteBoardURL(String url) {

            }

            @Override
            public void onNetWorkLossRate(int lossRate) {

            }

            @Override
            public void onNotifySharingScreen(String userId, boolean isSharing) {

            }

            @Override
            public void onNotifyDegradeNormalUserToObserver(String userId) {

            }

            @Override
            public void onNotifyAnswerObserverRequestBecomeNormalUser(String userId, long status) {

            }

            @Override
            public void onNotifyUpgradeObserverToNormalUser() {

            }

            @Override
            public void onNotifyHostControlUserDevice(String userId, int dType, int isOpen) {

            }
        });
    }

    public static void showFloatBoxToCall(Context context, Bundle bundle) {
        if (isShown) {
            return;
        }
        mContext = context;
        isShown = true;

        mBundle = bundle;
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        int type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < 24) {
            type = WindowManager.LayoutParams.TYPE_TOAST;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.type = type;
        params.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        params.format = PixelFormat.TRANSLUCENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        params.x = context.getResources().getDisplayMetrics().widthPixels;
        params.y = 0;

        mView = LayoutInflater.from(context).inflate(R.layout.rc_voip_float_box, null);
        mView.setOnTouchListener(new View.OnTouchListener() {
            float lastX, lastY;
            int oldOffsetX, oldOffsetY;
            int tag = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                float x = event.getX();
                float y = event.getY();
                if (tag == 0) {
                    oldOffsetX = params.x;
                    oldOffsetY = params.y;
                }
                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = x;
                    lastY = y;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    // 减小偏移量,防止过度抖动
                    params.x += (int) (x - lastX) / 3;
                    params.y += (int) (y - lastY) / 3;
                    tag = 1;
                    if (mView != null)
                        wm.updateViewLayout(mView, params);
                } else if (action == MotionEvent.ACTION_UP) {
                    int newOffsetX = params.x;
                    int newOffsetY = params.y;
                    if (Math.abs(oldOffsetX - newOffsetX) <= 20 && Math.abs(oldOffsetY - newOffsetY) <= 20) {
                        onClickToResume();
                    } else {
                        tag = 0;
                    }
                }
                return true;
            }
        });
        wm.addView(mView, params);
        showFBCallTime = (TextView) mView.findViewById(R.id.rc_time);
        showFBCallTime.setVisibility(View.GONE);

        ImageView mediaIconV = (ImageView) mView.findViewById(R.id.rc_voip_media_type);
        RongCallCommon.CallMediaType mediaType = RongCallCommon.CallMediaType.valueOf(bundle.getInt("mediaType"));
        if (mediaType.equals(RongCallCommon.CallMediaType.AUDIO)) {
            mediaIconV.setImageResource(R.drawable.rc_voip_float_audio);
        } else {
            mediaIconV.setImageResource(R.drawable.rc_voip_float_video);
        }
        RongCallClient.getInstance().setVoIPCallListener(new IRongCallListener() {
            @Override
            public void onCallOutgoing(RongCallSession callInfo, SurfaceView localVideo) {

            }

            @Override
            public void onRemoteUserRinging(String userId) {

            }

            @Override
            public void onCallDisconnected(RongCallSession callProfile, RongCallCommon.CallDisconnectedReason reason) {
                String senderId;
                String extra = "";
                senderId = callProfile.getInviterUserId();
                switch (reason) {
                    case HANGUP:
                    case REMOTE_HANGUP:
//                        if (mTime >= 3600) {
//                            extra = String.format("%d:%02d:%02d", mTime / 3600, (mTime % 3600) / 60, (mTime % 60));
//                        } else {
//                            extra = String.format("%02d:%02d", (mTime % 3600) / 60, (mTime % 60));
//                        }
                        break;
                }

                if (!TextUtils.isEmpty(senderId)) {
                    switch (callProfile.getConversationType()) {
                        case PRIVATE:
                            CallSTerminateMessage callSTerminateMessage = new CallSTerminateMessage();
                            callSTerminateMessage.setReason(reason);
                            callSTerminateMessage.setMediaType(callProfile.getMediaType());
                            callSTerminateMessage.setExtra(extra);
                            if (senderId.equals(callProfile.getSelfUserId())) {
                                callSTerminateMessage.setDirection("MO");
                                RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.PRIVATE, callProfile.getTargetId(),
                                        io.rong.imlib.model.Message.SentStatus.SENT, callSTerminateMessage, null);
                            } else {
                                callSTerminateMessage.setDirection("MT");
                                io.rong.imlib.model.Message.ReceivedStatus receivedStatus = new io.rong.imlib.model.Message.ReceivedStatus(0);
                                RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.PRIVATE, callProfile.getTargetId(),
                                        senderId, receivedStatus, callSTerminateMessage, null);
                            }
                            break;
                        case GROUP:
                            InformationNotificationMessage informationNotificationMessage;
                            if (reason.equals(RongCallCommon.CallDisconnectedReason.NO_RESPONSE)) {
                                informationNotificationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_voip_audio_no_response));
                            } else {
                                informationNotificationMessage = InformationNotificationMessage.obtain(mContext.getString(R.string.rc_voip_audio_ended));
                            }

                            if (senderId.equals(callProfile.getSelfUserId())) {
                                RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.GROUP, callProfile.getTargetId(),
                                        io.rong.imlib.model.Message.SentStatus.SENT, informationNotificationMessage, null);
                            } else {
                                io.rong.imlib.model.Message.ReceivedStatus receivedStatus = new io.rong.imlib.model.Message.ReceivedStatus(0);
                                RongIM.getInstance().insertIncomingMessage(Conversation.ConversationType.GROUP, callProfile.getTargetId(),
                                        senderId, receivedStatus, informationNotificationMessage, null);
                            }
                            break;
                        default:
                            break;
                    }
                }
                Toast.makeText(mContext, mContext.getString(R.string.rc_voip_call_terminalted), Toast.LENGTH_SHORT).show();

                if (wm != null && mView != null) {
                    wm.removeView(mView);
                    if(null!=timer){
                        timer.cancel();
                        timer = null;
                    }
                    isShown = false;
                    mView = null;
                    mTime = 0;
                }
                NotificationUtil.clearNotification(mContext, BaseCallActivity.CALL_NOTIFICATION_ID);
                RongCallClient.getInstance().setVoIPCallListener(RongCallProxy.getInstance());
            }

            @Override
            public void onRemoteUserLeft(String userId, RongCallCommon.CallDisconnectedReason reason) {

            }

            @Override
            public void onMediaTypeChanged(String userId, RongCallCommon.CallMediaType mediaType, SurfaceView video) {

            }

            @Override
            public void onError(RongCallCommon.CallErrorCode errorCode) {
            }

            @Override
            public void onCallConnected(RongCallSession callInfo, SurfaceView localVideo) {
                if(CallKitUtils.isDial && isShown){
                    CallFloatBoxView.showFloatBoxToCallTime();
                    CallKitUtils.isDial=false;
                }
            }


            @Override
            public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
                if(CallKitUtils.isDial && isShown){
                    CallFloatBoxView.showFloatBoxToCallTime();
                    CallKitUtils.isDial=false;
                }
            }

            @Override
            public void onRemoteUserInvited(String userId, RongCallCommon.CallMediaType mediaType) {

            }

            @Override
            public void onRemoteCameraDisabled(String userId, boolean muted) {

            }

            @Override
            public void onWhiteBoardURL(String url) {

            }

            @Override
            public void onNetWorkLossRate(int lossRate) {

            }

            @Override
            public void onNotifySharingScreen(String userId, boolean isSharing) {

            }

            @Override
            public void onNotifyDegradeNormalUserToObserver(String userId) {

            }

            @Override
            public void onNotifyAnswerObserverRequestBecomeNormalUser(String userId, long status) {

            }

            @Override
            public void onNotifyUpgradeObserverToNormalUser() {

            }

            @Override
            public void onNotifyHostControlUserDevice(String userId, int dType, int isOpen) {

            }
        });
    }

    /***
     * 调用showFloatBoxToCall 之后 调用该方法设置
     */
    public static void showFloatBoxToCallTime(){
        if(!isShown){
            return;
        }
        RongCallSession session = RongCallClient.getInstance().getCallSession();
        long activeTime = session != null ? session.getActiveTime() : 0;
        mTime = activeTime == 0 ? 0 : (System.currentTimeMillis() - activeTime) / 1000;
//        mView = LayoutInflater.from(context).inflate(R.layout.rc_voip_float_box, null);
//        TextView timeV = (TextView) mView.findViewById(R.id.rc_time);
        if(null!=showFBCallTime){
            setupTime(showFBCallTime);
        }
    }

    public static void hideFloatBox() {
        RongCallClient.getInstance().setVoIPCallListener(RongCallProxy.getInstance());
        if (isShown && null != mView) {
            wm.removeView(mView);
            if(null!=timer){
                timer.cancel();
                timer = null;
            }
            isShown = false;
            mView = null;
            mTime = 0;
            mBundle = null;
            showFBCallTime=null;
        }
    }

    public static Intent getResumeIntent() {
        if (mBundle == null) {
            return null;
        }
        mBundle.putBoolean("isDial",isDial);
        RongCallClient.getInstance().setVoIPCallListener(RongCallProxy.getInstance());
        Intent intent = new Intent(mBundle.getString("action"));
        intent.putExtra("floatbox", mBundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("callAction", RongCallAction.ACTION_RESUME_CALL.getName());

        return intent;
    }

    public static void onClickToResume() {
        //当快速双击悬浮窗时，第一次点击之后会把mBundle置为空，第二次点击的时候出现NPE
        if (mBundle == null) {
            RLog.d(TAG, "onClickToResume mBundle is null");
            return;
        }
        if(mBundle.getInt("mediaType")==RongCallCommon.CallMediaType.VIDEO.getValue() &&
                !isDial){
            RLog.d(TAG, "onClickToResume setEnableLocalVideo(true)");
            RongCallClient.getInstance().setEnableLocalVideo(true);
        }
        mBundle.putBoolean("isDial",isDial);
        RongCallClient.getInstance().setVoIPCallListener(RongCallProxy.getInstance());
        Intent intent = new Intent(mBundle.getString("action"));
        intent.setPackage(mContext.getPackageName());
        intent.putExtra("floatbox", mBundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("callAction", RongCallAction.ACTION_RESUME_CALL.getName());
        mContext.startActivity(intent);
        mBundle = null;
    }

    private static void setupTime(final TextView timeView) {
        final Handler handler = new Handler(Looper.getMainLooper());
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTime++;
                        if (mTime >= 3600) {
                            timeView.setText(String.format("%d:%02d:%02d", mTime / 3600, (mTime % 3600) / 60, (mTime % 60)));
                            timeView.setVisibility(View.VISIBLE);
                        } else {
                            timeView.setText(String.format("%02d:%02d", (mTime % 3600) / 60, (mTime % 60)));
                            timeView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        };

        timer = new Timer();
        timer.schedule(task, 0, 1000);
    }
}
