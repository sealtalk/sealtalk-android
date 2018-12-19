package io.rong.callkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bailingcloud.bailingvideo.engine.binstack.util.FinLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.rong.callkit.util.BluetoothUtil;
import io.rong.callkit.util.CallKitUtils;
import io.rong.callkit.util.HeadsetInfo;
import io.rong.callkit.util.HeadsetPlugReceiver;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.manager.AudioPlayManager;
import io.rong.imkit.manager.AudioRecordManager;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.utils.NotificationUtil;
import io.rong.imlib.model.UserInfo;

import static io.rong.callkit.CallFloatBoxView.showFB;
import static io.rong.callkit.util.CallKitUtils.isDial;

/**
 * Created by weiqinxiao on 16/3/9.
 */
public class BaseCallActivity extends BaseNoActionBarActivity implements IRongCallListener, PickupDetector.PickupDetectListener {

    private static final String TAG = "BaseCallActivity";
    private static final String MEDIAPLAYERTAG = "MEDIAPLAYERTAG";
    private final static long DELAY_TIME = 1000;
    static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 100;
    static final int REQUEST_CODE_ADD_MEMBER = 110;
    public final int REQUEST_CODE_ADD_MEMBER_NONE=120;
    static final int VOIP_MAX_NORMAL_COUNT = 6;

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;
    private long time = 0;
    private Runnable updateTimeRunnable;

    private boolean shouldRestoreFloat;
    //是否是请求开启悬浮窗权限的过程中
    private boolean checkingOverlaysPermission;
    protected Handler handler;
    /**
     * 表示是否正在挂断
     */
    protected boolean isFinishing;

    protected PickupDetector pickupDetector;
    protected PowerManager powerManager;
    protected PowerManager.WakeLock wakeLock;

    static final String[] VIDEO_CALL_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    static final String[] AUDIO_CALL_PERMISSIONS = {Manifest.permission.RECORD_AUDIO};

    public static final int CALL_NOTIFICATION_ID = 4000;

    /**
     * 判断是拨打界面还是接听界面
     */
    private boolean isIncoming;

    public void setShouldShowFloat(boolean ssf) {
        CallKitUtils.shouldShowFloat = ssf;
    }

    public void showShortToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void postRunnableDelay(Runnable runnable) {
        handler.postDelayed(runnable, DELAY_TIME);
    }

