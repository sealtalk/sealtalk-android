package cn.rongcloud.im.security;

import android.content.Context;
import android.text.TextUtils;
import com.ishumei.smantifraud.SmAntiFraud;
import io.rong.common.RLog;

/** 数美SDK */
public class SMSDKUtils {

    private static String devicesId = "";

    public abstract static class Callback implements SmAntiFraud.IServerSmidCallback {
        @Override
        public void onSuccess(String s) {}

        @Override
        public void onError(int i) {}
    }

    /**
     * 初始化数美SDK
     *
     * @param context
     */
    public static void init(Context context, Callback callback) {
        // 1 通用配置项
        SmAntiFraud.SmOption option = new SmAntiFraud.SmOption();
        option.setOrganization("EMfS28KrI7ee3Dxbe0uq"); // 必填，组织标识，邮件中 organization 项
        String appKey =
                "MIIDLzCCAhegAwIBAgIBMDANBgkqhkiG9w0BAQUFADAyMQswCQYDVQQGEwJDTjELMAkGA1UECwwCU00xFjAUBgNVBAMMDWUuaXNodW1laS5jb20wHhcNMjEwMzIyMDY0MDMyWhcNNDEwMzE3MDY0MDMyWjAyMQswCQYDVQQGEwJDTjELMAkGA1UECwwCU00xFjAUBgNVBAMMDWUuaXNodW1laS5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCE+ohyzQwWn3fcx7yPI9kKSaje3z8rowFxywBQ7W87iqUZLtexl3HF/1mlASBYLiTWYZ1KsthCw5YG60olf5jQXocKIMXHArvKPWQzaJQauI51mZGqkH4tA+HYYQXX4/kUzFinGyZXosJaZDFwnRsuO692Y5HtOF4e/bRrP9tE1FrOPMQsjjHs/s9/M9kJlVXvkby0xVFPA/AzdoVoFos/CaGeCfBBennmCQIBGSok8xtlFV5GZ2HHuJojNYkxbxPN2LxGcI2HU/thHnD2FFVrffd7y2yAwkFMuecbPWjG+BbnY/cVfpFGQ1kakbNev5Qh6ATefSIScutTSOe6SHc5AgMBAAGjUDBOMB0GA1UdDgQWBBQp8pOTfRMdE9Liq9WisyKOaEO1EzAfBgNVHSMEGDAWgBQp8pOTfRMdE9Liq9WisyKOaEO1EzAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBQUAA4IBAQA+Vo2Ci1YgssosCEaaJYn4mU2n8Smbbs1zaaUpGBlaUouyDtKFuKllGvasYQTQUVAZObP7LUH2a8/0I+ZlkHqMRuyPl0LYN3T7Qc1wZS5jiDGm66u88Oqb8Kb8rrG+cYlTv5pWJgxQGljaO28mbwSyWLz2HeZ3ehPPBQfuncK3z669HT6NNo1YhQHWdg6jhWdaUjI0q+rWw+ItKCor8dAPas+loaSM6lLsCWS20NIYGeJ8dUM5Wqb78U1hgtQseBPnRsEu70MQn8eczAMWOP8ql8ALIdD7WFRqY5i7Hjf5GEHCM/kk+ZLEQWvoz7uaiBc1GpWH8uoOCQv47ITBuLbC";
        option.setAppId("c9kqb3rdkbb8j"); // 必填，应用标识，登录数美后台应用管理查看，没有合适值，可以写 default
        option.setPublicKey(appKey); // 必填，加密 KEY，邮件中 android_public_key 附件内容
        SmAntiFraud.create(context, option);

        SmAntiFraud.registerServerIdCallback(
                new SmAntiFraud.IServerSmidCallback() {
                    @Override
                    public void onSuccess(String s) {
                        RLog.d("SMSDKUtils", "registerServerIdCallback: onSuccess:" + s);
                        devicesId = s;
                        if (callback != null) {
                            callback.onSuccess(s);
                        }
                    }

                    @Override
                    public void onError(int i) {
                        RLog.e("SMSDKUtils", "registerServerIdCallback: onError:" + i);
                        // errorCode 含义
                        // -1: ERROR_NO_NETWORK，无网络（检查网络是否正常连接）
                        // -2: ERROR_NO_RESPONSE，服务器无响应（检查接入是否有问题）
                        // -3: ERROR_SERVER_RESPONSE，服务器响应错误（检查配置是否填写正确）
                        if (callback != null) {
                            callback.onError(i);
                        }
                    }
                });
    }

    /**
     * 获取数美SDK的devicesId
     *
     * @return devicesId
     */
    public static String getDevicesId() {
        if (!TextUtils.isEmpty(devicesId)) {
            return devicesId;
        }
        return SmAntiFraud.getDeviceId();
    }
}
