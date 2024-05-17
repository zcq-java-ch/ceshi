package com.hxls.datasection.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
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
    public static String base64ToUrl(String picVehicleFileData, String path, String fileName) throws IOException {
        byte[] bfile = Base64.getDecoder().decode(picVehicleFileData);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bfile);
        // 创建HttpClient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 目标API的URL
        String apiUrl = "http://113.250.190.179:8899/api/rfms-system/sys/file/httpUpload"; //测试环境
//        String apiUrl = "http://182.150.57.78:18899/api/rfms-system/sys/file/httpUpload";
//        String apiUrl = "https://rns.huashijc.com/api/rfms-system/sys/file/httpUpload";

        // 创建HttpPost请求
        HttpPost httpPost = new HttpPost(apiUrl);

        // 构建请求体
        MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);

        String fileNameOr = generateRandomDigits(6)+".jpg";
        if (StringUtils.isNotBlank(fileName)){
            fileNameOr = fileName + '_' + fileNameOr;
        }
        builder.addBinaryBody("file", inputStream, ContentType.APPLICATION_OCTET_STREAM, fileNameOr);
        builder.addTextBody("sitePri", path);

        // 设置请求体
        HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);

        // 发送请求并获取响应
        CloseableHttpResponse response = httpClient.execute(httpPost);

        // 打印响应结果
        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity());
        JSONObject parse = com.alibaba.fastjson.JSON.parseObject(responseBody);
        JSONObject data = parse.getJSONObject("data");
        String url = data.getString("url");

        System.out.println("Status Code: " + statusCode);
        System.out.println("Response Body: " + responseBody);
        System.out.println("url地址: " + url);

        // 关闭HttpClient
        httpClient.close();
        return url;
    }

    // 生成指定位数的随机数字字符串
    private static String generateRandomDigits(int length) {

        long currentTimeMillis = System.currentTimeMillis();
        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));
        }
        builder.append("_");
        builder.append(currentTimeMillis);
        return builder.toString();
    }
}
