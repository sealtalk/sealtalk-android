package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendDescription;
import cn.rongcloud.im.file.FileManager;
import cn.rongcloud.im.model.CountryInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.dialog.SelectPictureBottomDialog;
import cn.rongcloud.im.utils.AsyncImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.PhotoUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.EditUserDescribeViewModel;


public class EditUserDescribeActivity extends TitleBaseActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_SELECT_COUNTRY = 1020;
    private EditText etMore;
    private EditText etPhone;
    private EditText etDisplayName;
    private TextView tvMoreNum;
    private TextView tvRegion;
    private AsyncImageView ivPhoto;
    private FrameLayout flMore;
    private String userId;
    private String mUri;
    private EditUserDescribeViewModel editUserDescribeViewModel;
    private FileManager fileManager;
    private final int REQUEST_OPERATION_PICTURE = 2889;
    public final static int OPERATE_PICTURE_SAVE = 0x1212;
    public final static int OPERATE_PICTURE_DELETE = 0x1211;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_describe);
        userId = getIntent().getStringExtra(IntentExtra.STR_TARGET_ID);
        fileManager = new FileManager(this);
        initView();
        initViewModel();
    }

    private void initView() {
        getTitleBar().setTitle(getString(R.string.profile_set_display_name));
        getTitleBar().getBtnRight().setVisibility(View.GONE);
        getTitleBar().setOnBtnRightClickListener(getString(R.string.seal_describe_more_btn_complete), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSaveConfirmDialog();
            }
        });
        etDisplayName = findViewById(R.id.et_display_name);
        etPhone = findViewById(R.id.et_phone);
        tvMoreNum = findViewById(R.id.tv_more_num);
        tvMoreNum.setText(getString(R.string.seal_describe_more_num, 0));
        etMore = findViewById(R.id.et_more);
        etMore.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tvMoreNum.setText(getString(R.string.seal_describe_more_num, s.length()));
            }
        });
        ivPhoto = findViewById(R.id.iv_photo);
        flMore = findViewById(R.id.fl_more);
        flMore.setOnClickListener(this);
        tvRegion = findViewById(R.id.tv_region);
        tvRegion.setOnClickListener(this);
    }

    private void setMorePhotoDialog() {
        SelectPictureBottomDialog.Builder builder = new SelectPictureBottomDialog.Builder();
        builder.setOnSelectPictureListener(new SelectPictureBottomDialog.OnSelectPictureListener() {
            @Override
            public void onSelectPicture(Uri uri) {
                insertPhoto(uri);
            }
        });
        SelectPictureBottomDialog dialog = builder.build();
        dialog.setType(PhotoUtils.NO_CROP);
        dialog.show(getSupportFragmentManager(), null);
    }

    private void initViewModel() {
        editUserDescribeViewModel = ViewModelProviders.of(this,
                new EditUserDescribeViewModel.Factory(getApplication(), userId)).get(EditUserDescribeViewModel.class);
        editUserDescribeViewModel.getFriendDescription().observe(this, new Observer<Resource<FriendDescription>>() {
            @Override
            public void onChanged(Resource<FriendDescription> friendDescriptionResource) {
                if (friendDescriptionResource.status != Status.LOADING && friendDescriptionResource.data != null) {
                    updateView(friendDescriptionResource.data);
                }
            }
        });
        editUserDescribeViewModel.setFriendDescriptionResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                if (voidResource.status == Status.SUCCESS) {
                    dismissLoadingDialog();
                    ToastUtils.showToast(R.string.seal_describe_more_btn_set_success);
                    finish();
                } else if (voidResource.status == Status.ERROR) {
                    dismissLoadingDialog();
                    ToastUtils.showToast(R.string.seal_describe_more_btn_set_fail);
                    finish();
                }
            }
        });
    }

    private void updateView(FriendDescription friendDescriptionResource) {
        if (!TextUtils.isEmpty(friendDescriptionResource.getDisplayName())) {
            etDisplayName.setText(friendDescriptionResource.getDisplayName(), TextView.BufferType.EDITABLE);
        }
        if (!TextUtils.isEmpty(friendDescriptionResource.getPhone())) {
            etPhone.setText(friendDescriptionResource.getPhone(), TextView.BufferType.EDITABLE);
        }
        if (!TextUtils.isEmpty(friendDescriptionResource.getDescription())) {
            etMore.setText(friendDescriptionResource.getDescription(), TextView.BufferType.EDITABLE);
        }
        if (!TextUtils.isEmpty(friendDescriptionResource.getImageUri())) {
            mUri = friendDescriptionResource.getImageUri();
            ImageLoaderUtils.displayUserDescritpionImage(friendDescriptionResource.getImageUri(), ivPhoto);
        }
        if (!TextUtils.isEmpty(friendDescriptionResource.getRegion())) {
            tvRegion.setText("+" + friendDescriptionResource.getRegion());
        }
    }

    private void showSaveConfirmDialog() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_describe_more_save_tips));
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                setFriendDescription();
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {

            }
        });
        CommonDialog deleteDialog = builder.build();
        deleteDialog.show(getSupportFragmentManager().beginTransaction(), "AddCategoriesDialogFragment");
    }

    private void setFriendDescription() {
        showLoadingDialog("");
        editUserDescribeViewModel.setFriendDescription(userId, etDisplayName.getText().toString(),
                tvRegion.getText().toString().substring(1, tvRegion.length()), etPhone.getText().toString(), etMore.getText().toString(), mUri);
    }

    private void insertPhoto(Uri uri) {
        mUri = uri != null ? uri.toString() : "";
        ivPhoto.setImageURI(null);
        ivPhoto.setImageURI(uri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_COUNTRY) {
            CountryInfo info = data.getParcelableExtra(SelectCountryActivity.RESULT_PARAMS_COUNTRY_INFO);
            SLog.d("edit_des_country", "info = " + info);
            tvRegion.setText(info.getZipCode());
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_OPERATION_PICTURE) {
            if (data.getIntExtra(IntentExtra.OPERATE_PICTURE_ACTION, -1) == OPERATE_PICTURE_SAVE) {
                savePicture();
            } else if (data.getIntExtra(IntentExtra.OPERATE_PICTURE_ACTION, -1) == OPERATE_PICTURE_DELETE) {
                deletePicture();
            }
        }
    }

    private void deletePicture() {
        mUri = "";
        ivPhoto.setImageDrawable(null);
    }

    private void savePicture() {
        Bitmap bitmap = ((BitmapDrawable) ivPhoto.getDrawable()).getBitmap();
        if (bitmap != null) {
            fileManager.saveBitmapToPictures(bitmap).observe(this, new Observer<Resource<String>>() {
                @Override
                public void onChanged(Resource<String> stringResource) {
                    if (stringResource.status == Status.SUCCESS) {
                        SLog.d("saveBitmapToPictures", stringResource.data);
                        ToastUtils.showToast(R.string.seal_dialog_describe_more_save_success);
                        MediaScannerConnection.scanFile(EditUserDescribeActivity.this.getApplicationContext(), new String[]{stringResource.data}, null, null);
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_more:
                if (TextUtils.isEmpty(mUri)) {
                    setMorePhotoDialog();
                } else {
                    if (!isFastClick()) {
                        enterImagePreview();
                    }
                }
                break;
            case R.id.tv_region:
                startActivityForResult(new Intent(this, SelectCountryActivity.class), REQUEST_CODE_SELECT_COUNTRY);
                break;
        }
    }

    private void enterImagePreview() {
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra(IntentExtra.URL, mUri);
        intent.putExtra(IntentExtra.IMAGE_PREVIEW_TYPE, ImagePreviewActivity.FROM_EDIT_USER_DESCRIBE);
        startActivityForResult(intent, REQUEST_OPERATION_PICTURE);
    }

}
