package io.rong.callkit;

import android.content.Context;
import android.content.Intent;

import com.bailingcloud.bailingvideo.engine.binstack.util.FinLog;

import java.util.ArrayList;
import java.util.List;

import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.common.RLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.manager.IExternalModule;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

/**
 * Created by weiqinxiao on 16/8/15.
 */
public class RongCallModule implements IExternalModule {
    private final static String TAG = "RongCallModule";

    private RongCallSession mCallSession;
    private boolean mViewLoaded;
    private Context mContext;

    public RongCallModule() {
        RLog.i(TAG, "Constructor");
    }

    @Override
    public void onInitialized(String appKey) {
        RongIM.registerMessageTemplate(new CallEndMessageItemProvider());
        RongIM.registerMessageTemplate(new MultiCallEndMessageProvider());
    }

    @Override
    public void onConnected(String token) {
        RongCallClient.getInstance().setVoIPCallListener(RongCallProxy.getInstance());
        // 开启音视频日志，如果不需要开启，则去掉下面这句。
        RongCallClient.getInstance().setEnablePrintLog(true);
        RongCallClient.getInstance().setVideoProfile(RongCallCommon.CallVideoProfile.VIDEO_PROFILE_480P);
    }

    @Override
    public void onCreate(final Context context) {
        mContext = context;
        IRongReceivedCallListener callListener = new IRongReceivedCallListener() {
            @Override
            public void onReceivedCall(final RongCallSession callSession) {
                FinLog.d("VoIPReceiver", "onReceivedCall");
                if (mViewLoaded) {
                    FinLog.d("VoIPReceiver", "onReceivedCall->onCreate->mViewLoaded=true");
                    startVoIPActivity(mContext, callSession, false);
                } else {
                    FinLog.d("VoIPReceiver", "onReceivedCall->onCreate->mViewLoaded=false");
                    mCallSession = callSession;
                }
            }

            @Override
            public void onCheckPermission(RongCallSession callSession) {
                FinLog.d("VoIPReceiver", "onCheckPermissions");
                if (mViewLoaded) {
                    startVoIPActivity(mContext, callSession, true);
                }
            }
        };

        RongCallClient.setReceivedCallListener(callListener);
    }

    /**
     * 此方法的目的是，防止 voip 通话界面被会话或者会话列表界面覆盖。
     * 所有要等待会话或者会话列表加载出后，再显示voip 通话界面。
     * <p>
     * 当会话列表或者会话界面加载出来后，此方法会被回调。
     * 如果开发者没有会话或者会话列表界面，只需要将下面的 mViewLoaded 在 onCreate 时设置为 true 即可。
     */
    @Override
    public void onViewCreated() {
        mViewLoaded = true;
        if (mCallSession != null) {
            startVoIPActivity(mContext, mCallSession, false);
        }
    }

    @Override
    public List<IPluginModule> getPlugins(Conversation.ConversationType conversationType) {
        List<IPluginModule> pluginModules = new ArrayList<>();
        try {
            if (RongCallClient.getInstance().isVoIPEnabled(mContext)) {
                pluginModules.add(new AudioPlugin());
                pluginModules.add(new VideoPlugin());
            }
        } catch (Exception e) {
            e.printStackTrace();
            RLog.i(TAG,"getPlugins()->Error :"+e.getMessage());
        }
        return pluginModules;
    }

    @Override
    public void onDisconnected() {

    }

    /**
     * 启动通话界面
     *
     * @param context                  上下文
     * @param callSession              通话实体
     * @param startForCheckPermissions android6.0需要实时获取应用权限。
     *                                 当需要实时获取权限时，设置startForCheckPermissions为true，
     *                                 其它情况下设置为false。
     */
    private void startVoIPActivity(Context context, final RongCallSession callSession, boolean startForCheckPermissions) {
        RLog.d("VoIPReceiver", "startVoIPActivity");
        FinLog.d("VoIPReceiver", "startVoIPActivity");
        String action;
        if (callSession.getConversationType().equals(Conversation.ConversationType.DISCUSSION)
                || callSession.getConversationType().equals(Conversation.ConversationType.GROUP)
                || callSession.getConversationType().equals(Conversation.ConversationType.NONE)) {
            if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                action = RongVoIPIntent.RONG_INTENT_ACTION_VOIP_MULTIVIDEO;
            } else {
                action = RongVoIPIntent.RONG_INTENT_ACTION_VOIP_MULTIAUDIO;
            }
            Intent intent = new Intent(action);
            intent.putExtra("callSession", callSession);
            intent.putExtra("callAction", RongCallAction.ACTION_INCOMING_CALL.getName());
            if (startForCheckPermissions) {
                intent.putExtra("checkPermissions", true);
            } else {
                intent.putExtra("checkPermissions", false);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage(context.getPackageName());
            context.startActivity(intent);
        } else {
            if (callSession.getMediaType().equals(RongCallCommon.CallMediaType.VIDEO)) {
                action = RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEVIDEO;
            } else {
                action = RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO;
            }
            Intent intent = new Intent(action);
            intent.putExtra("callSession", callSession);
            intent.putExtra("callAction", RongCallAction.ACTION_INCOMING_CALL.getName());
            if (startForCheckPermissions) {
                intent.putExtra("checkPermissions", true);
            } else {
                intent.putExtra("checkPermissions", false);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage(context.getPackageName());
            context.startActivity(intent);
        }
        mCallSession = null;
    }
}
