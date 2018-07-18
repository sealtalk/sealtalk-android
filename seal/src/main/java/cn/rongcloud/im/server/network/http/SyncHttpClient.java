package cn.rongcloud.im.server.network.http;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;


import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.utils.NLog;

public class SyncHttpClient {

    private final String tag = SyncHttpClient.class.getSimpleName();

    private static final String VERSION = "1.4.4";

    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    private static final int DEFAULT_SOCKET_TIMEOUT = 10 * 1000;
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    private static final String LOG_TAG = "AsyncHttpClient";

    private int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private int timeout = DEFAULT_SOCKET_TIMEOUT;

    private final DefaultHttpClient httpClient;
    private final HttpContext httpContext;
    private final Map<Context, List<WeakReference<Future<?>>>> requestMap;
    private final Map<String, String> clientHeaderMap;
    private boolean isUrlEncodingEnabled = true;

    //add by devin
    private static final String ASSETS_PATH = "assets";
    private static final String ENCODE_UTF8 = "UTF-8";
    private static PersistentCookieStore cookieStore;
    private static SyncHttpClient instance;

    /**
     * get the SyncHttpClient instance
     *
     * @return
     */
    public static SyncHttpClient getInstance(Context context) {
        if (instance == null) {
            synchronized (SyncHttpClient.class) {
                if (instance == null) {
                    instance = new SyncHttpClient();
                }
            }
        }
        cookieStore = new PersistentCookieStore(context);
        instance.setCookieStore(cookieStore);
        return instance;
    }

    /**
     * Creates a new AsyncHttpClient with default constructor arguments values
     */
    public SyncHttpClient() {
        this(false, 80, 443);
    }

    /**
     * Creates a new AsyncHttpClient.
     *
     * @param httpPort non-standard HTTP-only port
     */
    public SyncHttpClient(int httpPort) {
        this(false, httpPort, 443);
    }

    /**
     * Creates a new AsyncHttpClient.
     *
     * @param httpPort  non-standard HTTP-only port
     * @param httpsPort non-standard HTTPS-only port
     */
    public SyncHttpClient(int httpPort, int httpsPort) {
        this(false, httpPort, httpsPort);
    }

    /**
     * Creates new AsyncHttpClient using given params
     *
     * @param fixNoHttpResponseException Whether to fix or not issue, by ommiting SSL verification
     * @param httpPort                   HTTP port to be used, must be greater than 0
     * @param httpsPort                  HTTPS port to be used, must be greater than 0
     */
    public SyncHttpClient(boolean fixNoHttpResponseException, int httpPort, int httpsPort) {
        this(getDefaultSchemeRegistry(fixNoHttpResponseException, httpPort, httpsPort));
    }

