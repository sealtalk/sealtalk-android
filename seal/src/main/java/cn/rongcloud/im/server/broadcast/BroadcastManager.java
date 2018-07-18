/*
    ShengDao Android Client, BroadcastManager
    Copyright (c) 2015 ShengDao Tech Company Limited
*/

package cn.rongcloud.im.server.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


import java.util.HashMap;
import java.util.Map;

import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.utils.json.JsonMananger;

/**
 * [A brief description]
 * <p/>
 * //在任何地方发送广播
 * BroadcastManager.getInstance(mContext).sendBroadcast(FindOrderActivity.ACTION_RECEIVE_MESSAGE);
 * <p/>
 * //页面在oncreate中初始化广播
 * BroadcastManager.getInstance(mContext).addAction(ACTION_RECEIVE_MESSAGE, new BroadcastReceiver(){
 *
 * @author huxinwu
 * @version 1.0
 * @Override public void onReceive(Context arg0, Intent intent) {
 * String command = intent.getAction();
 * if(!TextUtils.isEmpty(command)){
 * if((ACTION_RECEIVE_MESSAGE).equals(command)){
 * //获取json结果
 * String json = intent.getStringExtra("result");
 * //做你该做的事情
 * }
 * }
 * }
 * });
 * <p/>
 * //页面在ondestory销毁广播
 * BroadcastManager.getInstance(mContext).destroy(ACTION_RECEIVE_MESSAGE);
 * @date 2015-9-17
 **/
public class BroadcastManager {

    private Context mContext;
    private static BroadcastManager instance;
    private Map<String, BroadcastReceiver> receiverMap;

    /**
     * 构造方法
     *
     * @param context
     */
    private BroadcastManager(Context context) {
        this.mContext = context.getApplicationContext();
        receiverMap = new HashMap<String, BroadcastReceiver>();
    }

    /**
     * [获取BroadcastManager实例，单例模式实现]
     *
     * @param context
     * @return
     */
    public static BroadcastManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AsyncTaskManager.class) {
                if (instance == null) {
                    instance = new BroadcastManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * 添加
     *
     * @param
     */
    public void addAction(String action, BroadcastReceiver receiver) {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(action);
            mContext.registerReceiver(receiver, filter);
            receiverMap.put(action, receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送广播
     *
     * @param action 唯一码
     */
    public void sendBroadcast(String action) {
        sendBroadcast(action, "");
    }

    /**
     * 发送广播
     *
     * @param action 唯一码
     * @param obj    参数
     */
    public void sendBroadcast(String action, Object obj) {
        try {
            Intent intent = new Intent();
            intent.setAction(action);
            intent.putExtra("result", JsonMananger.beanToJson(obj));
            mContext.sendBroadcast(intent);
        } catch (HttpException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送参数为 String 的数据广播
     *
     * @param action
     * @param s
     */
    public void sendBroadcast(String action, String s) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("String", s);
        mContext.sendBroadcast(intent);
    }


    /**
     * 销毁广播
     *
     * @param action
     */
    public void destroy(String action) {
        if (receiverMap != null) {
            BroadcastReceiver receiver = receiverMap.remove(action);
            if (receiver != null) {
                mContext.unregisterReceiver(receiver);
            }
        }
    }
}
