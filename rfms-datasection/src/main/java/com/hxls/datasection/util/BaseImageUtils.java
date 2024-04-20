package com.hxls.datasection.util;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Random;

public class BaseImageUtils {
    public String base64ToUrl(String picVehicleFileData, String path) throws IOException {
        byte[] bfile = Base64.getDecoder().decode(picVehicleFileData);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bfile);
        // 创建HttpClient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 目标API的URL
        String apiUrl = "http://113.250.190.179:8899/api/rfms-system/sys/file/httpUpload";

        // 创建HttpPost请求
        HttpPost httpPost = new HttpPost(apiUrl);

        // 构建请求体
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("file", inputStream, ContentType.APPLICATION_OCTET_STREAM, generateRandomDigits(6)+".jpg");
        builder.addTextBody("sitePri", path);

        // 设置请求体
        HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);

        // 发送请求并获取响应
        CloseableHttpResponse response = httpClient.execute(httpPost);

        // 打印响应结果
        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());
        JSON parse = JSONUtil.parse(responseBody);
        JSONObject data = parse.getByPath("data", JSONObject.class);
        String url = data.get("url", String.class);

        System.out.println("Status Code: " + statusCode);
        System.out.println("Response Body: " + responseBody);
        System.out.println("url地址: " + url);

        // 关闭HttpClient
        httpClient.close();
        return url;
    }

    // 生成指定位数的随机数字字符串
    private static String generateRandomDigits(int length) {

        // 获取当前时间
        LocalDateTime currentDateTime = LocalDateTime.now();
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化当前时间为字符串
        String formattedDateTime = currentDateTime.format(formatter);


        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }
        builder.append("_");
        builder.append(formattedDateTime);
        return builder.toString();
    }
}
