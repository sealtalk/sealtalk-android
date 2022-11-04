package cn.rongcloud.im.model;

/** @author gusd @Date 2022/09/14 */
public class ProxyModel {
    private String testHost;
    private String proxyHost;
    private int port;
    private String userName;
    private String password;

    public String getTestHost() {
        return testHost;
    }

    public void setTestHost(String testHost) {
        this.testHost = testHost;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
