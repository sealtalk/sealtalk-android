package io.rong.callkit.util;

/**
 * Created by Dengxudong on 2018/8/23.
 */

public class HeadsetInfo {
    private boolean isInsert;
    private HeadsetType type;

    public HeadsetInfo(boolean isInsert, HeadsetType type) {
        this.isInsert = isInsert;
        this.type = type;
    }

    public boolean isInsert() {
        return isInsert;
    }

    public void setInsert(boolean insert) {
        isInsert = insert;
    }

    public HeadsetType getType() {
        return type;
    }

    public void setType(HeadsetType type) {
        this.type = type;
    }


    public enum HeadsetType {
        /**
         * 有线耳机
         */
        WiredHeadset(0),
        /**
         * 蓝牙耳机
         */
        BluetoothA2dp(1);

        int value;

        HeadsetType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}
