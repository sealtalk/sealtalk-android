/*
    ShengDao Android Client, DownLoadCallback
    Copyright (c) 2014 ShengDao Tech Company Limited
 */

package cn.rongcloud.im.server.network.download;

import android.os.Handler;
import android.os.Message;


public class DownLoadCallback extends Handler {

    protected static final int START_MESSAGE = 0;
    protected static final int ADD_MESSAGE = 1;
    protected static final int PROGRESS_MESSAGE = 2;
    protected static final int SUCCESS_MESSAGE = 3;
    protected static final int FAILURE_MESSAGE = 4;
    protected static final int FINISH_MESSAGE = 5;
    protected static final int STOP_MESSAGE = 6;

    public void onStart() {

    }

    public void onAdd(String url, Boolean isInterrupt) {

    }

    public void onLoading(String url, int bytesWritten, int totalSize) {

    }

    public void onSuccess(String url, String filePath) {

    }

    public void onFailure(String url, String strMsg) {

    }

    public void onFinish(String url) {

    }

    public void onStop() {

    }

    @Override
    public void handleMessage(Message msg) {
        Object[] response = null;
        switch (msg.what) {
            case START_MESSAGE:
                onStart();
                break;

            case ADD_MESSAGE:
                response = (Object[]) msg.obj;
                onAdd((String) response[0], (Boolean) response[1]);
                break;

            case PROGRESS_MESSAGE:
                response = (Object[]) msg.obj;
                onLoading((String) response[0], (Integer) response[1], (Integer) response[2]);
                break;

            case SUCCESS_MESSAGE:
                response = (Object[]) msg.obj;
                onSuccess((String) response[0], (String) response[1]);
                break;

            case FAILURE_MESSAGE:
                response = (Object[]) msg.obj;
                onFailure((String) response[0], (String) response[1]);
                break;

            case FINISH_MESSAGE:
                response = (Object[]) msg.obj;
                onFinish((String) response[0]);
                break;

            case STOP_MESSAGE:
                onStop();
                break;
        }
    }

    protected void sendSuccessMessage(String url, String path) {
        sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[] { url, path }));
    }

    protected void sendLoadMessage(String url, int bytesWritten, int totalSize) {
        sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[] { url, bytesWritten, totalSize}));
    }

    protected void sendAddMessage(String url, Boolean isInterrupt) {
        sendMessage(obtainMessage(ADD_MESSAGE, new Object[] { url, isInterrupt }));
    }

    protected void sendFailureMessage(String url, String strMsg) {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[] { url, strMsg }));
    }

    protected void sendStartMessage() {
        sendMessage(obtainMessage(START_MESSAGE, null));
    }

    protected void sendStopMessage() {
        sendMessage(obtainMessage(STOP_MESSAGE, null));
    }

    protected void sendFinishMessage(String url) {
        sendMessage(obtainMessage(FINISH_MESSAGE, new Object[] { url }));
    }
}
