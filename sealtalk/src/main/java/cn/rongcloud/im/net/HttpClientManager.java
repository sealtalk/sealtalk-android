package cn.rongcloud.im.net;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import cn.rongcloud.im.common.NetConstant;
import io.rong.imlib.model.RCIMProxy;

public class HttpClientManager {
    private static final String TAG = "HttpClientManager";
    private static HttpClientManager instance;
    private final RCIMProxy mProxy;
    private Context context;
    private RetrofitClient client;

    private HttpClientManager(Context context, RCIMProxy proxy) {
        this.context = context;
        client = new RetrofitClient(context, SealTalkUrl.DOMAIN, proxy);
        mProxy = proxy;
    }

    public static HttpClientManager getInstance(Context context) {
        RCIMProxy currentProxy = AppProxyManager.getInstance().getProxy();
        if (instance != null && !proxyCompare(currentProxy, instance.mProxy)) {
            instance = null;
        }
        if (instance == null) {
            synchronized (HttpClientManager.class) {
                if (instance == null) {
                    instance = new HttpClientManager(context, currentProxy);
                }
            }
        }
        return instance;
    }

    private static boolean proxyCompare(RCIMProxy proxy1, RCIMProxy proxy2) {
        if (proxy1 == null && proxy2 == null) {
            return true;
        }
        if (proxy1 == null || proxy2 == null) {
            return false;
        }
        return TextUtils.equals(proxy1.getHost(), proxy2.getHost())
                && TextUtils.equals(proxy1.getUserName(), proxy2.getUserName())
                && TextUtils.equals(proxy1.getPassword(), proxy2.getPassword())
                && proxy1.getPort() == proxy2.getPort();
    }

    public RetrofitClient getClient() {
        return client;
    }

    /**
     * 设置用户登录认证
     *
     * @param auth
     */
    public void setAuthHeader(String auth) {
        SharedPreferences.Editor config =
                context.getSharedPreferences(NetConstant.API_SP_NAME_NET, MODE_PRIVATE).edit();
        config.putString(NetConstant.API_SP_KEY_NET_HEADER_AUTH, auth);
        config.commit();
    }

    /**
     * 获取用户登录认证
     *
     * @return
     */
    public String getCurrentAuth() {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(NetConstant.API_SP_NAME_NET, MODE_PRIVATE);
        return sharedPreferences.getString(NetConstant.API_SP_KEY_NET_HEADER_AUTH, null);
    }

    /** 清除包括cookie和登录认证 */
    public void clearRequestCache() {
        SharedPreferences.Editor config =
                context.getSharedPreferences(NetConstant.API_SP_NAME_NET, MODE_PRIVATE).edit();
        config.remove(NetConstant.API_SP_KEY_NET_HEADER_AUTH);
        config.remove(NetConstant.API_SP_KEY_NET_COOKIE_SET);
        config.commit();
    }
}
