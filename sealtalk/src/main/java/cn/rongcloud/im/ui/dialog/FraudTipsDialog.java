package cn.rongcloud.im.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;

/** @author gusd */
public class FraudTipsDialog extends Dialog {
    private static final String TAG = "FraudTipsDialog";

    public FraudTipsDialog(@NonNull Context context) {
        super(context, R.style.TipsDialog);
        setCanceledOnTouchOutside(false);
        View rootView =
                LayoutInflater.from(context).inflate(R.layout.dialog_fraud_tips, null, false);
        setContentView(rootView);
        rootView.findViewById(R.id.tv_confirm)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dismiss();
                            }
                        });
    }
}
