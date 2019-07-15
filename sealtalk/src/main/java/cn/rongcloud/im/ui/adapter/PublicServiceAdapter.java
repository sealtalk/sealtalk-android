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
import cn.rongcloud.im.ui.interfaces.OnPublicServiceClickListener;

public class PublicServiceAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private OnPublicServiceClickListener onPublicServiceClickListener;
    private List<ContactModel> data;

    public PublicServiceAdapter(OnPublicServiceClickListener listener) {
        this.data = new ArrayList<>();
        this.onPublicServiceClickListener = listener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(viewType, null, false);
        switch (viewType) {
            case R.layout.seal_public_service_item:
                viewHolder = new PublicServiceViewHolder(itemView,onPublicServiceClickListener);
                break;
            default:
                break;
        }
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
