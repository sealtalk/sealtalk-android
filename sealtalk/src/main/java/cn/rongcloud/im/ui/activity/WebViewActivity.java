package cn.rongcloud.im.ui.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import cn.rongcloud.im.R;

/**
 * 加载网页
 */
public class WebViewActivity extends TitleBaseActivity {


    public static final String PARAMS_TITLE = "title";
    public static final String PARAMS_URL = "url";
    private WebView webview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        initView();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        String title = getIntent().getStringExtra(PARAMS_TITLE);
        String url = getIntent().getStringExtra(PARAMS_URL);

        getTitleBar().setTitle(title);
        webview = (WebView) findViewById(R.id.vb_webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setSupportZoom(true);
        //自适应屏幕
        webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webview.getSettings().setLoadWithOverviewMode(true);


        MyWebViewClient mMyWebViewClient = new MyWebViewClient();
        mMyWebViewClient.onPageFinished(webview, url);
        mMyWebViewClient.shouldOverrideUrlLoading(webview, url);
        mMyWebViewClient.onPageFinished(webview, url);
        webview.setWebViewClient(mMyWebViewClient);

    }

    class MyWebViewClient extends WebViewClient {

        ProgressDialog progressDialog;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//网页页面开始加载的时候
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(WebViewActivity.this);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
                webview.setEnabled(false);// 当加载网页的时候将网页进行隐藏
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {//网页加载结束的时候
            //super.onPageFinished(view, url);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
                webview.setEnabled(true);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { //网页加载时的连接的网址
            view.loadUrl(url);
            return false;
        }
    }
}
