package cn.rongcloud.im.server.pinyin;


import java.util.Comparator;


/**
 *
 * @author
 *
 */
public class GroupPinyinComparator implements Comparator<Group> {


    public static GroupPinyinComparator instance = null;

    public static GroupPinyinComparator getInstance() {
        if (instance == null) {
            instance = new GroupPinyinComparator();
        }
        return instance;
    }

    public int compare(Group o1, Group o2) {
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
