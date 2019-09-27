package cn.rongcloud.im.ui.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.sp.UserConfigCache;
import cn.rongcloud.im.ui.adapter.decoration.GridSpacingItemDecoration;
import cn.rongcloud.im.ui.adapter.SelectChatBgAdapter;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.utils.PhotoUtils;
import cn.rongcloud.im.utils.ToastUtils;

public class SelectChatBgActivity extends TitleBaseActivity implements View.OnClickListener {

    private RecyclerView rvBgContent;
    private SettingItemView sivAlbum;
    private SelectChatBgAdapter mAdapter;
    private PhotoUtils photoUtils;
    private UserConfigCache userConfig;
    private String currentSelectUri;
    public static final int REQUEST_SET_BG = 0x1209;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_chat_bg);
        userConfig = new UserConfigCache(this);
        initView();
        initPhotoUtil();
    }

    private void initView() {
        getTitleBar().setTitle(R.string.seal_select_chat_bg_title);
        rvBgContent = findViewById(R.id.rl_bg_content);
        mAdapter = new SelectChatBgAdapter(this);
        if (TextUtils.isEmpty(userConfig.getChatbgUri())) {
            userConfig.setChatbgUri(drawableIdtoUri(R.drawable.seal_default_chat_bg1));
        }
        mAdapter.setCheckItem(userConfig.getChatbgUri());
        mAdapter.setOnItemClickListener(new SelectChatBgAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int id) {
                toImagePreview(drawableIdtoUri(id), ImagePreviewActivity.FROM_DEFAULT);
            }
        });
        rvBgContent.addItemDecoration(new GridSpacingItemDecoration(3, 10, true));
        rvBgContent.setHasFixedSize(true);
        rvBgContent.setLayoutManager(new GridLayoutManager(this, 3));
        rvBgContent.setAdapter(mAdapter);
        sivAlbum = findViewById(R.id.siv_album);
        sivAlbum.setOnClickListener(this);
    }

    private void toImagePreview(String uri, int type) {
        currentSelectUri = uri;
        Intent intent = new Intent(this, ImagePreviewActivity.class);
        intent.putExtra(IntentExtra.URL, uri);
        intent.putExtra(IntentExtra.IMAGE_PREVIEW_TYPE, type);
        startActivityForResult(intent, REQUEST_SET_BG);
    }

    private void initPhotoUtil() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
            }

            @Override
            public void onPhotoCancel() {
            }
        }, PhotoUtils.NO_CROP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SET_BG:
                if (resultCode == RESULT_OK) {
                    updateDefaultBgState();
                    //存储设置状态
                    userConfig.setChatbgUri(currentSelectUri);
                    ToastUtils.showToast(getString(R.string.seal_select_chat_bg_set_success));
                }
                break;
            case PhotoUtils.INTENT_SELECT:
                //从相册中选择图片
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    toImagePreview(imageUri.toString(), ImagePreviewActivity.FROM_ALUMB);
                }
                break;
        }
    }

    private void updateDefaultBgState() {
        mAdapter.setCheckItem(currentSelectUri);
    }

    private String drawableIdtoUri(int id) {
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + getResources().getResourcePackageName(id) + "/"
                + getResources().getResourceTypeName(id) + "/"
                + getResources().getResourceEntryName(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.siv_album:
                photoUtils.selectPicture(this);
                break;
        }
    }
}
