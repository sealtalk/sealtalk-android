package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {
    Context mContext;
    List<T> mList;

    public BaseAdapter() {
        mList = new ArrayList<T>();
    }

    public BaseAdapter(Context context) {
        mContext = context;
        mList = new ArrayList<T>();
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T findViewById(View view, int id) {
        return (T) view.findViewById(id);
    }

    public int findPosition(T message) {
        int index = getCount();
        int position = -1;
        while (index-- > 0) {
            if (message.equals(getItem(index))) {
                position = index;
                break;
            }
        }
        return position;
    }

    public int findPosition(long id) {
        int index = getCount();
        int position = -1;
        while (index-- > 0) {
            if (getItemId(index) == id) {
                position = index;
                break;
            }
        }
        return position;
    }

    public void addCollection(Collection<T> collection) {
        mList.addAll(collection);
    }

    public void addCollection(T ... collection) {

        for (T t : collection) {
            mList.add(t);
        }
    }

    public void add(T t) {
        mList.add(t);
    }

    public void add(T t, int position) {
        mList.add(position, t);
    }

    public void remove(int position) {
        mList.remove(position);
    }

    public void removeAll() {
        mList.clear();
    }

    public void clear() {
        mList.clear();
    }

    @Override
    public int getCount() {
        if (mList == null)
            return 0;

        return mList.size();
    }

    @Override
    public T getItem(int position) {
        if (mList == null)
            return null;

        if (position >= mList.size())
            return null;

        return mList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = newView(mContext, position, parent);
        }
        bindView(view, position, getItem(position));
        return view;
    }

    protected abstract View newView(Context context, int position, ViewGroup group);

    protected abstract void bindView(View v, int position, T data);



}
