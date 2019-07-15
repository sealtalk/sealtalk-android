package cn.rongcloud.im.model;

/**
 * 获取聊天室结果
 */
public class ChatRoomResult {

    private String type;
    private String id;
    private String name;
    private String portraitUri;
    private int memberCount;
    private int maxMemberCount;

    public void setType(String type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public void setMaxMemberCount(int maxMemberCount) {
        this.maxMemberCount = maxMemberCount;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public int getMaxMemberCount() {
        return maxMemberCount;
    }


}
