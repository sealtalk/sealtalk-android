package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.models.CheckType;

public class CheckableBaseViewHolder<T> extends BaseViewHolder<T> {


    public CheckableBaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void update(T t) {
        //继承刷新UI
    }

    public void updateCheck(ImageView checkBox, CheckType checkType) {
        switch (checkType) {
            case NONE:
                checkBox.setImageResource(R.drawable.seal_ic_checkbox_none);
                break;
            case CHECKED:
                checkBox.setImageResource(R.drawable.seal_ic_checkbox_full);
                break;
            case DISABLE:
                checkBox.setImageResource(R.drawable.seal_ic_checkbox_full_gray_disable);
                break;
            default:
                break;
        }
    }
}
