package cn.rongcloud.im.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态设置按钮的布局
 */
public abstract class TabGroupView extends LinearLayout {

    private OnTabSelectedListener listener;
    private int currentSelectedId = -1;

    public TabGroupView(Context context) {
        super(context);
    }

    public TabGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TabGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private HashMap<Integer, View> views;

    public void initView(List<TabItem> items, OnTabSelectedListener listener) {
        if (items == null || items.size() <= 0) {
            return;
        }
        this.listener = listener;
        for (final TabItem item : items) {
            if (views == null) {
                views = new HashMap<>();
            }
            View view = createView(item);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1.0f);
            views.put(item.id, view);
            view.setTag(item);
            addView(view, params);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(item, listener, view);
                }
            });
        }
    }


    protected void onItemClick(TabItem item, OnTabSelectedListener listener, View view) {
        setSelected(item);
        if (listener != null) {
            listener.onSelected(view, item);
        }
    }


    public void setSelected(TabItem item) {
        setSelected(item.id);
    }

    public void setSelected(int id) {
        // 重复点击
        if (getSelectedItemId() == id) {
            return;
        }
        int childCount = getChildCount();
        if (childCount > 0) {
            for (int i =0; i < childCount; i++) {
                View view = getChildAt(i);
                Object tag = view.getTag();
                if (tag != null && tag instanceof TabItem) {
                    TabItem itemdata = (TabItem)tag;
                    if (itemdata.id == id) {
                        if (!view.isSelected()) {
                            view.setSelected(true);
                            if (listener != null) {
                                listener.onSelected(view, itemdata);
                            }
                        }
                    } else {
                        view.setSelected(false);
                    }
                }
            }
            currentSelectedId = id;
        }
    }


    public <T extends View> T getView(TabItem item) {
        return getView(item.id);
    }

    public <T extends View> T getView(int id) {
        if (views == null) {
            return null;
        }
        View view = views.get(id);
        if (view == null) {
            return null;
        }

        return (T) view;
    }


    public void setItemEnabled(TabItem item, boolean enabled) {
        setItemEnabled(item.id, enabled);
    }

    public void setItemEnabled(int id, boolean enabled) {
        if (views == null) {
            return;
        }
        View view = views.get(id);
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
    }

    public void setItemVisibility(TabItem item, int visibile) {
        setItemVisibility(item.id, visibile);
    }

    public void setItemVisibility(int id, int visibile) {
        if (views == null) {
            return;
        }
        View view = views.get(id);
        if (view == null) {
            return;
        }
        view.setVisibility(visibile);
    }


    public void setEnabled(boolean enabled) {
        if (views == null) {
            return;
        }

        for (Map.Entry<Integer, View> entry : views.entrySet()) {
            entry.getValue().setEnabled(enabled);
        }
    }

    protected abstract View createView(TabItem item);

    /**
     * 当前选项的 id
     * @return
     */
    public int getSelectedItemId() {
        return currentSelectedId;
    }


    public interface OnTabSelectedListener {
        void onSelected(View view, TabItem item);
    }

}
