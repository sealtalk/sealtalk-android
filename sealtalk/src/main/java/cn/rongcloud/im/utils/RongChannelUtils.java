package cn.rongcloud.im.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** 渠道配置处理 */
public class RongChannelUtils {

    private static final String CHANNEL_FILE_REGEX = "_";
    private static final int CHANNEL_FILE_LENGTH = 2;
    private static final int CHANNEL_FILE_INDEX_HEADER = 0;
    private static final int CHANNEL_FILE_INDEX_CHANNEL_NAME = 1;
    private static String CHANNEL_NAME = "";

    /** 获取当前渠道 */
    public static String getChannelName(Context context) {
        if (!TextUtils.isEmpty(CHANNEL_NAME)) {
            return CHANNEL_NAME;
        }
        CHANNEL_NAME = getChannel(context);
        return CHANNEL_NAME;
    }

    private static String getChannel(Context context) {
        if (context == null) {
            return "";
        }
        ApplicationInfo appInfo = context.getApplicationInfo();
        String sourceDir = appInfo.sourceDir;
        String ret = "";
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith("META-INF/rongchannel")) {
                    ret = entryName;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (TextUtils.isEmpty(ret)) {
            return "";
        }
        String[] split = ret.split(CHANNEL_FILE_REGEX);
        if (split.length >= CHANNEL_FILE_LENGTH) {
            String channelName = split[CHANNEL_FILE_INDEX_CHANNEL_NAME];
            if (channelName != null) {
                return channelName;
            }
        }
        return "";
    }
}
