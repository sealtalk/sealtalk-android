package cn.rongcloud.im.ui.dialog;

import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.LoginActivity;
import cn.rongcloud.im.ui.activity.WebViewActivity;

/** 未同意隐私合规弹窗 */
public class PrivacyDialog extends CommonDialog {

    @Override
    protected View onCreateContentView(ViewGroup container) {
        TextView contentView = container.findViewById(R.id.dialog_tv_content);
        setContentText(contentView);
        return contentView;
    }

    private void setContentText(TextView contentView) {
        final String registrationTitle = getString(R.string.seal_talk_registration_title);
        final String privacyPolicyTitle = getString(R.string.seal_talk_privacy_policy_title);
        contentView.setText(
                Html.fromHtml(
                        "<font color='#5C6970'>"
                                + getString(R.string.seal_talk_login_bottom_registration_text_front)
                                + "</font>"
                                + "<br>"
                                + "<font color='#5C6970'>"
                                + String.format(
                                        getString(
                                                R.string
                                                        .seal_talk_login_bottom_registration_text_behand),
                                        registrationTitle,
                                        privacyPolicyTitle)
                                + "</font>"));

        String text = contentView.getText().toString();
        int indexRegistration = text.indexOf(registrationTitle);
        if (indexRegistration == -1) {
            return;
        }
        SpannableString str = new SpannableString(contentView.getText());
        str.setSpan(
                new LoginActivity.NoRefCopySpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Intent intent = new Intent(getContext(), WebViewActivity.class);
                        intent.putExtra(WebViewActivity.PARAMS_TITLE, registrationTitle);
                        intent.putExtra(
                                WebViewActivity.PARAMS_URL,
                                "file:///android_asset/agreement_zh.html");
                        startActivity(intent);
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                },
                indexRegistration - 1,
                indexRegistration + registrationTitle.length() + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        int indexPrivacyPolicy = text.indexOf(privacyPolicyTitle);

        str.setSpan(
                new LoginActivity.NoRefCopySpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Intent intent = new Intent(getContext(), WebViewActivity.class);
                        intent.putExtra(WebViewActivity.PARAMS_TITLE, privacyPolicyTitle);
                        intent.putExtra(
                                WebViewActivity.PARAMS_URL,
                                "file:///android_asset/PrivacyPolicy_zh.html");
                        startActivity(intent);
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                },
                indexPrivacyPolicy - 1,
                indexPrivacyPolicy + privacyPolicyTitle.length() + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        contentView.setText(str);
        contentView.setTextSize(14);
        contentView.setMovementMethod(LinkMovementMethod.getInstance()); // 不设置 没有点击事件
        contentView.setHighlightColor(Color.TRANSPARENT); // 设置点击后的颜色为透明
    }

    public static class Builder extends CommonDialog.Builder {
        @Override
        protected CommonDialog getCurrentDialog() {
            return new PrivacyDialog();
        }
    }
}
