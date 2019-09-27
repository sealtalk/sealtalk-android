package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.fragment.PublicServiceSearchFragment;
import cn.rongcloud.im.ui.interfaces.OnPublicServiceClickListener;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.PublicServiceProfileFragment;
import io.rong.imlib.model.PublicServiceProfile;

import static cn.rongcloud.im.ui.view.SealTitleBar.Type.NORMAL;

public class PublicServiceSearchActivity extends TitleBaseActivity implements OnPublicServiceClickListener, View.OnClickListener {
    private EditText editText;
    private Button button;
    private PublicServiceSearchFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = new PublicServiceSearchFragment();
        fragment.setOnPublicServiceClickListener(this);
        getTitleBar().setType(NORMAL);
        getTitleBar().setTitle(R.string.seal_search);
        setContentView(R.layout.activity_public_service_search_content);
        button = findViewById(R.id.rc_search_btn);
        editText = findViewById(R.id.rc_search_ed);
        button.setOnClickListener(this);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_fragment_container, fragment)
                .commit();
    }

    @Override
    public void OnPublicServiceClicked(PublicServiceProfile publicServiceProfile) {

        if (publicServiceProfile.isFollow()) {
            RongIM.getInstance().startConversation(getApplicationContext(), publicServiceProfile.getConversationType(), publicServiceProfile.getTargetId(), publicServiceProfile.getName());
        } else {
            Uri uri = Uri.parse("rong://" +getApplicationInfo().packageName).buildUpon()
                    .appendPath("publicServiceProfile")
                    .appendPath(publicServiceProfile.getConversationType()
                            .getName().toLowerCase())
                    .appendQueryParameter("targetId", publicServiceProfile.getTargetId()).build();
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(PublicServiceProfileFragment.AGS_PUBLIC_ACCOUNT_INFO, publicServiceProfile);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        fragment.search(editText.getText().toString());
    }
}
