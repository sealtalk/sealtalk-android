package cn.rongcloud.im.ui.adapter;

import android.widget.SectionIndexer;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 此方法是配合 ListWithSideBarBaseFragment 进行使用。是 ListWithSideBarBaseFragment 的适配器。
 * 子类继承时，需实现 RecyclerView.Adapter 和 SectionIndexer 的方法。
 * @param <V> 数据类型
 * @param <T> ViewHolder
 */
public abstract class ListWithSideBarBaseAdapter<V, T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> implements SectionIndexer {
    /**
     * 更新列表
     * @param datas 更新的数据
     */
    public abstract void updateData(List<V> datas);
}
