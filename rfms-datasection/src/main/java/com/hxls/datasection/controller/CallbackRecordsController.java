package com.hxls.datasection.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("datasection/CallbackRecordsHttp")
@Tag(name = "回调地址控制")
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

        if (ObjectUtil.isNotEmpty(dfCallBackDto)) {
//            log.info("万众人脸识别结果：{}",dfCallBackDto.toString());
//            log.info("设备唯一识别码：{}",dfCallBackDto);
            /**
             * 1. 先验证uuid是否存在，存在说明录入过了
             * */
            boolean whetherItExists = tPersonAccessRecordsService.whetherItExists(dfCallBackDto.getId());
            if (whetherItExists) {
                // 存在
                log.info("人脸数据已经存在不进行存储");
            } else {
                /**
                 * 2. 通过客户端传过来的设备名称，找到平台对应的设备，从而获取其他数据
                 * */
                JSONObject entries = deviceFeign.useTheAccountToQueryDeviceInformation(dfCallBackDto.getDevicename());
//                log.info("万众客户端传来的设备名称:{}",dfCallBackDto.getDevicename());
//                log.info("平台端的设备名称:{}",entries.getString("account"));
                String faceBase64 = dfCallBackDto.getFace_base64();

//                log.info("万众人脸开始转换base64");
                String faceUrl = "";
                if (StringUtils.isNotBlank(faceBase64)) {
                    faceUrl = BaseImageUtils.base64ToUrl(faceBase64, "WANZHONG/FACE", dfCallBackDto.getName());
                }
//                log.info("万众人脸转换完成：{}",faceUrl);

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

                    // 通过id
                    String telephone = dfCallBackDto.getEmployee_number();
                    if (StringUtils.isNotEmpty(telephone)) {
                        JSONObject userDetail = userFeign.queryUserInformationUserId(telephone);
                        if (ObjectUtils.isNotEmpty(userDetail)) {
                            body.setCompanyId(userDetail.getLong("orgId"));
                            body.setCompanyName(userDetail.getString("orgName"));
                            body.setSupervisorName(userDetail.getString("supervisor"));
                            body.setIdCardNumber(userDetail.getString("idCard"));
                            body.setPhone(userDetail.getString("mobile"));
                            body.setPositionId(userDetail.getLong("postId"));
                            body.setPositionName(userDetail.getString("postName"));
                            body.setBusis(userDetail.getString("busis"));
                        }
                    }
                    tPersonAccessRecordsService.save(body);
                } catch (Exception e) {
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
    @PostMapping(value = "/callbackAddressFaceRecognitionByHKWS", produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "海康威视人脸识别结果回调地址")
    public JSONObject callbackAddressFaceRecognitionByHKWS(@RequestParam("event_log") String event_log, @RequestParam(value = "Picture", required = false) MultipartFile pictureFile) throws ParseException {
        // 在这里处理接收到的事件日志 JSON 字符串和图片文件
//        System.out.println("Received event log JSON: " + event_log);
        log.info("此时有人脸识别到访");
        if (StringUtils.isNotEmpty(event_log)) {
            JSONObject jsonObject = JSON.parseObject(event_log);
            String ipAddress = jsonObject.getString("ipAddress");
            Date dateTime = jsonObject.getDate("dateTime");
            JSONObject accessControllerEvent = jsonObject.getJSONObject("AccessControllerEvent");
            String name = accessControllerEvent.getString("name");
            String employeeNoString = accessControllerEvent.getString("employeeNoString");
            String recordId = accessControllerEvent.getString("serialNo");

            boolean whetherItExists = tPersonAccessRecordsService.whetherItExists(recordId);
            if (whetherItExists) {
                // 存在
                log.info("海康威视人数据已经存在不进行存储，他的唯一值是{}", recordId);
            } else {
                // 检查文件是否为空或未上传
                if (pictureFile != null && !pictureFile.isEmpty()) {
                    try {
                        // 读取文件内容
                        byte[] fileContent = pictureFile.getBytes();

                        // 将文件内容转换为Base64编码的字符串
                        String base64Encoded = Base64.getEncoder().encodeToString(fileContent);
//                        log.info("海康人脸开始转换base64");
                        String faceUrl = BaseImageUtils.base64ToUrl(base64Encoded, "HAIKANG/FACE", name);
//                        log.info("海康人脸转换完成：{}",faceUrl);

                        // 输出Base64编码的字符串
//                System.out.println("Base64 encoded file content: " + base64Encoded);
                        // 进行存储
                        JSONObject entries = deviceFeign.useTheIpaddressToQueryDeviceInformation(ipAddress);
//                        log.info("海康人脸客户端传来的IP:{}",ipAddress);
//                        log.info("平台端的设备的IP:{}",entries.getString("ipAddress"));
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

                        // 通过用户唯一编码查询用户，然后将客户端的识别数据与平台的用户数据进行绑定
                        if (StringUtils.isNotEmpty(employeeNoString)) {
                            JSONObject userDetail = userFeign.queryUserInformationUserId(employeeNoString);
                            if (ObjectUtils.isNotEmpty(userDetail)) {
                                body.setCompanyId(userDetail.getLong("orgId"));
                                body.setCompanyName(userDetail.getString("orgName"));
                                body.setSupervisorName(userDetail.getString("supervisor"));
                                body.setIdCardNumber(userDetail.getString("idCard"));
                                body.setPhone(userDetail.getString("mobile"));
                                body.setPositionId(userDetail.getLong("postId"));
                                body.setPositionName(userDetail.getString("postName"));
                                body.setBusis(userDetail.getString("busis"));
                            }
                        }

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

        log.info("海康威视车辆回调，接收到得数据 " );

        if (ObjectUtil.isNotEmpty(jsonObject)) {
            String uniqueNo = jsonObject.getString("uniqueNo");
            String plateNo = jsonObject.getString("plateNo");
            String picPlateFilePath = jsonObject.getString("picPlateFilePath");
            String picVehicleFileData = jsonObject.getString("picVehicleFileData");
            String passTime = jsonObject.getString("passTime");
//            String terminalNo = jsonObject.getString("terminalNo");
            String laneCode = jsonObject.getString("laneCode");
            String parkCode = jsonObject.getString("parkCode");

            System.out.println("laneCode : "+laneCode );
            System.out.println("parkCode : "+parkCode );

            String carUrl = null;
            if (StringUtils.isNotEmpty(picVehicleFileData)) {
                try{
                    carUrl = BaseImageUtils.base64ToUrl(picVehicleFileData, "HAIKANG/CAR", plateNo);
                }catch (Exception e){
                    log.error("图片转换失败");
                }
            }

            /**
             * 1. 先验证uuid是否存在，存在说明录入过了
             * */
            boolean whetherItExists = tVehicleAccessRecordsService.whetherItExists(uniqueNo);
            if (whetherItExists) {
                // 存在
                log.info("车辆数据已经存在不进行存储");
            } else {
                /**
                 * 2. 通过客户端传过来的设备名称，找到平台对应的设备，从而获取其他数据
                 * */
                JSONObject entries = deviceFeign.useTheDeviceSnToQueryDeviceInformation(laneCode+"&"+parkCode);
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
                SimpleDateFormat dateFormat = StringUtils.isNotEmpty(picPlateFilePath)?
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") : new SimpleDateFormat("yyyyMMddHHmmssSSS");

                Date date;
                try {
                    date = dateFormat.parse(ObjectUtil.isNotEmpty(passTime) ? passTime : "2023-04-17 12:00:00");
                } catch (Exception e) {
                     date = ObjectUtil.isNotEmpty(passTime) ? new Date(Long.parseLong(passTime) * 1000) : new Date();
                    // 将时间戳字符串解析为日期对象
                }

                if (ObjectUtil.isNotEmpty(picPlateFilePath)){
                    date =  convertCustomDateTimeStringToDate(passTime);
                }

                body.setRecordTime(date);
                body.setManufacturerId(ObjectUtil.isNotEmpty(entries.getLong("manufacturer_id")) ? entries.getLong("manufacturer_id") : 999L);
                body.setManufacturerName(ObjectUtil.isNotEmpty(entries.getString("manufacturer_name")) ? entries.getString("manufacturer_name") : NOTFIND_DEVICE);
                body.setSiteId(ObjectUtil.isNotEmpty(entries.getLong("siteId")) ? entries.getLong("siteId") : 999L);
                body.setSiteName(ObjectUtil.isNotEmpty(entries.getString("siteName")) ? entries.getString("siteName") : NOTFIND_DEVICE);
                try {
                    /**
                     * 需要通过车牌绑定平台车辆信息数据
                     * */
                    if (StringUtils.isNotEmpty(plateNo)) {
                        JSONObject jsonObject1 = vehicleFeign.queryVehicleInformationByLicensePlateNumber(plateNo);
                        if (ObjectUtils.isNotEmpty(jsonObject1)) {
                            body.setVehicleModel(jsonObject1.getString("carType"));
                            body.setEmissionStandard(jsonObject1.getString("emissionStandard"));
                            body.setDriverId(jsonObject1.getLong("driverId"));
                            body.setDriverName(jsonObject1.getString("driverName"));
                            body.setDriverPhone(jsonObject1.getString("driverMobile"));
                            body.setImageUrl(jsonObject1.getString("imageUrl"));
                        }
                    }
                    tVehicleAccessRecordsService.save(body);
                } catch (Exception e) {
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

    private Date convertCustomDateTimeStringToDate(String dateTimeString) {

        try {
            // 截取年、月、日、时、分部分
            int year = Integer.parseInt(dateTimeString.substring(0, 4));
            int month = Integer.parseInt(dateTimeString.substring(4, 6));
            int day = Integer.parseInt(dateTimeString.substring(6, 8));
            int hour = Integer.parseInt(dateTimeString.substring(8, 10));
            int minute = Integer.parseInt(dateTimeString.substring(10, 12));
            int second = Integer.parseInt(dateTimeString.substring(12, 14));
            int millisecond = Integer.parseInt(dateTimeString.substring(14, 17)); // 毫秒部分

            // 使用 SimpleDateFormat 定义日期时间格式
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

            // 构造日期时间对象
            String formattedDate = String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d", year, month, day, hour, minute, second, millisecond);
            return sdf.parse(formattedDate);
        } catch (ParseException e) {
            // 如果解析失败，打印错误信息
            System.err.println("无法解析日期时间字符串：" + e.getMessage());
            return new Date();
        }
    }


    @PostMapping("/callbackAddressCarRecognitionByYFZN")
    @Operation(summary = "宇泛智能的接收")
    public JSONObject callbackAddressCarRecognitionByYFZN(@RequestBody JSONObject jsonObject) {

        log.info("宇泛智能已接受到回调数据 ：{}" , jsonObject);

        //  //https://rns.huashijc.com/api/rfms-datasection/datasection/CallbackRecordsHttp/callbackAddressCarRecognitionByYFZN

        //人像识别回调示例：
        //{"aliveType":"1","base64":"","data":"","deviceKey":"84E0F42BA3401801","idcardNum":"038
        //0949491","identifyType":"1","ip":"192.168.76.23","model":"0","passTimeType":"1","path":
        //"/data/record/IdentifyRecords/2021-12-28/17/170352_163_ef93cf4c265f41d9860ed356f6c1ea4
        //b_rgb.jpg","permissionTimeType":"1","personId":"ef93cf4c265f41d9860ed356f6c1ea4b","rec
        //ModeType": "1","recType": 1,"time": "1640682232374","type": "face_0"}

        //{"data":"","iDNumber":"","ip":"192.209.1.201","latitude":"0.000000","base64":"","aliveType":"1",
        // "deviceKey":"E03C1CB19B6C1805","identifyType":"1","type":"face_0","idcardNum":"45","recType":1,
        // "path":"http://192.209.1.201:8090/data/record/IdentifyRecords/2024-06-19/12/124001_244_51.jpg","permissionTimeType":"1","model":"0"
        // ,"personId":"51","time":"1718772001286","passTimeType":"1","longitude":"0.000000","recModeType":"1"}

        try {


            if (ObjectUtil.isNotEmpty(jsonObject)) {
                String time = jsonObject.getString("time");
                String personId = jsonObject.getString("personId");
                String base64 = jsonObject.getString("base64");
                String passTime = jsonObject.getString("time");
                String ipAddress = jsonObject.getString("ip");



                String faceUrl = null;
                if (StringUtils.isNotEmpty(base64)) {
                    try {
                        faceUrl = BaseImageUtils.base64ToUrl(base64, "YFZN/FACA", time);
                    } catch (IOException e) {
                        log.info("转换失败，不处理照片");
                        faceUrl = "照片存储失败";
                    }
                }

                /**
                 * 1. 先验证uuid是否存在，存在说明录入过了
                 * */
                boolean whetherItExists = tPersonAccessRecordsService.whetherItExists(time);
                if (whetherItExists) {
                    // 存在
                    log.info("人脸数据已经存在不进行存储");
                } else {

                    JSONObject entries = deviceFeign.useTheIpaddressToQueryDeviceInformation(ipAddress);
//                        log.info("海康人脸客户端传来的IP:{}",ipAddress);
//                        log.info("平台端的设备的IP:{}",entries.getString("ipAddress"));
                    TPersonAccessRecordsVO body = new TPersonAccessRecordsVO();
                    body.setChannelId(ObjectUtil.isNotEmpty(entries.getLong("channel_id")) ? entries.getLong("channel_id") : 999L);
                    body.setChannelName(ObjectUtil.isNotEmpty(entries.getString("channel_name")) ? entries.getString("channel_name") : "设备未匹配到");
                    body.setDeviceId(ObjectUtil.isNotEmpty(entries.getLong("device_id")) ? entries.getLong("device_id") : 999L);
                    body.setDeviceName(ObjectUtil.isNotEmpty(entries.getString("device_name")) ? entries.getString("device_name") : "设备未匹配到");
                    body.setAccessType(ObjectUtil.isNotEmpty(entries.getString("access_type")) ? entries.getString("access_type") : "1");
                    body.setHeadUrl(faceUrl);
                    body.setPersonName(personId+"(本地)");
                    body.setDevicePersonId(personId);
                    // 定义日期格式
                    Date date = null;
                    try {
                        date = new Date(Long.parseLong(passTime));
                    } catch (Exception e) {
                        log.info("日期转换失败");
                    }


                    body.setRecordTime(date);
                    body.setManufacturerId(ObjectUtil.isNotEmpty(entries.getLong("manufacturer_id")) ? entries.getLong("manufacturer_id") : 999L);
                    body.setManufacturerName(ObjectUtil.isNotEmpty(entries.getString("manufacturer_name")) ? entries.getString("manufacturer_name") : "设备未匹配到");
                    body.setSiteId(ObjectUtil.isNotEmpty(entries.getLong("siteId")) ? entries.getLong("siteId") : 999L);
                    body.setSiteName(ObjectUtil.isNotEmpty(entries.getString("siteName")) ? entries.getString("siteName") : "设备未匹配到");
                    body.setRecordsId(time);

                    // 通过用户唯一编码查询用户，然后将客户端的识别数据与平台的用户数据进行绑定
                    if (StringUtils.isNotEmpty(personId)) {
                        JSONObject userDetail = userFeign.queryUserInformationUserId(personId);
                        if (ObjectUtils.isNotEmpty(userDetail)) {
                            body.setCompanyId(userDetail.getLong("orgId"));
                            body.setCompanyName(userDetail.getString("orgName"));
                            body.setSupervisorName(userDetail.getString("supervisor"));
                            body.setIdCardNumber(userDetail.getString("idCard"));
                            body.setPhone(userDetail.getString("mobile"));
                            body.setPositionId(userDetail.getLong("postId"));
                            body.setPositionName(userDetail.getString("postName"));
                            body.setBusis(userDetail.getString("busis"));
                            body.setPersonName(userDetail.getString("personName"));
                        }
                    }
                    tPersonAccessRecordsService.save(body);
                }

            }

        } catch (Exception e) {

            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("result", 0);
            jsonObject1.put("message", "fail");
            return jsonObject1;

        }

        /**
         * {
         *  "result":0, //0接收成功，非0接收失败，设备会重新推送
         *   "message": "OK"
         * }
         *
         * */
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("result", 1);
        jsonObject1.put("message", "ok");
        return jsonObject1;

    }


}
