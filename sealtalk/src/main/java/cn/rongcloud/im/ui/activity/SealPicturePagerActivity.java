package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.io.File;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.qrcode.SealQrCodeUISelector;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.qrcode.QRCodeUtils;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.common.FileUtils;


//public class SealPicturePagerActivity extends PicturePagerActivity {
//
//    private String qrCodeResult;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//
//    @Override
//    public boolean onPictureLongClick(View v, Uri thumbUri, Uri largeImageUri) {
//        final File file;
//        if (largeImageUri != null) {
//            if (largeImageUri.getScheme().startsWith("http") || largeImageUri.getScheme().startsWith("https"))
//                file = ImageLoader.getInstance().getDiskCache().get(largeImageUri.toString());
//            else
//                file = new File(largeImageUri.getPath());
//        } else {
//            return false;
//        }
//
//        if (file == null || !file.exists()) {
//            return true;
//        }
//
//
//        /*
//         * 长按时先扫描图片中是否有二维码，再决定是否显示扫描二维码选项
//         */
//        qrCodeResult = QRCodeUtils.analyzeImage(file.getPath());
//        SLog.d(LogTag.COMMON, "SealPicturePagerActivity scan QR Code is " + qrCodeResult);
//
//        String[] items;
//        if (TextUtils.isEmpty(qrCodeResult)) {
//            items = new String[]{getString(io.rong.imkit.R.string.rc_save_picture)};
//        } else {
//            items = new String[]{getString(io.rong.imkit.R.string.rc_save_picture), getString(R.string.zxing_distinguish_picture)};
//        }
//
//        OptionsPopupDialog.newInstance(this, items).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
//            @Override
//            public void onOptionsItemClicked(int which) {
//                if (which == 0) {
//                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//                    if (!PermissionCheckUtil.requestPermissions(SealPicturePagerActivity.this, permissions)) {
//                        return;
//                    }
//
//                    String saveImagePath = RongUtils.getImageSavePath(SealPicturePagerActivity.this);
//                    if (file != null && file.exists()) {
//                        String name = System.currentTimeMillis() + ".jpg";
//                        FileUtils.copyFile(file, saveImagePath + File.separator, name);
//                        MediaScannerConnection.scanFile(SealPicturePagerActivity.this, new String[]{saveImagePath + File.separator + name}, null, null);
//                        ToastUtils.showToast(getString(io.rong.imkit.R.string.rc_save_picture_at));
//                    } else {
//                        ToastUtils.showToast(getString(io.rong.imkit.R.string.rc_src_file_not_found));
//                    }
//                } else if (which == 1) {
//                    if (!TextUtils.isEmpty(qrCodeResult)) {
//                        handleQRCodeResult(qrCodeResult);
//                    }
//                }
//            }
//        }).show();
//        return true;
//    }
//
//
//    /**
//     * 根据二维码中的信息跳转至其他页面
//     *
//     * @param result
//     */
//    private void handleQRCodeResult(String result) {
//        SealQrCodeUISelector uiSelector = new SealQrCodeUISelector(this);
//        LiveData<Resource<String>> resourceLiveData = uiSelector.handleUri(result);
//        resourceLiveData.observe(this, new Observer<Resource<String>>() {
//            @Override
//            public void onChanged(Resource<String> resource) {
//                if (resource.status != Status.LOADING) {
//                    resourceLiveData.removeObserver(this);
//                }
//
//                if (resource.status == Status.SUCCESS) {
//                    finish();
//                } else {
//                    String errorMsg = resource.data;
//                    ToastUtils.showToast(errorMsg);
//                }
//            }
//        });
//    }
//
//}
