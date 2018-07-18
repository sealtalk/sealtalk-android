package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.ChangePasswordResponse;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.LoadDialog;

/**
 * Created by AMing on 16/6/23.
 * Company RongCloud
 */
public class UpdatePasswordActivity extends BaseActivity implements View.OnClickListener {

    private static final int UPDATE_PASSWORD = 15;

    private EditText oldPasswordEdit, newPasswordEdit, newPassword2Edit;
    private String mOldPassword, mNewPassword;
    private Button mConfirm;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pswd);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        setTitle(R.string.change_password);
        initViews();
    }

    private void initViews() {
        oldPasswordEdit = (EditText) findViewById(R.id.old_password);
        newPasswordEdit = (EditText) findViewById(R.id.new_password);
        newPassword2Edit = (EditText) findViewById(R.id.new_password2);
        mConfirm = (Button) findViewById(R.id.update_pswd_confirm);
        mConfirm.setOnClickListener(this);
        mConfirm.setEnabled(false);
        oldPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setConformButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        newPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setConformButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        newPassword2Edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setConformButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setConformButtonState() {
        if (TextUtils.isEmpty(oldPasswordEdit.getText().toString().trim())
                && TextUtils.isEmpty(newPasswordEdit.getText().toString().trim())
                && TextUtils.isEmpty(oldPasswordEdit.getText().toString().trim())) {
            mConfirm.setEnabled(false);
        } else {
            mConfirm.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        String old = oldPasswordEdit.getText().toString().trim();
        String new1 = newPasswordEdit.getText().toString().trim();
        String new2 = newPassword2Edit.getText().toString().trim();
        String cachePassword = sp.getString(SealConst.SEALTALK_LOGING_PASSWORD, "");
        if (TextUtils.isEmpty(old)) {
            NToast.shortToast(mContext, R.string.original_password);
            return;
        }
        if (TextUtils.isEmpty(new1)) {
            NToast.shortToast(mContext, R.string.new_password_not_null);
            return;
        }

        if (new1.length() < 6 || new1.length() > 16) {
            NToast.shortToast(mContext, R.string.passwords_invalid);
            return;
        }

        if (TextUtils.isEmpty(new2)) {
            NToast.shortToast(
                mContext, R.string.confirm_password_not_null);
            return;
        }
        if (!cachePassword.equals(old)) {
            NToast.shortToast(mContext, R.string.original_password_mistake);
            return;
        }
        if (!new1.equals(new2)) {
            NToast.shortToast(mContext, R.string.passwords_do_not_match);
            return;
        }

        if (new1.equals(old)) {
            NToast.shortToast(mContext, R.string.new_and_old_password);
            return;
        }

        mOldPassword = old;
        mNewPassword = new1;
        LoadDialog.show(mContext);
        request(UPDATE_PASSWORD, true);

    }


    @Override
    public Object doInBackground(int requestCode, String id) throws HttpException {
        return action.changePassword(mOldPassword, mNewPassword);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        ChangePasswordResponse cpRes = (ChangePasswordResponse) result;
        if (cpRes.getCode() == 200) {
            editor.putString(SealConst.SEALTALK_LOGING_PASSWORD, newPasswordEdit.getText().toString().trim());
            editor.commit();
            NToast.shortToast(mContext, getString(R.string.update_success));
            LoadDialog.dismiss(mContext);
            finish();
        } else if (cpRes.getCode() == 1000) {
            NToast.shortToast(mContext, getString(R.string.original_password_mistake));
            LoadDialog.dismiss(mContext);
        } else {
            NToast.shortToast(mContext, "修改密码失败:" + cpRes.getCode());
            LoadDialog.dismiss(mContext);
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        LoadDialog.dismiss(mContext);
        NToast.shortToast(mContext, "修改密码请求失败");
    }
}
