package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.TitleBaseActivity;
import cn.rongcloud.im.utils.NetworkUtils;
import cn.rongcloud.im.utils.qrcode.barcodescanner.CaptureManager;
import cn.rongcloud.im.utils.qrcode.barcodescanner.DecoratedBarcodeView;
import io.rong.eventbus.EventBus;
import io.rong.imlib.RongIMClient;


/**
 *
 */
public class CaptureActivity extends TitleBaseActivity {
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        barcodeScannerView = initializeContent();
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        EventBus.getDefault().register(this);
        barcodeScannerView.getViewFinder().networkChange(!NetworkUtils.isNetWorkAvailable(this));
        if (!NetworkUtils.isNetWorkAvailable(this)) {
            capture.stopDecode();
        } else {
            capture.decode();
        }
    }

    /**
     * Override to use a different layout.
     *
     * @return the DecoratedBarcodeView
     */
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.zxing_capture);
        return (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    public void finishPager(View view) {
        finish();
    }

    public void onEventMainThread(final RongIMClient.ConnectionStatusListener.ConnectionStatus status) {
        barcodeScannerView.getViewFinder().networkChange(!status.equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED));
        if (!status.equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED)) {
            capture.stopDecode();
        } else {
            capture.decode();
        }
    }
}
