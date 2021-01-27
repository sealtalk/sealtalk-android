package cn.rongcloud.im.model;

/**
 * 定时清理服务器返回结果
 */
public class RegularClearStatusResult {
    // 0 关闭、 3 清理 3 天前、 7 清理 7 天前、 36 清理 36 小时前
    public int clearStatus;

    public enum ClearStatus {
        CLOSE(0),
        THREE_DAYS(3),
        SEVEN_DAYS(7),
        THIRTY_SIX_HOUR(36);
        int value;

        ClearStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ClearStatus getRole(int value) {
            if (value >= 0 && value < ClearStatus.values().length) {
                return ClearStatus.values()[value];
            }
            return CLOSE;
        }
    }
}
