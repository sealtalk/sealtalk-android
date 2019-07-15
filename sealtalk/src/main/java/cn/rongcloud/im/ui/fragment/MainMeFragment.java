package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.jrmf360.rylib.JrmfClient;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.model.qrcode.QrCodeDisplayType;
import cn.rongcloud.im.ui.activity.AboutSealTalkActivity;
import cn.rongcloud.im.ui.activity.AccountSettingActivity;
import cn.rongcloud.im.ui.activity.ChangeLanguageActivity;
import cn.rongcloud.im.ui.activity.MyAccountActivity;
import cn.rongcloud.im.ui.activity.QrCodeDisplayActivity;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.viewmodel.AppViewModel;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;
import io.rong.imkit.RongIM;
import io.rong.imkit.utilities.LangUtils;
import io.rong.imlib.model.CSCustomServiceInfo;

public class MainMeFragment extends BaseFragment {

    private SettingItemView sivAbout;
    private UserInfoItemView uivUserInfo;
    private AppViewModel appViewModel;
    private SettingItemView sivLanguage;

    @Override
    protected int getLayoutResId() {
        return R.layout.main_fragment_me;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {

        uivUserInfo = findView(R.id.uiv_userinfo, true);
        findView(R.id.siv_setting_qrcode, true);
        findView(R.id.siv_setting_account, true);
        sivLanguage = findView(R.id.siv_language, true);
        findView(R.id.siv_my_wallet, true);
        findView(R.id.siv_feedback, true);
        sivAbout = findView(R.id.siv_about, true);
    }

    @Override
    protected void onInitViewModel() {
        UserInfoViewModel userInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        appViewModel = ViewModelProviders.of(getActivity()).get(AppViewModel.class);

        userInfoViewModel.getUserInfo().observe(this, new Observer<Resource<UserInfo>>() {
            @Override
            public void onChanged(Resource<UserInfo> resource) {
                if (resource.data != null) {
                    UserInfo info = resource.data;
                    uivUserInfo.setName(info.getName());
                    ImageLoaderUtils.displayUserPortraitImage(info.getPortraitUri(), uivUserInfo.getHeaderImageView());
                }

            }
        });

        appViewModel.getHasNewVersion().observe(this, new Observer<Resource<VersionInfo.AndroidVersion>>() {
            @Override
            public void onChanged(Resource<VersionInfo.AndroidVersion> resource) {
                if (resource.status == Status.SUCCESS && resource.data != null) {
                    sivAbout.setTagImageVisibility(View.VISIBLE);
                }
            }
        });

        appViewModel.getLanguageLocal().observe(this, new Observer<LangUtils.RCLocale>() {
            @Override
            public void onChanged(LangUtils.RCLocale rcLocale) {
                if (rcLocale == LangUtils.RCLocale.LOCALE_US) {
                    sivLanguage.setValue(R.string.lang_english);
                } else {
                    sivLanguage.setValue(R.string.lang_chs);
                }
            }
        });
    }

    @Override
    protected void onClick(View v, int id) {
        switch (id) {
            case R.id.siv_setting_qrcode:
                Intent qrCodeIntent = new Intent(getActivity(), QrCodeDisplayActivity.class);
                qrCodeIntent.putExtra(IntentExtra.STR_TARGET_ID, RongIM.getInstance().getCurrentUserId());
                qrCodeIntent.putExtra(IntentExtra.SERIA_QRCODE_DISPLAY_TYPE, QrCodeDisplayType.PRIVATE);
                startActivity(qrCodeIntent);
                break;
            case R.id.uiv_userinfo:
                Intent intentUserInfo = new Intent(getActivity(), MyAccountActivity.class);
                startActivity(intentUserInfo);
                break;
            case R.id.siv_setting_account:
                startActivity(new Intent(getActivity(), AccountSettingActivity.class));

                break;
            case R.id.siv_language:
                startActivity(new Intent(getActivity(), ChangeLanguageActivity.class));

                break;
            case R.id.siv_my_wallet:
                JrmfClient.intentWallet(getActivity());
                break;
            case R.id.siv_feedback:
                CSCustomServiceInfo.Builder builder = new CSCustomServiceInfo.Builder();
                builder.province(getString(R.string.beijing));
                builder.city(getString(R.string.beijing));
                RongIM.getInstance().startCustomerServiceChat(getActivity(), "KEFU146001495753714", getString(R.string.seal_main_mine_online_custom_service), builder.build());

                break;
            case R.id.siv_about:
                sivAbout.setTagImageVisibility(View.GONE);
                Intent intent = new Intent(getActivity(), AboutSealTalkActivity.class);
                VersionInfo.AndroidVersion data = appViewModel.getHasNewVersion().getValue().data;
                if (data != null && !TextUtils.isEmpty(data.getUrl())) {
                    intent.putExtra(IntentExtra.URL, data.getUrl());
                }
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
