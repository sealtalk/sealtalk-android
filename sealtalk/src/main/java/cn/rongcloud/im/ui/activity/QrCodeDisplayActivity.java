package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.qrcode.QrCodeDisplayType;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.ViewCapture;
import cn.rongcloud.im.viewmodel.DisplayQRCodeViewModel;
import cn.rongcloud.im.utils.log.SLog;

/**
 * 显示二维码界面
 */
public class QrCodeDisplayActivity extends TitleBaseActivity implements View.OnClickListener {
    private final String TAG = "QrCodeDisplayActivity";
    private QrCodeDisplayType qrType;
    private String targetId;
    private String fromId;
    private SealTitleBar sealTitleBar;

    private LinearLayout qrCodeCardLl;
    private ImageView portraitIv;
    private TextView mainInfoTv;
    private TextView subInfoTv;
    private ImageView qrCodeIv;

    private DisplayQRCodeViewModel qrCodeViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sealTitleBar = getTitleBar();
        Intent intent = getIntent();
        if (intent == null) {
            SLog.d(TAG, "intent can't null, to finish.");
            finish();
            return;
        }

        qrType = (QrCodeDisplayType) intent.getSerializableExtra(IntentExtra.SERIA_QRCODE_DISPLAY_TYPE);
        targetId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        fromId = intent.getStringExtra(IntentExtra.START_FROM_ID);

        if (qrType == null || targetId == null) {
            SLog.d(TAG, "qrType and targetId can't null, to finish.");
            finish();
            return;
        }

        setContentView(R.layout.profile_activity_show_qrcode);

        initView();
        initViewModel();
    }

    private void initView() {
        // 二维码描述
        TextView qrCodeDescribeTv = findViewById(R.id.profile_tv_qr_card_info_describe);
        if (qrType == QrCodeDisplayType.GROUP) {
            sealTitleBar.setTitle(R.string.profile_group_qrcode);
            qrCodeDescribeTv.setText(R.string.profile_qrcode_group_tips);
        } else if (qrType == QrCodeDisplayType.PRIVATE) {
            sealTitleBar.setTitle(R.string.seal_main_mine_qrcode);
            qrCodeDescribeTv.setText(R.string.profile_qrcode_private_tips);
        }
        // 二维码卡片父容器
        qrCodeCardLl = findViewById(R.id.profile_ll_qr_card_container);
        // 二维码信息所属头像
        portraitIv = findViewById(R.id.profile_iv_card_info_portrait);
        // 二维码信息所属名称
        mainInfoTv = findViewById(R.id.profile_tv_qr_info_main);
        // 二维码信息所属副信息
        subInfoTv = findViewById(R.id.profile_tv_qr_info_sub);
        // 二维码图片
        qrCodeIv = findViewById(R.id.profile_iv_qr_code);
        // 保存图片
        findViewById(R.id.profile_tv_qr_save_phone).setOnClickListener(this);
    }

    private void initViewModel() {
        qrCodeViewModel = ViewModelProviders.of(this).get(DisplayQRCodeViewModel.class);

        // 获取 QRCode 结果
        qrCodeViewModel.getQRCode().observe(this, resource -> {
            if (resource.data != null) {
                qrCodeIv.setImageBitmap(resource.data);
            }
        });

        ViewGroup.LayoutParams qrCodeLayoutParams = qrCodeIv.getLayoutParams();

        if (qrType == QrCodeDisplayType.GROUP) {
            // 获取群组信息结果
            qrCodeViewModel.getGroupInfo().observe(this, resource -> {
                if (resource.data != null) {
                    updateGroupInfo(resource.data);
                }
            });
            // 请求群组信息
            qrCodeViewModel.requestGroupInfo(targetId);
            // 获取群组二维码
            qrCodeViewModel.requestGroupQRCode(targetId, fromId, qrCodeLayoutParams.width, qrCodeLayoutParams.height);
        } else if (qrType == QrCodeDisplayType.PRIVATE) {
            // 获取用户信息结果
            qrCodeViewModel.getUserInfo().observe(this, resource -> {
                if (resource.data != null) {
                    updateUserInfo(resource.data);
                }
            });

            // 请求用户信息
            qrCodeViewModel.requestUserInfo(targetId);
            // 获取用户二维码
            qrCodeViewModel.requestUserQRCode(targetId, qrCodeLayoutParams.width, qrCodeLayoutParams.height);
        }


        // 获取保存二维码结果
        qrCodeViewModel.getSaveBitmapResult().observe(this, resource -> {
            if (resource.status == Status.SUCCESS) {
                // 保存成功后加入媒体扫描中，使相册中可以显示此图片
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{resource.data}, null, null);

                String msg = getString(R.string.profile_save_picture_at) + ":" + resource.data;
                ToastUtils.showToast(msg, Toast.LENGTH_LONG);
            }
        });
    }

    /**
     * 更新群组相关信息
     *
     * @param groupEntity
     */
    private void updateGroupInfo(GroupEntity groupEntity) {
        ImageLoaderUtils.displayGroupPortraitImage(groupEntity.getPortraitUri(), portraitIv);
        mainInfoTv.setText(groupEntity.getName());
        subInfoTv.setText(getString(R.string.common_member_count, groupEntity.getMemberCount()));
    }

    /**
     * 更新用户相关信息
     *
     * @param userInfo
     */
    private void updateUserInfo(UserInfo userInfo) {
        ImageLoaderUtils.displayUserPortraitImage(userInfo.getPortraitUri(), portraitIv);
        mainInfoTv.setText(userInfo.getName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_tv_qr_save_phone:
                saveQRCodeToLocal();
                break;
        }
    }

    /**
     * 保存二维码到本地
     */
    private void saveQRCodeToLocal() {
        qrCodeViewModel.saveQRCodeToLocal(ViewCapture.getViewBitmap(qrCodeCardLl));
    }
}
