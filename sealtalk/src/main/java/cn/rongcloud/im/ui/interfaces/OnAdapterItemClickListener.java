package cn.rongcloud.im.ui.interfaces;

public interface OnAdapterItemClickListener<T> {
    void onItemClick(int position, T model);

    default void onItemLongClick(int position, T model) {}
}
