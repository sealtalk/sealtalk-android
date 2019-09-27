package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseItemViewHolder<T> extends RecyclerView.ViewHolder {
    public BaseItemViewHolder(@NonNull View itemView) {
        super(itemView);
    }

//    public BaseItemViewHolder(@NonNull View itemView, View.OnClickListener listener) {
//        super(itemView);
//    }

    public abstract void setOnClickItemListener(View.OnClickListener listener);

    public abstract void setOnLongClickItemListener(View.OnLongClickListener listener);

    public abstract void update(T t);

    public void setChecked(boolean b) {

    }
}
