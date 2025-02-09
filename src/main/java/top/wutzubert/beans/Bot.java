package top.wutzubert.beans;

public class Bot {
    private String appId;
    private String token;
    private String clientSecret;

    public String getAppId() {
        return appId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getToken() {
        return token;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setClientSecret(String appSecret) {
        this.clientSecret = appSecret;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
