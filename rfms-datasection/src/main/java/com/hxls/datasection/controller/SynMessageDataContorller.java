package com.hxls.datasection.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.api.feign.system.StationFeign;
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

@Tag(name="同步消息数据")
@Component
@RequiredArgsConstructor
@Slf4j
public class SynMessageDataContorller {

    private final TPersonAccessRecordsService tPersonAccessRecordsService;
    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;
    private final VehicleFeign vehicleFeign;
    private final UserFeign userFeign;
    private final AppointmentFeign appointmentFeign;
    private final StationFeign stationFeign;
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
                Long siteId = jsonObjectRecords.getLong("siteId");
                // 设备人员ID【客户端设备 数据清理后就是平台用户ID】
                String personId = jsonObjectRecords.getString("device_person_id");
                String personName = jsonObjectRecords.getString("person_name");
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


                /**
                 * 新增业务，多站点公用设备
                 * 如果传入的数据来源站点是：崇州搅拌站或者是 崇州装配式
                 * 1. 先把来源人去预约单里面找，他预约的是那个站点，然后保存通行记录的时候，设置预约的站点
                 * 2. 如果预约单里面没有的话，那就去人员管理里面找， 看他是那个站点的， 在设置对应的站点
                 * 3. 如果人员管理中没有该用户，或者有该用户但是没有站点，或者，有站点，但是站点不是崇州这两个站的情况，都设置为当前设备站点
                 * */
                // 崇州装配式
                Long czzps = 779L;
                // 崇州搅拌站
                Long czjbz = 1440L;
                // 跳蹬河
                Long tdh = 926L;
                // 新津厂
                Long xjc = 977L;
                // 新津站
                Long xjz = 974L;

                /**
                 * 改成多组数据公用，目前有的组
                 * 崇州装配式，崇州搅拌站 (两个站点会互相使用)
                 * 跳蹬河，新津厂(跳蹬河设备会过新津厂的人，新津厂的设备不会过跳蹬河的人)
                 * 新津站，新津厂(两个站点会互相使用)
                 * */
                if (czzps.equals(siteId) || czjbz.equals(siteId)){
                    // 如果是崇州装配式和崇州搅拌站组
                    Long[] longArray = new Long[2];
                    longArray[0] = 779L;
                    longArray[1] = 1440L;
                    deviceCommonSite(personId, personName, longArray, tPersonAccessRecordsEntity, siteId);

                }else if (tdh.equals(siteId)){
                    Long[] longArray = new Long[2];
                    longArray[0] = 926L;
                    longArray[1] = 977L;
                    deviceCommonSite(personId, personName, longArray, tPersonAccessRecordsEntity, siteId);
                }else if(xjc.equals(siteId) || xjz.equals(siteId)){
                    // 如果是新津厂和新津站组
                    Long[] longArray = new Long[2];
                    longArray[0] = 977L;
                    longArray[1] = 974L;
                    deviceCommonSite(personId, personName, longArray, tPersonAccessRecordsEntity, siteId);
                }else {
                    // 普通站点

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
                }

                tPersonAccessRecordsService.save(tPersonAccessRecordsEntity);
                log.info("人脸数据不存在，结束存储");
            }
        }

        //手动回执，不批量签收,回执后才能处理下一批消息
