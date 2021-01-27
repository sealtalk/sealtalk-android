package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.models.CharacterTitleInfo;
import cn.rongcloud.im.ui.adapter.models.ContactModel;

public class TitleViewHolder extends BaseViewHolder<ContactModel<CharacterTitleInfo>> {
    private TextView textView;

    public TitleViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.catalog);
    }

    @Override
    public void update(ContactModel<CharacterTitleInfo> characterTitleInfoContactModel) {
        textView.setText(characterTitleInfoContactModel.getBean().getCharacter());
    }
}
