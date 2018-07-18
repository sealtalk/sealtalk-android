/*
    ShengDao Android Client, DownLoad
    Copyright (c) 2014 ShengDao Tech Company Limited
 */

package cn.rongcloud.im.server.network.async;

/**
 * [A brief description]
 *
 *
 **/
public class AsyncResult {

    /**
     * 请求id
     */
    private int requestCode;
    /**
     * 是否检查网络，true表示检查，false表示不检查
     */
    private boolean isCheckNetwork;
    /**
     * 下载状态
     */
    private int state;
    /**
     * 返回结果
     */
    private Object result;
    /**
     * 处理监听
     */
    private OnDataListener listener;

    public AsyncResult() {
        super();
    }


    public AsyncResult(int requestCode, boolean isCheckNetwork, OnDataListener listener) {
        this.requestCode = requestCode;
        this.isCheckNetwork = isCheckNetwork;
        this.listener = listener;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public boolean isCheckNetwork() {
        return isCheckNetwork;
    }

    public void setCheckNetwork(boolean isCheckNetwork) {
        this.isCheckNetwork = isCheckNetwork;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public OnDataListener getListener() {
        return listener;
    }

    public void setListener(OnDataListener listener) {
        this.listener = listener;
    }

}
