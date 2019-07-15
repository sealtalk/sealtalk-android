package cn.rongcloud.im.model;

/**
 * 用户登录返回结果
 */
public class LoginResult {
    public String id;
    public String token;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
