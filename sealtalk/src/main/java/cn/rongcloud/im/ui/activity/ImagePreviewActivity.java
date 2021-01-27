package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.ui.dialog.OperatePictureBottomDialog;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class ImagePreviewActivity extends TitleBaseActivity implements View.OnClickListener {

    private ImageView ivCotnet;
    private TextView tvCancel;
    private TextView tvSet;
    private TextView tvSend;
    private LinearLayout llSet;
    private RelativeLayout rlOrgin;
    private LinearLayout llSelectOrgin;
    private CheckBox cbSelectOrgin;
    private String uri;
    public static final int FROM_ALUMB = 0x1247;
    public static final int FROM_DEFAULT = 0x1237;
    public static final int FROM_RECENT_PICTURE = 0x1222;
    public static final int FROM_EDIT_USER_DESCRIBE = 0x1224;
    private int mType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        Intent intent = getIntent();
        uri = intent.getStringExtra(IntentExtra.URL);
        mType = intent.getIntExtra(IntentExtra.IMAGE_PREVIEW_TYPE, 0);
        inintView();
    }

    private void inintView() {
        rlOrgin = findViewById(R.id.rl_orgin);
        llSelectOrgin = findViewById(R.id.ll_select_orgin);
        cbSelectOrgin = findViewById(R.id.cb_select_orgin);
        llSelectOrgin.setOnClickListener(this);
        llSet = findViewById(R.id.ll_set);
        tvSend = findViewById(R.id.tv_send);
        tvSend.setOnClickListener(this);
        ivCotnet = findViewById(R.id.iv_content);
        SealTitleBar titleBar = getTitleBar();
        TextView tvRight = titleBar.getTvRight();
        if (mType == FROM_DEFAULT) {
            llSet.setVisibility(View.VISIBLE);
            titleBar.setTitle(getString(R.string.seal_select_chat_bg_title));
        } else if (mType == FROM_ALUMB) {
            tvRight.setText(R.string.seal_select_chat_bg_set);
        } else if (mType == FROM_RECENT_PICTURE) {
            rlOrgin.setVisibility(View.VISIBLE);
            tvRight.setText(R.string.seal_select_chat_bg_cancel);
        } else if (mType == FROM_EDIT_USER_DESCRIBE) {
            titleBar.setTitle(getString(R.string.profile_picture_detail));
            tvRight.setText(R.string.profile_picture_more);
        }
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mType == FROM_DEFAULT) {
                    finish();
                } else if (mType == FROM_ALUMB) {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (mType == FROM_RECENT_PICTURE) {
                    finish();
                } else if (mType == FROM_EDIT_USER_DESCRIBE) {
                    showOperatePictureDialog();
                }
            }
        });
        if (uri.toLowerCase().startsWith("http://") || uri.toLowerCase().startsWith("https://")) {
            ImageLoaderUtils.displayUserDescritpionImage(uri, ivCotnet);
        } else {
            ivCotnet.setImageURI(Uri.parse(uri));
        }
        tvCancel = findViewById(R.id.tv_cancel);
        tvSet = findViewById(R.id.tv_set);
        tvSet.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }

    private void showOperatePictureDialog() {
        OperatePictureBottomDialog operatePictureBottomDialog = new OperatePictureBottomDialog();
        operatePictureBottomDialog.setOnDialogButtonClickListener(new OperatePictureBottomDialog.OnDialogButtonClickListener() {
            @Override
            public void onClickSave() {
                Intent intentSend = new Intent();
                intentSend.putExtra(IntentExtra.OPERATE_PICTURE_ACTION, EditUserDescribeActivity.OPERATE_PICTURE_SAVE);
                setResult(RESULT_OK, intentSend);
                finish();
            }

            @Override
            public void onClickDelete() {
                Intent intentSend = new Intent();
                intentSend.putExtra(IntentExtra.OPERATE_PICTURE_ACTION, EditUserDescribeActivity.OPERATE_PICTURE_DELETE);
                setResult(RESULT_OK, intentSend);
                finish();
            }
        });
        operatePictureBottomDialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_set:
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.tv_cancel:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.ll_select_orgin:
                cbSelectOrgin.setChecked(!cbSelectOrgin.isChecked());
                break;
            case R.id.tv_send:
                Intent intentSend = new Intent();
                intentSend.putExtra(IntentExtra.URL, "file://" + uri);
                intentSend.putExtra(IntentExtra.ORGIN, cbSelectOrgin.isChecked());
                setResult(RESULT_OK, intentSend);
                finish();
                break;
        }
    }
}
