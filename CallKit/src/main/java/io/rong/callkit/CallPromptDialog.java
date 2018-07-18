package io.rong.callkit;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;


public class CallPromptDialog extends AlertDialog {
    private Context mContext;
    private OnPromptButtonClickedListener mPromptButtonClickedListener;
    private String mTitle;
    private String mPositiveButton;
    private String mNegativeButton;
    private String mMessage;
    private int mLayoutResId;
    private boolean disableCancel;
    private int positiveTxtColor = 0;
    private int negativeTxtColor = 0;

    public static CallPromptDialog newInstance(final Context context, String title, String message) {
        return new CallPromptDialog(context, title, message);
    }

    public static CallPromptDialog newInstance(final Context context, String message) {
        return new CallPromptDialog(context, message);
    }

    public static CallPromptDialog newInstance(final Context context, String title, String message, String positiveButton) {
        return new CallPromptDialog(context, title, message, positiveButton);
    }

    public static CallPromptDialog newInstance(final Context context, String title, String message, String positiveButton, String negativeButton) {
        return new CallPromptDialog(context, title, message, positiveButton, negativeButton);
    }

    public CallPromptDialog(final Context context, String title, String message, String positiveButton, String negativeButton) {
        this(context, title, message, positiveButton);
        this.mNegativeButton = negativeButton;
    }

    public CallPromptDialog(final Context context, String title, String message, String positiveButton) {
        this(context, title, message);
        mPositiveButton = positiveButton;
    }

    public CallPromptDialog(final Context context, String title, String message) {
        super(context);
        mLayoutResId = R.layout.rc_voip_dialog_popup_prompt;
        mContext = context;
        mTitle = title;
        mMessage = message;
    }

    public CallPromptDialog(final Context context, String message) {
        this(context, "", message);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(mLayoutResId, null);
        TextView txtViewTitle = (TextView) view.findViewById(io.rong.imkit.R.id.popup_dialog_title);
        TextView txtViewMessage = (TextView) view.findViewById(io.rong.imkit.R.id.popup_dialog_message);
        TextView txtViewOK = (TextView) view.findViewById(io.rong.imkit.R.id.popup_dialog_button_ok);
        TextView txtViewCancel = (TextView) view.findViewById(io.rong.imkit.R.id.popup_dialog_button_cancel);
        if (disableCancel) txtViewCancel.setVisibility(View.GONE);
        if (positiveTxtColor != 0) {
            txtViewOK.setTextColor(positiveTxtColor);
        }
        if (negativeTxtColor != 0) {
            txtViewCancel.setTextColor(negativeTxtColor);
        }
        txtViewOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPromptButtonClickedListener != null) {
                    mPromptButtonClickedListener.onPositiveButtonClicked();
                }
                dismiss();
            }
        });
        txtViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPromptButtonClickedListener != null) {
                    mPromptButtonClickedListener.onNegativeButtonClicked();
                }
                dismiss();
            }
        });
        if (!TextUtils.isEmpty(mTitle)) {
            txtViewTitle.setText(mTitle);
            txtViewTitle.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(mPositiveButton)) {
            txtViewOK.setText(mPositiveButton);
        }

        if (!TextUtils.isEmpty(mNegativeButton)) {
            txtViewCancel.setText(mNegativeButton);
            txtViewCancel.setVisibility(View.VISIBLE);
        }

        txtViewMessage.setText(mMessage);

        setContentView(view);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = gePopupWidth();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(layoutParams);
    }

    public void disableCancel() {
        disableCancel = true;
    }

    public CallPromptDialog setPromptButtonClickedListener(OnPromptButtonClickedListener buttonClickedListener) {
        this.mPromptButtonClickedListener = buttonClickedListener;
        return this;
    }

    public CallPromptDialog setLayoutRes(int resId) {
        this.mLayoutResId = resId;
        return this;
    }

    public void setPositiveTextColor(int color) {
        positiveTxtColor = color;
    }

    public void setNegativeTextColor(int color) {
        negativeTxtColor = color;
    }

    public interface OnPromptButtonClickedListener {
        void onPositiveButtonClicked();

        void onNegativeButtonClicked();
    }

    private int gePopupWidth() {
        int distanceToBorder = (int) mContext.getResources().getDimension(R.dimen.rc_dimen_size_40);
        return getScreenWidth() - 2 * (distanceToBorder);
    }

    private int getScreenWidth() {
        return ((WindowManager) (mContext.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getWidth();
    }
}
