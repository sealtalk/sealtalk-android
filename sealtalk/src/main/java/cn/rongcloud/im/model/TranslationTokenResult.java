package cn.rongcloud.im.model;

import com.google.gson.annotations.SerializedName;

/** @author gusd */
public class TranslationTokenResult {

    @SerializedName("code")
    private Integer code;

    @SerializedName("errorMessage")
    private String errorMessage;

    @SerializedName("token")
    private String token;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
