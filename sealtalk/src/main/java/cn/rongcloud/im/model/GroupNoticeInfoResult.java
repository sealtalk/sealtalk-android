package cn.rongcloud.im.model;

public class GroupNoticeInfoResult {
    public String id;

    public int status;

    public int type;

    public String createdAt;

    public String timestamp;

    public Requester requester;

    public Receiver receiver;

    public Group group;

    public static class Requester{
        public Requester(String id, String nickname) {
            this.id = id;
            this.nickname = nickname;
        }

        public String id;
        public String nickname;

    }

    public static class Receiver{
        public Receiver(String id, String nickname) {
            this.id = id;
            this.nickname = nickname;
        }

        public String id;
        public String nickname;
    }

    public static class Group{
        public Group(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String id;
        public String name;
    }
}
