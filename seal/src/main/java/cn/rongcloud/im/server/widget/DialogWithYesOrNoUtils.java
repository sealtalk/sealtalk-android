package cn.rongcloud.im.server.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.utils.NToast;


/**
 * Created by AMing on 15/11/26.
 * Company RongCloud
 */
public class DialogWithYesOrNoUtils {

    private static DialogWithYesOrNoUtils instance = null;

    public static DialogWithYesOrNoUtils getInstance() {
        if (instance == null) {
            instance = new DialogWithYesOrNoUtils();
        }
        return instance;
    }

    private DialogWithYesOrNoUtils() {
    }

    public void showDialog(Context context, String titleInfo, final DialogWithYesOrNoUtils.DialogCallBack callBack) {
        AlertDialog.Builder alterDialog = new AlertDialog.Builder(context);
        alterDialog.setMessage(titleInfo);
        alterDialog.setCancelable(true);

        alterDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callBack.executeEvent();
            }
        });
        alterDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alterDialog.show();
    }

    public interface DialogCallBack {
        void executeEvent();

        void executeEditEvent(String editText);

        void updatePassword(String oldPassword, String newPassword);
    }


    public void showEditDialog(Context context, String hintText, String OKText, final DialogWithYesOrNoUtils.DialogCallBack callBack) {
        final EditText et_search;
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_view, null);
        dialog.setView(layout);
        et_search = (EditText) layout.findViewById(R.id.searchC);
        et_search.setHint(hintText);
        dialog.setPositiveButton(OKText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String s = et_search.getText().toString().trim();
                callBack.executeEditEvent(s);
            }
        });

        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }

        });
        dialog.show();
    }


    public void showUpdatePasswordDialog(final Context context, final DialogWithYesOrNoUtils.DialogCallBack callBack) {
        final EditText oldPasswordEdit, newPasswrodEdit, newPassword2Edit;
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialogchangeview, null);
        dialog.setView(layout);
        oldPasswordEdit = (EditText) layout.findViewById(R.id.old_password);
        newPasswrodEdit = (EditText) layout.findViewById(R.id.new_password);
        newPassword2Edit = (EditText) layout.findViewById(R.id.new_password2);
        dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String old = oldPasswordEdit.getText().toString().trim();
                String new1 = newPasswrodEdit.getText().toString().trim();
                String new2 = newPassword2Edit.getText().toString().trim();
                String cachePassword = context.getSharedPreferences("config", Context.MODE_PRIVATE).getString(SealConst.SEALTALK_LOGING_PASSWORD, "");
                if (TextUtils.isEmpty(old)) {
                    NToast.shortToast(context, R.string.original_password);
                    return;
                }
                if (TextUtils.isEmpty(new1)) {
                    NToast.shortToast(context, R.string.new_password_not_null);
                    return;
                }
                if (new1.length() < 6 || new1.length() > 16) {
                    NToast.shortToast(context, R.string.passwords_invalid);
                    return;
                }
                if (TextUtils.isEmpty(new2)) {
                    NToast.shortToast(context, R.string.confirm_password_not_null);
                    return;
                }
                if (!cachePassword.equals(old)) {
                    NToast.shortToast(context, R.string.original_password_mistake);
                    return;
                }
                if (!new1.equals(new2)) {
                    NToast.shortToast(context, R.string.passwords_do_not_match);
                    return;
                }
                callBack.updatePassword(old, new1);
            }
        });

        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }

        });
        dialog.show();
    }

}
