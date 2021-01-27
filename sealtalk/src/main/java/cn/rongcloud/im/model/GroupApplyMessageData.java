package cn.rongcloud.im.model;

public class GroupApplyMessageData {
    private String operatorNickname;
    private String targetGroupId;
    private String targetGroupName;
    private int status;
    private int type;
    private long timestamp;

    public String getOperatorNickname() {
        return operatorNickname;
    }

    public void setOperatorNickname(String operatorNickname) {
        this.operatorNickname = operatorNickname;
    }

    public String getTargetGroupId() {
        return targetGroupId;
    }

    public void setTargetGroupId(String targetGroupId) {
        this.targetGroupId = targetGroupId;
    }

    public String getTargetGroupName() {
        return targetGroupName;
    }

    public void setTargetGroupName(String targetGroupName) {
        this.targetGroupName = targetGroupName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "GroupApplyMessageData{" +
                "operatorNickname='" + operatorNickname + '\'' +
                ", targetGroupId='" + targetGroupId + '\'' +
                ", targetGroupName='" + targetGroupName + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", timestamp=" + timestamp +
                '}';
    }
}
