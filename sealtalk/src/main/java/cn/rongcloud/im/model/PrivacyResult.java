package cn.rongcloud.im.model;

public class PrivacyResult {

    public int phoneVerify;
    public int stSearchVerify;
    public int friVerify;
    public int groupVerify;

    public enum State {
        NOTALLOW(0),
        ALLOW(1);

        int value;

        State(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static State getState(int value) {
            if (value >= 0 && value < State.values().length) {
                return State.values()[value];
            }
            return NOTALLOW;
        }
    }
}
