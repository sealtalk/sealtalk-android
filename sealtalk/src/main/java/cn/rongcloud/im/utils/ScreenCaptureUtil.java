package cn.rongcloud.im.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取手机系统截屏的通知工具类
 */
public class ScreenCaptureUtil {

    private static final String[] KEYWORDS = {
            "screenshot", "screen_shot", "screen-shot", "screen shot",
            "screencapture", "screen_capture", "screen-capture", "screen capture",
            "screencap", "screen_cap", "screen-cap", "screen cap"
    };

    /**
     * 读取媒体数据库时需要读取的列
     */
    private static final String[] MEDIA_PROJECTIONS = {
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
    };

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    /**
     * 内部存储器内容观察者
     */
    private ContentObserver mInternalObserver;
    /**
     * 外部存储器内容观察者
     */
    private ContentObserver mExternalObserver;
    private Context mContext;
    private ScreenShotListener screenShotListener;
    private List<Long> handleDataTaken;


    public ScreenCaptureUtil(Context mContext) {
        this.mContext = mContext;
        handleDataTaken = new ArrayList<>();
        init();
    }

    private void init() {
        mHandlerThread = new HandlerThread("Screenshot_Observer");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        // 初始化
        mInternalObserver = new MediaContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, mHandler);
        mExternalObserver = new MediaContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mHandler);
    }

    public void setScreenShotListener(ScreenShotListener listener) {
        screenShotListener = listener;
    }

    public void register() {
        // 添加监听
        mContext.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                false,
                mInternalObserver
        );
        mContext.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                false,
                mExternalObserver
        );
    }

    /**
     * 在生命周期的结束时使用
     */
    public void unRegister() {
        // 注销监听
        mContext.getContentResolver().unregisterContentObserver(mInternalObserver);
        mContext.getContentResolver().unregisterContentObserver(mExternalObserver);
        handleDataTaken.clear();
    }

    /**
     * 处理监听到的资源
     */
    private synchronized void handleMediaRowData(String data, long dateTaken) {
        //有的机型会重复发同一资源处理消息，避免重复过滤
        if (handleDataTaken.contains(dateTaken)) {
            return;
        }
        handleDataTaken.add(dateTaken);
        if (checkScreenShot(data, dateTaken)) {
            Log.d("handleMediaRowData", data + " " + dateTaken);
            if (screenShotListener != null) {
                screenShotListener.onScreenShotComplete(data, dateTaken);
            }
        } else {
            Log.d("handleMediaRowData", "Not screenshot event");
        }
    }

    /**
     * 判断是否是截屏
     */
    private boolean checkScreenShot(String data, long dateTaken) {

        data = data.toLowerCase();
        // 判断图片路径是否含有指定的关键字之一, 如果有, 则认为当前截屏了
        for (String keyWork : KEYWORDS) {
            if (data.contains(keyWork)) {
                return true;
            }
        }
        return false;
    }

    private class MediaContentObserver extends ContentObserver {

        private Uri mContentUri;

        public MediaContentObserver(Uri contentUri, Handler handler) {
            super(handler);
            mContentUri = contentUri;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d("handleMediaRowData", mContentUri.toString());
            handleMediaContentChange(mContentUri);
        }
    }

    /**
     * 处理监听资源变化
     *
     * @param contentUri
     */
    private void handleMediaContentChange(Uri contentUri) {
        Cursor cursor = null;
        try {
            // 数据改变时查询数据库中最后加入的一条数据
            cursor = mContext.getContentResolver().query(
                    contentUri,
                    MEDIA_PROJECTIONS,
                    null,
                    null,
                    MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1"
            );

            if (cursor == null) {
                return;
            }
            if (!cursor.moveToFirst()) {
                return;
            }

            // 获取各列的索引
            int dataIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);

            // 获取行数据
            String data = cursor.getString(dataIndex);
            long dateTaken = cursor.getLong(dateTakenIndex);

            // 处理获取到的第一行数据
            handleMediaRowData(data, dateTaken);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    /**
     * 截屏后的通知回调
     */
    public interface ScreenShotListener {
        void onScreenShotComplete(String data, long dateTaken);
    }
}
