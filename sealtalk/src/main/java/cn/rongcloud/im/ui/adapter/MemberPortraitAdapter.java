package cn.rongcloud.im.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.models.CheckModel;
import cn.rongcloud.im.ui.adapter.models.SearchUserGroupMemberModel;
import cn.rongcloud.im.ui.adapter.viewholders.MemberPortraitHolder;
import java.util.ArrayList;
import java.util.List;

public class MemberPortraitAdapter extends RecyclerView.Adapter<MemberPortraitHolder> {

    private final List<CheckModel> modelList = new ArrayList<>();

    public MemberPortraitAdapter() {}

    @NonNull
    @Override
    public MemberPortraitHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.select_member_portrait_item, null, false);
        return new MemberPortraitHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberPortraitHolder holder, int position) {
        CheckModel checkModel = modelList.get(position);
        if (checkModel instanceof SearchUserGroupMemberModel) {
            holder.update(position, (SearchUserGroupMemberModel) checkModel);
        }
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return modelList.get(position).getType();
    }

    public void setData(List<CheckModel> data) {
        this.modelList.clear();
        this.modelList.addAll(data);
        notifyDataSetChanged();
    }
}
