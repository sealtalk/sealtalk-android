package cn.rongcloud.im.model;

import io.rong.imlib.model.Conversation;
import java.util.List;

public class TypingInfo {
    public Conversation.ConversationType conversationType;
    public String targetId;
    public List<Typing> typingList;

    public static class Typing {
        public enum Type {
            voice,
            text
        }

        public Type type;
        public long sendTime;
        public String userId;
    }
}
