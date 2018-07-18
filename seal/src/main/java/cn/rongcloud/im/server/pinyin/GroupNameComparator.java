package cn.rongcloud.im.server.pinyin;

import android.text.TextUtils;

import java.util.Comparator;

import cn.rongcloud.im.model.GroupMemberInfo;

/**
 * Created by tiankui on 16/9/7.
 */
public class GroupNameComparator implements Comparator<GroupMemberInfo> {


    private static GroupNameComparator singleInstance = null;
    private GroupNameComparator() {}
    public static GroupNameComparator getInstance() {
        if (singleInstance == null) {
            synchronized (FriendNameComparator.class) {
                if (singleInstance == null) {
                    singleInstance = new GroupNameComparator();
                }
            }
        }
        return singleInstance;
    }

    public int compare(GroupMemberInfo o1, GroupMemberInfo o2) {
        String nameOne;
        String nameTwo;
        if (!TextUtils.isEmpty(o1.getGroupName())) {
            nameOne = o1.getGroupName();
        } else if (!TextUtils.isEmpty(o1.getDisplayName())) {
            nameOne = o1.getDisplayName();
        } else {
            nameOne = o1.getName();
        }

        if (!TextUtils.isEmpty(o2.getGroupName())) {
            nameTwo = o2.getGroupName();
        } else if (!TextUtils.isEmpty(o2.getDisplayName())) {
            nameTwo = o2.getDisplayName();
        } else {
            nameTwo = o2.getName();
        }
        return nameOne.compareToIgnoreCase(nameTwo);
    }
}