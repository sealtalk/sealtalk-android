
package cn.rongcloud.im.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.ui.adapter.viewholders.BaseItemViewHolder;
import cn.rongcloud.im.ui.adapter.viewholders.CommonFriendItemViewHolder;
import cn.rongcloud.im.ui.adapter.viewholders.ViewHolderFactory;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.CommonListBaseViewModel;

/**
 * 通用列表 Adapter 类。 支持的数据类型为{@link ListItemModel}.
 * Adapter 可更具View 的资源 id 通过 {@link ViewHolderFactory#createViewHolder(int, View)}
 * 方法去获取对应的ViewHolder 进行使用。
 * <p>
 * 所以要特别注意的是。如果想单独使用此 Adapter 的时候。 需要记得提前在 ViewHolderFactory 中添加展示所需的布局 viewResId
 * 和其对应的 ViewHolder 的映射关系。否则会找不到ViewHolder 的构造， 从而出现崩溃。
 * <p>
 * 如果是和 {@link CommonListBaseViewModel} 类配合使用的话， 则使用其内部类{@link CommonListBaseViewModel.ModelBuilder}
 * 类进行构造的话， 就会自动添加 View 资源和ViewHolder 的映射关系，则无需手动添加。
 *
 * @see CommonListBaseViewModel
 * @see CommonListBaseViewModel.ModelBuilder
 * @see ListItemModel
 */
public class CommonListAdapter extends ListWithSideBarBaseAdapter<ListItemModel, BaseItemViewHolder> {
    private List<ListItemModel> data;
    private List<String> selectedGroupIds = new ArrayList<>();
    private List<String> selectedFriendsIds = new ArrayList<>();
    private List<String> selectedOtherIds = new ArrayList<>();

    private CommonListAdapter.OnItemClickListener listener;
    private CommonListAdapter.OnItemLongClickListener longClickListener;

    public static int CHECK_MODE_UNCHECK = 1;// 不启动选择模式
    private int mCheckMode;

    public CommonListAdapter() {
        data = new ArrayList<>();
    }

    public CommonListAdapter(int checkMode) {
        this.mCheckMode = checkMode;
    }

    @Override
    public void updateData(List<ListItemModel> data) {
        SLog.d("recent_adapter", "data===" + data);
        this.data = data;
        notifyDataSetChanged();
    }

