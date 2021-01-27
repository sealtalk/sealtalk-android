package cn.rongcloud.im.ui.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cn.rongcloud.im.R;

public class SelectChatBgAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private OnItemClickListener mListener;
    private String selectedBg;


    public SelectChatBgAdapter(Context context) {
        this.mContext = context;
    }

    public void setCheckItem(String uri) {
        selectedBg = uri;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    private int[] defaultBg = {R.drawable.seal_default_chat_bg1, R.drawable.seal_default_chat_bg2,
            R.drawable.seal_default_chat_bg3, R.drawable.seal_default_chat_bg4,
            R.drawable.seal_default_chat_bg5, R.drawable.seal_default_chat_bg6};

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_select_chat_bg, null, false);
        ChatBgViewHolder holder = new ChatBgViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatBgViewHolder chatBgViewHolder = (ChatBgViewHolder) holder;
        chatBgViewHolder.ivContent.setImageDrawable(mContext.getResources().getDrawable(defaultBg[position]));
        chatBgViewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(defaultBg[position]);
                }
            }
        });
        if (selectedBg.contains(mContext.getResources().getResourceEntryName(defaultBg[position]))) {
            chatBgViewHolder.ivSelect.setVisibility(View.VISIBLE);
        } else {
            chatBgViewHolder.ivSelect.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return defaultBg.length;
    }

    class ChatBgViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private ImageView ivContent;
        private ImageView ivSelect;

        public ChatBgViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            ivContent = itemView.findViewById(R.id.iv_bg_content);
            ivSelect = itemView.findViewById(R.id.iv_select);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int id);
    }
}
