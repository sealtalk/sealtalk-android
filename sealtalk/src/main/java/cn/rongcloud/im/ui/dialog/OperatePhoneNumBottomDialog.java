package cn.rongcloud.im.ui.dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.rongcloud.im.R;
import cn.rongcloud.im.utils.ToastUtils;


public class OperatePhoneNumBottomDialog extends BaseBottomDialog implements View.OnClickListener {

    private String phoneNum;

    public OperatePhoneNumBottomDialog(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_operate_phone_num, null);
        view.findViewById(R.id.btn_take_call).setOnClickListener(this);
        view.findViewById(R.id.btn_copy).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_take_call:
                toCallPhone(phoneNum);
                break;
            case R.id.btn_copy:
                copyText(phoneNum);
                break;
            case R.id.btn_cancel:
                break;
        }
        dismiss();
    }

    /**
     * 拨打电话
     */
    private void toCallPhone(String phoneNum) {
        if (!TextUtils.isEmpty(phoneNum)) {
            Uri telUri = Uri.parse("tel:" + phoneNum);
            Intent intent = new Intent(Intent.ACTION_DIAL, telUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     * 复制文本到剪贴板
     *
     * @param text 文本
     */
    public void copyText(final CharSequence text) {
        ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("text", text));
        ToastUtils.showToast(R.string.seal_dialog_operate_phone_num_copy_success);
    }
}
