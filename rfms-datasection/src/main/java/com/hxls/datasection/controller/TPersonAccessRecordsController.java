package com.hxls.datasection.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.datasection.entity.DfWZCallBackDto;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.query.TVehicleAccessRecordsQuery;
import com.hxls.datasection.vo.TVehicleAccessRecordsVO;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.rabbitmq.domain.MessageSendDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.datasection.convert.TPersonAccessRecordsConvert;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.query.TPersonAccessRecordsQuery;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
* 人员出入记录表
*
* @author zhaohong
* @since 1.0.0 2024-03-29
*/
@RestController
@RequestMapping("datasection/TPersonAccessRecords")
@Tag(name="人员出入记录表")
@AllArgsConstructor
@Slf4j
public class TPersonAccessRecordsController {
    private final TPersonAccessRecordsService tPersonAccessRecordsService;
    @Autowired
    private DeviceFeign deviceFeign;

    @GetMapping("/pageTpersonAccessRecords")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:page')")
    public Result<PageResult<TPersonAccessRecordsVO>> page(@ParameterObject @Valid TPersonAccessRecordsQuery query){
        PageResult<TPersonAccessRecordsVO> page = tPersonAccessRecordsService.page(query);

        return Result.ok(page);
    }

    @GetMapping("/tpersonAccessRecords/{id}")
    @Operation(summary = "信息")
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:info')")
    public Result<TPersonAccessRecordsVO> get(@PathVariable("id") Long id){
        TPersonAccessRecordsEntity entity = tPersonAccessRecordsService.getById(id);

        return Result.ok(TPersonAccessRecordsConvert.INSTANCE.convert(entity));
    }

    @PostMapping("/saveTpersonAccessRecords")
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:save')")
    public Result<String> saveTpersonAccessRecords(@RequestBody TPersonAccessRecordsVO vo){
        try {
            tPersonAccessRecordsService.save(vo);
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("添加失败！");
        }
    }

    @PutMapping("/updateTpersonAccessRecords")
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:update')")
    public Result<String> update(@RequestBody @Valid TPersonAccessRecordsVO vo){
        tPersonAccessRecordsService.update(vo);

        return Result.ok();
    }

    @DeleteMapping("/delTpersonAccessRecords")
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tPersonAccessRecordsService.delete(idList);

