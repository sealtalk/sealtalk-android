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

public class PushConfigDialog extends Dialog {

    public static int TYPE_REMOVE = 0x1456;
    public static int TYPE_GET = 0x1457;
    protected Context mContext;
    protected WindowManager.LayoutParams mLayoutParams;
    private int mType;
    private TextView mTvSure;
    private TextView mTvCancel;
    private EditText etId;
    private EditText etTitle;
    private EditText etContent;
    private EditText etData;
    private EditText etHW;
    private EditText etMi;
    private EditText etOppo;
    private EditText etThreadId;
    private EditText etApnId;
    private EditText edFcm;
    private EditText edTemplateId;
    private EditText edImageUrl;
    private EditText edCategory;
    private EditText edRichMediaUri;

    private CheckBox cbVivo;
    private CheckBox cbDisableTitle;
    private CheckBox cbForceDetail;

    private LinearLayout llValue;
    private LinearLayout llCheck;
    private LinearLayout llExtra;
    private EditText etHWImportance;

    public PushConfigDialog(Context context) {
        super(context);
        initView(context);
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
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_push_config, null);
        mTvSure = dialogView.findViewById(R.id.tv_sure);
        mTvCancel = dialogView.findViewById(R.id.tv_cancle);
        etId = dialogView.findViewById(R.id.et_id);
        etTitle = dialogView.findViewById(R.id.et_title);
        etContent = dialogView.findViewById(R.id.et_content);
        etContent = dialogView.findViewById(R.id.et_content);
        etData = dialogView.findViewById(R.id.et_data);
        etHW = dialogView.findViewById(R.id.et_hw);
        etHWImportance = dialogView.findViewById(R.id.et_importance_hw);
        etMi = dialogView.findViewById(R.id.et_mi);
        etOppo = dialogView.findViewById(R.id.et_oppo);
        etThreadId = dialogView.findViewById(R.id.et_thread_id);
        etApnId = dialogView.findViewById(R.id.et_apns_id);
        edCategory = dialogView.findViewById(R.id.et_category);
        edRichMediaUri = dialogView.findViewById(R.id.et_richMediaUri);
        edTemplateId = dialogView.findViewById(R.id.et_template_id);

        cbVivo = dialogView.findViewById(R.id.cb_vivo);
        cbDisableTitle = dialogView.findViewById(R.id.cb_is_disable_title);
        cbForceDetail = dialogView.findViewById(R.id.cb_is_show_detail);
        edFcm = dialogView.findViewById(R.id.et_fcm);
        edImageUrl = dialogView.findViewById(R.id.et_image_url);

        setContentView(dialogView);
    }

    public PushConfigDialog(Activity context) {
        super(context);
        initView(context);
    }

    public PushConfigDialog(Context context, int type) {
        super(context);
        mType = type;
        initView(context);
    }

    public EditText getEdImageUrl() {
        return edImageUrl;
    }

    public void setEdImageUrl(EditText edImageUrl) {
        this.edImageUrl = edImageUrl;
    }

    public EditText getEdFcm() {
        return edFcm;
    }

    public void setEdFcm(EditText edFcm) {
        this.edFcm = edFcm;
    }

    public EditText getEtId() {
        return etId;
    }

    public EditText getEtTitle() {
        return etTitle;
    }

    public EditText getEtContent() {
        return etContent;
    }

    public EditText getEtData() {
        return etData;
    }

    public EditText getEtHW() {
        return etHW;
    }

    public EditText getEtMi() {
        return etMi;
    }

    public EditText getEtOppo() {
        return etOppo;
    }

    public EditText getEtThreadId() {
        return etThreadId;
    }

    public EditText getEtApnId() {
        return etApnId;
    }

    public EditText getEdTemplateId() {
        return edTemplateId;
    }

    public EditText getEdCategory() {
        return edCategory;
    }

    public void setEdCategory(EditText edCategory) {
        this.edCategory = edCategory;
    }

    public EditText getEdRichMediaUri() {
        return edRichMediaUri;
    }

    public void setEdRichMediaUri(EditText edRichMediaUri) {
        this.edRichMediaUri = edRichMediaUri;
    }

    public CheckBox getCbVivo() {
        return cbVivo;
    }

    public CheckBox getCbDisableTitle() {
        return cbDisableTitle;
    }

    public CheckBox getCbForceDetail() {
        return cbForceDetail;
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

    public EditText getEtHWImportance() {
        return etHWImportance;
    }

    public void setEtHWImportance(EditText etHWImportance) {
        this.etHWImportance = etHWImportance;
    }
}
