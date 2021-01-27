package cn.rongcloud.im.model;

import java.util.List;

import io.rong.imlib.model.Conversation;

public class TypingInfo {
    public Conversation.ConversationType conversationType;
    public String targetId;
    public List<Typing> typingList;

    public static class Typing {
        public enum Type{
            voice,
            text
        }

        public Type type;
        public long sendTime;
        public String userId;
    }
}
