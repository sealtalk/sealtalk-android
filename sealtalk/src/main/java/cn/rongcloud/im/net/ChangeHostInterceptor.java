package cn.rongcloud.im.net;

import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** @author gusd @Date 2022/03/29 */
public class ChangeHostInterceptor implements Interceptor {
    private static final String TAG = "ChangeHostInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        HttpUrl newBaseUrl = createNewUrl(request.url(), SealTalkUrl.DOMAIN);
        if (newBaseUrl != null) {
            newBaseUrl
                    .newBuilder()
                    .scheme(newBaseUrl.scheme())
                    .host(newBaseUrl.host())
                    .port(newBaseUrl.port())
                    .build();
        } else {
            newBaseUrl = request.url();
        }
        return chain.proceed(builder.url(newBaseUrl).build());
    }

    private HttpUrl createNewUrl(HttpUrl oldUrl, String url) {
        try {
            String urlStr = oldUrl.url().toString();
            HttpUrl newUrl = HttpUrl.parse(url);
            String replace = null;
            if (newUrl != null) {
                replace = urlStr.replace(oldUrl.host(), newUrl.host());
                return HttpUrl.parse(replace);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
