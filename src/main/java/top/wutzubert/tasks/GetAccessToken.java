package top.wutzubert.tasks;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import top.wutzubert.Main;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GetAccessToken extends Thread{
    public GetAccessToken(String appId,String clientSecret){
        this.appId = appId;
        this.clientSecret = clientSecret;
    }
    private String appId;
    private String clientSecret;
    private String accessToken;
    public ScheduledExecutorService cycle;
    private JSONObject responseJSON;
    @Override
    public void run(){
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"appId\": \""+appId+"\",\"clientSecret\": \""+clientSecret+"\"}");
            Request request = new Request.Builder()
                    .url("https://bots.qq.com/app/getAppAccessToken")
                    .method("POST", body)
                    .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "*/*")
                    .addHeader("Host", "bots.qq.com")
                    .addHeader("Connection", "keep-alive")
                    .build();
            Response response = client.newCall(request).execute();
            String responseMSG = response.body().string();
            Main.logger.info(responseMSG);
            responseJSON = JSONObject.parseObject(responseMSG);
            accessToken = responseJSON.getString("access_token");
            Main.logger.info("AccessToken已刷新");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            try {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\"appId\": \""+appId+"\",\"clientSecret\": \""+clientSecret+"\"}");
                Request request = new Request.Builder()
                        .url("https://bots.qq.com/app/getAppAccessToken")
                        .method("POST", body)
                        .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "*/*")
                        .addHeader("Host", "bots.qq.com")
                        .addHeader("Connection", "keep-alive")
                        .build();
                Response response = client.newCall(request).execute();
                responseJSON = JSONObject.parseObject(response.body().string());
                accessToken = responseJSON.getString("access_token");
                Main.logger.info("AccessToken已刷新");
                scheduler.schedule(this,Integer.parseInt(responseJSON.getString("expires_in")),TimeUnit.SECONDS);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
        scheduler.schedule(task,0,TimeUnit.SECONDS);
        cycle = scheduler;
    }
    public String getAccessToken(){
        return accessToken;
    }
}
