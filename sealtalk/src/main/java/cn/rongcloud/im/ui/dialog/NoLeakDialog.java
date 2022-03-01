package cn.rongcloud.im.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.lang.ref.WeakReference;

/** 解决DialogFragment内存泄漏Bug Created by yanke on 2021/12/8 */
public class NoLeakDialog extends Dialog {

    /** 主要是防止弱引用指向的 Listener被清除 */
    private OnDismissListener mOnDismissListener;

    private OnCancelListener mOnCancelListener;
    private OnShowListener mOnShowListener;

    public NoLeakDialog(@NonNull Context context) {
        super(context);
    }

    public NoLeakDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected NoLeakDialog(
            @NonNull Context context,
            boolean cancelable,
            @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void setOnCancelListener(@Nullable OnCancelListener listener) {
        this.mOnCancelListener = listener;
        super.setOnCancelListener(new WrappedCancelListener(listener));
    }

    @Override
    public void setOnShowListener(@Nullable OnShowListener listener) {
        this.mOnShowListener = listener;
        super.setOnShowListener(listener);
    }

    @Override
    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        this.mOnDismissListener = listener;
        super.setOnDismissListener(listener);
    }

    static class WrappedDismissListener implements OnDismissListener {
        private WeakReference<OnDismissListener> weakReference;

        public WrappedDismissListener(OnDismissListener listener) {
            weakReference = new WeakReference<>(listener);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            OnDismissListener listener = weakReference.get();
            if (listener != null) {
                listener.onDismiss(dialog);
            }
        }
    }

    static class WrappedShowListener implements OnShowListener {
        private WeakReference<OnShowListener> weakReference;

        public WrappedShowListener(OnShowListener listener) {
            weakReference = new WeakReference<>(listener);
        }

        @Override
        public void onShow(DialogInterface dialog) {
            OnShowListener listener = weakReference.get();
            if (listener != null) {
                listener.onShow(dialog);
            }
        }
    }

    static class WrappedCancelListener implements OnCancelListener {
        private WeakReference<OnCancelListener> weakReference;

        public WrappedCancelListener(OnCancelListener listener) {
            weakReference = new WeakReference<>(listener);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            OnCancelListener listener = weakReference.get();
            if (listener != null) {
                listener.onCancel(dialog);
            }
        }
    }
}
