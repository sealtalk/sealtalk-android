package cn.rongcloud.im.model;

import java.util.List;

import cn.rongcloud.im.db.model.GroupEntity;

public class ContactGroupResult {
    private int total;
    private String limit;
    private String offset;
    private List<GroupEntity> list;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public List<GroupEntity> getList() {
        return list;
    }

    public void setList(List<GroupEntity> list) {
        this.list = list;
    }
}
