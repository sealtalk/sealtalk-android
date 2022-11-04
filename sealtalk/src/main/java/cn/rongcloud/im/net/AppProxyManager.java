package cn.rongcloud.im.net;

import cn.rongcloud.rtc.api.socks.RCRTCProxy;
import cn.rongcloud.rtc.socks.proxy.Socks5ProxyHelper;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.RCIMProxy;

/** @author gusd @Date 2022/09/16 */
public class AppProxyManager {

    private RCIMProxy mProxy;

    private AppProxyManager() {}

    public static AppProxyManager getInstance() {
        return SingleHolder.INSTANCE;
    }

    private static class SingleHolder {
        private static final AppProxyManager INSTANCE = new AppProxyManager();
    }

    public void setProxy(RCIMProxy proxy) {
        mProxy = proxy;
        RCRTCProxy rtcProxy = null;
        if (proxy != null) {
            rtcProxy =
                    new RCRTCProxy(
                            proxy.getHost(),
                            proxy.getPort(),
                            proxy.getUserName(),
                            proxy.getPassword());
        }
        Socks5ProxyHelper.getInstance().setRCRTCProxy(rtcProxy);
        RongIMClient.getInstance().setProxy(proxy);
    }

    public RCIMProxy getProxy() {
        return mProxy;
    }
}
