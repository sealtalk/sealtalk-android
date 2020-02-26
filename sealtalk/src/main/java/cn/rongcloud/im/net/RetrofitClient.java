package cn.rongcloud.im.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.rongcloud.im.common.NetConstant;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

public class RetrofitClient {
    private Context mContext;
    private Retrofit mRetrofit;

    public RetrofitClient(Context context, String baseUrl) {
        mContext = context;

//        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
//                .addInterceptor(new AddHeaderInterceptor(mContext))
//                .addInterceptor(new ReceivedCookiesInterceptor(mContext))
//                .connectTimeout(NetConstant.API_CONNECT_TIME_OUT, TimeUnit.SECONDS)
//                .readTimeout(NetConstant.API_READ_TIME_OUT, TimeUnit.SECONDS)
//                .writeTimeout(NetConstant.API_WRITE_TIME_OUT, TimeUnit.SECONDS);

        /*
         * 当 baseUrl 没有以 "/" 结尾时加入 "/"
         * 防止当 baseUrl 为非纯域名的，如：域名+ path 时，如果不以 "/" 结尾，Retrofit 会抛出异常
         */
        if (!TextUtils.isEmpty(baseUrl)
                && baseUrl.lastIndexOf("/") != baseUrl.length() - 1) {
            baseUrl = baseUrl + "/";
        }
        mRetrofit = new Retrofit.Builder()
                .client(getUnsafeOkHttpClient())
                .baseUrl(baseUrl) //设置网络请求的Url地址
                .addConverterFactory(GsonConverterFactory.create()) //设置数据解析器
                .addCallAdapterFactory(new LiveDataCallAdapterFactory()) //设置请求响应适配 LiveData
                .build();
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                    .addInterceptor(new AddHeaderInterceptor(mContext))
                    .addInterceptor(new ReceivedCookiesInterceptor(mContext))
                    .connectTimeout(NetConstant.API_CONNECT_TIME_OUT, TimeUnit.SECONDS)
                    .readTimeout(NetConstant.API_READ_TIME_OUT, TimeUnit.SECONDS)
                    .writeTimeout(NetConstant.API_WRITE_TIME_OUT, TimeUnit.SECONDS);
            okHttpBuilder.sslSocketFactory(sslSocketFactory);
            okHttpBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return okHttpBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * 接受cookie拦截器
     */
    public class ReceivedCookiesInterceptor implements Interceptor {
        private Context mContext;

        public ReceivedCookiesInterceptor(Context context) {
            mContext = context;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());

            if (!originalResponse.headers("Set-Cookie").isEmpty()) {
                HashSet<String> cookiesSet = new HashSet<>(originalResponse.headers("Set-Cookie"));

                SharedPreferences.Editor config = mContext.getSharedPreferences(NetConstant.API_SP_NAME_NET, MODE_PRIVATE)
                        .edit();
                config.putStringSet(NetConstant.API_SP_KEY_NET_COOKIE_SET, cookiesSet);
                config.apply();
            }

            return originalResponse;
        }
    }

    /**
     * 添加header包含cookie拦截器
     */
    public class AddHeaderInterceptor implements Interceptor {
        private Context mContext;

        public AddHeaderInterceptor(Context context) {
            mContext = context;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request.Builder builder = chain.request().newBuilder();
            SharedPreferences preferences = mContext.getSharedPreferences(NetConstant.API_SP_NAME_NET,
                    Context.MODE_PRIVATE);

            //添加cookie
            HashSet<String> cookieSet = (HashSet<String>) preferences.getStringSet(NetConstant.API_SP_KEY_NET_COOKIE_SET, null);
            if (cookieSet != null) {
                for (String cookie : cookieSet) {
                    builder.addHeader("Cookie", cookie);
                }
            }

            //添加用户登录认证
            String auth = preferences.getString(NetConstant.API_SP_KEY_NET_HEADER_AUTH, null);
            if (auth != null) {
                builder.addHeader("Authorization", auth);
            }

            return chain.proceed(builder.build());
        }
    }

    public <T> T createService(Class<T> service) {
        return mRetrofit.create(service);
    }
}
