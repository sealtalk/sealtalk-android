package cn.rongcloud.im.server.pinyin;


import java.util.Comparator;

import cn.rongcloud.im.db.Friend;


/**
 *
 * @author
 *
 */
public class PinyinComparator implements Comparator<Friend> {


    public static PinyinComparator instance = null;

    public static PinyinComparator getInstance() {
        if (instance == null) {
            instance = new PinyinComparator();
        }
        return instance;
    }

    public int compare(Friend o1, Friend o2) {
        if (o1.getLetters().equals("@")
                || o2.getLetters().equals("#")) {
            return -1;
        } else if (o1.getLetters().equals("#")
                   || o2.getLetters().equals("@")) {
            return 1;
        } else {
            return o1.getLetters().compareTo(o2.getLetters());
        }
    }

}
