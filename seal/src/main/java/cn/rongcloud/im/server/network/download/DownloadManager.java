/*
    ShengDao Android Client, DownloadManager
    Copyright (c) 2014 ShengDao Tech Company Limited
 */

package cn.rongcloud.im.server.network.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cn.rongcloud.im.server.network.http.AsyncHttpClient;
import cn.rongcloud.im.server.network.http.AsyncHttpResponseHandler;
import cn.rongcloud.im.server.network.http.BreakpointHttpResponseHandler;

/**
 * [下载器管理类，支持并发、暂停、继续、删除任务操作以及断点续传]
 *
	DownloadManager downloadMgr = DownloadManager.getInstance();
	downloadMgr.setDownLoadCallback(new DownLoadCallback(){

		@Override
		public void onLoading(String url, int bytesWritten, int totalSize) {
			super.onLoading(url, bytesWritten, totalSize);
		}

		@Override
		public void onSuccess(String url) {
			super.onSuccess(url);
		}

		@Override
		public void onFailure(String url, String strMsg) {
			super.onFailure(url, strMsg);
		}
	});

	//添加下载任务
	downloadMgr.addHandler(url);
 *
 * @author huxinwu
 * @version 1.0
 * @date 2014-3-12
 *
 **/
public class DownloadManager extends Thread {

    private final String tag = DownloadManager.class.getSimpleName();

    private static final int MAX_HANDLER_COUNT = 20;
    private static final int MAX_DOWNLOAD_THREAD_COUNT = 5;
    private static DownloadManager instance;

    private HandlerQueue mhandlerQueue;
    private List<AsyncHttpResponseHandler> mDownloadinghandlers;
    private List<AsyncHttpResponseHandler> mPausinghandlers;
    private AsyncHttpClient asyncHttpClient;
    private Boolean isRunning = false;

    private DownLoadCallback mDownLoadCallback;
    private String rootPath = "";


    /**
     * 得到DownloadManager实例
     * @return
     */
    public static DownloadManager getInstance() {
        return getInstance("");
    }

