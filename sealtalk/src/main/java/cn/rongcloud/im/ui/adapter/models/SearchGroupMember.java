package cn.rongcloud.im.ui.adapter.models;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import cn.rongcloud.im.db.model.GroupEntity;

public class SearchGroupMember {

    @Embedded
    private GroupEntity groupEntity;

    @ColumnInfo(name = "member_id")
    private String memberId;

    @ColumnInfo(name = "nickname")
    private String nickName;

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }

    public void setGroupEntity(GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
