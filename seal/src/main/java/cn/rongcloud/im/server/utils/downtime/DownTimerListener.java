/*
    ShengDao Android Client, DownTimerListener
    Copyright (c) 2014 ShengDao Tech Company Limited
 */
package cn.rongcloud.im.server.utils.downtime;

/**
 * [倒计时监听类]
 *
 * @author devin.hu
 * @version 1.0
 * @date 2014-12-1
 *
 **/
public interface DownTimerListener {

    /**
     * [倒计时每秒方法]<BR>
     * @param millisUntilFinished
     */
    void onTick(long millisUntilFinished);

    /**
     * [倒计时完成方法]<BR>
     */
    void onFinish();
}