    /**
     * Returns default instance of SchemeRegistry
     *
     * @param fixNoHttpResponseException Whether to fix or not issue, by ommiting SSL verification
     * @param httpPort                   HTTP port to be used, must be greater than 0
     * @param httpsPort                  HTTPS port to be used, must be greater than 0
     */
    private static SchemeRegistry getDefaultSchemeRegistry(
            boolean fixNoHttpResponseException, int httpPort, int httpsPort) {
        if (fixNoHttpResponseException) {
            Log.d(LOG_TAG, "Beware! Using the fix is insecure, as it doesn't verify SSL certificates.");
        }

        if (httpPort < 1) {
            httpPort = 80;
            Log.d(LOG_TAG, "Invalid HTTP port number specified, defaulting to 80");
        }

        if (httpsPort < 1) {
            httpsPort = 443;
            Log.d(LOG_TAG, "Invalid HTTPS port number specified, defaulting to 443");
        }

        // Fix to SSL flaw in API < ICS
        // See https://code.google.com/p/android/issues/detail?id=13117
        SSLSocketFactory sslSocketFactory;
        if (fixNoHttpResponseException)
            sslSocketFactory = MySSLSocketFactory.getFixedSocketFactory();
        else
            sslSocketFactory = SSLSocketFactory.getSocketFactory();

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), httpPort));
        schemeRegistry.register(new Scheme("https", sslSocketFactory, httpsPort));

        return schemeRegistry;
    }

    /**
     * Creates a new AsyncHttpClient.
     *
     * @param schemeRegistry SchemeRegistry to be used
     */
    public SyncHttpClient(SchemeRegistry schemeRegistry) {

        BasicHttpParams httpParams = new BasicHttpParams();

        ConnManagerParams.setTimeout(httpParams, timeout);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
        ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);

        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);

        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(httpParams, " (Android; targetsdkversion " + Build.VERSION.SDK_INT + ";)");

        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);

        requestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
        clientHeaderMap = new HashMap<String, String>();

        httpContext = new SyncBasicHttpContext(new BasicHttpContext());
        httpClient = new DefaultHttpClient(cm, httpParams);
        httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) {
                if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                    request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                }
                for (String header : clientHeaderMap.keySet()) {
                    request.addHeader(header, clientHeaderMap.get(header));
                }
            }
        });

        httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
            @Override
            public void process(HttpResponse response, HttpContext context) {
                final HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return;
                }
                final Header encoding = entity.getContentEncoding();
                if (encoding != null) {
                    for (HeaderElement element : encoding.getElements()) {
                        if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                            response.setEntity(new InflatingEntity(entity));
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * Get the underlying HttpClient instance. This is useful for setting
     * additional fine-grained settings for requests by accessing the client's
     * ConnectionManager, HttpParams and SchemeRegistry.
     *
     * @return underlying HttpClient instance
     */
    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * Get the underlying HttpContext instance. This is useful for getting and
     * setting fine-grained settings for requests by accessing the context's
     * attributes such as the CookieStore.
     *
     * @return underlying HttpContext instance
     */
    public HttpContext getHttpContext() {
        return this.httpContext;
    }

    /**
     * Sets an optional CookieStore to use when making requests
     *
     * @param cookieStore The CookieStore implementation to use, usually an instance of
     *                    {@link PersistentCookieStore}
     */
    public void setCookieStore(CookieStore cookieStore) {
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }


    /**
     * Simple interface method, to enable or disable redirects. If you set
     * manually RedirectHandler on underlying HttpClient, effects of this method
     * will be canceled.
     *
     * @param enableRedirects boolean
     */
    public void setEnableRedirects(final boolean enableRedirects) {
        httpClient.setRedirectHandler(new DefaultRedirectHandler() {
            @Override
            public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                return enableRedirects;
            }
        });
    }

    /**
     * Sets the User-Agent header to be sent with each request. By default,
     * "Android Asynchronous Http Client/VERSION
     * (http://loopj.com/android-async-http/)" is used.
     *
     * @param userAgent the string to use in the User-Agent header.
     */
    public void setUserAgent(String userAgent) {
        HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
    }

    /**
     * Returns current limit of parallel connections
     *
     * @return maximum limit of parallel connections, default is 10
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Sets maximum limit of parallel connections
     *
     * @param maxConnections maximum parallel connections, must be at least 1
     */
    public void setMaxConnections(int maxConnections) {
        if (maxConnections < 1) maxConnections = DEFAULT_MAX_CONNECTIONS;
        this.maxConnections = maxConnections;
        final HttpParams httpParams = this.httpClient.getParams();
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(this.maxConnections));
    }

    /**
     * Returns current socket timeout limit (milliseconds), default is 10000
     * (10sec)
     *
     * @return Socket Timeout limit in milliseconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Set the connection and socket timeout. By default, 10 seconds.
     *
     * @param timeout the connect/socket timeout in milliseconds, at least 1 second
     */
    public void setTimeout(int timeout) {
        if (timeout < 1000) timeout = DEFAULT_SOCKET_TIMEOUT;
        this.timeout = timeout;
        final HttpParams httpParams = this.httpClient.getParams();
        ConnManagerParams.setTimeout(httpParams, this.timeout);
        HttpConnectionParams.setSoTimeout(httpParams, this.timeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, this.timeout);
    }

    /**
     * Sets the Proxy by it's hostname and port
     *
     * @param hostname the hostname (IP or DNS name)
     * @param port     the port number. -1 indicates the scheme default port.
     */
    public void setProxy(String hostname, int port) {
        final HttpHost proxy = new HttpHost(hostname, port);
        final HttpParams httpParams = this.httpClient.getParams();
        httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

    /**
     * Sets the Proxy by it's hostname,port,username and password
     *
     * @param hostname the hostname (IP or DNS name)
     * @param port     the port number. -1 indicates the scheme default port.
     * @param username the username
     * @param password the password
     */
    public void setProxy(String hostname, int port, String username, String password) {
        httpClient.getCredentialsProvider().setCredentials(new AuthScope(hostname, port), new UsernamePasswordCredentials(username, password));
        final HttpHost proxy = new HttpHost(hostname, port);
        final HttpParams httpParams = this.httpClient.getParams();
        httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

    /**
     * Sets the SSLSocketFactory to user when making requests. By default, a
     * new, default SSLSocketFactory is used.
     *
     * @param sslSocketFactory the socket factory to use for https requests.
     */
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslSocketFactory, 443));
    }

    /**
     * Sets the maximum number of retries and timeout for a particular Request.
     *
     * @param retries maximum number of retries per request
     * @param timeout sleep between retries in milliseconds
     */
    public void setMaxRetriesAndTimeout(int retries, int timeout) {
        this.httpClient.setHttpRequestRetryHandler(new RetryHandler(retries,
                timeout));
    }

    /**
     * Sets headers that will be added to all requests this client makes (before
     * sending).
     *
     * @param header the name of the header
     * @param value  the contents of the header
     */
    public void addHeader(String header, String value) {
        clientHeaderMap.put(header, value);
    }

    /**
     * Remove header from all requests this client makes (before sending).
     *
     * @param header the name of the header
     */
    public void removeHeader(String header) {
        clientHeaderMap.remove(header);
    }

    /**
     * Sets basic authentication for the request. Uses AuthScope.ANY. This is
     * the same as setBasicAuth('username','password',AuthScope.ANY)
     *
     * @param username Basic Auth username
     * @param password Basic Auth password
     */
    public void setBasicAuth(String username, String password) {
        AuthScope scope = AuthScope.ANY;
        setBasicAuth(username, password, scope);
    }

    /**
     * Sets basic authentication for the request. You should pass in your
     * AuthScope for security. It should be like this
     * setBasicAuth("username","password", new
     * AuthScope("host",port,AuthScope.ANY_REALM))
     *
     * @param username Basic Auth username
     * @param password Basic Auth password
     * @param scope    - an AuthScope object
     */
    public void setBasicAuth(String username, String password, AuthScope scope) {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                username, password);
        this.httpClient.getCredentialsProvider().setCredentials(scope,
                credentials);
    }

    /**
     * Removes set basic auth credentials
     */
    public void clearBasicAuth() {
        this.httpClient.getCredentialsProvider().clear();
    }

    /**
     * Cancels any pending (or potentially active) requests associated with the
     * passed Context.
     * <p>
     * &nbsp;
     * </p>
     * <b>Note:</b> This will only affect requests which were created with a
     * non-null android Context. This method is intended to be used in the
     * onDestroy method of your android activities to destroy all requests which
     * are no longer required.
     *
     * @param context               the android Context instance associated to the request.
     * @param mayInterruptIfRunning specifies if active requests should be cancelled along with
     *                              pending requests.
     */
    public void cancelRequests(Context context, boolean mayInterruptIfRunning) {
        List<WeakReference<Future<?>>> requestList = requestMap.get(context);
        if (requestList != null) {
            for (WeakReference<Future<?>> requestRef : requestList) {
                Future<?> request = requestRef.get();
                if (request != null) {
                    request.cancel(mayInterruptIfRunning);
                }
            }
        }
        requestMap.remove(context);
    }


    //
    // HTTP GET Requests
    //

    /**
     * Perform a HTTP GET request, without any parameters.
     *
     * @param url the URL to send the request to.
     * @return String
     * @throws HttpException
     */
    public String get(String url) throws HttpException {
        return get(null, url, null);
    }

    /**
     * Perform a HTTP GET request with parameters.
     *
     * @param url    the URL to send the request to.
     * @param params additional GET parameters to send with the request.
     * @return String
     * @throws HttpException
     */
    public String get(String url, RequestParams params) throws HttpException {
        return get(null, url, params);
    }

    /**
     * Perform a HTTP GET request without any parameters and track the Android
     * Context which initiated the request.
     *
     * @param context the Android Context which initiated the request.
     * @param url     the URL to send the request to.
     * @return String
     * @throws HttpException
     */
    public String get(Context context, String url) throws HttpException {
        return get(context, url, null);
    }

    /**
     * Perform a HTTP GET request and track the Android Context which initiated
     * the request.
     *
     * @param context the Android Context which initiated the request.
     * @param url     the URL to send the request to.
     * @param params  additional GET parameters to send with the request.
     * @return String
     * @throws HttpException
     */
    public String get(Context context, String url, RequestParams params) throws HttpException {
        return sendRequest(httpClient, httpContext, new HttpGet(getUrlWithQueryString(isUrlEncodingEnabled, url, params)), null, context);
    }

    /**
     * Perform a HTTP GET request and track the Android Context which initiated
     * the request with customized headers
     *
     * @param context Context to execute request against
     * @param url     the URL to send the request to.
     * @param headers set headers only for this request
     * @param params  additional GET parameters to send with the request.
     * @return String
     * @throws HttpException
     */
    public String get(Context context, String url, Header[] headers, RequestParams params) throws HttpException {
        HttpUriRequest request = new HttpGet(getUrlWithQueryString(isUrlEncodingEnabled, url, params));
        if (headers != null) request.setHeaders(headers);
        return sendRequest(httpClient, httpContext, request, null, context);
    }

    //
    // HTTP POST Requests
    //

    /**
     * Perform a HTTP POST request, without any parameters.
     *
     * @param url the URL to send the request to.
     * @return String
     * @throws HttpException
     */
    public String post(String url) throws HttpException {
        return post(null, url, null);
    }

    /**
     * Perform a HTTP POST request with parameters.
     *
     * @param url    the URL to send the request to.
     * @param params additional POST parameters or files to send with the request.
     * @return String
     * @throws HttpException
     */
    public String post(String url, RequestParams params) throws HttpException {
        return post(null, url, params);
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated
     * the request.
     *
     * @param context the Android Context which initiated the request.
     * @param url     the URL to send the request to.
     * @param params  additional POST parameters or files to send with the request.
     * @return String
     * @throws HttpException
     */
    public String post(Context context, String url, RequestParams params) throws HttpException {
        return post(context, url, paramsToEntity(params), null);
    }

    public String post(Context context, String url, RequestParams params, String contentType) throws HttpException {
        return post(context, url, paramsToEntity(params), contentType);
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated
     * the request.
     *
     * @param context     the Android Context which initiated the request.
     * @param url         the URL to send the request to.
     * @param entity      a raw {@link HttpEntity} to send with the
     *                    request, for example, use this to send string/json/xml
     *                    payloads to a server by passing a
     *                    {@link StringEntity}.
     * @param contentType the content type of the payload you are sending, for example
     *                    application/json if sending a json payload.
     * @return String
     * @throws HttpException
     */
    public String post(Context context, String url, HttpEntity entity, String contentType) throws HttpException {
        return sendRequest(httpClient, httpContext, addEntityToRequestBase(new HttpPost(url), entity), contentType, context);
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated
     * the request. Set headers only for this request
     *
     * @param context     the Android Context which initiated the request.
     * @param url         the URL to send the request to.
     * @param headers     set headers only for this request
     * @param params      additional POST parameters to send with the request.
     * @param contentType the content type of the payload you are sending, for example
     *                    application/json if sending a json payload.
     * @return String
     * @throws HttpException
     */
    public String post(Context context, String url, Header[] headers, RequestParams params, String contentType) throws HttpException {
        HttpEntityEnclosingRequestBase request = new HttpPost(url);
        if (params != null) request.setEntity(paramsToEntity(params));
        if (headers != null) request.setHeaders(headers);
        return sendRequest(httpClient, httpContext, request, contentType, context);
    }


    /**
     * 支持post提交Restful风格的json字符串
     * post
     *
     * @param context
     * @param url
     * @param params
     * @param jsonContent
     * @return
     * @throws HttpException
     */
    public String postRestful(Context context, String url, RequestParams params, String jsonContent) throws HttpException {

        StringEntity entity = null;
        StringBuilder urlBilder = new StringBuilder(url);

        try {
            //拼装公共参数
            if (params != null) {
                String paramString = params.getParamString();
                if (paramString != null && !"".equals(paramString)) {
                    urlBilder.append("?").append(paramString);
                    url = urlBilder.toString();
                }
            }
            //post提交的Json内容
            if (jsonContent != null && !"".equals(jsonContent)) {
                Log.e(tag, "jsonContent: " + jsonContent);
                entity = new StringEntity(jsonContent, ENCODE_UTF8);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return post(context, url, entity, "application/json");
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated
     * the request. Set headers only for this request
     *
     * @param context     the Android Context which initiated the request.
     * @param url         the URL to send the request to.
     * @param headers     set headers only for this request
     * @param entity      a raw {@link HttpEntity} to send with the request, for
     *                    example, use this to send string/json/xml payloads to a server
     *                    by passing a {@link StringEntity}.
     * @param contentType the content type of the payload you are sending, for example
     *                    application/json if sending a json payload.
     * @return String
     * @throws HttpException
     */
    public String post(Context context, String url, Header[] headers, HttpEntity entity, String contentType) throws HttpException {
        HttpEntityEnclosingRequestBase request = addEntityToRequestBase(new HttpPost(url), entity);
        if (headers != null) request.setHeaders(headers);
        return sendRequest(httpClient, httpContext, request, contentType, context);
    }


    /**
     * Puts a new request in queue as a new thread in pool to be executed
     *
     * @param client      HttpClient to be used for request, can differ in single requests
     * @param httpContext HttpContext in which the request will be executed
     * @param uriRequest  instance of HttpUriRequest, which means it must be of
     *                    HttpDelete, HttpPost, HttpGet, HttpPut, etc.
     * @param contentType MIME body type, for POST and PUT requests, may be null
     * @param context     Context of Android application
     * @return
     * @throws HttpException
     */
    protected String sendRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, Context context) throws HttpException {

        String responseBody = "";

        if (contentType != null) {
            uriRequest.addHeader("Content-Type", contentType);
        }

//    	//set cookie
//    	List<Cookie> list = cookieStore.getCookies();
//		if(list != null && list.size() > 0){
//			for(Cookie cookie : list){
////				uriRequest.setHeader("Cookie", cookie.getValue()); 通用cookie
//				uriRequest.setHeader("rong_im_auth", cookie.getValue());
//			}
//		}


        List<Cookie> list = cookieStore.getCookies();
        StringBuilder s = new StringBuilder();
        if (list != null && list.size() > 0) {
            for (Cookie cookie : list) {
                s.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
            }
            uriRequest.setHeader("Cookie", s.toString());
        }

        try {
            //get the response from assets
            URI uri = uriRequest.getURI();
            NLog.e(tag, "url : " + uri.toString());

            String scheme = uri.getScheme();
            if (!TextUtils.isEmpty(scheme) && ASSETS_PATH.equals(scheme)) {
                String fileName = uri.getAuthority();
                InputStream intput = context.getAssets().open(fileName);
                responseBody = inputSteamToString(intput);
                NLog.e(tag, "responseBody : " + responseBody);
                return responseBody;
            }

            //get the response from network
            HttpEntity bufferEntity = null;
            HttpResponse response = client.execute(uriRequest, httpContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                bufferEntity = new BufferedHttpEntity(entity);
                responseBody = EntityUtils.toString(bufferEntity, ENCODE_UTF8);
                NLog.e(tag, "responseBody : " + responseBody);
            }

            // get cookie to save local  获取 cookie 存入本地
//			Header[] headers = response.getHeaders("Set-Cookie");
//	        if (headers != null && headers.length > 0) {
//	        	for(int i=0;i<headers.length;i++) {
//	        		String cookie = headers[i].getValue();
//	        		BasicClientCookie newCookie = new BasicClientCookie("cookie"+i, cookie);
//	        		cookieStore.addCookie(newCookie);
//	        	}
//	        }

            SaveCookies(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new HttpException(e);
        }

        return responseBody;
    }


    public void SaveCookies(HttpResponse httpResponse) {
        Header[] headers = httpResponse.getHeaders("Set-Cookie");
        String headerstr = headers.toString();

        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {

                String cookie = headers[i].getValue();
                String[] cookievalues = cookie.split(";");

                for (int j = 0; j < cookievalues.length; j++) {
                    String[] keyPair = cookievalues[j].split("=");
                    String key = keyPair[0].trim();
                    String value = keyPair.length > 1 ? keyPair[1].trim() : "";
                    BasicClientCookie newCookie = new BasicClientCookie(key, value);
                    cookieStore.addCookie(newCookie);
                }
            }
        }
    }

    /**
     * Sets state of URL encoding feature, see bug #227, this method allows you
     * to turn off and on this auto-magic feature on-demand.
     *
     * @param enabled desired state of feature
     */
    public void setURLEncodingEnabled(boolean enabled) {
        this.isUrlEncodingEnabled = enabled;
    }

    /**
     * Will encode url, if not disabled, and adds params on the end of it
     *
     * @param url             String with URL, should be valid URL without params
     * @param params          RequestParams to be appended on the end of URL
     * @param shouldEncodeUrl whether url should be encoded (replaces spaces with %20)
     * @return encoded url if requested with params appended if any available
     */
    public static String getUrlWithQueryString(boolean shouldEncodeUrl,
                                               String url, RequestParams params) {
        if (shouldEncodeUrl)
            url = url.replace(" ", "%20");

        if (params != null) {
            String paramString = params.getParamString();
            if (!url.contains("?")) {
                url += "?" + paramString;
            } else {
                url += "&" + paramString;
            }
        }

        return url;
    }

    /**
     * Returns HttpEntity containing data from RequestParams included with
     * request declaration. Allows also passing progress from upload via
     * provided ResponseHandler
     *
     * @param params additional request params
     */
    private HttpEntity paramsToEntity(RequestParams params) {
        HttpEntity entity = null;
        try {
            if (params != null) {
                entity = params.getEntity(null);
                Log.e(tag, "params : " + params.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entity;
    }

    public boolean isUrlEncodingEnabled() {
        return isUrlEncodingEnabled;
    }

    /**
     * Applicable only to HttpRequest methods extending
     * HttpEntityEnclosingRequestBase, which is for example not DELETE
     *
     * @param entity      entity to be included within the request
     * @param requestBase HttpRequest instance, must not be null
     */
    private HttpEntityEnclosingRequestBase addEntityToRequestBase(
            HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
        if (entity != null) {
            requestBase.setEntity(entity);
        }

        return requestBase;
    }

    /**
     * Enclosing entity to hold stream of gzip decoded data for accessing
     * HttpEntity contents
     */
    private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }

    public static String inputSteamToString(InputStream in) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int count = -1;
        while ((count = in.read(data, 0, 1024)) != -1) {
            outStream.write(data, 0, count);
        }
        data = null;
        return new String(outStream.toByteArray(), ENCODE_UTF8);
    }
}
