package io.rong.pushperm.perm;

/**
 * 特殊权限类型
 */
public enum PermissionType {
    /**
     * 自启动权限
     */
    PERM_AUTO_START("auto_start", "auto_start"),
    /**
     * 通知权限
     */
    PERM_NOTIFICATION("notifi", "notifination"),
    /**
     * 不清理应用
     */
    PERM_NO_CLEAN("no_clean", "no_clean");

    private String value ;
    private String name ;

    PermissionType(String name, String value) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }
    public String getName() {
        return name;
    }
}