package cn.rongcloud.im.ui.test;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;

public class ChatRoomStatusInputDialog extends Dialog {

    protected Context mContext;

    protected WindowManager.LayoutParams mLayoutParams;

    private int mType;
    public static int TYPE_REMOVE = 0x1456;
    public static int TYPE_GET = 0x1457;
    public static int TYPE_SET_BATCH = 0x1458;
    public static int TYPE_REMOVE_BATCH = 0x1459;

    private TextView mTvSure;
    private TextView mTvCancel;
    private EditText etKey;
    private EditText etValue;
    private EditText etExtra;
    private CheckBox cbAutoDel;
    private CheckBox cbIsSendMsg;
    private LinearLayout llValue;
    private LinearLayout llCheck;
    private LinearLayout llExtra;


    public ChatRoomStatusInputDialog(Context context) {
        super(context);
        initView(context);
    }

    public ChatRoomStatusInputDialog(Activity context) {
        super(context);
        initView(context);
    }

    public ChatRoomStatusInputDialog(Context context, int type) {
        super(context);
        mType = type;
        initView(context);
    }

    public EditText getEtKey() {
        return etKey;
    }

    public EditText getEtValue() {
        return etValue;
    }

    public EditText getEtExtra() {
        return etExtra;
    }

    public CheckBox getCbAutoDel() {
        return cbAutoDel;
    }

    public CheckBox getCbIsSendMsg() {
        return cbIsSendMsg;
    }

    public TextView getSureView() {
        return mTvSure;
    }

    public void setSure(String strSure) {
        this.mTvSure.setText(strSure);
    }

    public TextView getCancelView() {
        return mTvCancel;
    }

    public void setCancel(String strCancel) {
        this.mTvCancel.setText(strCancel);
    }

    private void initView(Context context) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawableResource(R.drawable.transparent_bg);
        mContext = context;
        Window window = this.getWindow();
        mLayoutParams = window.getAttributes();
        mLayoutParams.alpha = 1f;
        window.setAttributes(mLayoutParams);
        if (mLayoutParams != null) {
            mLayoutParams.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
            mLayoutParams.gravity = Gravity.CENTER;
        }
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_chat_room_status_input, null);
        mTvSure = dialogView.findViewById(R.id.tv_sure);
        mTvCancel = dialogView.findViewById(R.id.tv_cancle);
        etKey = dialogView.findViewById(R.id.et_key);
        etValue = dialogView.findViewById(R.id.et_value);
        llValue = dialogView.findViewById(R.id.ll_value);
        etExtra = dialogView.findViewById(R.id.et_extras);
        cbAutoDel = dialogView.findViewById(R.id.cb_auto_del);
        llCheck = dialogView.findViewById(R.id.ll_check);
        cbIsSendMsg = dialogView.findViewById(R.id.cb_is_send_msg);
        llExtra = dialogView.findViewById(R.id.ll_extra);
        if (mType == TYPE_REMOVE) {
            llValue.setVisibility(View.GONE);
            cbAutoDel.setVisibility(View.GONE);
        } else if (mType == TYPE_GET) {
            llValue.setVisibility(View.GONE);
            llCheck.setVisibility(View.GONE);
            llExtra.setVisibility(View.GONE);
        } else if (mType == TYPE_REMOVE_BATCH) {
            llCheck.setVisibility(View.GONE);
            llExtra.setVisibility(View.GONE);
            llValue.setVisibility(View.GONE);
        }
        setContentView(dialogView);
    }
}
