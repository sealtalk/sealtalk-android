package cn.rongcloud.im.net;

import com.google.gson.Gson;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RetrofitUtil {
    private final static MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=UTF-8");

    /**
     * 通过参数 Map 合集
     * @param paramsMap
     * @return
     */
    public static RequestBody createJsonRequest(HashMap<String,Object> paramsMap){
        Gson gson = new Gson();
        String strEntity = gson.toJson(paramsMap);
        return RequestBody.create(MEDIA_TYPE_JSON,strEntity);
    }
}