    /**
     * 监听情景模式（Ringer Mode）发生改变后，切换为铃声或振动
     */
    protected final BroadcastReceiver mRingModeReceiver = new BroadcastReceiver() {
        boolean isFirstReceivedBroadcast = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            // 此类广播为 sticky 类型的，首次注册广播便会收到，因此第一次收到的广播不作处理
            if (isFirstReceivedBroadcast) {
                isFirstReceivedBroadcast = false;
                return;
            }
            // 根据 isIncoming 判断只有在接听界面时做铃声和振动的切换，拨打界面不作处理
            if (isIncoming && intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION) && !CallKitUtils.callConnected) {
                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                final int ringMode = am.getRingerMode();
                Log.i(TAG,"Ring mode Receiver mode="+ringMode);
                switch (ringMode) {
                    case AudioManager.RINGER_MODE_NORMAL:
                        stopRing();
                        startRing();
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        stopRing();
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        stopRing();
                        startVibrator();
                        break;
                    default:
                }
            }
        }
    };

    private HeadsetPlugReceiver headsetPlugReceiver=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RLog.d(TAG, "BaseCallActivity onCreate");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        shouldRestoreFloat = true;

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if (!isScreenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            wl.acquire();
            wl.release();
        }
        handler = new Handler();
        RongCallProxy.getInstance().setCallListener(this);

        AudioPlayManager.getInstance().stopPlay();
        AudioRecordManager.getInstance().destroyRecord();
        RongContext.getInstance().getEventBus().register(this);

        initMp();

        //注册 BroadcastReceiver 监听情景模式的切换
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(mRingModeReceiver, filter);
    }

    private void initMp() {
        if(mMediaPlayer==null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    try {
                        if (mp != null) {
                            mp.setLooping(true);
                            mp.start();
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        Log.i(MEDIAPLAYERTAG,"setOnPreparedListener Error!");
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("floatbox");
        if (shouldRestoreFloat && bundle != null) {
            onRestoreFloatBox(bundle);
        }
    }

    public void onOutgoingCallRinging() {
        isIncoming = false;
        try {
            initMp();
            AssetFileDescriptor assetFileDescriptor = getResources().openRawResourceFd(R.raw.voip_outgoing_ring);
            mMediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            assetFileDescriptor.close();
            // 设置 MediaPlayer 播放的声音用途
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes attributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .build();
                mMediaPlayer.setAudioAttributes(attributes);
            } else {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            }
            mMediaPlayer.prepareAsync();
            final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                am.setSpeakerphoneOn(false);
                // 设置此值可在拨打时控制响铃音量
                am.setMode(AudioManager.MODE_IN_COMMUNICATION);
                // 设置拨打时响铃音量默认值
                am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 5, AudioManager.STREAM_VOICE_CALL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception  e1){
            Log.i(MEDIAPLAYERTAG,"---onOutgoingCallRinging Error---"+e1.getMessage());
        }
    }

    public void onIncomingCallRinging() {
        isIncoming = true;
        int ringerMode = NotificationUtil.getRingerMode(this);
        if (ringerMode != AudioManager.RINGER_MODE_SILENT) {
            if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                startVibrator();
            } else {
                if (isVibrateWhenRinging()) {
                    startVibrator();
                }
                startRing();
            }
        }
    }

    public void setupTime(final TextView timeView) {
        try {
            if (updateTimeRunnable != null) {
                handler.removeCallbacks(updateTimeRunnable);
            }
            timeView.setVisibility(View.VISIBLE);
            updateTimeRunnable = new UpdateTimeRunnable(timeView);
            handler.post(updateTimeRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getTime() {
        return time;
    }

    @SuppressLint("MissingPermission")
    protected void stopRing() {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            if (mVibrator != null) {
                mVibrator.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(MEDIAPLAYERTAG,"mMediaPlayer stopRing error="+((e==null)?"null":e.getMessage()));
        }
    }

    protected void startRing() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        try {
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            RLog.e(TAG, "TYPE_RINGTONE not found : " + uri);
            try {
                uri = RingtoneManager.getValidRingtoneUri(this);
                mMediaPlayer.setDataSource(this, uri);
                mMediaPlayer.prepareAsync();
            } catch (IOException e1) {
                e1.printStackTrace();
                RLog.e(TAG, "Ringtone not found: " + uri);
            }catch (IllegalStateException el) {
                el.printStackTrace();
                Log.i(MEDIAPLAYERTAG,"startRing--IllegalStateException");
            }
        }
    }

    protected void startVibrator() {
        if (mVibrator == null) {
            mVibrator = (Vibrator) RongContext.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
        } else {
            mVibrator.cancel();
        }
        mVibrator.vibrate(new long[]{500, 1000}, 0);
    }

    @Override
    public void onCallOutgoing(RongCallSession callProfile, SurfaceView localVideo) {
        CallKitUtils.shouldShowFloat = true;
        CallKitUtils.isDial=true;
    }

    @Override
    public void onRemoteUserRinging(String userId) {

    }

    @Override
    public void onCallDisconnected(RongCallSession callProfile, RongCallCommon.CallDisconnectedReason reason) {
        if (RongCallKit.getCustomerHandlerListener() != null) {
            RongCallKit.getCustomerHandlerListener().onCallDisconnected(callProfile, reason);
        }
        CallKitUtils.callConnected=false;
        CallKitUtils.shouldShowFloat = false;

        String text = null;
        switch (reason) {
            case CANCEL:
                text = getString(R.string.rc_voip_mo_cancel);
                break;
            case REJECT:
                text = getString(R.string.rc_voip_mo_reject);
                break;
            case NO_RESPONSE:
            case BUSY_LINE:
                text = getString(R.string.rc_voip_mo_no_response);
                break;
            case REMOTE_BUSY_LINE:
                text = getString(R.string.rc_voip_mt_busy);
                break;
            case REMOTE_CANCEL:
                text = getString(R.string.rc_voip_mt_cancel);
                break;
            case REMOTE_REJECT:
                text = getString(R.string.rc_voip_mt_reject);
                break;
            case REMOTE_NO_RESPONSE:
                text = getString(R.string.rc_voip_mt_no_response);
                break;
            case REMOTE_HANGUP:
            case HANGUP:
            case NETWORK_ERROR:
            case INIT_VIDEO_ERROR:
                text = getString(R.string.rc_voip_call_terminalted);
                break;
            case OTHER_DEVICE_HAD_ACCEPTED:
                text = getString(R.string.rc_voip_call_other);
                break;
        }
        if (text != null) {
            showShortToast(text);
        }
        stopRing();
        NotificationUtil.clearNotification(this, BaseCallActivity.CALL_NOTIFICATION_ID);
        RongCallProxy.getInstance().setCallListener(null);
    }

    @Override
    public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
        CallKitUtils.isDial=false;
    }

    @Override
    public void onRemoteUserInvited(String userId, RongCallCommon.CallMediaType mediaType) {
        if (RongCallKit.getCustomerHandlerListener() != null) {
            RongCallKit.getCustomerHandlerListener().onRemoteUserInvited(userId, mediaType);
        }
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
    public void onCallConnected(RongCallSession callProfile, SurfaceView localVideo) {
        if (RongCallKit.getCustomerHandlerListener() != null) {
            RongCallKit.getCustomerHandlerListener().onCallConnected(callProfile, localVideo);
        }
        CallKitUtils.callConnected=true;
        CallKitUtils.shouldShowFloat = true;
        CallKitUtils.isDial=false;
        AudioRecordManager.getInstance().destroyRecord();
    }


    @Override
    protected void onPause() {
        if (CallKitUtils.shouldShowFloat && !checkingOverlaysPermission) {
            Bundle bundle = new Bundle();
            String action = onSaveFloatBoxState(bundle);
            if (checkDrawOverlaysPermission(true)) {
                if (action != null) {
                    bundle.putString("action", action);
                    showFB(getApplicationContext(),bundle);
                    int mediaType = bundle.getInt("mediaType");
                    showOnGoingNotification(getString(R.string.rc_call_on_going),
                            mediaType == RongCallCommon.CallMediaType.AUDIO.getValue()
                                    ? getString(R.string.rc_audio_call_on_going) : getString(R.string.rc_video_call_on_going));
                    if (!isFinishing()) {
                        finish();
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.rc_voip_float_window_not_allowed), Toast.LENGTH_SHORT).show();
            }
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RLog.d(TAG, "BaseCallActivity onResume");
        try {
            RongCallSession session = RongCallClient.getInstance().getCallSession();
            if (session != null) {
                RongCallProxy.getInstance().setCallListener(this);
                if (shouldRestoreFloat) {
                    CallFloatBoxView.hideFloatBox();
                    NotificationUtil.clearNotification(this, BaseCallActivity.CALL_NOTIFICATION_ID);
                }
                long activeTime = session != null ? session.getActiveTime() : 0;
                time = activeTime == 0 ? 0 : (System.currentTimeMillis() - activeTime) / 1000;
                shouldRestoreFloat = true;
                if (time > 0) {
                    CallKitUtils.shouldShowFloat = true;
                }
                if (checkingOverlaysPermission) {
                    checkDrawOverlaysPermission(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            RLog.d(TAG, "BaseCallActivity onResume Error : "+e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        shouldRestoreFloat = false;
        if (RongCallKit.getCustomerHandlerListener() != null) {
            List<String> selectedUserIds = RongCallKit.getCustomerHandlerListener().handleActivityResult(requestCode, resultCode, data);
            if (selectedUserIds != null && selectedUserIds.size() > 0)
                onAddMember(selectedUserIds);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            RongContext.getInstance().getEventBus().unregister(this);
            handler.removeCallbacks(updateTimeRunnable);
            unregisterReceiver(mRingModeReceiver);
            if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            // 退出此页面后应设置成正常模式，否则按下音量键无法更改其他音频类型的音量
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                am.setMode(AudioManager.MODE_NORMAL);
            }
            if(mMediaPlayer!=null){
                mMediaPlayer=null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.i(MEDIAPLAYERTAG,"--- onDestroy IllegalStateException---");
        }
        super.onDestroy();
        unRegisterHeadsetplugReceiver();
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

    /** onStart时恢复浮窗 **/
    public void onRestoreFloatBox(Bundle bundle) {

    }

    protected void addMember(ArrayList<String> currentMemberIds) {
        // do your job to add more member
        // after got your new member, call onAddMember
        if (RongCallKit.getCustomerHandlerListener() != null) {
            RongCallKit.getCustomerHandlerListener().addMember(this, currentMemberIds);
        }
    }

    protected void onAddMember(List<String> newMemberIds) {
    }

    /** onPause时保存页面各状态数据 **/
    public String onSaveFloatBoxState(Bundle bundle) {
        return null;
    }

    public void showOnGoingNotification(String title, String content) {
        Intent intent = new Intent(getIntent().getAction());
        Bundle bundle = new Bundle();
        onSaveFloatBoxState(bundle);
        bundle.putBoolean("isDial",isDial);
        intent.putExtra("floatbox", bundle);
        intent.putExtra("callAction", RongCallAction.ACTION_RESUME_CALL.getName());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationUtil.showNotification(this, title, content, pendingIntent, CALL_NOTIFICATION_ID, Notification.DEFAULT_LIGHTS);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @TargetApi(23)
    boolean requestCallPermissions(RongCallCommon.CallMediaType type, int requestCode) {
        String[] permissions = null;
        Log.i(TAG,"BaseActivty requestCallPermissions requestCode="+requestCode);
        if (type.equals(RongCallCommon.CallMediaType.VIDEO) || type.equals(RongCallCommon.CallMediaType.AUDIO)) {
            permissions =CallKitUtils.getCallpermissions();
        }
        boolean result = false;
        if (permissions != null) {
            boolean granted = CallKitUtils.checkPermissions(this, permissions);
            Log.i(TAG,"BaseActivty requestCallPermissions granted="+granted);
            if (granted) {
                result = true;
            } else {
                PermissionCheckUtil.requestPermissions(this, permissions, requestCode);
            }
        }
        return result;
    }

    private class UpdateTimeRunnable implements Runnable {
        private TextView timeView;

        public UpdateTimeRunnable(TextView timeView) {
            this.timeView = timeView;
        }

        @Override
        public void run() {
            time++;
            if (time >= 3600) {
                timeView.setText(String.format("%d:%02d:%02d", time / 3600, (time % 3600) / 60, (time % 60)));
            } else {
                timeView.setText(String.format("%02d:%02d", (time % 3600) / 60, (time % 60)));
            }
            handler.postDelayed(this, 1000);
        }
    }

    void onMinimizeClick(View view) {
        if (checkDrawOverlaysPermission(true)) {
            finish();
        } else {
            Toast.makeText(this, getString(R.string.rc_voip_float_window_not_allowed), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkDrawOverlaysPermission(boolean needOpenPermissionSetting) {
        if (Build.BRAND.toLowerCase().contains("xiaomi") || Build.VERSION.SDK_INT >= 23) {
            if (PermissionCheckUtil.canDrawOverlays(this, needOpenPermissionSetting)) {
                checkingOverlaysPermission = false;
                return true;
            } else {
                if (needOpenPermissionSetting && !Build.BRAND.toLowerCase().contains("xiaomi")) {
                    checkingOverlaysPermission = true;
                }
                return false;
            }
        } else {
            checkingOverlaysPermission = false;
            return true;
        }
    }

    protected void createPowerManager() {
        if (powerManager == null) {
            powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
        }
    }

    protected void createPickupDetector() {
        if (pickupDetector == null) {
            pickupDetector = new PickupDetector(this);
        }
    }

    @Override
    public void onPickupDetected(boolean isPickingUp) {
        if (wakeLock == null) {
            RLog.d(TAG, "No PROXIMITY_SCREEN_OFF_WAKE_LOCK");
            return;
        }
        if (isPickingUp && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
        if (!isPickingUp && wakeLock.isHeld()) {
            try {
                wakeLock.setReferenceCounted(false);
                wakeLock.release();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!PermissionCheckUtil.checkPermissions(this, permissions)) {
            PermissionCheckUtil.showRequestPermissionFailedAlter(this, PermissionCheckUtil.getNotGrantedPermissionMsg(this, permissions, grantResults));
        }
    }

    /**
     * 判断系统是否设置了 响铃时振动
     */
    private boolean isVibrateWhenRinging() {
        ContentResolver resolver = getApplicationContext().getContentResolver();
        if (Build.MANUFACTURER.equals("Xiaomi")) {
            return Settings.System.getInt(resolver, "vibrate_in_normal", 0) == 1;
        } else if (Build.MANUFACTURER.equals("smartisan")) {
            return Settings.Global.getInt(resolver, "telephony_vibration_enabled", 0) == 1;
        } else {
            return Settings.System.getInt(resolver, "vibrate_when_ringing", 0) == 1;
        }
    }

    public void openSpeakerphoneNoWiredHeadsetOn(){
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager.isWiredHeadsetOn()) {
            RongCallClient.getInstance().setEnableSpeakerphone(false);
        } else {
            RongCallClient.getInstance().setEnableSpeakerphone(true);
        }
    }

    /**
     * outgoing （initView）incoming处注册
     */
    public void regisHeadsetPlugReceiver(){
        if(BluetoothUtil.isSupportBluetooth()){
            IntentFilter intentFilter=new IntentFilter();
            intentFilter.addAction("android.intent.action.HEADSET_PLUG");
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            headsetPlugReceiver=new HeadsetPlugReceiver();
            registerReceiver(headsetPlugReceiver,intentFilter);
        }
    }

    /**
     * onHangupBtnClick onDestory 处解绑
     */
    public void unRegisterHeadsetplugReceiver(){
        if(headsetPlugReceiver!=null){
            unregisterReceiver(headsetPlugReceiver);
            headsetPlugReceiver=null;
        }
    }
}
