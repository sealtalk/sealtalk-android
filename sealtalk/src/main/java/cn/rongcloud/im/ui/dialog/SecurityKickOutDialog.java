package cn.rongcloud.im.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import cn.rongcloud.im.R;

/** 安全问题踢出弹出 */
public class SecurityKickOutDialog extends DialogFragment {

    @Override
    public void onStart() {
        super.onStart();
        // 透明化背景
        Window window = getDialog().getWindow();
        // 背景色
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.security_dialog_kick_out, container, false);
        Button confirmBtn = contentView.findViewById(R.id.dialog_btn_positive);
        // 确定按钮监听
        confirmBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });

        return contentView;
    }
}
