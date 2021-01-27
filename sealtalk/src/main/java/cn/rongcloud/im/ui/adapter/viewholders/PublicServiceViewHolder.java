package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.models.PublicServiceModel;
import cn.rongcloud.im.ui.interfaces.PublicServiceClickListener;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import io.rong.imlib.publicservice.model.PublicServiceProfile;
;

public class PublicServiceViewHolder extends BaseViewHolder<PublicServiceModel> {
    private ImageView portrait;
    private TextView name;
    private TextView introduction;
    private PublicServiceClickListener listener;
    private PublicServiceProfile profile;

    public PublicServiceViewHolder(@NonNull View itemView, PublicServiceClickListener listener) {
        super(itemView);
        portrait = itemView.findViewById(R.id.pub_portrait);
        name = itemView.findViewById(R.id.pub_name);
        introduction = itemView.findViewById(R.id.pub_introduction);
        this.listener = listener;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onPublicServiceClicked(profile);
                }
            }
        });
    }

    @Override
    public void update(PublicServiceModel publicServiceModel) {
        profile = publicServiceModel.getBean();
        String portraitUri = profile.getPortraitUri() != null ? profile.getPortraitUri().toString() : "";
        ImageLoaderUtils.displayUserPortraitImage(portraitUri, portrait);
        name.setText(profile.getName());
        introduction.setText(profile.getIntroduction());
    }
}
