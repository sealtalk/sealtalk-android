package cn.rongcloud.im.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.rongcloud.im.R;

public class OperatePictureBottomDialog extends BaseBottomDialog implements View.OnClickListener {

    private OnDialogButtonClickListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_operation_picture, null);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnDelete = view.findViewById(R.id.btn_delete);
        Button btnCancle = view.findViewById(R.id.btn_cancel);
        btnSave.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnCancle.setOnClickListener(this);

        Dialog dialog = getDialog();
        if(dialog != null){
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            switch (v.getId()) {
                case R.id.btn_save:
                    mListener.onClickSave();
                    break;
                case R.id.btn_delete:
                    mListener.onClickDelete();
                    break;
                case R.id.btn_cancel:
                    break;
            }
            OperatePictureBottomDialog.this.dismiss();
        }

    }

    public void setOnDialogButtonClickListener(OnDialogButtonClickListener listener) {
        this.mListener = listener;
    }

    public interface OnDialogButtonClickListener {
        void onClickSave();

        void onClickDelete();
    }
}
