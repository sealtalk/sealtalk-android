package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.im.IMManager;
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
import io.rong.calllib.ReportUtil;
import io.rong.common.RLog;
import io.rong.imkit.IMCenter;
import io.rong.imkit.RongIM;
import io.rong.imkit.userinfo.RongUserInfoManager;

import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imkit.utils.language.LangUtils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.cs.model.CSCustomServiceInfo;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;

public class MainMeFragment extends BaseFragment {

    private SettingItemView sivAbout;
    private UserInfoItemView uivUserInfo;
    private AppViewModel appViewModel;
    private SettingItemView sivLanguage;
    private UserInfoViewModel userInfoViewModel;

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
        findView(R.id.siv_feedback, true);
        sivAbout = findView(R.id.siv_about, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(IMManager.getInstance().getCurrentId())) {
            io.rong.imlib.model.UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(IMManager.getInstance().getCurrentId());
            if (userInfo == null) {
                userInfoViewModel.requestUserInfo(IMManager.getInstance().getCurrentId());
            }
        }
    }

    @Override
    protected void onInitViewModel() {
        appViewModel = ViewModelProviders.of(getActivity()).get(AppViewModel.class);
        RongUserInfoManager.getInstance().addUserDataObserver(mUserDataObserver);

        userInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        userInfoViewModel.getUserInfo().observe(getActivity(), new Observer<Resource<UserInfo>>() {
            @Override
            public void onChanged(Resource<UserInfo> resource) {
                if (resource.data != null) {
                    UserInfo info = resource.data;
                    uivUserInfo.setName(info.getName());
                    if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
                        if (!TextUtils.isEmpty(info.getPortraitUri()) && getActivity() != null) {
                            Glide.with(getActivity()).load(info.getPortraitUri()).placeholder(R.drawable.rc_default_portrait).into(uivUserInfo.getHeaderImageView());
                        }
                    }
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
                } else if (rcLocale == LangUtils.RCLocale.LOCALE_CHINA) {
                    sivLanguage.setValue(R.string.lang_chs);
                } else if (rcLocale == LangUtils.RCLocale.LOCALE_ARAB) {
                    sivLanguage.setValue(R.string.lang_arab);
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
            case R.id.siv_feedback:
                CSCustomServiceInfo.Builder builder = new CSCustomServiceInfo.Builder();
                builder.province(getString(R.string.beijing));
                builder.city(getString(R.string.beijing));
                io.rong.imlib.model.UserInfo info = RongUserInfoManager.getInstance().getUserInfo(RongIM.getInstance().getCurrentUserId());
                if (info != null && !TextUtils.isEmpty(info.getName())) {
                    builder.name(info.getName());
                }
                //佳信客服配置
                builder.referrer("10001");
                Bundle bundle = new Bundle();
                bundle.putString(RouteUtils.TITLE, getString(R.string.seal_main_mine_online_custom_service));
                bundle.putParcelable(RouteUtils.CUSTOM_SERVICE_INFO, builder.build());
                RouteUtils.routeToConversationActivity(getContext(), Conversation.ConversationType.CUSTOMER_SERVICE, "service"
                        , bundle);
                break;
            case R.id.siv_about:
                Intent intent = new Intent(getActivity(), AboutSealTalkActivity.class);
                Resource<VersionInfo.AndroidVersion> resource = appViewModel.getHasNewVersion().getValue();
                if (resource != null && resource.data != null && !TextUtils.isEmpty(resource.data.getUrl())) {
                    intent.putExtra(IntentExtra.URL, resource.data.getUrl());
                }
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RongUserInfoManager.getInstance().removeUserDataObserver(mUserDataObserver);
    }

    private RongUserInfoManager.UserDataObserver mUserDataObserver = new RongUserInfoManager.UserDataObserver() {
        @Override
        public void onUserUpdate(io.rong.imlib.model.UserInfo userInfo) {
            if (userInfo != null && getActivity() != null && userInfo.getUserId().equals(RongIMClient.getInstance().getCurrentUserId())) {
                ThreadManager.getInstance().runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getActivity()).load(userInfo.getPortraitUri()).placeholder(R.drawable.rc_default_portrait).into(uivUserInfo.getHeaderImageView());
                        uivUserInfo.setName(userInfo.getName());
                    }
                });
            }
        }

        @Override
        public void onGroupUpdate(Group group) {

        }

        @Override
        public void onGroupUserInfoUpdate(GroupUserInfo groupUserInfo) {

        }
    };
}