        return Result.ok();
    }


    @GetMapping("/pageUnidirectionalTpersonAccessRecords")
    @Operation(summary = "查询单向通行记录")
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:unidirectional')")
    public Result<PageResult<TPersonAccessRecordsVO>> pageUnidirectionalTVehicleAccessRecords(@ParameterObject @Valid TPersonAccessRecordsQuery query){

        PageResult<TPersonAccessRecordsVO> page = tPersonAccessRecordsService.pageUnidirectionalTpersonAccessRecords(query);
        List<TPersonAccessRecordsVO> list = page.getList();


        return Result.ok(page);
    }

    @PostMapping("/callbackAddressFaceRecognitionByWZ")
    @Operation(summary = "万众人脸识别结果回调地址")
    public JSONObject callbackAddressFaceRecognitionByWZ(@RequestBody DfWZCallBackDto dfCallBackDto) throws ParseException {
        if(ObjectUtil.isNotEmpty(dfCallBackDto)){
            log.info("万众人脸识别结果：{}",dfCallBackDto.toString());
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
                log.info("平台端的设备名称:{}",entries.get("account", String.class));

                TPersonAccessRecordsVO body = new TPersonAccessRecordsVO();
                body.setChannelId(ObjectUtil.isNotEmpty(entries.get("channel_id", Long.class)) ? entries.get("channel_id", Long.class) : 999L);
                body.setChannelName(ObjectUtil.isNotEmpty(entries.get("channel_name", String.class)) ? entries.get("channel_name", String.class) : "设备未匹配到");
                body.setDeviceId(ObjectUtil.isNotEmpty(entries.get("device_id", Long.class)) ? entries.get("device_id", Long.class) : 999L);
                body.setDeviceName(ObjectUtil.isNotEmpty(entries.get("device_name", String.class)) ? entries.get("device_name", String.class) : "设备未匹配到");
                body.setAccessType(ObjectUtil.isNotEmpty(entries.get("access_type", String.class)) ? entries.get("access_type", String.class) : "1");
                body.setHeadUrl(dfCallBackDto.getFace_base64());
                body.setPersonName(dfCallBackDto.getName());
                body.setDevicePersonId(dfCallBackDto.getEmployee_number());
                // 定义日期格式
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                body.setRecordTime(dateFormat.parse(ObjectUtil.isNotEmpty(dfCallBackDto.getTime()) ? dfCallBackDto.getTime() : "2023-04-17 12:00:00"));
                body.setManufacturerId(ObjectUtil.isNotEmpty(entries.get("manufacturer_id", Long.class)) ? entries.get("manufacturer_id", Long.class) : 999L);
                body.setManufacturerName(ObjectUtil.isNotEmpty(entries.get("manufacturer_name", String.class)) ? entries.get("manufacturer_name", String.class) : "设备未匹配到");
                body.setSiteId(ObjectUtil.isNotEmpty(entries.get("siteId", Long.class)) ? entries.get("siteId", Long.class) : 999L);
                body.setSiteName(ObjectUtil.isNotEmpty(entries.get("siteName", String.class)) ? entries.get("siteName", String.class) : "设备未匹配到");
                body.setRecordsId(dfCallBackDto.getId());
                try {
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
        JSONObject obj = JSONUtil.createObj();
        obj.set("result", 0);
        obj.set("message", "ok");
        return obj;
    }


    @PostMapping(value= "/callbackAddressFaceRecognitionByHKWS", produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "海康威视人脸识别结果回调地址")
    public JSONObject callbackAddressFaceRecognitionByHKWS( @RequestParam("event_log") String event_log,@RequestParam(value = "Picture", required = false) MultipartFile pictureFile) throws ParseException {
        // 在这里处理接收到的事件日志 JSON 字符串和图片文件
//        System.out.println("Received event log JSON: " + event_log);
        if (StringUtils.isNotEmpty(event_log)){
            JSONObject jsonObject = JSONUtil.parseObj(event_log);
            String ipAddress = jsonObject.get("ipAddress", String.class);
            Date dateTime = jsonObject.get("dateTime", Date.class);
            JSONObject accessControllerEvent = jsonObject.get("AccessControllerEvent", JSONObject.class);
            String name = accessControllerEvent.get("name", String.class);
            String employeeNoString = accessControllerEvent.get("employeeNoString", String.class);
            String recordId = accessControllerEvent.get("serialNo", String.class);

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

                        // 输出Base64编码的字符串
//                System.out.println("Base64 encoded file content: " + base64Encoded);
                        // 进行存储
                        JSONObject entries = deviceFeign.useTheIpaddressToQueryDeviceInformation(ipAddress);
                        log.info("海康人脸客户端传来的IP:{}",ipAddress);
                        log.info("平台端的设备的IP:{}",entries.get("ipAddress", String.class));
                        TPersonAccessRecordsVO body = new TPersonAccessRecordsVO();
                        body.setChannelId(ObjectUtil.isNotEmpty(entries.get("channel_id", Long.class)) ? entries.get("channel_id", Long.class) : 999L);
                        body.setChannelName(ObjectUtil.isNotEmpty(entries.get("channel_name", String.class)) ? entries.get("channel_name", String.class) : "设备未匹配到");
                        body.setDeviceId(ObjectUtil.isNotEmpty(entries.get("device_id", Long.class)) ? entries.get("device_id", Long.class) : 999L);
                        body.setDeviceName(ObjectUtil.isNotEmpty(entries.get("device_name", String.class)) ? entries.get("device_name", String.class) : "设备未匹配到");
                        body.setAccessType(ObjectUtil.isNotEmpty(entries.get("access_type", String.class)) ? entries.get("access_type", String.class) : "1");
                        body.setHeadUrl(base64Encoded);
                        body.setPersonName(name);
                        body.setDevicePersonId(employeeNoString);
                        // 定义日期格式
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//                    body.setRecordTime(dateFormat.parse(dateTime));
                        body.setRecordTime(dateTime);
                        body.setManufacturerId(ObjectUtil.isNotEmpty(entries.get("manufacturer_id", Long.class)) ? entries.get("manufacturer_id", Long.class) : 999L);
                        body.setManufacturerName(ObjectUtil.isNotEmpty(entries.get("manufacturer_name", String.class)) ? entries.get("manufacturer_name", String.class) : "设备未匹配到");
                        body.setSiteId(ObjectUtil.isNotEmpty(entries.get("siteId", Long.class)) ? entries.get("siteId", Long.class) : 999L);
                        body.setSiteName(ObjectUtil.isNotEmpty(entries.get("siteName", String.class)) ? entries.get("siteName", String.class) : "设备未匹配到");
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
        JSONObject obj = JSONUtil.createObj();
        obj.set("result", 0);
        obj.set("message", "ok");
        return obj;
    }

}
