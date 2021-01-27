package cn.rongcloud.im.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.adapter.viewholders.CheckableBaseViewHolder;
import cn.rongcloud.im.ui.adapter.viewholders.CheckableConversationViewHolder;
import cn.rongcloud.im.ui.interfaces.OnCheckConversationClickListener;

public class SelectConversationAdapter extends RecyclerView.Adapter<CheckableBaseViewHolder> {

    private List<CheckableContactModel> data;
    private OnCheckConversationClickListener mListener;

    public SelectConversationAdapter(OnCheckConversationClickListener mListener) {
        this.mListener = mListener;
    }

    public void setData(List<CheckableContactModel> data) {
        this.data = data;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public CheckableBaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CheckableBaseViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(viewType, null, false);
        viewHolder = new CheckableConversationViewHolder(itemView,mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CheckableBaseViewHolder holder, int position) {
        holder.update(data.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return data != null ? data.get(position).getType() : 0;
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    public void remove(int position){
        data.remove(position);
        notifyItemRemoved(position);
    }
}
