package cn.rongcloud.im.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import cn.rongcloud.im.R;


/**
 * 简易可设置提示信息的加载对话框
 */
public class LoadingDialog extends DialogFragment {
    private TextView contentTv;
    private String loadingInfo;

    @Override
    public void onStart() {
        super.onStart();
        //透明化背景
        Window window = getDialog().getWindow();
        //背景色
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.common_dialog_loading, container, false);
        contentTv = contentView.findViewById(R.id.common_dialog_tv_information);
        if(TextUtils.isEmpty(loadingInfo)){
            contentTv.setVisibility(View.GONE);
        }else {
            contentTv.setText(loadingInfo);
        }
        setCancelable(false);

        Dialog dialog = getDialog();
        if(dialog != null){
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        return contentView;
    }

    /**
     * 设置加载时提示
     *
     * @param info
     */
    public void setLoadingInformation(String info) {
        loadingInfo = info;
        if(!TextUtils.isEmpty(loadingInfo) && contentTv != null){
            contentTv.setText(info);
            contentTv.setVisibility(View.VISIBLE);
        }
    }

}
