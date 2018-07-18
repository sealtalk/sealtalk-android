package cn.rongcloud.im.ui.adapter;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.rong.imkit.widget.adapter.BaseAdapter;

public abstract class CompositeAdapter<T> extends BaseAdapter<T> {

    private static final int ITEM_VIEW_BASE_TYPE = 1;
    private static final String fTag = "CompositeAdapter";

    public static class Partition<T> {
        boolean showIfEmpty;
        boolean hasHeader;

        List<T> list;
        int idColumnIndex;
        int count;

        public Partition(boolean showIfEmpty, boolean hasHeader, List<T> list) {
            this.showIfEmpty = showIfEmpty;
            this.hasHeader = hasHeader;
            this.list = list;
        }

        /**
         * True if the directory should be shown even if no contacts are found.
         */
        public boolean getShowIfEmpty() {
            return showIfEmpty;
        }

        public boolean getHasHeader() {
            return hasHeader;
        }

        public List<T> getList() {
            return list;
        }
    }

    public final Context mContext;
    private Partition<T>[] mPartitions;
    private int mSize = 0;
    private int mCount = 0;
    private boolean mCacheValid = true;
    private boolean mNotificationsEnabled = true;
    private boolean mNotificationNeeded;

    public CompositeAdapter(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Registers a partition. The cursor for that partition can be set later.
     * Partitions should be added in the order they are supposed to appear in
     * the list.
     */
    public void addPartition(boolean showIfEmpty, boolean hasHeader, List<T> list, int size) {
        addPartition(new Partition<T>(showIfEmpty, hasHeader, list), size);
    }

    @SuppressWarnings("unchecked")
    public void addPartition(Partition<T> partition, int size) {
        if (mPartitions == null || mPartitions.length != size) {

            mPartitions = new Partition[size];
            mSize = 0;
        }
        mPartitions[mSize++] = partition;
//        Log.e(fTag, "create Partition -------------- mPartitions = " + mPartitions);
        invalidate();
        notifyDataSetChanged();
    }

    protected Partition<T>[] getPartitions() {
        return mPartitions;
    }

    public void removePartition(int partitionIndex) {

        System.arraycopy(mPartitions, partitionIndex + 1, mPartitions, partitionIndex, mSize - partitionIndex - 1);
        mSize--;
        invalidate();
        notifyDataSetChanged();
    }

    /**
     * Removes cursors for all partitions.
     */
    public void clearPartitions() {
        if (mSize == 0) {
            return;
        }
        for (int i = 0; i < mSize; i++) {
            List<T> list = mPartitions[i].list;
            if (list != null) {
                list.clear();
                mPartitions[i].list = list = null;
            }
        }
        mSize = 0;
        invalidate();
        notifyDataSetChanged();
    }

    public void setHasHeader(int partitionIndex, boolean flag) {
        mPartitions[partitionIndex].hasHeader = flag;
        invalidate();
    }

    public void setShowIfEmpty(int partitionIndex, boolean flag) {
        mPartitions[partitionIndex].showIfEmpty = flag;
        invalidate();
    }

    public Partition<T> getPartition(int partitionIndex) {
        if (partitionIndex >= mSize) {
            throw new ArrayIndexOutOfBoundsException(partitionIndex);
        }
        return mPartitions[partitionIndex];
    }

    protected void invalidate() {
        mCacheValid = false;
    }

    public int getPartitionCount() {
        return mSize;
    }

    protected void ensureCacheValid() {
        if (mCacheValid) {
            return;
        }

        mCount = 0;
        for (int i = 0; i < mSize; i++) {

            int count = mPartitions[i].list != null ? mPartitions[i].list.size() : 0;
            if (mPartitions[i].hasHeader) {
                if (count != 0 || mPartitions[i].showIfEmpty) {
                    count++;
                }
            }
            mPartitions[i].count = count;
            mCount += count;
        }

        mCacheValid = true;
    }

    /**
     * Returns true if the specified partition was configured to have a header.
     */
    public boolean hasHeader(int partition) {
        return mPartitions[partition].hasHeader;
    }

    /**
     * Returns the total number of list items in all partitions.
     */
    public int getCount() {
        ensureCacheValid();
        return mCount;
    }

    /**
     * Returns the cursor for the given partition
     */
    public List<T> getData(int partition) {
        return mPartitions[partition].list;
    }

    /**
     * Changes the cursor for an individual partition.
     */
    public void changeCursor(int partition, List<T> data) {
        mPartitions[partition].list = data;

        invalidate();
        notifyDataSetChanged();
    }

    /**
     * Returns true if the specified partition has no cursor or an empty cursor.
     */
    public boolean isPartitionEmpty(int partition) {
        return mPartitions[partition].list == null || mPartitions[partition].list.size() == 0;
    }

    /**
     * Given a list position, returns the index of the corresponding partition.
     */
    public int getPartitionForPosition(int position) {
        ensureCacheValid();
        int start = 0;
        for (int i = 0; i < mSize; i++) {
            int end = start + mPartitions[i].count;
            if (position >= start && position < end) {
                return i;
            }
            start = end;
        }
        return -1;
    }

    /**
     * Given a list position, return the offset of the corresponding item in its
     * partition. The header, if any, will have offset -1.
     */
    public int getOffsetInPartition(int position) {
        ensureCacheValid();
        int start = 0;
        for (int i = 0; i < mSize; i++) {
            int end = start + mPartitions[i].count;
            if (position >= start && position < end) {
                int offset = position - start;
                if (mPartitions[i].hasHeader) {
                    offset--;
                }
                return offset;
            }
            start = end;
        }
        return -1;
    }

    /**
     * Returns the first list position for the specified partition.
     */
    public int getPositionForPartition(int partition) {
        ensureCacheValid();
        int position = 0;
        for (int i = 0; i < partition; i++) {
            position += mPartitions[i].count;
        }
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return getItemViewTypeCount() + 1;
    }

    /**
     * Returns the overall number of item view types across all partitions. An
     * implementation of this method needs to ensure that the returned count is
     * consistent with the values returned by {@link #getItemViewType(int, int)}
     * .
     */
    public int getItemViewTypeCount() {
        return 2;
    }

    /**
     * Returns the view type for the list item at the specified position in the
     * specified partition.
     */
    protected int getItemViewType(int partition, int position) {
        return ITEM_VIEW_BASE_TYPE;
    }

    @Override
    public int getItemViewType(int position) {
        ensureCacheValid();
        int start = 0;
        for (int i = 0; i < mSize; i++) {
            int end = start + mPartitions[i].count;
            if (position >= start && position < end) {
                int offset = position - start;
                if (mPartitions[i].hasHeader && offset == 0) {
                    // return IGNORE_ITEM_VIEW_TYPE;
                    return ITEM_VIEW_BASE_TYPE + 1;
                }
                return getItemViewType(i, position);
            }
            start = end;
        }

        throw new ArrayIndexOutOfBoundsException(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ensureCacheValid();
        int start = 0;
        for (int i = 0; i < mSize; i++) {
            int end = start + mPartitions[i].count;
            if (position >= start && position < end) {
                int offset = position - start;
                if (mPartitions[i].hasHeader) {
                    offset--;
                }
                View view;
                if (offset == -1) {
                    view = getHeaderView(i, mPartitions[i].list, convertView, parent);
                } else {

                    view = getView(i, mPartitions[i].list, offset, convertView, parent);
                }
                if (view == null) {
                    throw new NullPointerException("View should not be null, partition: " + i + " position: " + offset);
                }
                return view;
            }
            start = end;
        }

        throw new ArrayIndexOutOfBoundsException(position);
    }

    /**
     * Returns the header view for the specified partition, creating one if
     * needed.
     */
    protected View getHeaderView(int partition, List<T> data, View convertView, ViewGroup parent) {
        View view = convertView != null ? convertView : newHeaderView(mContext, partition, data, parent);
        bindHeaderView(view, partition, data);
        return view;
    }

    /**
     * Creates the header view for the specified partition.
     */
    protected View newHeaderView(Context context, int partition, List<T> data, ViewGroup parent) {
        return null;
    }

    /**
     * Binds the header view for the specified partition.
     */
    protected void bindHeaderView(View view, int partition, List<T> data) {
    }

    /**
     * Returns an item view for the specified partition, creating one if needed.
     */
    protected View getView(int partition, List<T> data, int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = newView(mContext, partition, data, position, parent);
        }
        bindView(view, partition, data, position);
        return view;
    }

    /**
     * Creates an item view for the specified partition and position. Position
     * corresponds directly to the current cursor position.
     */
    protected abstract View newView(Context context, int partition, List<T> data, int position, ViewGroup parent);

    /**
     * Binds an item view for the specified partition and position. Position
     * corresponds directly to the current cursor position.
     */
    protected abstract void bindView(View v, int partition, List<T> data, int position);

    /**
     * Returns a pre-positioned cursor for the specified list position.
     */
    public T getItem(int position) {
        ensureCacheValid();
        int start = 0;
        for (int i = 0; i < mSize; i++) {
            int end = start + mPartitions[i].count;
            if (position >= start && position < end) {
                int offset = position - start;
                if (mPartitions[i].hasHeader) {
                    offset--;
                }
                if (offset == -1) {
                    return null;
                }

                return mPartitions[i].list.get(offset);
            }
            start = end;
        }

        return null;
    }

    /**
     * Returns the item ID for the specified list position.
     */
    public long getItemId(int position) {
        ensureCacheValid();
        int start = 0;
        for (int i = 0; i < mSize; i++) {
            int end = start + mPartitions[i].count;
            if (position >= start && position < end) {
                int offset = position - start;
                if (mPartitions[i].hasHeader) {
                    offset--;
                }
                if (offset == -1) {
                    return 0;
                }
                if (mPartitions[i].idColumnIndex == -1) {
                    return 0;
                }

                if (mPartitions[i].list == null || mPartitions[i].list.size() <= 0) {
                    return 0;
                }
                return position;
            }
            start = end;
        }

        return 0;
    }

    /**
     * Returns false if any partition has a header.
     */
    @Override
    public boolean areAllItemsEnabled() {
        for (int i = 0; i < mSize; i++) {
            if (mPartitions[i].hasHeader) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true for all items except headers.
     */
    @Override
    public boolean isEnabled(int position) {
        ensureCacheValid();
        int start = 0;
        for (int i = 0; i < mSize; i++) {
            int end = start + mPartitions[i].count;
            if (position >= start && position < end) {
                int offset = position - start;
                if (mPartitions[i].hasHeader && offset == 0) {
                    return false;
                } else {
                    return isEnabled(i, offset);
                }
            }
            start = end;
        }

        return false;
    }

    /**
     * Returns true if the item at the specified offset of the specified
     * partition is selectable and clickable.
     */
    protected boolean isEnabled(int partition, int position) {
        return true;
    }

    /**
     * Enable or disable data change notifications. It may be a good idea to
     * disable notifications before making changes to several partitions at
     * once.
     */
    public void setNotificationsEnabled(boolean flag) {
        mNotificationsEnabled = flag;
        if (flag && mNotificationNeeded) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (mNotificationsEnabled) {
            mNotificationNeeded = false;
            super.notifyDataSetChanged();
        } else {
            mNotificationNeeded = true;
        }
    }

}