    /**
     * 已经选择的
     *
     * @param selectedGroupIds
     * @param selectedFriendsIds
     */
    public void setSelected(List<String> selectedGroupIds, List<String> selectedFriendsIds) {
        this.selectedGroupIds = selectedGroupIds;
        this.selectedFriendsIds = selectedFriendsIds;
    }


    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        final BaseItemViewHolder viewHolder = ViewHolderFactory.getInstance().createViewHolder(viewType, view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder holder, int position) {
        ListItemModel listItemModel = data.get(position);

        holder.update(listItemModel);
        holder.setOnClickItemListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 非点击选中模式不处理此事件
                if (mCheckMode != CHECK_MODE_UNCHECK) {
                    handleSelectedStatusCache(listItemModel);
                }
                if (listener != null) {
                    listener.onClick(v, position, listItemModel);
                }
            }
        });

        holder.setOnLongClickItemListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    return longClickListener.onLongClick(v, position, listItemModel);
                }
                return false;
            }
        });
        if (holder instanceof CommonFriendItemViewHolder) {
            if (data.size() - 1 != position) {
                ListItemModel itemModel = data.get(position + 1);
                if (itemModel.getItemView().getItemResId() != (listItemModel.getItemView().getItemResId())) {
                    ((CommonFriendItemViewHolder) holder).setDividerVisibility(false);
                } else {
                    ((CommonFriendItemViewHolder) holder).setDividerVisibility(true);
                }
            } else {
                ((CommonFriendItemViewHolder) holder).setDividerVisibility(false);
            }
        }
        int type = listItemModel.getItemView().getTypeValue();
        updateSelectedStatus(holder, listItemModel, type);
    }

    /**
     * 处理更新item 的选择状态。如果有其他的处理， 可在子类中对此方法进行复写修改逻辑
     *
     * @param holder
     * @param checkableContactModel
     * @param type
     */
    protected void updateSelectedStatus(@NonNull BaseItemViewHolder holder, ListItemModel checkableContactModel, int type) {
        if (type == ListItemModel.ItemView.Type.GROUP.getValue()) {
            if (selectedGroupIds.contains(checkableContactModel.getId())) {
                holder.setChecked(true);
            }
        } else if (type == ListItemModel.ItemView.Type.FRIEND.getValue()) {
            if (selectedFriendsIds.contains(checkableContactModel.getId())) {
                holder.setChecked(true);
            }
        } else if (type == ListItemModel.ItemView.Type.OTHER.getValue()) {
            if (selectedOtherIds.contains(checkableContactModel.getId())) {
                checkableContactModel.setCheckStatus(ListItemModel.CheckStatus.CHECKED);
                holder.setChecked(true);
            }
        }
    }


    /**
     * 处理更新item 集合的维护状态。如果有其他的处理， 可在子类中对此方法进行复写修改逻辑
     *
     * @param model
     */
    protected void handleSelectedStatusCache(ListItemModel model) {
        if (model.getItemView().getType() == ListItemModel.ItemView.Type.GROUP) {
            if (selectedGroupIds.contains(model.getId())) {
                selectedGroupIds.remove(model.getId());
            } else {
                selectedGroupIds.add(model.getId());
            }
        } else if (model.getItemView().getType() == ListItemModel.ItemView.Type.FRIEND) {
            if (selectedFriendsIds.contains(model.getId())) {
                selectedFriendsIds.remove(model.getId());
            } else {
                selectedFriendsIds.add(model.getId());
            }
        } else if (model.getItemView().getType() == ListItemModel.ItemView.Type.OTHER) {
            if (model.getCheckStatus() == ListItemModel.CheckStatus.CHECKED
                    && !selectedOtherIds.contains(model.getId())) {
                selectedOtherIds.add(model.getId());
            } else if (model.getCheckStatus() != ListItemModel.CheckStatus.CHECKED) {
                selectedOtherIds.remove(model.getId());
            }
        }
    }

    /**
     * 获取当前数据
     *
     * @return
     */
    public List<ListItemModel> getData() {
        return data;
    }


    @Override
    public int getItemViewType(int position) {
        if (data == null) {
            return -1;
        }
        // 为了获取位置， 这里要type 值加位置拼接. 这里 type 的code 值不得超过
        return data.get(position).getItemView().getItemResId();
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getSectionForPosition(int position) {
        if (data != null && data.size() > 0) {
            final ListItemModel model = data.get(position);
            final String firstChar = model.getFirstChar();
            if (!TextUtils.isEmpty(firstChar)) {
                return firstChar.charAt(0);
            }
        }
        return 0;
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        for (int i = 0; i < getItemCount(); i++) {
            final ListItemModel model = data.get(i);
            final String displayName = model.getDisplayName();
            ListItemModel.ItemView.Type type = model.getItemView().getType();
            if (!TextUtils.isEmpty(displayName) && type.getValue() == ListItemModel.ItemView.Type.TEXT.getValue()) {
                int index = displayName.toUpperCase().charAt(0);
                if (index == sectionIndex) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 设置点击监听
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 设置长按点击监听
     *
     * @param listener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    /**
     * 获取其他类型选择的列表
     *
     * @return
     */
    public List<String> getSelectedOtherIds() {
        return selectedOtherIds;
    }

    /**
     * 点击监听接口
     */
    public interface OnItemClickListener {
        /**
         * 点击回调方法
         *
         * @param v
         * @param position 位置
         * @param data     item 的数据
         */
        void onClick(View v, int position, ListItemModel data);
    }

    /**
     * 长按点击监听接口
     */
    public interface OnItemLongClickListener {
        /**
         * 长按回调方法
         *
         * @param v
         * @param position
         * @param data
         */
        boolean onLongClick(View v, int position, ListItemModel data);
    }

}
