package cn.rongcloud.im.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.adapter.viewholders.ForwardCheckViewHolder;
import cn.rongcloud.im.ui.adapter.viewholders.ForwardSearchFriendViewHolder;
import cn.rongcloud.im.ui.adapter.viewholders.ForwardSearchGroupViewHolder;
import cn.rongcloud.im.ui.interfaces.OnContactItemClickListener;
import cn.rongcloud.im.ui.interfaces.OnGroupItemClickListener;

/**
 * 转发搜索
 * 转发搜索, 分为多选和单选.
 * 界面的控制有 SearchModel 来进行控制. 其有
 */
public class ForwardSearchAdapter extends RecyclerView.Adapter<ForwardCheckViewHolder> {
    private List<SearchModel> searchModelList;
    private OnGroupItemClickListener groupItemClickListener;
    private OnContactItemClickListener contactItemClickListener;
    private List<String> selectedGroupIds = new ArrayList<>();
    private List<String> selectedFriendIds = new ArrayList<>();

    public ForwardSearchAdapter(OnGroupItemClickListener onGroupItemClickListener, OnContactItemClickListener onContactItemClickListener) {
        this.searchModelList = new ArrayList<>();
        this.groupItemClickListener = onGroupItemClickListener;
        this.contactItemClickListener = onContactItemClickListener;
    }

    public void updateData(List<SearchModel> data) {
        searchModelList = data;
        notifyDataSetChanged();
    }

    /**
     * 已经选择的
     * @param selectedGroupIds
     * @param selectedFriendIds
     */
    public void setSelected(List<String> selectedGroupIds, List<String> selectedFriendIds) {
        this.selectedGroupIds = selectedGroupIds;
        this.selectedFriendIds = selectedFriendIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ForwardCheckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ForwardCheckViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(viewType, null, false);
        switch (viewType) {
            case R.layout.serach_fragment_forward_recycler_friend_item:
                viewHolder = new ForwardSearchFriendViewHolder(itemView, new OnContactItemClickListener() {
                    @Override
                    public void onItemContactClick(FriendShipInfo friendShipInfo) {

                        if (selectedFriendIds.contains(friendShipInfo.getUser().getId())) {
                            selectedFriendIds.remove(friendShipInfo.getUser().getId());
                        } else {
                            selectedFriendIds.add(friendShipInfo.getUser().getId());
                        }

                        if (contactItemClickListener != null) {
                            contactItemClickListener.onItemContactClick(friendShipInfo);
                        }
                    }
                });
                break;
            case R.layout.serach_fragment_forward_recycler_group_item:
                viewHolder = new ForwardSearchGroupViewHolder(itemView, new OnGroupItemClickListener() {
                    @Override
                    public void onGroupClicked(GroupEntity groupEntity) {
                        if (selectedGroupIds.contains(groupEntity.getId())) {
                            selectedGroupIds.remove(groupEntity.getId());
                        } else {
                            selectedGroupIds.add(groupEntity.getId());
                        }
                        if (groupItemClickListener !=  null) {
                            groupItemClickListener.onGroupClicked(groupEntity);
                        }
                    }
                });
                break;
            default:
                break;
        }
        return viewHolder;
    }

    public void clear() {
        searchModelList = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ForwardCheckViewHolder holder, int position) {
        SearchModel searchModel = searchModelList.get(position);
        holder.update(searchModel);
        int type = searchModel.getType();
        switch (type) {
            case R.layout.serach_fragment_forward_recycler_friend_item:
                if (selectedFriendIds.contains(searchModel.getId())) {
                    holder.setChecked(true);
                }
                break;
            case R.layout.serach_fragment_forward_recycler_group_item:
                if (selectedGroupIds.contains(searchModel.getId())) {
                    holder.setChecked(true);
                }
                break;
            default:
                break;
        }

    }

    @Override
    public int getItemCount() {
        return searchModelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return searchModelList.get(position).getType();
    }
}
