package io.rong.callkit.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.bailingcloud.bailingvideo.engine.binstack.util.FinLog;

import org.w3c.dom.Text;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import io.rong.common.RLog;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by degnxudong on 2018/8/24.
 */

public class BluetoothUtil {

    private final static  String TAG="BluetoothUtil";

    /**
     * 是否连接了蓝牙耳机
     * @return
     */
    @SuppressLint("WrongConstant")
    public static boolean hasBluetoothA2dpConnected(){
        boolean bool=false;
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mAdapter.isEnabled()){
            int a2dp = mAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
            if (a2dp == BluetoothProfile.STATE_CONNECTED) {
                bool=true;
            }
        }
        return bool;
    }

    /**
     * 是否插入了有线耳机
     * @param context
     * @return
     */
    public static boolean isWiredHeadsetOn(Context context){
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        return audioManager.isWiredHeadsetOn();
    }

    private static String getStyleContent(int styleMajor){
        String content = "未知....";
        switch (styleMajor){
            case BluetoothClass.Device.Major.AUDIO_VIDEO://音频设备
                content = "音配设备";
                break;
            case BluetoothClass.Device.Major.COMPUTER://电脑
                content = "电脑";
                break;
            case BluetoothClass.Device.Major.HEALTH://健康状况
                content = "健康状况";
                break;
            case BluetoothClass.Device.Major.IMAGING://镜像，映像
                content = "镜像";
                break;
            case BluetoothClass.Device.Major.MISC://麦克风
                content = "麦克风";
                break;
            case BluetoothClass.Device.Major.NETWORKING://网络
                content = "网络";
                break;
            case BluetoothClass.Device.Major.PERIPHERAL://外部设备
                content = "外部设备";
                break;
            case BluetoothClass.Device.Major.PHONE://电话
                content = "电话";
                break;
            case BluetoothClass.Device.Major.TOY://玩具
                content = "玩具";
                break;
            case BluetoothClass.Device.Major.UNCATEGORIZED://未知的
                content = "未知的";
                break;
            case BluetoothClass.Device.Major.WEARABLE://穿戴设备
                content = "穿戴设备";
                break;
        }
        return content;
    }

    private static boolean getDeviceClass(int deviceClass){
        boolean bool=false;
        switch (deviceClass){
            case BluetoothClass.Device.AUDIO_VIDEO_CAMCORDER://录像机
                //"录像机";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                //"车载设备";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
                //"蓝牙耳机";
                bool=true;
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER:
                //"扬声器";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE:
                //"麦克风";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO:
                //"打印机";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX:
                //"BOX";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED:
                //"未知的";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VCR:
                //"录像机";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CAMERA:
                //"照相机录像机";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CONFERENCING:
                //"conferencing";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER:
                //"显示器和扬声器";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_GAMING_TOY:
                //"游戏";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR:
                //"显示器";
                break;
            case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                //"可穿戴设备";
                bool=true;
                break;
            case BluetoothClass.Device.PHONE_CELLULAR:
                //"手机";
                break;
            case BluetoothClass.Device.PHONE_CORDLESS:
                //"无线电设备";
                break;
            case BluetoothClass.Device.PHONE_ISDN:
                //"手机服务数据网";
                break;
            case BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY:
                //"手机调节器";
                break;
            case BluetoothClass.Device.PHONE_SMART:
                //"手机卫星";
                break;
            case BluetoothClass.Device.PHONE_UNCATEGORIZED:
                //"未知手机";
                break;
            case BluetoothClass.Device.WEARABLE_GLASSES:
                //"可穿戴眼睛";
                break;
            case BluetoothClass.Device.WEARABLE_HELMET:
                //"可穿戴头盔";
                break;
            case BluetoothClass.Device.WEARABLE_JACKET:
                //"可穿戴上衣";
                break;
            case BluetoothClass.Device.WEARABLE_PAGER:
                //"客串点寻呼机";
                break;
            case BluetoothClass.Device.WEARABLE_UNCATEGORIZED:
                //"未知的可穿戴设备";
                break;
            case BluetoothClass.Device.WEARABLE_WRIST_WATCH:
                //"手腕监听设备";
                break;
            case BluetoothClass.Device.TOY_CONTROLLER:
                //"可穿戴设备";
                break;
            case BluetoothClass.Device.TOY_DOLL_ACTION_FIGURE:
                //"玩具doll_action_figure";
                break;
            case BluetoothClass.Device.TOY_GAME:
                //"游戏";
                break;
            case BluetoothClass.Device.TOY_ROBOT:
                //"玩具遥控器";
                break;
            case BluetoothClass.Device.TOY_UNCATEGORIZED:
                //"玩具未知设备";
                break;
            case BluetoothClass.Device.TOY_VEHICLE:
                //"vehicle";
                break;
            case BluetoothClass.Device.HEALTH_BLOOD_PRESSURE:
                //"健康状态-血压";
                break;
            case BluetoothClass.Device.HEALTH_DATA_DISPLAY:
                //"健康状态数据";
                break;
            case BluetoothClass.Device.HEALTH_GLUCOSE:
                //"健康状态葡萄糖";
                break;
            case BluetoothClass.Device.HEALTH_PULSE_OXIMETER:
                //"健康状态脉搏血氧计";
                break;
            case BluetoothClass.Device.HEALTH_PULSE_RATE:
                //"健康状态脉搏速率";
                break;
            case BluetoothClass.Device.HEALTH_THERMOMETER:
                //"健康状态体温计";
                break;
            case BluetoothClass.Device.HEALTH_WEIGHING:
                //"健康状态体重";
                break;
            case BluetoothClass.Device.HEALTH_UNCATEGORIZED:
                //"未知健康状态设备";
                break;
            case BluetoothClass.Device.COMPUTER_DESKTOP:
                //"电脑桌面";
                break;
            case BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA:
                //"手提电脑或Pad";
                break;
            case BluetoothClass.Device.COMPUTER_LAPTOP:
                //"便携式电脑";
                break;
            case BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA:
                //"微型电脑";
                break;
            case BluetoothClass.Device.COMPUTER_SERVER:
                //"电脑服务";
                break;
            case BluetoothClass.Device.COMPUTER_UNCATEGORIZED:
                //"未知的电脑设备";
                break;
            case BluetoothClass.Device.COMPUTER_WEARABLE:
                ///"可穿戴的电脑";
                break;
        }
        return bool;
    }


    public static boolean isForground(Activity activity){
        return isForground(activity,activity.getClass().getName());
    }

    private static boolean isForground(Context context,String className){
        if(context==null || TextUtils.isEmpty(className)){
            return false;
        }
        ActivityManager activityManager= (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list= activityManager.getRunningTasks(1);
        if(null!=list && list.size()>0){
            ComponentName componentName=list.get(0).topActivity;
            if(className.equals(componentName.getClassName())){
                return true;
            }
        }
        return false;
    }

    /**
     * 是否支持蓝牙
     * @return
     */
    public static boolean isSupportBluetooth(){
        boolean bool=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if(null!=bluetoothAdapter){
            bool=true;
        }
        RLog.i(TAG,"isSupportBluetooth = "+bool);
        return bool;
    }
}
