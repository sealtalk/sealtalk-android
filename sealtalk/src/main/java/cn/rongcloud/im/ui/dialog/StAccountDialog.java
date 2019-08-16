package cn.rongcloud.im.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import cn.rongcloud.im.R;

/**
 * 通用对话框
 */
public class StAccountDialog extends DialogFragment {

    private static class ControllerParams {
        public boolean isCancelable;
        public CharSequence contentMessage;
        public CharSequence titleMessage;
        public SpannableString contentSpanna;
        public Bundle expandParams;
        public OnDialogButtonClickListener listener;
        public int positiveText;
        public int negativeText;
        private boolean isOnlyConfirm;
    }

    private ControllerParams params;

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

        View view = getDialogView();
        if (view == null) {
            view = View.inflate(getContext(), R.layout.dialog_set_staccount, null);
        }
        Button negative = view.findViewById(R.id.dialog_btn_negative);
        Button positive = view.findViewById(R.id.dialog_btn_positive);
        View btnSeparate = view.findViewById(R.id.dialog_v_btn_separate);
        RelativeLayout contentContainer = view.findViewById(R.id.dialog_content_container);
        TextView content = view.findViewById(R.id.dialog_tv_content);
        TextView title = view.findViewById(R.id.dialog_tv_title);
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onNegativeClick()) {
                    return ;
                }
                if (params.listener != null) {
                    params.listener.onNegativeClick(v, getNegativeDatas());
                }
            }
        });
        positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onPositiveClick()) {
                    return ;
                }
                if (params.listener != null) {
                    params.listener.onPositiveClick(v, getPositiveDatas());
                }
            }
        });

        if (params != null) {
            if (!TextUtils.isEmpty(params.contentMessage)){
                content.setText(Html.fromHtml(params.contentMessage.toString()));
            }

            if (!TextUtils.isEmpty(params.titleMessage)){
                title.setText(Html.fromHtml(params.titleMessage.toString()));
            }

            if (params.contentSpanna!=null&&params.contentSpanna.length()>0){
                content.setText(params.contentSpanna);
            }

            if (params.positiveText > 0) {
                positive.setText(params.positiveText);
            }

            if (params.negativeText > 0) {
                negative.setText(params.negativeText);
            }

            if(params.isOnlyConfirm){
                negative.setVisibility(View.GONE);
                btnSeparate.setVisibility(View.GONE);
                positive.setBackgroundResource(R.drawable.common_dialog_single_positive_seletor);
            }

            setCancelable(params.isCancelable);
        }

        Dialog dialog = getDialog();
        if(dialog != null){
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        return view;

    }

    /**
     * 此方法只提供给布局改变， 但是控件id 不变的自定义 dialog 使用
     * @return
     */
    protected  View getDialogView() {
        return null;
    }


    /**
     * 通过复写此方法， 在子类中，可重新创建设置
     * 新的内容布局
     * @return
     */
    protected View onCreateContentView() {
        return null;
    }

    /**
     * 复写此方法， 并可在此方法中设置，回调监听确定按钮所需的数据
     * @return
     */
    protected Bundle getPositiveDatas() {
        return null;
    }

    /**
     * 复写此方法， 并可在此方法中设置，回调监听取消按钮所需的数据
     * @return
     */
    protected Bundle getNegativeDatas() {
        return null;
    }

    /**
     * 集成的子类假如想在内部处理 Positive 点击监听， 可复写此方法。 返回 true 则可拦截，不会走外部设置的点击监听
     * @return true 拦截监听， false 不拦截
     */
    protected boolean onPositiveClick() {
        return false;
    }

    /**
     *  集成的子类假如想在内部处理 Negative 点击监听， 可复写此方法。 返回 true 则可拦截，不会走外部设置的点击监听
     * @return
     */
    protected boolean onNegativeClick() {
        return false;
    }


    private void setParams(ControllerParams params) {
        this.params = params;
    }

    public Bundle getExpandParams() {
        if (params == null) {
            return  null;
        }
        return params.expandParams;
    }


    public interface OnDialogButtonClickListener {
        void onPositiveClick(View v, Bundle bundle);
        void onNegativeClick(View v, Bundle bundle);
    }


    /**
     * 集成 CommonDialog 的子类， 需要继承此类， 并要复写
     * getCurrentDialog 方法，返回子类的dialog 对象
     */
    public static class Builder {
        private ControllerParams params ;
        public Builder() {
            params = new ControllerParams();
        }

        public Builder setContentSpanna(SpannableString content) {
            params.contentSpanna = content;
            return this;
        }

        public Builder setContentMessage(CharSequence content) {
            params.contentMessage = content;
            return this;
        }

        public Builder setTitleMessage(CharSequence title){
            params.titleMessage = title;
            return this;
        }

        public Builder isCancelable(boolean cancelable) {
            params.isCancelable = cancelable;
            return this;
        }

        public Builder setButtonText(int positiveText, int negativeText) {
            params.positiveText = positiveText;
            params.negativeText = negativeText;
            return this;
        }



        public Builder setDialogButtonClickListener(OnDialogButtonClickListener listener) {
            params.listener = listener;
            return this;
        }


        public Builder setExpandParams(Bundle expandParams) {
            params.expandParams = expandParams;
            return this;
        }

        public Builder setIsOnlyConfirm(boolean isOnlyConfirm){
            params.isOnlyConfirm = isOnlyConfirm;
            return this;
        }

        public StAccountDialog build() {
            StAccountDialog dialog = getCurrentDialog();
            dialog.setParams(params);
            return dialog;
        }


        protected StAccountDialog getCurrentDialog() {
            return new StAccountDialog();
        }
    }
}
