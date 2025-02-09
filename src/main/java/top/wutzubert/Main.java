package top.wutzubert;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import top.wutzubert.beans.Bot;
import top.wutzubert.tasks.GetAccessToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class Main {
    public static final Logger logger = Logger.getLogger(Main.class.getName());
    public static GetAccessToken accessTokenTask;
    public static void main(String[] args){;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        logger.setUseParentHandlers(false);
        Formatter formatter = new Formatter(){
            @Override
            public String format(LogRecord record) {
                StringBuilder builder = new StringBuilder();
                builder.append(dateFormat.format(new Date(record.getMillis()))).append(" ");
                builder.append("[").append(record.getLevel().getName()).append("] ").append(formatMessage(record)).append("\n");
                // 添加颜色控制字符
                if (record.getLevel() == Level.SEVERE) {
                    builder.insert(0, "\033[31m"); // 红色
                } else if (record.getLevel() == Level.WARNING) {
                    builder.insert(0, "\033[33m"); // 黄色
                } else {
                    builder.insert(0, "\033[32m"); // 绿色
                }

                // 添加重置颜色和样式的控制字符
                builder.append("\033[0m");

                return builder.toString();
            }
        };
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        try {
            FileHandler loggerHandler = new FileHandler(new SimpleDateFormat("yyyy-MM-dd").format(new Date())+".log",false);
            loggerHandler.setFormatter(formatter);
            logger.addHandler(loggerHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("读取配置文件……");
        File config = new File("./config.json");
        Bot bot = null;
        try{
            if(!config.exists()){
                config.createNewFile();
                BufferedWriter configWriter = new BufferedWriter(new FileWriter(config,true));
                configWriter.write("{\"appId\": \"114514\",\"clientSecret\": \"0d000721\",\"token\": \"1919810\"}");
                logger.warning("未检测到配置文件，已生成新配置文件，请完成配置后再启动程序");
                System.exit(-1);
            }else{
                bot = JSONObject.parseObject(Files.readString(Paths.get(config.getPath())),Bot.class);
                accessTokenTask = new GetAccessToken(bot.getAppId(),bot.getClientSecret());
                accessTokenTask.start();
        }}catch (Exception e){logger.warning(e.getMessage());}
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            WebSocketEvent webSocketEvent = new WebSocketEvent(new URI(getWSSGateway(accessTokenTask.getAccessToken())),bot.getAppId(), bot.getToken());
            webSocketEvent.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getWSSGateway(String accessToken){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        Request request = new Request.Builder()
                .url("https://sandbox.api.sgroup.qq.com/gateway")
                .method("GET",null)
                .addHeader("Authorization", "QQBot "+accessToken)
                .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                .addHeader("Accept", "*/*")
                .addHeader("Host", "sandbox.api.sgroup.qq.com")
                .addHeader("Connection", "keep-alive")
                .build();
        try {
            Response response = client.newCall(request).execute();
            return JSONObject.parseObject(response.body().string()).getString("url");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
