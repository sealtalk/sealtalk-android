package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.ui.adapter.models.PublicServiceModel;
import cn.rongcloud.im.ui.interfaces.OnPublicServiceClickListener;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.PublicServiceProfile;

public class PublicServiceViewHolder extends BaseViewHolder<PublicServiceModel> {
    private AsyncImageView portrait;
    private TextView name;
    private TextView introduction;
    private OnPublicServiceClickListener listener;
    private PublicServiceProfile profile;

    public PublicServiceViewHolder(@NonNull View itemView, OnPublicServiceClickListener listener) {
        super(itemView);
        portrait = itemView.findViewById(io.rong.imkit.R.id.portrait);
        name = itemView.findViewById(io.rong.imkit.R.id.name);
        introduction = itemView.findViewById(io.rong.imkit.R.id.introduction);
        this.listener = listener;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) ;
                {
                    listener.OnPublicServiceClicked(profile);
                }
            }
        });
    }

    @Override
    public void update(PublicServiceModel publicServiceModel) {
        profile = publicServiceModel.getBean();
        portrait.setResource(profile.getPortraitUri());
        name.setText(profile.getName());
        introduction.setText(profile.getIntroduction());
    }
}