    /**
     * 得到DownloadManager实例
     * @param rootPath
     * @return
     */
    public static DownloadManager getInstance(String rootPath) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager(rootPath);
                }
            }
        }
        return instance;
    }

    /**
     * 构造方法
     * @param rootPath
     */
    private DownloadManager(String rootPath) {
        if (TextUtils.isEmpty(rootPath)) {
            rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
        }

        this.rootPath = rootPath;
        mhandlerQueue = new HandlerQueue();

        mDownloadinghandlers = new ArrayList<AsyncHttpResponseHandler>();
        mPausinghandlers = new ArrayList<AsyncHttpResponseHandler>();
        asyncHttpClient = AsyncHttpClient.getInstance();

        if (!TextUtils.isEmpty(rootPath)) {
            File rootFile = new File(rootPath);
            if (!rootFile.exists()) {
                rootFile.mkdir();
            }
        }
    }

    /**
     * 得到下载文件的根目录
     * @return
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * 设置下载回调监听事件
     * @param downLoadCallback
     */
    public void setDownLoadCallback(DownLoadCallback downLoadCallback) {
        this.mDownLoadCallback = downLoadCallback;
    }

    /**
     * 开始下载
     */
    private void startManage() {
        isRunning = true;
        this.start();
        if (mDownLoadCallback != null) {
            mDownLoadCallback.sendStartMessage();
        }
    }

    /**
     * 关闭
     */
    @SuppressWarnings("deprecation")
    public void close() {
        isRunning = false;
        pauseAllHandler();
        if (mDownLoadCallback != null) {
            mDownLoadCallback.sendStopMessage();
        }
        this.stop();
    }

    /**
     * 判断线程是否运行
     * @return
     */
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        while (isRunning) {
            BreakpointHttpResponseHandler handler = (BreakpointHttpResponseHandler) mhandlerQueue.poll();
            if (handler != null) {
                mDownloadinghandlers.add(handler);
                handler.setInterrupt(false);
                asyncHttpClient.get(handler.getUrl(), handler);
            }
        }
    }

    /**
     * 浏览器下载
     * @param context
     * @param uriString 下载资源地址
     */
    public void addHandler(Context context, String uriString) {
        if (TextUtils.isEmpty(uriString)) {
            throw new IllegalArgumentException("addHandler uriString is not null.");
        }
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    /**
     * 添加一个任务
     * @param url
     */
    public void addHandler(String url) {
        if (getTotalhandlerCount() >= MAX_HANDLER_COUNT) {
            if (mDownLoadCallback != null) {
                mDownLoadCallback.sendFailureMessage(url, "任务列表已满");
            }
            return;
        }

        if (TextUtils.isEmpty(url) || hasHandler(url)) {
            Log.e(tag, "addHandler url is not null.");
            return;
        }

        broadcastAddHandler(url);
        mhandlerQueue.offer(newAsyncHttpResponseHandler(url));
        if (!isAlive()) {
            startManage();
        }
    }

    /**
     * 发送添加广播
     * @param url
     */
    private void broadcastAddHandler(String url) {
        broadcastAddHandler(url, false);
    }

    /**
     * 发送添加广播
     * @param url
     * @param isInterrupt
     */
    private void broadcastAddHandler(String url, boolean isInterrupt) {
        if (mDownLoadCallback != null) {
            mDownLoadCallback.sendAddMessage(url, isInterrupt);
        }
    }

    public void reBroadcastAddAllhandler() {

        BreakpointHttpResponseHandler handler = null;
        for (int i = 0; i < mDownloadinghandlers.size(); i++) {
            handler = (BreakpointHttpResponseHandler) mDownloadinghandlers.get(i);
            broadcastAddHandler(handler.getUrl(), handler.isInterrupt());
        }
        for (int i = 0; i < mhandlerQueue.size(); i++) {
            handler = (BreakpointHttpResponseHandler) mhandlerQueue.get(i);
            broadcastAddHandler(handler.getUrl());
        }
        for (int i = 0; i < mPausinghandlers.size(); i++) {
            handler = (BreakpointHttpResponseHandler) mPausinghandlers.get(i);
            broadcastAddHandler(handler.getUrl());
        }
    }

    public boolean hasHandler(String url) {

        BreakpointHttpResponseHandler handler;
        for (int i = 0; i < mDownloadinghandlers.size(); i++) {
            handler = (BreakpointHttpResponseHandler) mDownloadinghandlers.get(i);
            if (handler.getUrl().equals(url)) {
                return true;
            }
        }

        for (int i = 0; i < mhandlerQueue.size(); i++) {
            handler = (BreakpointHttpResponseHandler) mhandlerQueue.get(i);
            if (handler.getUrl().equals(url)) {
                return true;
            }
        }

        return false;
    }

    public int getTotalhandlerCount() {
        return mhandlerQueue.size() + mDownloadinghandlers.size() + mPausinghandlers.size();
    }


    /**
     * 根据url删除下载任务
     * @param url
     */
    public synchronized void deleteHandler(String url) {

        BreakpointHttpResponseHandler handler = null;

        for (int i = 0; i < mDownloadinghandlers.size(); i++) {
            handler = (BreakpointHttpResponseHandler) mDownloadinghandlers.get(i);
            if (handler != null && handler.getUrl().equals(url)) {
                File file = handler.getTargetFile();
                if (file.exists()) {
                    file.delete();
                }
                File tempFile = handler.getTempFile();
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                handler.setInterrupt(true);
                completehandler(handler);
                return;
            }
        }

        for (int i = 0; i < mhandlerQueue.size(); i++) {
            handler = (BreakpointHttpResponseHandler) mhandlerQueue.get(i);
            if (handler != null && handler.getUrl().equals(url)) {
                mhandlerQueue.remove(handler);
            }
        }

        for (int i = 0; i < mPausinghandlers.size(); i++) {
            handler = (BreakpointHttpResponseHandler) mPausinghandlers.get(i);
            if (handler != null && handler.getUrl().equals(url)) {
                mPausinghandlers.remove(handler);
            }
        }
    }

    /**
     * 继续下载
     * @param url
     */
    public synchronized void continueHandler(String url) {
        BreakpointHttpResponseHandler handler = null;
        for (int i = 0; i < mPausinghandlers.size(); i++) {
            handler = (BreakpointHttpResponseHandler) mPausinghandlers.get(i);
            if (handler != null && handler.getUrl().equals(url)) {
                mPausinghandlers.remove(handler);
                mhandlerQueue.offer(handler);
            }
        }
    }

    /**
     * 根据Url暂停下载任务
     * @param url
     */
    public synchronized void pauseHandler(String url) {
        BreakpointHttpResponseHandler handler;
        for (int i = 0; i < mDownloadinghandlers.size(); i++) {
            handler = (BreakpointHttpResponseHandler) mDownloadinghandlers.get(i);
            if (handler != null && handler.getUrl().equals(url)) {
                pausehandler(handler);
            }
        }
    }

    /**
     * 暂停所有下载任务
     */
    public synchronized void pauseAllHandler() {

        AsyncHttpResponseHandler handler = null;

        for (int i = 0; i < mhandlerQueue.size(); i++) {
            handler = mhandlerQueue.get(i);
            mhandlerQueue.remove(handler);
            mPausinghandlers.add(handler);
        }

        for (int i = 0; i < mDownloadinghandlers.size(); i++) {
            handler = mDownloadinghandlers.get(i);
            if (handler != null) {
                pausehandler(handler);
            }
        }
    }

    /**
     * 暂停下载handler
     * @param handler
     */
    private synchronized void pausehandler(AsyncHttpResponseHandler handler) {
        BreakpointHttpResponseHandler fileHttpResponseHandler = (BreakpointHttpResponseHandler) handler;
        if (handler != null) {
            fileHttpResponseHandler.setInterrupt(true);
            // move to pausing list
            String url = fileHttpResponseHandler.getUrl();
            mDownloadinghandlers.remove(handler);
            handler = newAsyncHttpResponseHandler(url);
            mPausinghandlers.add(handler);
        }
    }


    /**
     * 完成下载任务
     * @param handler
     */
    private synchronized void completehandler(AsyncHttpResponseHandler handler) {
        if (mDownloadinghandlers.contains(handler)) {
            mDownloadinghandlers.remove(handler);

            if (mDownLoadCallback != null) {
                mDownLoadCallback.sendFinishMessage(((BreakpointHttpResponseHandler) handler).getUrl());
            }
        }
    }


    /**
     * 构造一个BreakpointHttpResponseHandler
     * @param url
     * @return
     */
    private AsyncHttpResponseHandler newAsyncHttpResponseHandler(String url) {

        BreakpointHttpResponseHandler handler = new BreakpointHttpResponseHandler(url, rootPath) {

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                super.onProgress(bytesWritten, totalSize);
                if (mDownLoadCallback != null) {
                    mDownLoadCallback.sendLoadMessage(getUrl(), bytesWritten, totalSize);
                }
            }

            @Override
            public void onSuccess(File file) {
                if (mDownLoadCallback != null) {
                    mDownLoadCallback.sendSuccessMessage(getUrl(), file.getPath());
                }
            }

            public void onFinish() {
                completehandler(this);
            }

            public void onStart() {
                if (mDownLoadCallback != null) {
                    mDownLoadCallback.onStart();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                String message = "";
                if (error != null) {
                    message = error.getMessage();
                }

                if (mDownLoadCallback != null) {
                    mDownLoadCallback.sendFailureMessage(getUrl(), message);
                }
            }
        };

        return handler;
    }


    /**
     * [A brief description]
     *
     * @author huxinwu
     * @version 1.0
     * @date 2014-3-13
     *
     **/
    private class HandlerQueue {

        private Queue<AsyncHttpResponseHandler> handlerQueue;

        public HandlerQueue() {
            handlerQueue = new LinkedList<AsyncHttpResponseHandler>();
        }

        public void offer(AsyncHttpResponseHandler handler) {
            handlerQueue.offer(handler);
        }

        public AsyncHttpResponseHandler get(int position) {
            if (position >= size()) {
                return null;
            }
            return ((LinkedList<AsyncHttpResponseHandler>) handlerQueue).get(position);
        }

        public AsyncHttpResponseHandler poll() {
            AsyncHttpResponseHandler handler = null;
            while (mDownloadinghandlers.size() >= MAX_DOWNLOAD_THREAD_COUNT || (handler = handlerQueue.poll()) == null) {
                try {
                    Thread.sleep(1000); // sleep
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return handler;
        }

        @SuppressWarnings("unused")
        public boolean remove(int position) {
            return handlerQueue.remove(get(position));
        }

        public boolean remove(AsyncHttpResponseHandler handler) {
            return handlerQueue.remove(handler);
        }

        public int size() {
            return handlerQueue.size();
        }
    }

}
