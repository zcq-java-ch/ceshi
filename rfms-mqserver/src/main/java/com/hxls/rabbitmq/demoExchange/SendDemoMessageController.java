package com.hxls.rabbitmq.demoExchange;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hxls.rabbitmq.domain.MessageSendDto;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class SendDemoMessageController {

    public static final String ROUTING_FACE = "_ROUTING_FACE";
    @Autowired
    RabbitTemplate rabbitTemplate ;

    // 人脸
    @GetMapping("/sendTopicFaceMessage")
    public String sendTopicMessage1() throws InterruptedException {
        String messageId = String.valueOf(UUID.randomUUID());
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 模拟人脸注册参数
        /*
        * stationid: 站点编码
        * stationname: 站点名字
        * stationip: 站点ip
        * type: 人脸/车辆
        * devicelist:[
        *   {
        *       deviceip: 设备ip
        *       devicekey: 设备相关密钥
        *   }
        * ]
        * facedata: {
        *   userid: 用户ID
        *   username: 用户姓名
        *   facebase64:
        * }
        *
        * */

        List<JSONObject> objects = new ArrayList<>();
        JSONObject device1 = JSONUtil.createObj();
        device1.putOnce("deviceip", "10.31.1.2");
        device1.putOnce("devicekey", "xxxxxxxx");
        JSONObject device2 = JSONUtil.createObj();
        device2.putOnce("deviceip", "10.31.1.2");
        device2.putOnce("devicekey", "cccccccccc");
        objects.add(device1);
        objects.add(device2);

        JSONObject facedata = JSONUtil.createObj();
        facedata.putOnce("userid", "96543");
        facedata.putOnce("username", "小白");
        facedata.putOnce("facebase64", "base64kkkkkkkkkkkkkkk");

        JSONObject messageData = JSONUtil.createObj();
        messageData.putOnce("stationid", "1234312");
        messageData.putOnce("stationname", "西昌");
        messageData.putOnce("stationtopic", "XICHANG");
        messageData.putOnce("stationip", "10.31.1.1");
        messageData.putOnce("type", "FACE");
        messageData.putOnce("devicelist", objects);
        messageData.putOnce("facedata", facedata);
        MessageSendDto map = new MessageSendDto();
        map.setMessageId(messageId);
        map.setMessageData(messageData);
        map.setCreateTime(createTime);

        // 选择交换机，通过传入参数·
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setExpiration("10000"); // 设置 TTL 为 10 秒
        Message message = new Message(map.toString().getBytes(), messageProperties);
        rabbitTemplate.convertAndSend(messageData.get("stationtopic").toString()+"_EXCHANGE", messageData.get("stationtopic").toString()+ ROUTING_FACE, map);

        return "ok";
    }

    // 车辆
//    @GetMapping("/sendTopicCarMessage")
//    public String sendTopicMessage2() {
//        String messageId = String.valueOf(UUID.randomUUID());
//        String messageData = "message: woman is all ";
//        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        Map<String, Object> womanMap = new HashMap<>();
//        womanMap.put("messageId", messageId);
//        womanMap.put("messageData", messageData);
//        womanMap.put("createTime", createTime);
//        rabbitTemplate.convertAndSend(messageData.get("stationtopic").toString()+"_EXCHANGE", messageData.get("stationtopic").toString()+"_ROUTING_FACE", manMap);
//
//        return "ok";
//    }
}
