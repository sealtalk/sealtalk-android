package io.rong.callkit.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.bailingcloud.bailingvideo.engine.binstack.util.FinLog;

import io.rong.imkit.RongContext;

/**
 * Created by Dengxudong on 2018/8/23.
 */

public class HeadsetPlugReceiver extends BroadcastReceiver{

    // 动态注册了监听有线耳机之后 默认会调用一次有限耳机拔出
    public boolean FIRST_HEADSET_PLUG_RECEIVER=false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        HeadsetInfo headsetInfo=null;
        if("android.intent.action.HEADSET_PLUG".equals(action)){
            int state = -1;
            if(FIRST_HEADSET_PLUG_RECEIVER){
                if(intent.hasExtra("state")){
                    state=intent.getIntExtra("state",-1);
                }
                if(state==1){
                    headsetInfo=new HeadsetInfo(true,HeadsetInfo.HeadsetType.WiredHeadset);
                }else if(state==0){
                    headsetInfo=new HeadsetInfo(false,HeadsetInfo.HeadsetType.WiredHeadset);
                }
            }else{
               FIRST_HEADSET_PLUG_RECEIVER=true;
            }
        }else if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
            int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
            switch (state) {
                case BluetoothProfile.STATE_DISCONNECTED:
                    headsetInfo=new HeadsetInfo(false,HeadsetInfo.HeadsetType.BluetoothA2dp);
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    headsetInfo=new HeadsetInfo(true,HeadsetInfo.HeadsetType.BluetoothA2dp);
                    break;
            }
        }
        if(null!=headsetInfo){//onHandFreeButtonClick
            RongContext.getInstance().getEventBus().post(headsetInfo);
        }else{
            FinLog.e("HeadsetPlugReceiver headsetInfo=null !");
        }
    }
}
