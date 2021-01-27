package cn.rongcloud.im.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luoyanlong on 2018/05/29.
 */
public class PinyinUtil {

    private static Map<String, String> letterMap = new HashMap<>();

    static {
        letterMap.put("a", "2");
        letterMap.put("b", "2");
        letterMap.put("c", "2");
        letterMap.put("d", "3");
        letterMap.put("e", "3");
        letterMap.put("f", "3");
        letterMap.put("g", "4");
        letterMap.put("h", "4");
        letterMap.put("i", "4");
        letterMap.put("j", "5");
        letterMap.put("k", "5");
        letterMap.put("l", "5");
        letterMap.put("m", "6");
        letterMap.put("n", "6");
        letterMap.put("o", "6");
        letterMap.put("p", "7");
        letterMap.put("q", "7");
        letterMap.put("r", "7");
        letterMap.put("s", "7");
        letterMap.put("t", "8");
        letterMap.put("u", "8");
        letterMap.put("v", "8");
        letterMap.put("w", "9");
        letterMap.put("x", "9");
        letterMap.put("y", "9");
        letterMap.put("z", "9");
    }

    public static String pinyinToNumber(String pinyin) {
        if (pinyin == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        String[] strings = splitString(pinyin);
        for (String s : strings) {
            String number = letterMap.get(s);
            if (number == null) {
                result.append(s);
            } else {
                result.append(number);
            }
        }
        return result.toString();
    }

    private static String[] splitString(String s) {
        String[] strings = new String[s.length()];
        for (int i = 0; i < s.length(); i++) {
            strings[i] = s.substring(i, i + 1);
        }
        return strings;
    }

}
