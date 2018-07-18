package cn.rongcloud.im.model;

/**
 * Created by tiankui on 16/9/7.
 */
public class GroupMemberInfo {

    private String groupId;
    private String groupName;
    private String displayName;
    private String name;
    private String portraitUri;

    public GroupMemberInfo(String groupId, String groupName, String displayName, String name, String portraitUri) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.displayName = displayName;
        this.name = name;
        this.portraitUri = portraitUri;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

}
