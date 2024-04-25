package com.hxls.datasection.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hxls.api.feign.system.UserFeign;
import com.hxls.api.feign.system.VehicleFeign;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import com.hxls.framework.rabbitmq.domain.MessageSendDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.Date;

@Tag(name="同步消息数据")
@Component
@RequiredArgsConstructor
@Slf4j
public class SynMessageDataContorller {

    private final TPersonAccessRecordsService tPersonAccessRecordsService;
    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;
    private final VehicleFeign vehicleFeign;
    private final UserFeign userFeign;
    /**
     * 接收客户端传来的人员人别记录
     *
     * 目前接收的是：华安视讯  凌智恒
     * */
    @RabbitHandler
    @RabbitListener(queues = "#{dynamicQueueNameProvider.getDynamicFaceQueueNameFromCloud}")
    public void receiveFaceDataFromTheClient(Message message, Channel c, String s) throws IOException, ClassNotFoundException {
        MessageProperties properties = message.getMessageProperties();

        MessageSendDto student = JSONObject.parseObject(s, MessageSendDto.class);
        JSONObject messageData = student.getMessageData();
        JSONArray records = messageData.getJSONArray("faceRecords");
        for (int i = 0; i < records.size(); i++) {
            JSONObject jsonObjectRecords = (JSONObject)records.get(i);

            // 唯一编码
            String recordsId =  jsonObjectRecords.getString("device_person_id") +  jsonObjectRecords.getString("record_time");
            boolean whetherItExists = tPersonAccessRecordsService.whetherItExists(recordsId);
            if (whetherItExists){
                // 存在
                log.info("人脸数据已经存在不进行存储");
            }else {
                log.info("人脸数据不存在，开始存储");
                TPersonAccessRecordsEntity tPersonAccessRecordsEntity = new TPersonAccessRecordsEntity();
                tPersonAccessRecordsEntity.setChannelId(jsonObjectRecords.getLong("channel_id"));
                tPersonAccessRecordsEntity.setChannelName(jsonObjectRecords.getString("channel_name"));
                tPersonAccessRecordsEntity.setDeviceId(jsonObjectRecords.getLong("device_id"));
                tPersonAccessRecordsEntity.setDeviceName(jsonObjectRecords.getString("deviceName"));
                tPersonAccessRecordsEntity.setAccessType(jsonObjectRecords.getString("access_type"));
                tPersonAccessRecordsEntity.setHeadUrl(jsonObjectRecords.getString("head_url"));
                tPersonAccessRecordsEntity.setPersonName(jsonObjectRecords.getString("person_name"));
                tPersonAccessRecordsEntity.setDevicePersonId(jsonObjectRecords.getString("device_person_id"));
                tPersonAccessRecordsEntity.setRecordTime(jsonObjectRecords.getDate("record_time"));
                tPersonAccessRecordsEntity.setManufacturerId(jsonObjectRecords.getLong("manufacturer_id"));
                tPersonAccessRecordsEntity.setManufacturerName(jsonObjectRecords.getString("manufacturer_name"));
                tPersonAccessRecordsEntity.setSiteId(jsonObjectRecords.getLong("siteId"));
                tPersonAccessRecordsEntity.setSiteName(jsonObjectRecords.getString("siteName"));
                tPersonAccessRecordsEntity.setRecordsId(recordsId);

                // 通过用户唯一编码查询用户，然后将客户端的识别数据与平台的用户数据进行绑定
                String devicePersonId = jsonObjectRecords.getString("device_person_id");
                if (StringUtils.isNotEmpty(devicePersonId)){
                    JSONObject userDetail = userFeign.queryUserInformationUserId(devicePersonId);
                    if (ObjectUtils.isNotEmpty(userDetail)) {
                        tPersonAccessRecordsEntity.setCompanyId(userDetail.getLong("orgId"));
                        tPersonAccessRecordsEntity.setCompanyName(userDetail.getString("orgName"));
                        tPersonAccessRecordsEntity.setSupervisorName(userDetail.getString("supervisor"));
                        tPersonAccessRecordsEntity.setIdCardNumber(userDetail.getString("idCard"));
                        tPersonAccessRecordsEntity.setPhone(userDetail.getString("mobile"));
                        tPersonAccessRecordsEntity.setPositionId(userDetail.getLong("postId"));
                        tPersonAccessRecordsEntity.setPositionName(userDetail.getString("postName"));
                        tPersonAccessRecordsEntity.setBusis(userDetail.getString("busis"));
                    }
                }
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

        MessageSendDto student = JSONObject.parseObject(s, MessageSendDto.class);
        JSONObject messageData = student.getMessageData();
        JSONArray records = messageData.getJSONArray("carRecords");
        for (int i = 0; i < records.size(); i++) {
            JSONObject jsonObjectRecords = (JSONObject)records.get(i);

            // 判断数据  数据库中已经存在
            String recordsId = jsonObjectRecords.getString("records_id");
            boolean whetherItExists = tVehicleAccessRecordsService.whetherItExists(recordsId);
            if (whetherItExists){
                // 存在
            }else {
                log.info("开始插入车辆记录");
                TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity = new TVehicleAccessRecordsEntity();
                tVehicleAccessRecordsEntity.setChannelId(jsonObjectRecords.getLong("channel_id"));
                tVehicleAccessRecordsEntity.setChannelName(jsonObjectRecords.getString("channel_name"));
                tVehicleAccessRecordsEntity.setDeviceId(jsonObjectRecords.getLong("device_id"));
                tVehicleAccessRecordsEntity.setDeviceName(jsonObjectRecords.getString("deviceName"));

                tVehicleAccessRecordsEntity.setManufacturerId(jsonObjectRecords.getLong("manufacturer_id"));
                tVehicleAccessRecordsEntity.setManufacturerName(jsonObjectRecords.getString("manufacturer_name"));
                tVehicleAccessRecordsEntity.setPlateNumber(jsonObjectRecords.getString("plateNumber"));
                tVehicleAccessRecordsEntity.setRecordsId(jsonObjectRecords.getString("records_id"));

                String passChannelType = jsonObjectRecords.getString("passChannelType");
                String accessType = jsonObjectRecords.getString("access_type");
                tVehicleAccessRecordsEntity.setAccessType(accessType);
                tVehicleAccessRecordsEntity.setCarUrl(jsonObjectRecords.getString("car_url"));
                tVehicleAccessRecordsEntity.setRecordTime(jsonObjectRecords.getDate("record_time"));
                tVehicleAccessRecordsEntity.setSiteId(jsonObjectRecords.getLong("siteId"));
                tVehicleAccessRecordsEntity.setSiteName(jsonObjectRecords.getString("siteName"));

                /**
                 * 需要通过车牌绑定平台车辆信息数据
                 * */
                if (ObjectUtils.isNotEmpty(jsonObjectRecords.getString("plateNumber"))){
                    JSONObject jsonObject = vehicleFeign.queryVehicleInformationByLicensePlateNumber(jsonObjectRecords.getString("plateNumber"));
                    if(ObjectUtils.isNotEmpty(jsonObject)){
                        tVehicleAccessRecordsEntity.setVehicleModel(jsonObject.getString("carType"));
                        tVehicleAccessRecordsEntity.setEmissionStandard(jsonObject.getString("emissionStandard"));
                        tVehicleAccessRecordsEntity.setDriverId(jsonObject.getLong("driverId"));
                        tVehicleAccessRecordsEntity.setDriverName(jsonObject.getString("driverName"));
                        tVehicleAccessRecordsEntity.setDriverPhone(jsonObject.getString("driverMobile"));
                    }
                }

                tVehicleAccessRecordsService.save(tVehicleAccessRecordsEntity);

                // 存储车辆进出场展示台账
                log.info("通信记录存储完成，开始记录台账");
                tVehicleAccessRecordsService.saveLedger(tVehicleAccessRecordsEntity);

            }
        }
        //手动回执，不批量签收,回执后才能处理下一批消息
//        c.basicAck(tag,false);
//        System.out.println("正常进入人脸设备  : " + o);
    }
}
