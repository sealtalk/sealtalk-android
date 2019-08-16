package cn.rongcloud.im.ui.adapter.models;


import cn.rongcloud.im.ui.adapter.viewholders.BaseItemViewHolder;

/**
 * 此类主要为 {@link cn.rongcloud.im.ui.adapter.CommonListAdapter} 进行服务。
 * 配置其使用，进行数据展示。
 *
 * 用于多个数据类型时， 统一的列表展示。
 *
 * 用于联系人列表等类似类似展示类型.
 * ItemView 是必须填充的数据， 否则经无法展示布局。
 */
public class ListItemModel <T>{
    private String id; // ID
    private String displayName; // 展示名字
    private String portraitUrl;// 头像
    private T data; // 原数据类型

    private ItemView itemView; // 当前数据项对应的展示类型
    private CheckStatus checkStatus = CheckStatus.NONE;
    private String firstChar; // 首字母， 用于 side 的展示

    public ListItemModel(String id, String displayName, T data, ItemView itemView ) {
        setId(id);
        setDisplayName(displayName);
        setData(data);
        setItemView(itemView);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPortraitUrl() {
        return portraitUrl;
    }

    public void setPortraitUrl(String portraitUrl) {
        this.portraitUrl = portraitUrl;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ItemView getItemView() {
        return itemView;
    }

    public void setItemView(ItemView itemView) {
        this.itemView = itemView;
    }

    public CheckStatus getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(CheckStatus checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getFirstChar() {
        return firstChar;
    }

    public void setFirstChar(String firstChar) {
        this.firstChar = firstChar;
    }

    /**
     * 选择状态。
     */
    public enum CheckStatus {
        NONE,
        CHECKED,
        UNCHECKED,
        DISABLE
    }


    /**
     * 当前数据对应的布局类型资源以及ViewHolder
     * @param <T>
     */
    public static class ItemView <T extends BaseItemViewHolder> {

        public enum Type {
            FUN(0),
            TEXT(1),
            GROUP(2),
            FRIEND(3),
            OTHER(4);

            int value;
            Type(int value) {
                this.value = value;
            }

            public int getValue() {
                return  value;
            }
        }

        private int itemResId;
        private Type type;
        private Class<T> viewHolder;

        public ItemView(int itemResId, Type type, Class viewHolder) {
            this.itemResId = itemResId;
            this.type = type;
            this.viewHolder = viewHolder;
        }

        public int getItemResId() {
            return itemResId;
        }

        public int getTypeValue() {
            return type.getValue();
        }

        public Type getType() {
            return type;
        }

        public Class<T> getViewHolder() {
            return viewHolder;
        }
    }

}
