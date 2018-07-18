package cn.rongcloud.im.server.pinyin;

import android.text.TextUtils;

import java.util.Comparator;

import cn.rongcloud.im.db.Friend;

/**
 * Created by tiankui on 16/9/1.
 */
public class FriendNameComparator implements Comparator<Friend> {


    private static FriendNameComparator singleInstance = null;
    private FriendNameComparator() {}
    public static FriendNameComparator getInstance() {
        if (singleInstance == null) {
            synchronized (FriendNameComparator.class) {
                if (singleInstance == null) {
                    singleInstance = new FriendNameComparator();
                }
            }
        }
        return singleInstance;
    }

    public int compare(Friend o1, Friend o2) {
        String nameOne;
        String nameTwo;
        if (!TextUtils.isEmpty(o1.getDisplayName())) {
            nameOne = o1.getDisplayName();
        } else {
            nameOne = o1.getName();
        }

        if (!TextUtils.isEmpty(o2.getDisplayName())) {
            nameTwo = o2.getDisplayName();
        } else {
            nameTwo = o2.getName();
        }
        return nameOne.compareToIgnoreCase(nameTwo);
    }
}
