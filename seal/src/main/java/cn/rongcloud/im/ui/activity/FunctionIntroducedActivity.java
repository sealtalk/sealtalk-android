package cn.rongcloud.im.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.rongcloud.im.R;


@SuppressLint("SetJavaScriptEnabled")
public class FunctionIntroducedActivity extends BaseActivity {

    private WebView mWebView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_introduced);
        setTitle(R.string.function_introduce);

        mWebView = (WebView) findViewById(R.id.function_introdiced_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setSupportZoom(true);


        MyWebViewClient mMyWebViewClient = new MyWebViewClient();
        mMyWebViewClient.onPageFinished(mWebView, "http://rongcloud.cn/features");
        mMyWebViewClient.shouldOverrideUrlLoading(mWebView, "http://rongcloud.cn/features");
        mMyWebViewClient.onPageFinished(mWebView, "http://rongcloud.cn/features");
        mWebView.setWebViewClient(mMyWebViewClient);
    }

    class MyWebViewClient extends WebViewClient {

        ProgressDialog progressDialog;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//网页页面开始加载的时候
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(FunctionIntroducedActivity.this);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                mWebView.setEnabled(false);// 当加载网页的时候将网页进行隐藏
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {//网页加载结束的时候
            //super.onPageFinished(view, url);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
                mWebView.setEnabled(true);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { //网页加载时的连接的网址
            view.loadUrl(url);
            return false;
        }
    }

}
