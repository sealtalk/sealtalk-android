package cn.rongcloud.im.ui.adapter;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.adapter.ViewHolder;
import java.util.List;

public class UltraConversationListAdapterEx extends BaseAdapter<BaseUiConversation> {
    public UltraConversationListAdapterEx() {
        super();
        mProviderManager = RongConfigCenter.conversationListConfig().getProviderManager();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void setDataCollection(final List<BaseUiConversation> data) {
        if (mDataList == null || mDataList.size() == 0) {
            mDataList = data;
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult result =
                    DiffUtil.calculateDiff(
                            new DiffUtil.Callback() {
                                @Override
                                public int getOldListSize() {
                                    return mDataList.size();
                                }

                                @Override
                                public int getNewListSize() {
                                    return data.size();
                                }

                                @Override
                                public boolean areItemsTheSame(
                                        int oldItemPosition, int newItemPosition) {
                                    BaseUiConversation oldItem = mDataList.get(oldItemPosition);
                                    BaseUiConversation newItem = data.get(newItemPosition);
                                    return oldItem.mCore
                                                    .getTargetId()
                                                    .equals(newItem.mCore.getTargetId())
                                            && oldItem.mCore
                                                    .getConversationType()
                                                    .equals(newItem.mCore.getConversationType());
                                }

                                @Override
                                public boolean areContentsTheSame(
                                        int oldItemPosition, int newItemPosition) {
                                    return false;
                                }
                            });
            mDataList = data;
            notifyDataSetChanged();
            //            result.dispatchUpdatesTo(this);
        }
    }
}
