package cn.rongcloud.im.ui.activity;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.UpdateService;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.server.widget.DialogWithYesOrNoUtils;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.common.BuildVar;

public class AboutRongCloudActivity extends BaseActivity {

    private boolean isHasNewVersion;
    private ImageView mNewVersionView;
    private String url;
    long[] mHits = new long[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle(R.string.set_rongcloud);

        RelativeLayout mUpdateLog = (RelativeLayout) findViewById(R.id.rl_update_log);
        RelativeLayout mFunctionIntroduce = (RelativeLayout) findViewById(R.id.rl_function_introduce);
        RelativeLayout mRongCloudWeb = (RelativeLayout) findViewById(R.id.rl_rongcloud_web);
        mNewVersionView = (ImageView) findViewById(R.id.about_sealtalk_version);
        TextView mSDKVersion = (TextView) findViewById(R.id.sdk_version_text);
        RelativeLayout mVersionItem = (RelativeLayout) findViewById(R.id.rl_version);
        TextView version = (TextView) findViewById(R.id.sealtalk_version);
        RelativeLayout mCloseDebug = (RelativeLayout) findViewById(R.id.close_debug);
        RelativeLayout mStartDebug = (RelativeLayout) findViewById(R.id.start_debug);
        RelativeLayout mSetOnlineStatus = (RelativeLayout) findViewById(R.id.set_online_status);
        version.setText(getVersionInfo()[1]);
        mUpdateLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AboutRongCloudActivity.this, UpdateLogActivity.class));
            }
        });
        mFunctionIntroduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AboutRongCloudActivity.this, FunctionIntroducedActivity.class));
            }
        });

        mRongCloudWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AboutRongCloudActivity.this, RongWebActivity.class));
            }
        });
        url = getIntent().getStringExtra("url");
        isHasNewVersion = getIntent().getBooleanExtra("isHasNewVersion", false);
        if (isHasNewVersion) {
            mNewVersionView.setVisibility(View.VISIBLE);
            mVersionItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNewVersionView.setVisibility(View.GONE);
                    final AlertDialog dlg = new AlertDialog.Builder(AboutRongCloudActivity.this).create();
                    dlg.show();
                    Window window = dlg.getWindow();
                    window.setContentView(R.layout.dialog_download);
                    TextView text = (TextView) window.findViewById(R.id.friendship_content1);
                    TextView photo = (TextView) window.findViewById(R.id.friendship_content2);
                    text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse(url);
                            intent.setData(content_url);
                            startActivity(intent);
                            dlg.cancel();
                        }
                    });
                    photo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NToast.shortToast(mContext, getString(R.string.downloading_apk));
                            UpdateService.Builder.create(url)
                                    .setStoreDir("update/flag")
                                    .setDownloadSuccessNotificationFlag(Notification.DEFAULT_ALL)
                                    .setDownloadErrorNotificationFlag(Notification.DEFAULT_ALL)
                                    .build(mContext);
                            dlg.cancel();
                        }
                    });
                    isHasNewVersion = false;
                }
            });
        }


        mStartDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] > SystemClock.uptimeMillis() - 10000) {
                    if (getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false)) {
                        NToast.shortToast(mContext, "debug 模式已开启");
                    } else {
                        DialogWithYesOrNoUtils.getInstance().showDialog(mContext, "是否开启 App Debug 模式(需要重新登录应用)?", new DialogWithYesOrNoUtils.DialogCallBack() {
                            @Override
                            public void executeEvent() {
                                SharedPreferences.Editor editor = getSharedPreferences("config", MODE_PRIVATE).edit();
                                editor.putBoolean("isDebug", true);
                                editor.commit();
                                BroadcastManager.getInstance(mContext).sendBroadcast(SealConst.EXIT);

                            }

                            @Override
                            public void executeEditEvent(String editText) {

                            }

                            @Override
                            public void updatePassword(String oldPassword, String newPassword) {

                            }
                        });
                    }
                }
            }
        });

        if (getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false)) {
            mCloseDebug.setVisibility(View.VISIBLE);
            mCloseDebug.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogWithYesOrNoUtils.getInstance().showDialog(mContext, "是否关闭 App Debug 模式(需要重新登录应用)?", new DialogWithYesOrNoUtils.DialogCallBack() {
                        @Override
                        public void executeEvent() {
                            SharedPreferences.Editor editor = getSharedPreferences("config", MODE_PRIVATE).edit();
                            editor.putBoolean("isDebug", false);
                            editor.commit();
                            BroadcastManager.getInstance(mContext).sendBroadcast(SealConst.EXIT);

                        }

                        @Override
                        public void executeEditEvent(String editText) {

                        }

                        @Override
                        public void updatePassword(String oldPassword, String newPassword) {

                        }
                    });
                }
            });

            mSetOnlineStatus.setVisibility(View.VISIBLE);
            mSetOnlineStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOnlineStatusDialog();
                }
            });

        }

        mSDKVersion.setText(BuildVar.SDK_VERSION);
    }


    private void showOnlineStatusDialog() {
        String[] items = new String[2];

        items[0] = getString(R.string.ipad_online);
        items[1] = getString(R.string.imac_online);
        OptionsPopupDialog.newInstance(this, items).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
            @Override
            public void onOptionsItemClicked(int which) {
                if (which == 0) {
                    RongIMClient.getInstance().setUserOnlineStatus(5, null);
                } else if (which == 1) {
                    RongIMClient.getInstance().setUserOnlineStatus(6, null);
                }
            }
        }).show();

    }

    private String[] getVersionInfo() {
        String[] version = new String[2];

        PackageManager packageManager = getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            version[0] = String.valueOf(packageInfo.versionCode);
            version[1] = packageInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }
}
