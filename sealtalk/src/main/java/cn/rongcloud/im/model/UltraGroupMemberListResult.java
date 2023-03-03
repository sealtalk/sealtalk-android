package cn.rongcloud.im.model;

public class UltraGroupMemberListResult {

    public String memberName;
    public Integer role;
    public Long createdTime;
    public Long updatedTime;
    public UserDTO user;

    public static class UserDTO {
        public String id;
        public String nickname;
        public String portraitUri;
        public String gender;
        public String stAccount;
        public String phone;
    }
}
