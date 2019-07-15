package cn.rongcloud.im.utils;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SearchUtils {
    private SoftReference<HashMap<String, ArrayList<String>>> softHanziMap;
    private Context mContext;

    private static SearchUtils instance;

    private SearchUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    public synchronized static void init(Context context) {
        if (instance == null) {
            instance = new SearchUtils(context);
        }
    }

    private synchronized static HashMap<String, ArrayList<String>> getHanziMap() {
        HashMap<String, ArrayList<String>> pinyinHashMap;
        if (instance.softHanziMap == null || (pinyinHashMap = instance.softHanziMap.get()) == null) {
            pinyinHashMap = instance.loadHanziMap();
            instance.softHanziMap = new SoftReference<>(pinyinHashMap);
        }
        return pinyinHashMap;
    }

    private HashMap<String, ArrayList<String>> loadHanziMap() {
        HashMap<String, ArrayList<String>> tmpMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(mContext.getAssets().open("unicode_to_hanyu_pinyin.txt")));
            String line = br.readLine();
            char[] ch = new char[1];
            while (line != null) {
                String[] kv = line.split(" ");
                if (kv.length == 2) {
                    String key = kv[0];
                    String value = kv[1];
                    if (value.startsWith("(") && value.endsWith(")")) {
                        value = value.substring(1, value.length() - 1);
                        String[] vs = value.split(",");
                        ArrayList<String> values = new ArrayList<>();
                        for (String v :
                                vs) {
                            String pinyin = v.substring(0, v.length() - 1);
                            if (!values.contains(pinyin)) {
                                values.add(values.size(), pinyin);
                            }
                        }
                        ch[0] = (char) Integer.parseInt(key, 16);
                        String ks = new String(ch);
                        tmpMap.put(ks, values);
                    }
                }
                line = br.readLine();
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmpMap;
    }

    /**
     * 例1: name = 乐查. return = $lecha|$lezha|$yuecha|$yuezha|$cha|$zha|
     * 例2: name = 一一一乐查. return = $yiyiyilecha|$yiyiyiyuecha|$yiyilecha|$yiyiyuecha|$yilecha|$yiyuecha|$lecha|$yuecha|$cha|
     * 例3: name = 一一一一乐查. return = $yiyiyiyilecha|$yiyiyilecha|$yiyilecha|$yilecha|$lecha|$cha|
     */
    public static String fullSearchableString(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<List<String>> pinyins = hanziToPinyin(name);
        for (int i = 0; i < pinyins.size(); i++) {
            List<String> list = combinations(pinyins.subList(i, pinyins.size()));
            for (String pinyin : list) {
                sb.append("$");
                sb.append(pinyin);
                sb.append("|");
            }
        }
        return sb.toString().toLowerCase();
    }

    /**
     * 例1: name = 乐查. return = [lc, lz, yc, yz]
     * 例2: name = 一一一乐查. return = [yyylc, yyyyc]
     * 例3: name = 一一一一乐查. return = [yyyylc]
     */
    private static List<String> initialKeyword(String name) {
        List<List<String>> lists = new ArrayList<>();
        for (int i = 0; i < name.length(); i++) {
            String ch = name.substring(i, i + 1);
            List<String> pinyins = getHanziMap().get(ch);
            if (pinyins != null && !pinyins.isEmpty()) {
                if (i < 4) { // 前 4 个字支持多音字搜索
                    Set<String> set = new LinkedHashSet<>();
                    for (String pinyin : pinyins) {
                        set.add(pinyin.substring(0, 1));
                    }
                    lists.add(new ArrayList<>(set));
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(pinyins.get(0).substring(0, 1));
                    lists.add(list);
                }
            } else {
                lists.add(new ArrayList<>(Collections.singletonList(ch)));
            }
        }
        return combinations(lists);
    }

    /**
     * 例1: name = 乐查. return = lc|lz|yc|yz|
     * 例2: name = 一一一乐查. return = yyylc|yyyyc|
     * 例3: name = 一一一一乐查. return = yyyylc|
     */
    public static String initialSearchableString(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        if (name.matches("[a-zA-Z]+")) {
            return name;
        }
        StringBuilder sb = new StringBuilder();
        List<String> pinyins = initialKeyword(name);
        for (String pinyin : pinyins) {
            sb.append(pinyin);
            sb.append("|");
        }
        return sb.toString();
    }

    /**
     * 例1: name = 乐查. return = [[le, yue], [cha, zha]]
     * 例2: name = 一一一乐查. return = [[yi], [yi], [yi], [le, yue], [cha]]
     * 例3: name = 一一一一乐查. return = [[yi], [yi], [yi], [yi], [le], [cha]]
     */
    private static List<List<String>> hanziToPinyin(String name) {
        List<List<String>> lists = new ArrayList<>();
        for (int i = 0; i < name.length(); i++) {
            String ch = name.substring(i, i + 1);
            List<String> pinyins = getHanziMap().get(ch);
            if (pinyins != null && !pinyins.isEmpty()) {
                if (i < 4) { // 前 4 个字支持多音字搜索
                    lists.add(pinyins);
                } else {
                    lists.add(new ArrayList<>(pinyins.subList(0, 1)));
                }
            } else {
                lists.add(new ArrayList<>(Collections.singletonList(ch)));
            }
        }
        return lists;
    }

    /**
     * 例1: name = 乐查. return = [lecha, lezha, yuecha, yuezha]
     * 例2: name = 一一一乐查. return = [yiyiyilecha, yiyiyiyuecha]
     * 例2: name = 一一一一乐查. return = [yiyiyiyilecha]
     */
    private static List<String> hanziToPinyinCombination(String name) {
        return combinations(hanziToPinyin(name));
    }

    private static List<String> combinations(List<List<String>> lists) {
        if (lists.size() == 0) {
            return new ArrayList<>();
        }
        if (lists.size() == 1) {
            return lists.get(0);
        }
        int sizeArray[] = new int[lists.size()];
        int counterArray[] = new int[lists.size()];
        int totalCombinationCount = 1;
        for (int i = 0; i < lists.size(); ++i) {
            sizeArray[i] = lists.get(i).size();
            totalCombinationCount *= lists.get(i).size();
        }
        List<String> combinationList = new ArrayList<>(totalCombinationCount);
        StringBuilder sb;
        for (int countdown = totalCombinationCount; countdown > 0; --countdown) {
            sb = new StringBuilder();
            for (int i = 0; i < lists.size(); ++i) {
                sb.append(lists.get(i).get(counterArray[i]));
            }
            combinationList.add(sb.toString());
            for (int incIndex = lists.size() - 1; incIndex >= 0; --incIndex) {
                if (counterArray[incIndex] + 1 < sizeArray[incIndex]) {
                    ++counterArray[incIndex];
                    break;
                }
                counterArray[incIndex] = 0;
            }
        }
        return combinationList;
    }
    //TODO 这个地方有问题，输入"i"时，结果显示不对。
    public static Range rangeOfKeyword(String name, String keyword) {
        Range range;

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(keyword)) {
            return null;
        }
        name = name.toLowerCase();
        keyword = keyword.toLowerCase();

        if (name.contains(keyword)) {
            int index = name.indexOf(keyword);
            range = new Range(index, index + keyword.length() - 1);
            return range;
        }

        List<String> initialKeys = initialKeyword(name);

        for (String key : initialKeys) {
            int index = key.indexOf(keyword);
            if (index != -1) {
                range = new Range(index, index + keyword.length() - 1);
                return range;
            }
        }

        for (int i = 0; i < name.length(); i++) {
            String subName = name.substring(i);
            List<List<String>> pinyinList = hanziToPinyin(subName);
            List<String> pinyinCombinations = combinations(pinyinList);
            for (String pinyin : pinyinCombinations) {
                if (pinyin.startsWith(keyword)) {
                    range = new Range();
                    range.setStart(i);
                    int[] weight = new int[pinyinList.size()];
                    for (int j = 0; j < weight.length; j++) {
                        int mul = 1;
                        for (int k = j + 1; k < weight.length; k++) {
                            mul *= pinyinList.get(k).size();
                        }
                        weight[j] = mul;
                    }
                    int[] indexArray = new int[pinyinList.size()];
                    int index = pinyinCombinations.indexOf(pinyin);
                    for (int j = 0; j < indexArray.length; j++) {
                        indexArray[j] = index / weight[j];
                        index -= indexArray[j] * weight[j];
                    }
                    String[] pinyinArray = new String[pinyinList.size()];
                    for (int j = 0; j < pinyinArray.length; j++) {
                        pinyinArray[j] = pinyinList.get(j).get(indexArray[j]);
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < pinyinList.size(); j++) {
                        sb.append(pinyinArray[j]);
                        if (sb.toString().contains(keyword)) {
                            range.setEnd(j + i);
                            return range;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Range rangeOfNumber(String name, String number) {
        Range range;

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)) {
            return null;
        }
        name = name.toLowerCase();
        number = number.toLowerCase();

        List<String> initialKeys = initialKeyword(name);
        for (int i = 0; i < initialKeys.size(); i++) {
            initialKeys.set(i, PinyinUtil.pinyinToNumber(initialKeys.get(i)));
        }

        for (String key : initialKeys) {
            int index = key.indexOf(number);
            if (index != -1) {
                range = new Range(index, index + number.length() - 1);
                return range;
            }
        }

        for (int i = 0; i < name.length(); i++) {
            String subName = name.substring(i);
            List<List<String>> pinyinList = new ArrayList<>(hanziToPinyin(subName));
            for (List<String> list : pinyinList) {
                for (int j = 0; j < list.size(); j++) {
                    list.set(j, PinyinUtil.pinyinToNumber(list.get(j)));
                }
            }
            List<String> pinyinCombinations = combinations(pinyinList);
            for (String pinyin : pinyinCombinations) {
                if (pinyin.startsWith(number)) {
                    range = new Range();
                    range.setStart(i);
                    int[] weight = new int[pinyinList.size()];
                    for (int j = 0; j < weight.length; j++) {
                        int mul = 1;
                        for (int k = j + 1; k < weight.length; k++) {
                            mul *= pinyinList.get(k).size();
                        }
                        weight[j] = mul;
                    }
                    int[] indexArray = new int[pinyinList.size()];
                    int index = pinyinCombinations.indexOf(pinyin);
                    for (int j = 0; j < indexArray.length; j++) {
                        indexArray[j] = index / weight[j];
                        index -= indexArray[j] * weight[j];
                    }
                    String[] pinyinArray = new String[pinyinList.size()];
                    for (int j = 0; j < pinyinArray.length; j++) {
                        pinyinArray[j] = pinyinList.get(j).get(indexArray[j]);
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < pinyinList.size(); j++) {
                        sb.append(pinyinArray[j]);
                        if (sb.toString().contains(number)) {
                            range.setEnd(j + i);
                            return range;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * attention: the range of '王' in "王明强" is 0, 0
     */
    public static class Range {
        private int start;
        private int end;

        public Range() {

        }

        public Range(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
    }
}
