package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by AMing on 15/11/13.
 * Company RongCloud
 */
public abstract class BaseAdapters<T> extends android.widget.BaseAdapter {

    protected List<T> dataSet;
    protected Context mContext;
    protected LayoutInflater mInflater;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BaseAdapters(Context context) {
        this(context, new ArrayList());
    }

    public BaseAdapters(Context context, List<T> data) {
        this.mContext = context;
        this.dataSet = data;
        mInflater = LayoutInflater.from(mContext);
    }

    public Context getContext() {
        return this.mContext;
    }

    public void addData(T data) {
        if (data != null) {
            this.dataSet.add(data);
        }
    }

    public void addData(Collection<T> data) {
        if (data != null) {
            this.dataSet.addAll(data);
        }
    }

    public void addData(int index, Collection<T> data) {
        if (data != null) {
            this.dataSet.addAll(index, data);
        }
    }

    public void removeData(Collection<T> data) {
        if (data != null) {
            this.dataSet.removeAll(data);
        }
    }

    public void removeAll() {
        this.dataSet.clear();
    }

    public void remove(T data) {
        if (data != null) {
            this.dataSet.remove(data);
        }
    }

    public void remove(int position) {
        this.dataSet.remove(position);
    }

    public List<T> subData(int index, int count) {
        return this.dataSet.subList(index, index + count);
    }

    @Override
    public int getCount() {
        return this.dataSet.size();
    }

    @Override
    public T getItem(int position) {
        return this.dataSet.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0L;
    }

    public List<T> getDataSet() {
        return dataSet;
    }

    public void setDataSet(List<T> dataSet) {
        this.dataSet = dataSet;
    }
}
