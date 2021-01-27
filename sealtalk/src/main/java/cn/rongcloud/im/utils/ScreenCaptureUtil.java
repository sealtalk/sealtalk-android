package cn.rongcloud.im.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import androidx.loader.content.CursorLoader;

import java.io.File;
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

    private static final String[] projection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Video.Media.DURATION
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
            if (screenShotListener != null) {
                screenShotListener.onFaild(e);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    /**
     * 获取相册最新图片
     *
     * @param context
     * @return
     */
    public MediaItem getLastPictureItems(Context context) {
        String selection;
        selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
        Uri queryUri = MediaStore.Files.getContentUri("external");
        CursorLoader cursorLoader = new CursorLoader(
                context,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        Cursor cursor = cursorLoader.loadInBackground();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    MediaItem item = new MediaItem();
                    item.name = cursor.getString(5);
                    item.mediaType = cursor.getInt(3);
                    item.mimeType = cursor.getString(4);
                    item.uri = cursor.getString(1);
                    item.duration = cursor.getInt(6);
                    item.addTime = cursor.getLong(2);
                    item.id = cursor.getString(0);
                    if (item.uri == null) {
                        continue;
                    }
                    File file = new File(item.uri);
                    if (!file.exists() || file.length() == 0) {
                        continue;
                    }
                    int last = item.uri.lastIndexOf("/");
                    if (last == -1) {
                        continue;
                    }
                    return item;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return null;
    }

    public class MediaItem implements Parcelable {
        public String id;
        public String name;
        public int mediaType;
        public String mimeType;
        public String uri;
        public boolean selected;
        public int duration;
        public long addTime;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.name);
            dest.writeInt(this.mediaType);
            dest.writeString(this.mimeType);
            dest.writeString(this.uri);
            dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
            dest.writeInt(this.duration);
            dest.writeLong(this.addTime);
        }

        public MediaItem() {
        }

        protected MediaItem(Parcel in) {
            this.id = in.readString();
            this.name = in.readString();
            this.mediaType = in.readInt();
            this.mimeType = in.readString();
            this.uri = in.readString();
            this.selected = in.readByte() != 0;
            this.duration = in.readInt();
            this.addTime = in.readLong();
        }

        public final Creator<ScreenCaptureUtil.MediaItem> CREATOR = new Creator<MediaItem>() {
            @Override
            public MediaItem createFromParcel(Parcel source) {
                return new ScreenCaptureUtil.MediaItem(source);
            }

            @Override
            public MediaItem[] newArray(int size) {
                return new MediaItem[size];
            }
        };

        @Override
        public String toString() {
            return "MediaItem{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", mediaType=" + mediaType +
                    ", mimeType='" + mimeType + '\'' +
                    ", uri='" + uri + '\'' +
                    ", selected=" + selected +
                    ", duration=" + duration +
                    ", addTime=" + addTime +
                    ", CREATOR=" + CREATOR +
                    '}';
        }
    }

    /**
     * 截屏后的通知回调
     */
    public interface ScreenShotListener {
        void onScreenShotComplete(String data, long dateTaken);

        void onFaild(Exception e);
    }
}
