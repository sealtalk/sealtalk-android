package cn.rongcloud.im.ui.adapter.models;

public class SearchShowMorModel extends SearchModel<Integer> {

    public SearchShowMorModel(Integer bean, int type, int priority) {
        super(bean, type);
        this.priority = priority;
    }
}