//        c.basicAck(tag,false);
//        System.out.println("正常进入人脸设备  : " + o);
    }

    private void deviceCommonSite(String personId, String personName, Long[] longArray, TPersonAccessRecordsEntity tPersonAccessRecordsEntity, Long siteId) {
        Long czzps = longArray[0];
        Long czjbz = longArray[1];
        // 1. 先通过用户ID或者用户名字找对应的预约单，返回对应预约单的站点ID
        JSONObject stationJson = appointmentFeign.queryStationIdFromAppointmentByUserInfo(personId, personName);
        Long yystationId = stationJson.getLong("stationId");
        if (ObjectUtils.isNotEmpty(yystationId)){

            if (yystationId.equals(czzps) || yystationId.equals(czjbz)){
                // 如果预约的站点 就是当前的设备公用站点
                tPersonAccessRecordsEntity.setSiteId(yystationId);
                // 找站点名字
                JSONObject jsonObject = stationFeign.queryStationInfoByStationId(siteId);
                if (ObjectUtil.isNotEmpty(jsonObject)){
                    String stationName = jsonObject.getString("stationName");
                    tPersonAccessRecordsEntity.setSiteName(stationName);
                }
            }else {
                // 如果预约的站点不是当前设备公用站点而是别的站点的预约单
                tPersonAccessRecordsEntity.setSiteId(siteId);
            }

            // 前期还无法用用户id作为用户唯一值的时候，用用户姓名进行判断站点
            JSONObject userDetail = userFeign.queryUserInformationUserName(personName);
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

        }else {
            // 2. 如果用户ID和用户名字没有找到预约单，那么就去找用户表
            // 通过用户唯一编码查询用户，然后将客户端的识别数据与平台的用户数据进行绑定
            if (StringUtils.isNotEmpty(personId)){
                JSONObject userDetail = userFeign.queryUserInformationUserId(personId);
                Long userStationId = userDetail.getLong("stationId");
                if (ObjectUtils.isNotEmpty(userDetail)) {
                    tPersonAccessRecordsEntity.setCompanyId(userDetail.getLong("orgId"));
                    tPersonAccessRecordsEntity.setCompanyName(userDetail.getString("orgName"));
                    tPersonAccessRecordsEntity.setSupervisorName(userDetail.getString("supervisor"));
                    tPersonAccessRecordsEntity.setIdCardNumber(userDetail.getString("idCard"));
                    tPersonAccessRecordsEntity.setPhone(userDetail.getString("mobile"));
                    tPersonAccessRecordsEntity.setPositionId(userDetail.getLong("postId"));
                    tPersonAccessRecordsEntity.setPositionName(userDetail.getString("postName"));
                    tPersonAccessRecordsEntity.setBusis(userDetail.getString("busis"));
                    if (ObjectUtils.isNotEmpty(userStationId)){
                        if (czzps.equals(userStationId) || czjbz.equals(userStationId)) {
                            // 如果不为空，并且站点ID是崇州这两个站的，才会设置用户的站点ID [相当于，当前记录的用户是崇州搅拌站或者装配式的员工]
                            tPersonAccessRecordsEntity.setSiteId(userStationId);
                            // 找站点名字
                            JSONObject jsonObject = stationFeign.queryStationInfoByStationId(userStationId);
                            if (ObjectUtil.isNotEmpty(jsonObject)){
                                String stationName = jsonObject.getString("stationName");
                                tPersonAccessRecordsEntity.setSiteName(stationName);
                            }
                        }
                        // 如果用户站点不为空，但是又不是崇州两个站的，一般不会出现这种情况，但是出现了又不能设置用户自己的站点，只能设置当前设备的站点
                    }
                    // 如果为空个，说明是供应商人员，或者外部预约人员，则需要设置为当前设备所属站点ID
                }
            }
        }

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
                Long siteId = jsonObjectRecords.getLong("siteId");
                String palteNumber = jsonObjectRecords.getString("plateNumber");
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
                        tVehicleAccessRecordsEntity.setImageUrl(jsonObject.getString("imageUrl"));
                        tVehicleAccessRecordsEntity.setLicenseImage(jsonObject.getString("licenseImage"));
                    }
                }

                /**
                 * 新增业务，多站点公用设备
                 * 如果传入的数据来源站点是：崇州搅拌站或者是 崇州装配式
                 * 1. 先把车牌拿到预约单中找，他预约的是那个站点，然后保存通行记录的时候，设置预约的站点
                 * 2. 如果预约单里面没有的话，那就去车辆管理里面找， 看他是那个站点的
                 * 3. 如果有站点，并且站点是崇州两个站之一的话，就设置车辆的站点
                 * 4. 如果没有站点，或者站点不是崇州两个站点之一，或者没有车辆数据，都将站点设置为当前设备站点
                 * */
                // 崇州装配式
                Long czzps = 779L;
                // 崇州搅拌站
                Long czjbz = 1440L;
                // 跳蹬河
                Long tdh = 926L;
                // 新津厂
                Long xjc = 977L;
                // 新津站
                Long xjz = 974L;
                if (czzps.equals(siteId) || czjbz.equals(siteId)){
                    // 如果是崇州装配式和崇州搅拌站组
                    Long[] longArray = new Long[2];
                    longArray[0] = 779L;
                    longArray[1] = 1440L;
                    deviceCommonSiteCar(palteNumber, longArray, siteId, tVehicleAccessRecordsEntity, jsonObjectRecords);

                }else if (tdh.equals(siteId)){
                    Long[] longArray = new Long[2];
                    longArray[0] = 926L;
                    longArray[1] = 977L;
                    deviceCommonSiteCar(palteNumber, longArray, siteId, tVehicleAccessRecordsEntity, jsonObjectRecords);
                }else if(xjc.equals(siteId) || xjz.equals(siteId)){
                    // 如果是新津厂和新津站组
                    Long[] longArray = new Long[2];
                    longArray[0] = 977L;
                    longArray[1] = 974L;
                    deviceCommonSiteCar(palteNumber, longArray, siteId, tVehicleAccessRecordsEntity, jsonObjectRecords);
                }

                tVehicleAccessRecordsService.save(tVehicleAccessRecordsEntity);

                // 存储车辆进出场展示台账
                log.info("通信记录存储完成，开始记录台账");
                tVehicleAccessRecordsService.saveLedger(tVehicleAccessRecordsEntity);
                /**
                 * 2024-5-10增加逻辑
                 * 生成车辆对应人员的出入记录
                 * 1. 通过车牌，以及通行时间和出入类型，查找有没有对应的预约单，有则找到对应预约单的所有人，生成对应出入记录
                 * 2. 没有对应预约单，查找是否有对应的 供应商车辆预约申请，有则生成对应司机的出入记录
                 * 3. 没有对应申请，则查找有没有对应的user，有则生成对应人员的出入记录
                 * 4. 没有则查找车辆管理表，有则生成对应司机的出入记录
                 * */
                if (StringUtils.isNotEmpty(tVehicleAccessRecordsEntity.getPlateNumber())){
                    tVehicleAccessRecordsService.retinuegenerateRecords(tVehicleAccessRecordsEntity);
                }
            }
        }
    }

    private void deviceCommonSiteCar(String palteNumber, Long[] longArray, Long siteId, TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity, JSONObject jsonObjectRecords) {
        Long czzps = longArray[0];
        Long czjbz = longArray[1];
        // 1. 先通过车牌找有没有该时间的预约单，如果有 返回对应预约单的站点ID
        JSONObject stationJson = appointmentFeign.queryStationIdFromAppointmentByPlatenumber(palteNumber);
        Long yystationId = stationJson.getLong("stationId");
        if (ObjectUtils.isNotEmpty(yystationId)) {
            if (yystationId.equals(czzps) || yystationId.equals(czjbz)){
                // 如果预约的站点 就是当前的设备公用站点
                tVehicleAccessRecordsEntity.setSiteId(yystationId);
                // 找站点名字
                JSONObject jsonObject = stationFeign.queryStationInfoByStationId(siteId);
                if (ObjectUtil.isNotEmpty(jsonObject)){
                    String stationName = jsonObject.getString("stationName");
                    tVehicleAccessRecordsEntity.setSiteName(stationName);
                }
            }else {
                tVehicleAccessRecordsEntity.setSiteId(siteId);
            }

        }else {
            JSONObject jsonObject = vehicleFeign.queryVehicleInformationByLicensePlateNumber(jsonObjectRecords.getString("plateNumber"));
            Long vehicleStationId = jsonObject.getLong("stationId");
            if(ObjectUtils.isNotEmpty(jsonObject)){
                tVehicleAccessRecordsEntity.setVehicleModel(jsonObject.getString("carType"));
                tVehicleAccessRecordsEntity.setEmissionStandard(jsonObject.getString("emissionStandard"));
                tVehicleAccessRecordsEntity.setDriverId(jsonObject.getLong("driverId"));
                tVehicleAccessRecordsEntity.setDriverName(jsonObject.getString("driverName"));
                tVehicleAccessRecordsEntity.setDriverPhone(jsonObject.getString("driverMobile"));
                tVehicleAccessRecordsEntity.setImageUrl(jsonObject.getString("imageUrl"));
                tVehicleAccessRecordsEntity.setLicenseImage(jsonObject.getString("licenseImage"));

                if (ObjectUtils.isNotEmpty(vehicleStationId)){
                    if (czzps.equals(vehicleStationId) || czjbz.equals(vehicleStationId)) {
                        // 如果不为空，并且站点ID是崇州这两个站的，才会设置车辆的站点ID [相当于，当前记录的用户是崇州搅拌站或者装配式的车辆]
                        tVehicleAccessRecordsEntity.setSiteId(jsonObject.getLong("stationId"));
                        // 找站点名字
                        JSONObject jsonObject1 = stationFeign.queryStationInfoByStationId(vehicleStationId);
                        if (ObjectUtil.isNotEmpty(jsonObject1)){
                            String stationName = jsonObject1.getString("stationName");
                            tVehicleAccessRecordsEntity.setSiteName(stationName);
                        }
                    }
                    // 如果车辆站点不为空，但是又不是崇州两个站的，一般不会出现这种情况，但是出现了又不能设置用户自己的站点，只能设置当前设备的站点
                }
                // 如果查询车辆没有站点，说明是供应商车辆，或者供应商私家车，所以站点设置为当前设备占地啊你
            }
            // 车牌号为空说名是外部车辆临时放行，站点设置为当前设备站点
        }
    }
}
