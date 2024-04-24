package com.hxls.datasection.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.api.feign.system.UserFeign;
import com.hxls.api.feign.system.VehicleFeign;
import com.hxls.datasection.entity.DfWZCallBackDto;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import com.hxls.datasection.util.BaseImageUtils;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("datasection/CallbackRecordsHttp")
@Tag(name="回调地址控制")
@AllArgsConstructor
@Slf4j
public class CallbackRecordsController {

    private final TPersonAccessRecordsService tPersonAccessRecordsService;
    private final DeviceFeign deviceFeign;
    private final UserFeign userFeign;
    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;
    private final VehicleFeign vehicleFeign;
    private static final String NOTFIND_DEVICE = "设备未找到";

    /**
     * @author: Mryang
     * @Description: 所有万众人脸识别结果回调地址
     * @Date: 22:41 2024/4/20
     * @Param:
     * @return:
     */
    @PostMapping("/callbackAddressFaceRecognitionByWZ")
    @Operation(summary = "万众人脸识别结果回调地址")
    public JSONObject callbackAddressFaceRecognitionByWZ(@RequestBody DfWZCallBackDto dfCallBackDto) throws ParseException, IOException {
        if(ObjectUtil.isNotEmpty(dfCallBackDto)){
//            log.info("万众人脸识别结果：{}",dfCallBackDto.toString());
            /**
             * 1. 先验证uuid是否存在，存在说明录入过了
             * */
            boolean whetherItExists = tPersonAccessRecordsService.whetherItExists(dfCallBackDto.getId());
            if (whetherItExists){
                // 存在
                log.info("人脸数据已经存在不进行存储");
            }else {
                /**
                 * 2. 通过客户端传过来的设备名称，找到平台对应的设备，从而获取其他数据
                 * */
                JSONObject entries = deviceFeign.useTheAccountToQueryDeviceInformation(dfCallBackDto.getDevicename());
                log.info("万众客户端传来的设备名称:{}",dfCallBackDto.getDevicename());
                log.info("平台端的设备名称:{}",entries.getString("account"));
                String faceBase64 = dfCallBackDto.getFace_base64();

                log.info("万众人脸开始转换base64");
                String faceUrl = "";
                if (StringUtils.isNotBlank(faceBase64)) {
                    faceUrl = BaseImageUtils.base64ToUrl(faceBase64, "WANZHONG/FACE");
                }
                log.info("万众人脸转换完成：{}",faceUrl);

                TPersonAccessRecordsVO body = new TPersonAccessRecordsVO();
                body.setChannelId(ObjectUtil.isNotEmpty(entries.getLong("channel_id")) ? entries.getLong("channel_id") : 999L);
                body.setChannelName(ObjectUtil.isNotEmpty(entries.getString("channel_name")) ? entries.getString("channel_name") : "设备未匹配到");
                body.setDeviceId(ObjectUtil.isNotEmpty(entries.getLong("device_id")) ? entries.getLong("device_id") : 999L);
                body.setDeviceName(ObjectUtil.isNotEmpty(entries.getString("device_name")) ? entries.getString("device_name") : "设备未匹配到");
                body.setAccessType(ObjectUtil.isNotEmpty(entries.getString("access_type")) ? entries.getString("access_type") : "1");
                body.setHeadUrl(faceUrl);
                body.setPersonName(dfCallBackDto.getName());
                body.setDevicePersonId(dfCallBackDto.getEmployee_number());
                // 定义日期格式
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                body.setRecordTime(dateFormat.parse(ObjectUtil.isNotEmpty(dfCallBackDto.getTime()) ? dfCallBackDto.getTime() : "2023-04-17 12:00:00"));
                body.setManufacturerId(ObjectUtil.isNotEmpty(entries.getLong("manufacturer_id")) ? entries.getLong("manufacturer_id") : 999L);
                body.setManufacturerName(ObjectUtil.isNotEmpty(entries.getString("manufacturer_name")) ? entries.getString("manufacturer_name") : "设备未匹配到");
                body.setSiteId(ObjectUtil.isNotEmpty(entries.getLong("siteId")) ? entries.getLong("siteId") : 999L);
                body.setSiteName(ObjectUtil.isNotEmpty(entries.getString("siteName")) ? entries.getString("siteName") : "设备未匹配到");
                body.setRecordsId(dfCallBackDto.getId());
                body.setPhone(dfCallBackDto.getTelephone());
                try {

                    // 通过手机号查询用户，然后将客户端的识别数据与平台的用户数据进行绑定
                    String telephone = dfCallBackDto.getTelephone();
                    if (StringUtils.isNotEmpty(telephone)){
                        JSONObject userDetail = userFeign.queryUserInformationThroughMobilePhoneNumber(telephone);
                    }
                    tPersonAccessRecordsService.save(body);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }


        /**
         * {
         *  "result":0, //0接收成功，非0接收失败，设备会重新推送
         *   "message": "OK"
         * }
         *
         * */
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", 0);
        jsonObject.put("message", "ok");
        return jsonObject;
    }


    /**
     * @author: Mryang
     * @Description: 所有海康威视人脸识别结果回调地址
     * @Date: 22:40 2024/4/20
     * @Param:
     * @return:
     */
    @PostMapping(value= "/callbackAddressFaceRecognitionByHKWS", produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "海康威视人脸识别结果回调地址")
    public JSONObject callbackAddressFaceRecognitionByHKWS(@RequestParam("event_log") String event_log, @RequestParam(value = "Picture", required = false) MultipartFile pictureFile) throws ParseException {
        // 在这里处理接收到的事件日志 JSON 字符串和图片文件
//        System.out.println("Received event log JSON: " + event_log);
        if (StringUtils.isNotEmpty(event_log)){
            JSONObject jsonObject = JSON.parseObject(event_log);
            String ipAddress = jsonObject.getString("ipAddress");
            Date dateTime = jsonObject.getDate("dateTime");
            JSONObject accessControllerEvent = jsonObject.getJSONObject("AccessControllerEvent");
            String name = accessControllerEvent.getString("name");
            String employeeNoString = accessControllerEvent.getString("employeeNoString");
            String recordId = accessControllerEvent.getString("serialNo");

            boolean whetherItExists = tPersonAccessRecordsService.whetherItExists(recordId);
            if (whetherItExists){
                // 存在
                log.info("人脸数据已经存在不进行存储，他的唯一值是{}",recordId);
            }else {
                // 检查文件是否为空或未上传
                if (pictureFile != null && !pictureFile.isEmpty()) {
                    try {
                        // 读取文件内容
                        byte[] fileContent = pictureFile.getBytes();

                        // 将文件内容转换为Base64编码的字符串
                        String base64Encoded = Base64.getEncoder().encodeToString(fileContent);
                        log.info("海康人脸开始转换base64");
                        String faceUrl = BaseImageUtils.base64ToUrl(base64Encoded, "HAIKANG/FACE");
                        log.info("海康人脸转换完成：{}",faceUrl);

                        // 输出Base64编码的字符串
//                System.out.println("Base64 encoded file content: " + base64Encoded);
                        // 进行存储
                        JSONObject entries = deviceFeign.useTheIpaddressToQueryDeviceInformation(ipAddress);
                        log.info("海康人脸客户端传来的IP:{}",ipAddress);
                        log.info("平台端的设备的IP:{}",entries.getString("ipAddress"));
                        TPersonAccessRecordsVO body = new TPersonAccessRecordsVO();
                        body.setChannelId(ObjectUtil.isNotEmpty(entries.getLong("channel_id")) ? entries.getLong("channel_id") : 999L);
                        body.setChannelName(ObjectUtil.isNotEmpty(entries.getString("channel_name")) ? entries.getString("channel_name") : "设备未匹配到");
                        body.setDeviceId(ObjectUtil.isNotEmpty(entries.getLong("device_id")) ? entries.getLong("device_id") : 999L);
                        body.setDeviceName(ObjectUtil.isNotEmpty(entries.getString("device_name")) ? entries.getString("device_name") : "设备未匹配到");
                        body.setAccessType(ObjectUtil.isNotEmpty(entries.getString("access_type")) ? entries.getString("access_type") : "1");
                        body.setHeadUrl(faceUrl);
                        body.setPersonName(name);
                        body.setDevicePersonId(employeeNoString);
                        // 定义日期格式
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//                    body.setRecordTime(dateFormat.parse(dateTime));
                        body.setRecordTime(dateTime);
                        body.setManufacturerId(ObjectUtil.isNotEmpty(entries.getLong("manufacturer_id")) ? entries.getLong("manufacturer_id") : 999L);
                        body.setManufacturerName(ObjectUtil.isNotEmpty(entries.getString("manufacturer_name")) ? entries.getString("manufacturer_name") : "设备未匹配到");
                        body.setSiteId(ObjectUtil.isNotEmpty(entries.getLong("siteId")) ? entries.getLong("siteId") : 999L);
                        body.setSiteName(ObjectUtil.isNotEmpty(entries.getString("siteName")) ? entries.getString("siteName") : "设备未匹配到");
                        body.setRecordsId(recordId);
                        tPersonAccessRecordsService.save(body);
                        // 返回结果或进行其他处理
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 处理文件读取异常
                    }
                } else {
                    // 处理文件为空或未上传的情况
                    System.out.println("No picture file received.");
                }
            }

        }


        /**
         * {
         *  "result":0, //0接收成功，非0接收失败，设备会重新推送
         *   "message": "OK"
         * }
         *
         * */
        JSONObject obj = new JSONObject();
        obj.put("result", 0);
        obj.put("message", "ok");
        return obj;
    }

    /**
     * @author: Mryang
     * @Description: 所有海康威视车辆识别结果回调地址
     * @Date: 22:41 2024/4/20
     * @Param:
     * @return:
     */
    @PostMapping("/callbackAddressCarRecognitionByHKWS")
    @Operation(summary = "海康威视车辆识别结果回调地址")
    public JSONObject callbackAddressCarRecognitionByHKWS(@RequestBody JSONObject jsonObject) throws ParseException, IOException {
        if(ObjectUtil.isNotEmpty(jsonObject)){
            String uniqueNo = jsonObject.getString("uniqueNo");
            String plateNo = jsonObject.getString("plateNo");
            String picVehicleFileData = jsonObject.getString("picVehicleFileData");
            String passTime = jsonObject.getString("passTime");
//            String terminalNo = jsonObject.getString("terminalNo");
            String laneCode = jsonObject.getString("laneCode");


            String carUrl = BaseImageUtils.base64ToUrl(picVehicleFileData, "HAIKANG/CAR");
            /**
             * 1. 先验证uuid是否存在，存在说明录入过了
             * */
            boolean whetherItExists = tVehicleAccessRecordsService.whetherItExists(uniqueNo);
            if (whetherItExists){
                // 存在
                log.info("车辆数据已经存在不进行存储");
            }else {
                /**
                 * 2. 通过客户端传过来的设备名称，找到平台对应的设备，从而获取其他数据
                 * */
                JSONObject entries = deviceFeign.useTheDeviceSnToQueryDeviceInformation(laneCode);
//                log.info("海康客户端传来的设备编号:{}",laneCode);
//                log.info("平台端的设备编号:{}",entries.getString("device_name"));

                TVehicleAccessRecordsEntity body = new TVehicleAccessRecordsEntity();
                body.setChannelId(ObjectUtil.isNotEmpty(entries.getLong("channel_id")) ? entries.getLong("channel_id") : 999L);
                body.setChannelName(ObjectUtil.isNotEmpty(entries.getString("channel_name")) ? entries.getString("channel_name") : NOTFIND_DEVICE);
                body.setDeviceId(ObjectUtil.isNotEmpty(entries.getLong("device_id")) ? entries.getLong("device_id") : 999L);
                body.setDeviceName(ObjectUtil.isNotEmpty(entries.getString("device_name")) ? entries.getString("device_name") : NOTFIND_DEVICE);
                body.setAccessType(ObjectUtil.isNotEmpty(entries.getString("access_type")) ? entries.getString("access_type") : "1");
                body.setCarUrl(carUrl);
                body.setPlateNumber(plateNo);
                body.setRecordsId(uniqueNo);
                // 定义日期格式
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                body.setRecordTime(dateFormat.parse(ObjectUtil.isNotEmpty(passTime) ? passTime : "2023-04-17 12:00:00"));
                body.setManufacturerId(ObjectUtil.isNotEmpty(entries.getLong("manufacturer_id")) ? entries.getLong("manufacturer_id") : 999L);
                body.setManufacturerName(ObjectUtil.isNotEmpty(entries.getString("manufacturer_name")) ? entries.getString("manufacturer_name") : NOTFIND_DEVICE);
                body.setSiteId(ObjectUtil.isNotEmpty(entries.getLong("siteId")) ? entries.getLong("siteId") : 999L);
                body.setSiteName(ObjectUtil.isNotEmpty(entries.getString("siteName")) ? entries.getString("siteName") : NOTFIND_DEVICE);
                try {

                    /**
                     * 需要通过车牌绑定平台车辆信息数据
                     * */
                    if (StringUtils.isNotEmpty(plateNo)){
                        JSONObject jsonObject1 = vehicleFeign.queryVehicleInformationByLicensePlateNumber(plateNo);
                        if(ObjectUtils.isNotEmpty(jsonObject1)){
                            body.setVehicleModel(jsonObject.getString("carType"));
                            body.setEmissionStandard(jsonObject.getString("emissionStandard"));
                            body.setDriverId(jsonObject.getLong("driverId"));
                            body.setDriverName(jsonObject.getString("driverName"));
                            body.setDriverPhone(jsonObject.getString("driverMobile"));
                        }
                    }

                    tVehicleAccessRecordsService.save(body);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }


        /**
         * {
         *  "result":0, //0接收成功，非0接收失败，设备会重新推送
         *   "message": "OK"
         * }
         *
         * */
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("result", 0);
        jsonObject1.put("message", "ok");
        return jsonObject1;
    }


}
