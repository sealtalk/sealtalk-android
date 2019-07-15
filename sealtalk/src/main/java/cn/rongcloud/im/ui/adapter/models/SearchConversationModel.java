package cn.rongcloud.im.ui.adapter.models;

import io.rong.imlib.model.SearchConversationResult;

public class SearchConversationModel extends SearchModel<SearchConversationResult> {
    private String filter;
    private String name;
    private String portraitUrl;

    public SearchConversationModel(SearchConversationResult bean, int type, String filter, String name, String portraitUrl) {
        super(bean, type);
        priority = SHOW_PRIORITY_CONVERSATION;
        this.filter = filter;
        this.name = name;
        this.portraitUrl = portraitUrl;
    }

    public String getFilter() {
        return filter;
    }

    public String getName() {
        return name;
    }

    public String getPortraitUrl() {
        return portraitUrl;
    }
}
