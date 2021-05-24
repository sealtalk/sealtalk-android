package cn.rongcloud.im.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;

public class MsgExtraInputDialog extends Dialog {

    protected Context mContext;

    protected WindowManager.LayoutParams mLayoutParams;

    private int mType;
    public static int TYPE_SET= 0x1456;
    public static int TYPE_DELETE = 0x1457;
    public static int TYPE_SEND_MESSAGE = 0x1458;
    public static int TYPE_SHORTAGE = 0x1459;

    private TextView mTvSure;
    private TextView mTvCancel;
    private TextView uidText;
    private TextView keyText;
    private TextView valText;
    private TextView mTVAdd;
    private EditText etKey;
    private EditText etValue;
    private EditText etUID;
    private EditText etMsgContent;
    private LinearLayout llUID;
    private LinearLayout llKey;
    private LinearLayout llValue;
    private LinearLayout llSendMsg;

    public TextView getmTVAdd() {
        return mTVAdd;
    }

    public void setmTVAdd(TextView mTVAdd) {
        this.mTVAdd = mTVAdd;
    }

    public void setEtKey(EditText etKey) {
        this.etKey = etKey;
    }

    public void setEtValue(EditText etValue) {
        this.etValue = etValue;
    }

    public EditText getEtUID() {
        return etUID;
    }

    public void setEtUID(EditText etUID) {
        this.etUID = etUID;
    }

    public EditText getEtMsgContent() {
        return etMsgContent;
    }

    public void setEtMsgContent(EditText etMsgContent) {
        this.etMsgContent = etMsgContent;
    }

    public MsgExtraInputDialog(Context context) {
        super(context);
        initView(context);
    }

    public MsgExtraInputDialog(Activity context) {
        super(context);
        initView(context);
    }

    public MsgExtraInputDialog(Context context, int type) {
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
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_msg_extra_input, null);
        mTvSure = dialogView.findViewById(R.id.tv_sure);
        mTvCancel = dialogView.findViewById(R.id.tv_cancle);
        mTVAdd = dialogView.findViewById(R.id.add_item);
        uidText = dialogView.findViewById(R.id.uid_text);
        keyText = dialogView.findViewById(R.id.key_text);
        valText = dialogView.findViewById(R.id.val_text);
        etKey = dialogView.findViewById(R.id.et_key);
        etValue = dialogView.findViewById(R.id.et_value);
        etMsgContent = dialogView.findViewById(R.id.et_send_msg);
        etUID = dialogView.findViewById(R.id.et_uid);
        llUID = dialogView.findViewById(R.id.ll_uid);
        llKey = dialogView.findViewById(R.id.ll_key);
        llValue = dialogView.findViewById(R.id.ll_value);
        llSendMsg = dialogView.findViewById(R.id.ll_send_msg);

        if (mType == TYPE_SET) {
            llSendMsg.setVisibility(View.GONE);
        } else if (mType == TYPE_DELETE) {
            llValue.setVisibility(View.GONE);
            llSendMsg.setVisibility(View.GONE);
            mTVAdd.setText("添加 Key");
        } else if(mType == TYPE_SEND_MESSAGE) {
            llUID.setVisibility(View.GONE);
            mTvSure.setText("发送");
        } else if(mType == TYPE_SHORTAGE) {
            uidText.setText("时间");
            keyText.setText("数量");
            valText.setText("顺序");
            mTVAdd.setVisibility(View.GONE);
            llSendMsg.setVisibility(View.GONE);
        }
        setContentView(dialogView);
    }
}
