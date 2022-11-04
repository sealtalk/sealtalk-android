package cn.rongcloud.im.net.proxy;

import android.content.Context;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/** @author gusd @Date 2022/09/16 */
public class RetrofitProxyServiceCreator {

    private static final HashMap<String, Object> MAP = new HashMap<>();

    public static synchronized <T> T getRetrofitService(Context context, Class<T> clazz) {
        T serviceProxy = (T) MAP.get(clazz.getName());
        if (serviceProxy == null) {
            RetrofitProxyHandler<T> retrofitProxyHandler =
                    new RetrofitProxyHandler<>(context, clazz);
            serviceProxy =
                    (T)
                            Proxy.newProxyInstance(
                                    clazz.getClassLoader(),
                                    new Class[] {clazz},
                                    retrofitProxyHandler);
            MAP.put(clazz.getName(), serviceProxy);
        }
        return serviceProxy;
    }
}
