package cn.rongcloud.im.ui.dialog;

import android.os.Environment;

import java.io.File;

import cn.rongcloud.im.R;
import cn.rongcloud.im.utils.ToastUtils;

public class ClearCacheDialog extends CommonDialog {


    @Override
    protected boolean onPositiveClick() {
        clearFiles();
        ToastUtils.showToast(R.string.seal_set_account_dialog_toast_clear_cache_clear_success);
        return true;
    }

    /**
     * 清理缓存
     */
    private void clearFiles() {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + getContext().getPackageName());
        deleteFile(file);
    }

    public void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                deleteFile(f);
            }
            file.delete();
        }
    }

    public static class Builder extends CommonDialog.Builder {
        @Override
        protected CommonDialog getCurrentDialog() {
            return new ClearCacheDialog();
        }
    }
}
