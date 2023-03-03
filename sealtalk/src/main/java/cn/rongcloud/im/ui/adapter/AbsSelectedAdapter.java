package cn.rongcloud.im.ui.adapter;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.ui.adapter.models.CheckModel;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.viewholders.CheckableBaseViewHolder;
import cn.rongcloud.im.ui.interfaces.OnAdapterItemClickListener;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsSelectedAdapter<VH extends CheckableBaseViewHolder, M extends CheckModel>
        extends RecyclerView.Adapter<VH> implements OnAdapterItemClickListener<M> {

    public static final int VIEW_TYPE_NORMAL = 1;
    public static final int VIEW_TYPE_EDIT = 2;

    private boolean isMultiModel = true;

    // 原始数据
    protected final ArrayList<CheckModel> originModelList = new ArrayList<>();
    // 展示数据
    protected final ArrayList<CheckModel> filterModelList = new ArrayList<>();

    private OnAdapterItemClickListener itemClickListener;

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final int index = position;
        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (itemClickListener != null) {
                            itemClickListener.onItemClick(index, filterModelList.get(index));
                        }
                    }
                });
        holder.itemView.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (itemClickListener != null) {
                            itemClickListener.onItemLongClick(index, filterModelList.get(index));
                        }
                        return false;
                    }
                });
        holder.update(position, filterModelList.get(position));
    }

    public void setItemClickListener(OnAdapterItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /** 添加原始数据 */
    public void setData(List<CheckModel> data) {
        filterModelList.clear();
        filterModelList.addAll(data);
        originModelList.clear();
        originModelList.addAll(data);
        notifyDataSetChanged();
    }

    /** 添加过滤数据 */
    public void setFilterData(List<CheckModel> data) {
        filterModelList.clear();
        filterModelList.addAll(data);
        notifyDataSetChanged();
    }

    /** 回复原始数据 */
    public void resetOriginData() {
        filterModelList.clear();
        filterModelList.addAll(originModelList);
        notifyDataSetChanged();
    }

    /** 清空数据 */
    public void clearData() {
        filterModelList.clear();
        originModelList.clear();
        notifyDataSetChanged();
    }

    public ArrayList<CheckModel> getModelList(CheckType checkType) {
        if (checkType == null) {
            return originModelList;
        }
        ArrayList<CheckModel> list = new ArrayList<>();
        for (CheckModel model : originModelList) {
            if (model.getCheckType() == checkType) {
                list.add(model);
            }
        }
        return list;
    }

    @Override
    public int getItemCount() {
        return filterModelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return filterModelList.get(position).getType();
    }

    @Override
    public void onItemClick(int position, CheckModel model) {
        switch (model.getCheckType()) {
            case CHECKED:
                model.setCheckType(CheckType.NONE);
                notifyItemChanged(position);
                break;
            case NONE:
                if (!isMultiModel) {
                    // 单选模式，先重置状态
                    for (int i = 0; i < filterModelList.size(); i++) {
                        if (filterModelList.get(i).getCheckType() == CheckType.CHECKED) {
                            filterModelList.get(i).setCheckType(CheckType.NONE);
                            notifyItemChanged(i);
                        }
                    }
                }
                model.setCheckType(CheckType.CHECKED);
                notifyItemChanged(position);
                break;
            default:
                break;
        }
    }
}
