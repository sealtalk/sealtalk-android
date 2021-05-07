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

public class TagTestInputDialog extends Dialog {

    protected Context mContext;

    protected WindowManager.LayoutParams mLayoutParams;

    private int mType;
    public static int TYPE_SET = 0x1456;
    public static int TYPE_DELETE = 0x1457;
    public static int TYPE_ADD_CONVERSATION = 0x1458;
    public static int TYPE_REMOVE_TAGS = 0x1459;
    public static int TYPE_GET_CONVERSATION_TAGS = 0x1460;
    public static int TYPE_GET_CONVERSATION_TOP = 0x1461;
    public static int TYPE_GET_CONVERSATION_FOR_TAG = 0x1462;
    public static int TYPE_GET_UNREAD_FOR_TAG = 0x1463;
    public static int TYPE_SET_TOP = 0x1465;


    private TextView mTvSure;
    private TextView mTvCancel;
    private TextView mTVAdd;
    private TextView mTvType;
    private TextView mTvTargetId;
    private TextView mTvTagName;
    private EditText etTagId;
    private EditText etTagName;
    private EditText etType;
    private EditText etTargetId;
    private LinearLayout llTagId;
    private LinearLayout llTagName;
    private LinearLayout llType;
    private LinearLayout llTargetId;

    public EditText getEtTagId() {
        return etTagId;
    }

    public void setEtTagId(EditText etTagId) {
        this.etTagId = etTagId;
    }

    public EditText getEtTagName() {
        return etTagName;
    }

    public void setEtTagName(EditText etTagName) {
        this.etTagName = etTagName;
    }

    public EditText getEtType() {
        return etType;
    }

    public void setEtType(EditText etType) {
        this.etType = etType;
    }

    public EditText getEtTargetId() {
        return etTargetId;
    }

    public void setEtTargetId(EditText etTargetId) {
        this.etTargetId = etTargetId;
    }

    public TagTestInputDialog(Context context) {
        super(context);
        initView(context);
    }

    public TagTestInputDialog(Activity context) {
        super(context);
        initView(context);
    }

    public TagTestInputDialog(Context context, int type) {
        super(context);
        mType = type;
        initView(context);
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

    public TextView getAddView() {
        return mTVAdd;
    }

    public void setmTVAdd(TextView mTVAdd) {
        this.mTVAdd = mTVAdd;
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
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_tag_test_input, null);
        mTvSure = dialogView.findViewById(R.id.tv_sure);
        mTvCancel = dialogView.findViewById(R.id.tv_cancle);
        mTVAdd = dialogView.findViewById(R.id.add_item);
        mTvType = dialogView.findViewById(R.id.text_type);
        mTvTargetId = dialogView.findViewById(R.id.text_target_id);
        mTvTagName = dialogView.findViewById(R.id.text_tag_name);
        etTagId = dialogView.findViewById(R.id.et_tag_id);
        etTagName = dialogView.findViewById(R.id.et_tag_name);
        etType = dialogView.findViewById(R.id.et_tag_type);
        etTargetId = dialogView.findViewById(R.id.et_target_id);
        llTagId = dialogView.findViewById(R.id.ll_tag_id);
        llTagName = dialogView.findViewById(R.id.ll_tag_name);
        llType = dialogView.findViewById(R.id.ll_tag_type);
        llTargetId = dialogView.findViewById(R.id.ll_tag_target_id);

        mTvType.setText("会话类型");
        mTvTargetId.setText("target id");
        mTvTagName.setText("name");
        if (mType == TYPE_SET) {
            llType.setVisibility(View.GONE);
            llTargetId.setVisibility(View.GONE);
            mTVAdd.setVisibility(View.GONE);
        } else if (mType == TYPE_DELETE) {
            llTagName.setVisibility(View.GONE);
            llType.setVisibility(View.GONE);
            llTargetId.setVisibility(View.GONE);
            mTVAdd.setVisibility(View.GONE);
        } else if (mType == TYPE_ADD_CONVERSATION) {
            llTagName.setVisibility(View.GONE);
            llType.setVisibility(View.VISIBLE);
            llTargetId.setVisibility(View.VISIBLE);
            mTVAdd.setVisibility(View.VISIBLE);
            mTVAdd.setText("添加会话");
        } else if (mType == TYPE_REMOVE_TAGS) {
            llTagName.setVisibility(View.GONE);
            llType.setVisibility(View.VISIBLE);
            llTargetId.setVisibility(View.VISIBLE);
            mTVAdd.setVisibility(View.VISIBLE);
            mTVAdd.setText("添加 tag id");
        } else if (mType == TYPE_GET_CONVERSATION_TAGS) {
            llTagId.setVisibility(View.GONE);
            llTagName.setVisibility(View.GONE);
            llType.setVisibility(View.VISIBLE);
            llTargetId.setVisibility(View.VISIBLE);
            mTVAdd.setVisibility(View.GONE);
        } else if (mType == TYPE_GET_CONVERSATION_TOP) {
            llTagId.setVisibility(View.VISIBLE);
            llTagName.setVisibility(View.GONE);
            llType.setVisibility(View.VISIBLE);
            llTargetId.setVisibility(View.VISIBLE);
            mTVAdd.setVisibility(View.GONE);
        } else if (mType == TYPE_GET_CONVERSATION_FOR_TAG) {
            llTagId.setVisibility(View.VISIBLE);
            llTagName.setVisibility(View.GONE);
            llType.setVisibility(View.VISIBLE);
            llTargetId.setVisibility(View.VISIBLE);
            mTvType.setText("时间戳");
            mTvTargetId.setText("count");
            mTVAdd.setVisibility(View.GONE);
        } else if(mType == TYPE_GET_UNREAD_FOR_TAG) {
            llTagId.setVisibility(View.VISIBLE);
            llTagName.setVisibility(View.GONE);
            llType.setVisibility(View.GONE);
            llTargetId.setVisibility(View.VISIBLE);
            mTvTargetId.setText("true/false");
            mTVAdd.setVisibility(View.GONE);
        } else if(mType == TYPE_SET_TOP) {
            llTagId.setVisibility(View.VISIBLE);
            llTagName.setVisibility(View.VISIBLE);
            llType.setVisibility(View.VISIBLE);
            llTargetId.setVisibility(View.VISIBLE);
            mTvTagName.setText("isTop(true/false)");
            mTVAdd.setVisibility(View.GONE);
        }
        setContentView(dialogView);
    }
}
