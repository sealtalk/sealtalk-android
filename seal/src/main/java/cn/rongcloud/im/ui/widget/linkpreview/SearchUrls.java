package cn.rongcloud.im.ui.widget.linkpreview;

import java.net.URL;
import java.util.ArrayList;

public class SearchUrls {

    public static final int ALL = 0;
    public static final int FIRST = 1;

    /** It finds urls inside the text and return the matched ones */
    public static ArrayList<String> matches(String text) {
        return matches(text, ALL);
    }

    /** It finds urls inside the text and return the matched ones */
    public static ArrayList<String> matches(String text, int results) {

        ArrayList<String> urls = new ArrayList<String>();

        String[] splitString = (text.split(" "));
        for (String string : splitString) {

            try {
                URL item = new URL(string);
                urls.add(item.toString());
            } catch (Exception e) {
            }

            if (results == FIRST && urls.size() > 0)
                break;
        }

        return urls;
    }

}
