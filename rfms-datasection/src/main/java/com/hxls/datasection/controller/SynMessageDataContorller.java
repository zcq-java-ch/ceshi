package com.hxls.datasection.controller;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import com.hxls.framework.rabbitmq.domain.MessageSendDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.Queue;
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

    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;
    /**
     * 接收客户端传来的人员人别记录
     * */
    @RabbitHandler
//    @RabbitListener(queues = "#{T(java.util.Arrays).stream(dynamicQueueNameProvider.getDynamicFaceQueueNameFromCloud()).filter(queue -> dynamicQueueNameProvider.isQueueExist(queue)).toArray()}")
    @RabbitListener(queues = "#{dynamicQueueNameProvider.getDynamicFaceQueueNameFromCloud}")
    public void receiveFaceDataFromTheClient(Message message, Channel c, String s) throws IOException, ClassNotFoundException {
        MessageProperties properties = message.getMessageProperties();

//        log.info("接收到来自通道{},的消息{}",c,s);

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

            // 判断数据  数据库中已经存在
//            String records_id = jsonObjectRecords.get("records_id", String.class);
            // 唯一编码
            String records_id =  jsonObjectRecords.get("device_person_id", String.class) +  jsonObjectRecords.get("record_time", String.class);
//            log.info("人脸惟一值是{}", records_id);
            boolean whetherItExists = tPersonAccessRecordsService.whetherItExists(records_id);
            if (whetherItExists){
                // 存在
                log.info("人脸数据已经存在不进行存储");
            }else {
                log.info("人脸数据不存在，开始存储");
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
                tPersonAccessRecordsEntity.setManufacturerId(jsonObjectRecords.get("manufacturer_id", Long.class));
                tPersonAccessRecordsEntity.setManufacturerName(jsonObjectRecords.get("manufacturer_name", String.class));
                tPersonAccessRecordsEntity.setSiteId(jsonObjectRecords.get("siteId", Long.class));
                tPersonAccessRecordsEntity.setSiteName(jsonObjectRecords.get("siteName", String.class));
                tPersonAccessRecordsEntity.setRecordsId(records_id);
                tPersonAccessRecordsService.save(tPersonAccessRecordsEntity);
                log.info("人脸数据不存在，结束存储");
            }
        }

        //手动回执，不批量签收,回执后才能处理下一批消息
//        c.basicAck(tag,false);
//        System.out.println("正常进入人脸设备  : " + o);
    }

    /**
     * 接收客户端传来的车辆道闸记录
     * */
    @RabbitHandler
    @RabbitListener(queues = "#{dynamicQueueNameProvider.getDynamicCarQueueNameFromCloud}")
    public void receiveCarDataFromTheClient(Message message, Channel c, String s) throws IOException, ClassNotFoundException {
        MessageProperties properties = message.getMessageProperties();

//        log.info("接收到来自通道{},的消息{}",c,s);

        MessageSendDto student = com.alibaba.fastjson.JSONObject.parseObject(s, MessageSendDto.class);
        cn.hutool.json.JSONObject messageData = student.getMessageData();
        JSONArray records = messageData.get("carRecords", JSONArray.class);
        for (int i = 0; i < records.size(); i++) {
            JSONObject jsonObjectRecords = (JSONObject)records.get(i);

            // 判断数据  数据库中已经存在
            String records_id = jsonObjectRecords.get("records_id", String.class);
            log.info("车辆惟一值是{}", records_id);
            boolean whetherItExists = tVehicleAccessRecordsService.whetherItExists(records_id);
            if (whetherItExists){
                // 存在
            }else {
                log.info("开始插入车辆记录");
                TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity = new TVehicleAccessRecordsEntity();
                tVehicleAccessRecordsEntity.setChannelId(jsonObjectRecords.get("channel_id", Long.class));
                tVehicleAccessRecordsEntity.setChannelName(jsonObjectRecords.get("channel_name", String.class));
                tVehicleAccessRecordsEntity.setDeviceId(jsonObjectRecords.get("device_id", Long.class));
                tVehicleAccessRecordsEntity.setDeviceName(jsonObjectRecords.get("deviceName", String.class));

                tVehicleAccessRecordsEntity.setManufacturerId(jsonObjectRecords.get("manufacturer_id", Long.class));
                tVehicleAccessRecordsEntity.setManufacturerName(jsonObjectRecords.get("manufacturer_name", String.class));
                tVehicleAccessRecordsEntity.setPlateNumber(jsonObjectRecords.get("plateNumber", String.class));
                tVehicleAccessRecordsEntity.setRecordsId(jsonObjectRecords.get("records_id", String.class));

                String passChannelType = jsonObjectRecords.get("passChannelType", String.class);
                String access_type = jsonObjectRecords.get("access_type", String.class);
                tVehicleAccessRecordsEntity.setAccessType(access_type);
                tVehicleAccessRecordsEntity.setCarUrl(jsonObjectRecords.get("car_url", String.class));
                tVehicleAccessRecordsEntity.setRecordTime(jsonObjectRecords.get("record_time", Date.class));
                tVehicleAccessRecordsEntity.setSiteId(jsonObjectRecords.get("siteId", Long.class));
                tVehicleAccessRecordsEntity.setSiteName(jsonObjectRecords.get("siteName", String.class));
                tVehicleAccessRecordsService.save(tVehicleAccessRecordsEntity);

                // 存储车辆进出场展示台账
                tVehicleAccessRecordsService.saveLedger(tVehicleAccessRecordsEntity);

            }
        }

        //手动回执，不批量签收,回执后才能处理下一批消息
//        c.basicAck(tag,false);
//        System.out.println("正常进入人脸设备  : " + o);
    }

}
