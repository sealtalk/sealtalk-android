package io.rong.callkit;

import android.view.SurfaceView;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import io.rong.calllib.IRongCallListener;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.common.RLog;

/**
 * Created by jiangecho on 2016/10/27.
 */

public class RongCallProxy implements IRongCallListener {

    private static final String TAG = "RongCallProxy";
    private IRongCallListener mCallListener;
    private Queue<CallDisconnectedInfo> mCachedCallQueue;
    private static RongCallProxy mInstance;

    private RongCallProxy() {
        mCachedCallQueue = new LinkedBlockingQueue<>();
    }

    public static synchronized RongCallProxy getInstance() {
        if (mInstance == null) {
            mInstance = new RongCallProxy();
        }
        return mInstance;
    }

    public void setCallListener(IRongCallListener listener) {
        RLog.d(TAG, "setCallListener listener = " + listener);
        this.mCallListener = listener;
//        if (listener != null) {
//            CallDisconnectedInfo callDisconnectedInfo = mCachedCallQueue.poll();
//            if (callDisconnectedInfo != null) {
//                listener.onCallDisconnected(callDisconnectedInfo.mCallSession, callDisconnectedInfo.mReason);
//            }
//        }
    }

    @Override
    public void onCallOutgoing(RongCallSession callSession, SurfaceView localVideo) {
        if (mCallListener != null) {
            mCallListener.onCallOutgoing(callSession, localVideo);
        }
    }

    @Override
    public void onCallConnected(RongCallSession callSession, SurfaceView localVideo) {
        if (mCallListener != null) {
            mCallListener.onCallConnected(callSession, localVideo);
        }
    }

    @Override
    public void onCallDisconnected(RongCallSession callSession, RongCallCommon.CallDisconnectedReason reason) {
        RLog.d(TAG, "RongCallProxy onCallDisconnected mCallListener = " + mCallListener);
        if (mCallListener != null) {
            mCallListener.onCallDisconnected(callSession, reason);
        } else {
            mCachedCallQueue.offer(new CallDisconnectedInfo(callSession, reason));
        }
    }

    @Override
    public void onRemoteUserRinging(String userId) {
        if (mCallListener != null) {
            mCallListener.onRemoteUserRinging(userId);
        }
    }

    @Override
    public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
        if (mCallListener != null) {
            mCallListener.onRemoteUserJoined(userId, mediaType, userType, remoteVideo);
        }
    }

    @Override
    public void onRemoteUserInvited(String userId, RongCallCommon.CallMediaType mediaType) {
        if (mCallListener != null) {
            mCallListener.onRemoteUserInvited(userId, mediaType);
        }
    }

    @Override
    public void onRemoteUserLeft(String userId, RongCallCommon.CallDisconnectedReason reason) {
        if (mCallListener != null) {
            mCallListener.onRemoteUserLeft(userId, reason);
        }
    }

    @Override
    public void onMediaTypeChanged(String userId, RongCallCommon.CallMediaType mediaType, SurfaceView video) {
        if (mCallListener != null) {
            mCallListener.onMediaTypeChanged(userId, mediaType, video);
        }
    }

    @Override
    public void onError(RongCallCommon.CallErrorCode errorCode) {
        if (mCallListener != null) {
            mCallListener.onError(errorCode);
        }
    }

    @Override
    public void onRemoteCameraDisabled(String userId, boolean disabled) {
        if (mCallListener != null) {
            mCallListener.onRemoteCameraDisabled(userId, disabled);
        }
    }

    @Override
    public void onWhiteBoardURL(String url) {
        if (mCallListener != null) {
            mCallListener.onWhiteBoardURL(url);
        }
    }

    @Override
    public void onNetWorkLossRate(int lossRate) {
        if (mCallListener != null) {
            mCallListener.onNetWorkLossRate(lossRate);
        }
    }

    @Override
    public void onNotifySharingScreen(String userId, boolean isSharing) {
        if (mCallListener != null) {
            mCallListener.onNotifySharingScreen(userId, isSharing);
        }
    }

    @Override
    public void onNotifyDegradeNormalUserToObserver(String userId) {
        if (mCallListener != null) {
            mCallListener.onNotifyDegradeNormalUserToObserver(userId);
        }
    }

    @Override
    public void onNotifyAnswerObserverRequestBecomeNormalUser(String userId, long status) {
        if (mCallListener != null) {
            mCallListener.onNotifyAnswerObserverRequestBecomeNormalUser(userId, status);
        }
    }

    @Override
    public void onNotifyUpgradeObserverToNormalUser() {
        if (mCallListener != null) {
            mCallListener.onNotifyUpgradeObserverToNormalUser();
        }
    }

    @Override
    public void onNotifyHostControlUserDevice(String userId, int dType, int isOpen) {
        if (mCallListener != null) {
            mCallListener.onNotifyHostControlUserDevice(userId, dType, isOpen);
        }
    }

    private static class CallDisconnectedInfo {
        RongCallSession mCallSession;
        RongCallCommon.CallDisconnectedReason mReason;

        public CallDisconnectedInfo(RongCallSession callSession, RongCallCommon.CallDisconnectedReason reason) {
            this.mCallSession = callSession;
            this.mReason = reason;
        }
    }
}
