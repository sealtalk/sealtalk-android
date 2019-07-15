package cn.rongcloud.im.model;

/**
 * 请求上传图片结果
 */
public class UploadTokenResult {
    /**
     * 云存储类型
     */
    private String target;
    /**
     * 云存储图片地址域名
     */
    private String domain;
    /**
     * 云存储 Token
     */
    private String token;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
