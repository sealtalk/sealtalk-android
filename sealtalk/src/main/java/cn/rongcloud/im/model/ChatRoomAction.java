package cn.rongcloud.im.model;

/**
 * 聊天室状态行为
 */
public class ChatRoomAction {
    public String roomId;
    public Status status;

    private ChatRoomAction(String roomId, Status status){
        this.roomId = roomId;
        this.status = status;
    }

    public static ChatRoomAction joining(String roomId){
       return new ChatRoomAction(roomId, Status.JOINING);
    }

    public static ChatRoomAction joined(String roomId){
        return new ChatRoomAction(roomId, Status.JOINED);
    }

    public static ChatRoomAction reset(String roomId){
        return new ChatRoomAction(roomId, Status.RESET);
    }

    public static ChatRoomAction quited(String roomId){
        return new ChatRoomAction(roomId, Status.QUITED);
    }

    public static ChatRoomAction destroyed(String roomId){
        return new ChatRoomAction(roomId, Status.DESTROY);
    }

    public static ChatRoomAction error(String roomId){
        return new ChatRoomAction(roomId, Status.ERROR);
    }


    public enum Status {
        JOINING,
        JOINED,
        RESET,
        DESTROY,
        QUITED,
        ERROR
    }
}
