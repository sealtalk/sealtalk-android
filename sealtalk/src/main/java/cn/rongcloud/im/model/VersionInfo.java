package cn.rongcloud.im.model;

/**
 * 版本信息类
 */
public class VersionInfo {


    /**
     * version : 1.0.5
     * build : 201607181821
     * url : https://dn-rongcloud.qbox.me/app.plist
     */

    private IOSVersion iOS;
    /**
     * version : 1.0.5
     * url : http://downloads.rongcloud.cn/SealTalk_by_RongCloud_Android_v1_0_5.apk
     */

    private AndroidVersion Android;

    public void setIos(IOSVersion ios) {
        this.iOS = ios;
    }

    public void setAndroidVersion(AndroidVersion android) {
        this.Android = android;
    }

    public IOSVersion getIosVersion() {
        return iOS;
    }

    public AndroidVersion getAndroidVersion() {
        return Android;
    }

    public static class IOSVersion {
        private String version;
        private String build;
        private String url;

        public void setVersion(String version) {
            this.version = version;
        }

        public void setBuild(String build) {
            this.build = build;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getVersion() {
            return version;
        }

        public String getBuild() {
            return build;
        }

        public String getUrl() {
            return url;
        }
    }

    public static class AndroidVersion {
        private String version;
        private String url;

        public void setVersion(String version) {
            this.version = version;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getVersion() {
            return version;
        }

        public String getUrl() {
            return url;
        }
    }
}
