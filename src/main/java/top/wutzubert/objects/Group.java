package top.wutzubert.objects;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import top.wutzubert.Main;

import java.io.IOException;

public class Group {
    private String id;
    public Group(String id){
        this.id = id;
    }
    public void replyStringMessage(String content,String messageId){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"content\": \""+content+"\",\"msg_type\": 0,\"msg_id\": \""+messageId+"\"}");
        Main.logger.info("{\"content\": \""+content+"\",\"msg_type\": 0,\"msg_id\": \""+messageId+"\"}");
        Request request = new Request.Builder()
                .url("https://sandbox.api.sgroup.qq.com/v2/groups/"+id+"/messages")
                .method("POST", body)
                .addHeader("Authorization", "QQBot "+Main.accessTokenTask.getAccessToken())
                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "*/*")
                .addHeader("Host", "sandbox.api.sgroup.qq.com")
                .addHeader("Connection", "keep-alive")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String responseBodyString = response.body().string();
            Main.logger.info(responseBodyString);
            JSONObject responseBody = JSONObject.parseObject(responseBodyString);
            if(responseBodyString.contains("err_code")){
                Main.logger.warning("消息发送失败,错误码"+responseBody.getString("err_code"));
            }else{
                Main.logger.info("消息发送成功，id="+responseBody.getString("id"));
            }
        } catch (IOException e) {
            Main.logger.warning(e.getMessage());
        }
    }
}
