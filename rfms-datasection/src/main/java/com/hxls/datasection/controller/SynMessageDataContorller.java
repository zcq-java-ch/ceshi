package com.hxls.datasection.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.framework.rabbitmq.domain.MessageSendDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;

@Tag(name="同步消息数据")
@Component
@RequiredArgsConstructor
@Slf4j
public class SynMessageDataContorller {

    private final TPersonAccessRecordsService tPersonAccessRecordsService;
    /**
     * 接收客户端传来的人员人别记录
     * */
    @RabbitHandler
    @RabbitListener(queues = "#{dynamicQueueNameProvider.getDynamicFaceQueueNameFromCloud()}")
    public void receiveFaceDataFromTheClient(Message message, Channel c, String s) throws IOException, ClassNotFoundException {
        MessageProperties properties = message.getMessageProperties();
//        long tag = properties.getDeliveryTag();
//        log.info("简单模式的消费者收到:{}",s);
//        System.out.println("接收到来自客户端的人脸识别历史数据:"+s);
//        MessageSendDto student = Convert.convert(MessageSendDto.class, s);
        MessageSendDto student = com.alibaba.fastjson.JSONObject.parseObject(s, MessageSendDto.class);
//        log.info(student.toString());
        cn.hutool.json.JSONObject messageData = student.getMessageData();
        JSONArray records = messageData.get("faceRecords", JSONArray.class);
        for (int i = 0; i < records.size(); i++) {
            JSONObject jsonObjectRecords = (JSONObject)records.get(i);
            TPersonAccessRecordsEntity tPersonAccessRecordsEntity = new TPersonAccessRecordsEntity();
            tPersonAccessRecordsEntity.setChannelId(jsonObjectRecords.get("channel_id", Long.class));
            tPersonAccessRecordsEntity.setChannelName(jsonObjectRecords.get("channel_name", String.class));
            tPersonAccessRecordsEntity.setDeviceId(jsonObjectRecords.get("device_id", Long.class));
            tPersonAccessRecordsEntity.setDeviceName(jsonObjectRecords.get("deviceName", String.class));
            tPersonAccessRecordsEntity.setAccessType(jsonObjectRecords.get("access_type", String.class));
            tPersonAccessRecordsEntity.setHeadUrl(jsonObjectRecords.get("head_url", String.class));
            tPersonAccessRecordsEntity.setPersonName(jsonObjectRecords.get("person_name", String.class));
            tPersonAccessRecordsEntity.setDevicePersonId(jsonObjectRecords.get("device_person_id", String.class));
            tPersonAccessRecordsEntity.setRecordTime(jsonObjectRecords.get("record_time", Date.class));

            tPersonAccessRecordsService.save(tPersonAccessRecordsEntity);
        }


        System.out.println("接收到来自客户端的人脸识别历史数据"+records.get(0, cn.hutool.json.JSONObject.class));
        //手动回执，不批量签收,回执后才能处理下一批消息
//        c.basicAck(tag,false);
//        System.out.println("正常进入人脸设备  : " + o);
    }

    /**
     * 接收客户端传来的车辆道闸记录
     * */
//    @RabbitHandler
//    @RabbitListener(queues = "#{dynamicQueueNameProvider.getDynamicCarQueueNameFromCloud()}")
//    public void receiveCarDataFromTheClient(Message message, Channel c, String s) throws IOException, ClassNotFoundException {
//        MessageProperties properties = message.getMessageProperties();
////        long tag = properties.getDeliveryTag();
////        log.info("简单模式的消费者收到:{}",s);
//        System.out.println("接收到来自客户端的人脸识别历史数据:"+s);
//        MessageSendDto student = JSONObject.parseObject(s, MessageSendDto.class);
////        log.info(student.toString());
//        System.out.println("student.toString()"+student.toString());
//        //手动回执，不批量签收,回执后才能处理下一批消息
////        c.basicAck(tag,false);
////        System.out.println("正常进入人脸设备  : " + o);
//    }
}
