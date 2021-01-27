package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.GroupUserInfoActivity;
import cn.rongcloud.im.ui.activity.UserDetailActivity;
import cn.rongcloud.im.ui.widget.ClearWriteEditText;
import cn.rongcloud.im.utils.ToastUtils;

public class GroupUserInfoDesAdapter extends RecyclerView.Adapter {

    private ArrayList<String> data;
    private Context mContext;
    private int mType;
    private static final int ITEM_CONTENT = 0x129;
    private static final int ITEM_ADD = 0x130;

    public GroupUserInfoDesAdapter(Context mContext) {
        this.mContext = mContext;
        data = new ArrayList<>();
        //最少添加一个
        data.add("");
    }

    public GroupUserInfoDesAdapter(Context context, int type) {
        data = new ArrayList<>();
        //最少添加一个
        data.add("");
        mContext = context;
        mType = type;
    }

    public void setData(ArrayList<String> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public ArrayList<String> getData() {
        //删除空的字符串
        Iterator<String> iterator = data.iterator();
        while (iterator.hasNext()) {
            String str = iterator.next();
            if (TextUtils.isEmpty(str)) {
                iterator.remove();
            }
        }
        if (data.size() == 0) {
            return new ArrayList<>();
        }
        return data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder holder = null;
        if (viewType == ITEM_CONTENT) {
            View itemView = inflater.inflate(R.layout.item_group_user_info_des, null, false);
            holder = new DescriptionViewHolder(itemView);
        } else if (viewType == ITEM_ADD) {
            View itemView = inflater.inflate(R.layout.item_group_user_info_des_add, null, false);
            holder = new DescriptionAddViewHolder(itemView);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DescriptionViewHolder) {
            DescriptionViewHolder desHolder = (DescriptionViewHolder) holder;
            if (mType == GroupUserInfoActivity.FROM_USER_DETAIL) {
                //只做展示，无法编辑
                desHolder.cetDes.setEnabled(false);
                desHolder.cetDes.setClearDrawableNeverShow(true);
                desHolder.tvDelete.setVisibility(View.GONE);
                if (TextUtils.isEmpty(data.get(position))) {
                    desHolder.cetDes.setText(R.string.seal_group_user_info_des_no_set, TextView.BufferType.EDITABLE);
                } else {
                    desHolder.cetDes.setText(data.get(position), TextView.BufferType.EDITABLE);
                }
            } else {
                desHolder.cetDes.setText(data.get(position), TextView.BufferType.EDITABLE);
            }
            desHolder.cetDes.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    //替换元素
                    data.set(desHolder.getAdapterPosition(), s.toString());
                }
            });
            desHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItem(desHolder.getAdapterPosition());
                }
            });
        } else if (holder instanceof DescriptionAddViewHolder) {
            DescriptionAddViewHolder desHolder = (DescriptionAddViewHolder) holder;
            desHolder.tvAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addItem("");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mType == GroupUserInfoActivity.FROM_USER_DETAIL) {
            return data != null ? data.size() : 0;
        } else {
            return data != null ? data.size() + 1 : 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return data != null && position < data.size() ? ITEM_CONTENT : ITEM_ADD;
    }

    public void removeItem(int postion) {
        //最少保留一条
        if (data.size() <= 1) {
            return;
        }
        data.remove(postion);
        notifyItemRemoved(postion);
    }

    public void addItem(String s) {
        //最多添加10条
        if (data.size() >= 10) {
            return;
        }
        data.add(s);
        notifyItemInserted(data.size() - 1);
    }

    class DescriptionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDelete;
        ClearWriteEditText cetDes;

        public DescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDelete = itemView.findViewById(R.id.tv_delete);
            cetDes = itemView.findViewById(R.id.cet_description);
            cetDes.setClearDrawable(itemView.getContext().getResources().getDrawable(R.drawable.seal_st_account_delete));
            cetDes.setShowClearDrawableNoFocus(true);
        }

    }

    class DescriptionAddViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdd;

        public DescriptionAddViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdd = itemView.findViewById(R.id.tv_add);
        }

    }
}
