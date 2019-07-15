package io.rong.pushperm.config;

import java.util.List;

/**
 * 机型配置信息封装类。用于封装配置的信息
 */
class ModelInfo {
    /**
     * 手机厂商
     */
    public String manufacturer;
    /**
     * 自启动跳转消息配置
     */
    public List<Info> autoStart;

    /**
     * 普通动态权限跳转消息配置
     */
    public List<Info> commomPerm;

    /**
     * 通知跳转消息配置
     */
    public List<Info> notification;

    /**
     * 锁屏清理跳转消息配置
     */
    public List<Info> lockClean;

    /**
     * 悬浮窗跳转消息配置
     */
    public List<Info> floatWindow;

    /**
     * 配置具体跳转消息的封装。
     */
    public static class Info {
        /**
         * 包名
         */
        public String packageName;
        /**
         * 类名
         */
        public String clazzName;
        /**
         * action 消息
         */
        public String actionName;

        /**
         * catagoty
         */
        public String catagoty;

        /**
         * 设置权限的路径
         */
        public String setPath;

        /**
         * data 数据
         */
        public Param dataUri;

        /**
         * extra 数据
         */
        public List<Param> extras;


        @Override
        public String toString() {
            return "Info{" +
                    "packageName='" + packageName + '\'' +
                    ", clazzName='" + clazzName + '\'' +
                    ", actionName='" + actionName + '\'' +
                    ", catagoty='" + catagoty + '\'' +
                    ", setPath='" + setPath + '\'' +
                    '}';
        }
    }

    /**
     * 配置具体跳转消息的封装。
     */
    public static class Param {
        public static final String AUTO = "auto";
        /**
         * 参数名
         */
        public String key;
        /**
         *  参数值
         */
        public String value;
        /**
         *  类型， 预留字段, 加入要支持不同类型的数据
         */
        public String type = "String";

    }

    @Override
    public String toString() {
        return "ModelInfo{" +
                "manufacturer='" + manufacturer + '\'' +
                ", autoStart=" + autoStart +
                ", commomPerm=" + commomPerm +
                ", notification=" + notification +
                ", lockClean=" + lockClean +
                ", floatWindow=" + floatWindow +
                '}';
    }
}
