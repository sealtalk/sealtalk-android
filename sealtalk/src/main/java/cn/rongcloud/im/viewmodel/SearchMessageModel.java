package cn.rongcloud.im.viewmodel;

import cn.rongcloud.im.ui.adapter.models.SearchModel;
import io.rong.imlib.model.Message;

public class SearchMessageModel extends SearchModel<Message> {
    private String name;
    private String portiaitUrl;
    private String search;

    public SearchMessageModel(Message bean, int type, String name, String portiaitUrl, String search) {
        super(bean, type);
        this.name = name;
        this.portiaitUrl = portiaitUrl;
        this.search = search;
    }

    public String getName() {
        return name;
    }

    public String getPortiaitUrl() {
        return portiaitUrl;
    }

    public String getSearch() {
        return search;
    }
}
