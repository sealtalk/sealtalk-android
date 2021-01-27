package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.EditAliasViewModel;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.conversation.extension.component.emoticon.AndroidEmoji;

/**
 * 设置备注名界面
 */
public class EditAliasActivity extends TitleBaseActivity {
    private final String TAG = "EditAliasActivity";

    private SealTitleBar titleBar;
    private TextView titleConfirmTv;
    private EditText inputAliasEt;

    private String targetId;
    private EditAliasViewModel editAliasViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titleBar = getTitleBar();
        titleBar.setTitle(R.string.profile_alias_info);
        titleConfirmTv = titleBar.getTvRight();
        titleConfirmTv.setText(R.string.common_save);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            finish();
            return;
        }

        targetId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        if (targetId == null) {
            SLog.e(TAG, "targetId is null, finish" + TAG);
            finish();
            return;
        }

        setContentView(R.layout.profile_activity_edit_alias);

        initView();
        initViewModel();
    }

    private void initView() {
        inputAliasEt = findViewById(R.id.profile_et_input_alias);
        inputAliasEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    int start = inputAliasEt.getSelectionStart();
                    int end = inputAliasEt.getSelectionEnd();
                    inputAliasEt.removeTextChangedListener(this);
                    inputAliasEt.setText(AndroidEmoji.ensure(s.toString()));
                    inputAliasEt.addTextChangedListener(this);
                    inputAliasEt.setSelection(start, end);
                }
            }
        });

        // 点击保存时设置备注名
        titleConfirmTv.setOnClickListener(v -> editAliasViewModel.setAlias(inputAliasEt.getText().toString()));
    }

    private void initViewModel() {
        editAliasViewModel = ViewModelProviders.of(this
                , new EditAliasViewModel.Factory(getApplication(), targetId))
                .get(EditAliasViewModel.class);

        editAliasViewModel.getSetAliasResult().observe(this, resource -> {
            if (resource.status == Status.SUCCESS) {
                finish();
            } else if (resource.status == Status.ERROR) {
                ToastUtils.showToast(resource.message);
            }
        });
    }
}
