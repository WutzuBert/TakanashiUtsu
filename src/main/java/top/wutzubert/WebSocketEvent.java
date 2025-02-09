package top.wutzubert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import top.wutzubert.objects.Group;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketEvent extends WebSocketClient {
    private String appId;
    private String token;
    private int d;
    public WebSocketEvent(URI uri,String appId,String token){
        super(uri);
        this.appId = appId;
        this.token = token;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Main.logger.info("与服务器建立WebSocket连接");
    }

    @Override
    public void onMessage(String message) {
        JSONObject response = JSON.parseObject(message);
        Main.logger.info(message);
        int op = response.getIntValue("op");
        if(op == 10){
            this.send("{\"op\": 2,\"d\": {\"token\": \"Bot "+appId+"."+token+"\",\"intents\": 33554432,\"shard\": [0,1],\"properties\": {}}}");
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                this.send("{\"op\":1,\"d\":"+d+"}");
                Main.logger.info("发送心跳ACK信息，d="+d);
            };
            scheduler.scheduleAtFixedRate(task,0,41250,TimeUnit.MILLISECONDS);
        } else if (op == 0) {
            String type = response.getString("t");
            switch (type){
                case "READY"->Main.logger.info("服务器鉴权成功，事件监听已开启");
                case "GROUP_AT_MESSAGE_CREATE"->{
                    Group group = new Group(response.getJSONObject("d").getString("group_id"));
                    String content = response.getJSONObject("d").getString("content");
                    if(content.startsWith(" /天气 ")){
                        String city = content.replace(" /天气 ","");
                        OkHttpClient client = new OkHttpClient().newBuilder()
                                .build();
                        Request request = new Request.Builder()
                                .url("http://apis.juhe.cn/simpleWeather/query?city="+city+"&key=1b18650a01aafb9cf90125f994a9f7ce")
                                .method("GET",null)
                                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                                .addHeader("Accept", "*/*")
                                .addHeader("Host", "apis.juhe.cn")
                                .addHeader("Connection", "keep-alive")
                                .addHeader("Cookie", "aliyungf_tc=1df3da29e1a9ec6c40db3f24b94f2e9627b5bf9ffa20a75722419340e1376ffa")
                                .build();
                        try {
                            Response weatherResponse = client.newCall(request).execute();
                            JSONObject weather = JSONObject.parseObject(weatherResponse.body().string());
                            if(weather.getIntValue("error_code") == 0){
                                String weatherInfo = "{city}当前天气数据\\n"
                                        +"实时:\\n"
                                        +"气温:{realtime_temp}\\n"
                                        +"湿度:{humidity}%\\n"
                                        +"天气:{info}\\n"
                                        +"风向:{direct}\\n"
                                        +"风机:{power}\\n"
                                        +"AQI指数:{aqi}\\n"
                                        +"------------------\\n"
                                        +"未来天气预报\\n";
                                JSONObject realtime = weather.getJSONObject("result").getJSONObject("realtime");
                                weatherInfo = weatherInfo.replace("{realtime_temp}",realtime.getString("temperature"))
                                        .replace("{humidity}",realtime.getString("humidity"))
                                        .replace("{info}", realtime.getString("info"))
                                        .replace("{direct}",realtime.getString("direct"))
                                        .replace("{power}", realtime.getString("power"))
                                        .replace("{aqi}", realtime.getString("aqi"));
                                JSONArray future = weather.getJSONObject("result").getJSONArray("future");
                                for(int i=0;i < future.size();i++){
                                    weatherInfo = weatherInfo+future.getJSONObject(i).getString("date")+":\\n"
                                            +"气温:"+future.getJSONObject(i).getString("temperature")+"\\n"
                                            +"天气:"+future.getJSONObject(i).getString("weather")+"\\n"
                                            +"风向:"+future.getJSONObject(i).getString("direct")+"\\n";
                                }
                                group.replyStringMessage(weatherInfo,response.getJSONObject("d").getString("id"));
                            } else if (weather.getIntValue("error_code")==10012) {
                                group.replyStringMessage("当日接口调用次数已达上限，请次日再试",response.getJSONObject("d").getString("id"));
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (content.startsWith(" /一言")) {
                        OkHttpClient client = new OkHttpClient().newBuilder()
                                .build();
                        Request request = new Request.Builder()
                                .url("https://api.qjqq.cn/api/Yi")
                                .method("GET",null)
                                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                                .addHeader("Accept", "*/*")
                                .addHeader("Host", "api.qjqq.cn")
                                .addHeader("Connection", "keep-alive")
                                .addHeader("Cookie", "sl-session=1H50VFmqqGfb3CkVPDz6ew==")
                                .build();
                        try {
                            Response sentenceResponse = client.newCall(request).execute();
                            JSONObject responseJSON = JSONObject.parseObject(sentenceResponse.body().string());
                            String reply = "”"+responseJSON.getString("hitokoto")+"“\\n"+"——"+responseJSON.getString("from_who")+"《"+responseJSON.getString("from")+"》";
                            reply = reply.replace("null","");
                            group.replyStringMessage(reply,response.getJSONObject("d").getString("id"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            }
            d=response.getIntValue("s");
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Main.logger.info("WebSocket连接断开，状态码"+code+"，原因:"+reason);
    }

    @Override
    public void onError(Exception ex) {

    }
}

