package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {
    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void update(T t);

    public void update(int position, T t) {
        update(t);
    }

    public void setChecked(boolean b) {}
}
