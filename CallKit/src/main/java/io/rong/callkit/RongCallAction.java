package io.rong.callkit;

/**
 * Created by weiqinxiao on 16/3/15.
 */
public enum RongCallAction {
    ACTION_OUTGOING_CALL(1, "ACTION_OUTGOING_CALL"),
    ACTION_INCOMING_CALL(2, "ACTION_INCOMING_CALL"),
    ACTION_ADD_MEMBER(3, "ACTION_ADD_MEMBER"),
    ACTION_RESUME_CALL(4, "ACTION_RESUME_CALL");

    int value;
    String msg;
    RongCallAction(int v, String msg) {
        this.value = v;
        this.msg = msg;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return msg;
    }
}
