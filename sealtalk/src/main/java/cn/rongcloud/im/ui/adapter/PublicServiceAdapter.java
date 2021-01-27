package cn.rongcloud.im.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.models.ContactModel;
import cn.rongcloud.im.ui.adapter.viewholders.BaseViewHolder;
import cn.rongcloud.im.ui.adapter.viewholders.PublicServiceViewHolder;
import cn.rongcloud.im.ui.interfaces.PublicServiceClickListener;

public class PublicServiceAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private PublicServiceClickListener publicServiceClickListener;
    private List<ContactModel> data;

    public PublicServiceAdapter(PublicServiceClickListener listener) {
        this.data = new ArrayList<>();
        this.publicServiceClickListener = listener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder = null;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.seal_public_service_item, parent, false);
        viewHolder = new PublicServiceViewHolder(itemView, publicServiceClickListener);
        return viewHolder;
    }

    public void updateData(List<ContactModel> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getType();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.update(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
