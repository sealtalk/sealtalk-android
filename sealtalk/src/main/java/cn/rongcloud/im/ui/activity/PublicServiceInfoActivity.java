package cn.rongcloud.im.ui.activity;

import static cn.rongcloud.im.ui.view.SealTitleBar.Type.NORMAL;

import android.os.Bundle;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;

public class PublicServiceInfoActivity extends TitleBaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setType(NORMAL);
        getTitleBar().setTitle(R.string.seal_public_account_information);
        setContentView(R.layout.seal_public_account_info);
    }
}
