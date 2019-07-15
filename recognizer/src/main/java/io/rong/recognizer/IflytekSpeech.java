package io.rong.recognizer;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 集成科大讯飞 SDK 的工具类
 */
public class IflytekSpeech {

    /**
     * 初始化科大讯飞 SDK
     * @param appID 科大讯飞平台上应用的唯一标识，与下载的SDK一一对应
     */
    public static void initSDK (@NonNull Context context, @Nullable String appID) {
        if (SpeechUtility.getUtility() == null) {
            if (appID == null) {
                appID = "5be142c2"; // 默认的、最初的
            }

            String params = SpeechConstant.APPID + "=" + appID;
            SpeechUtility.createUtility(context.getApplicationContext(), params);
        }
    }

    /**
     * 解析科大讯飞语音识别返回的结果
     */
    @NonNull
    public static String parseRecognizeResult(RecognizerResult results) {
        StringBuilder ret = new StringBuilder();
        try {
            JSONTokener jsonTokener = new JSONTokener(results.getResultString());
            JSONObject jsonObject = new JSONObject(jsonTokener);

            JSONArray words = jsonObject.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
//        				如果需要多候选结果，解析数组其他字段
//        				for(int j = 0; j < items.length(); j++)
//        				{
//        					JSONObject obj = items.getJSONObject(j);
//        					ret.append(obj.getString("w"));
//        				}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }
}
