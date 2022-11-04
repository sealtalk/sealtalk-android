package cn.rongcloud.im.net.proxy;

import android.content.Context;
import android.text.TextUtils;
import cn.rongcloud.im.net.AppProxyManager;
import cn.rongcloud.im.net.HttpClientManager;
import io.rong.imlib.model.RCIMProxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/** @author gusd @Date 2022/09/16 */
public class RetrofitProxyHandler<T> implements InvocationHandler {
    private static final String TAG = "RetrofitProxyHandler";
    private final Context mContext;
    private final Class<T> retrofitServiceClass;
    private T retrofitService;
    private RCIMProxy mProxy;

    public RetrofitProxyHandler(Context context, Class<T> retrofitServiceClass) {
        this.retrofitServiceClass = retrofitServiceClass;
        mContext = context;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        T retrofitServiceInstance = getRetrofitServiceInstance();
        return retrofitServiceInstance
                .getClass()
                .getMethod(method.getName(), method.getParameterTypes())
                .invoke(retrofitService, args);
    }

    private T getRetrofitServiceInstance() {
        RCIMProxy proxy = AppProxyManager.getInstance().getProxy();
        if (retrofitService == null) {
            retrofitService =
                    HttpClientManager.getInstance(mContext)
                            .getClient()
                            .createService(retrofitServiceClass);
        }
        if (!proxyCompare(proxy, mProxy)) {
            retrofitService =
                    HttpClientManager.getInstance(mContext)
                            .getClient()
                            .createService(retrofitServiceClass);
        }
        mProxy = proxy;
        return retrofitService;
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
}
