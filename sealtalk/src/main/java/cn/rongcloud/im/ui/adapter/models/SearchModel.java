package cn.rongcloud.im.ui.adapter.models;

public class SearchModel<T> extends ContactModel<T> {

    public static final int SHOW_PRIORITY_FRIEND = 1; //展示的优先级
    public static final int SHOW_PRIORITY_GROUP = 2;
    public static final int SHOW_PRIORITY_CONVERSATION = 3;

    protected int priority = SHOW_PRIORITY_FRIEND; // 展示的优先级


    private String id;

    public SearchModel(T bean, int type) {
        super(bean, type);
    }

    public int getPriority() {
        return priority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
