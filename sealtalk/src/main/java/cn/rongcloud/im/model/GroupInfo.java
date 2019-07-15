package cn.rongcloud.im.model;

public class GroupInfo {
    /**
     * 群组 Id
     */
    private String id;
    /**
     * 群名称
     */
    private String name;
    /**
     * 群头像
     */
    private String portraitUri;
    /**
     *  群人数
     */
    private int memberCount;
    /**
     * 群人数上限
     */
    private int maxMemberCount;
    /**
     * 群主 id
     */
    private String creatorId;
    /**
     * 类型: 1,普通群;2,企业群
     */
    private int type;
    /**
     * 群公告
     */
    private String bulletin;
    /**
     * 删除日期
     */
    private long deletedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getMaxMemberCount() {
        return maxMemberCount;
    }

    public void setMaxMemberCount(int maxMemberCount) {
        this.maxMemberCount = maxMemberCount;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBulletin() {
        return bulletin;
    }

    public void setBulletin(String bulletin) {
        this.bulletin = bulletin;
    }

    public long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(long deletedAt) {
        this.deletedAt = deletedAt;
    }
}
