package cn.rongcloud.im.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.rongcloud.im.R;

public class ClearGroupNoticeDialog extends BaseBottomDialog {

    private ClearClickListener mOnClearClick;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_clear_group_notice, null);
        view.findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClearClick != null) {
                    mOnClearClick.onClearClick();
                    dismiss();
                }
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    public void setmOnClearClick(ClearClickListener onClearClick) {
        this.mOnClearClick = onClearClick;
    }

    public interface ClearClickListener {
        void onClearClick();
    }
}
