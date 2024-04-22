package com.hxls.datasection.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.datasection.util.BaseImageUtils;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.security.user.UserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.datasection.convert.TVehicleAccessRecordsConvert;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import com.hxls.datasection.query.TVehicleAccessRecordsQuery;
import com.hxls.datasection.vo.TVehicleAccessRecordsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.util.ImageUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

/**
* 车辆出入记录表
*
* @author zhaohong
* @since 1.0.0 2024-03-29
*/
@RestController
@RequestMapping("datasection/TVehicleAccessRecords")
@Tag(name="车辆出入记录表")
@AllArgsConstructor
@Slf4j
public class TVehicleAccessRecordsController extends BaseController {
    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;
    @Autowired
    private DeviceFeign deviceFeign;


    @GetMapping("/pageTVehicleAccessRecords")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:page')")
    public Result<PageResult<TVehicleAccessRecordsVO>> page(@ParameterObject @Valid TVehicleAccessRecordsQuery query, @ModelAttribute("baseUser") UserDetail baseUser){
        PageResult<TVehicleAccessRecordsVO> page = tVehicleAccessRecordsService.page(query,baseUser);

        return Result.ok(page);
    }

    @GetMapping("/TVehicleAccessRecords/{id}")
    @Operation(summary = "信息")
//    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:info')")
    public Result<TVehicleAccessRecordsVO> get(@PathVariable("id") Long id){
        TVehicleAccessRecordsEntity entity = tVehicleAccessRecordsService.getById(id);

        return Result.ok(TVehicleAccessRecordsConvert.INSTANCE.convert(entity));
    }

    @PostMapping("/saveTVehicleAccessRecords")
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
//    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:save')")
    public Result<String> save(@RequestBody TVehicleAccessRecordsVO vo){
        tVehicleAccessRecordsService.save(vo);

        return Result.ok();
    }

    @PutMapping("/updateTVehicleAccessRecords")
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
//    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:update')")
    public Result<String> update(@RequestBody @Valid TVehicleAccessRecordsVO vo){
        tVehicleAccessRecordsService.update(vo);

        return Result.ok();
    }

    @DeleteMapping("/delTVehicleAccessRecords")
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
//    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tVehicleAccessRecordsService.delete(idList);

        return Result.ok();
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
            String uniqueNo = jsonObject.get("uniqueNo", String.class);
            String plateNo = jsonObject.get("plateNo", String.class);
            String picVehicleFileData = jsonObject.get("picVehicleFileData", String.class);
            String passTime = jsonObject.get("passTime", String.class);
            String terminalNo = jsonObject.get("terminalNo", String.class);
            String laneCode = jsonObject.get("laneCode", String.class);


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
                log.info("海康客户端传来的设备编号:{}",laneCode);
                log.info("平台端的设备编号:{}",entries.get("device_name", String.class));

                TVehicleAccessRecordsEntity body = new TVehicleAccessRecordsEntity();
                body.setChannelId(ObjectUtil.isNotEmpty(entries.get("channel_id", Long.class)) ? entries.get("channel_id", Long.class) : 999L);
                body.setChannelName(ObjectUtil.isNotEmpty(entries.get("channel_name", String.class)) ? entries.get("channel_name", String.class) : "设备未匹配到");
                body.setDeviceId(ObjectUtil.isNotEmpty(entries.get("device_id", Long.class)) ? entries.get("device_id", Long.class) : 999L);
                body.setDeviceName(ObjectUtil.isNotEmpty(entries.get("device_name", String.class)) ? entries.get("device_name", String.class) : "设备未匹配到");
                body.setAccessType(ObjectUtil.isNotEmpty(entries.get("access_type", String.class)) ? entries.get("access_type", String.class) : "1");
                body.setCarUrl(carUrl);
                body.setPlateNumber(plateNo);
                body.setRecordsId(uniqueNo);
                // 定义日期格式
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                body.setRecordTime(dateFormat.parse(ObjectUtil.isNotEmpty(passTime) ? passTime : "2023-04-17 12:00:00"));
                body.setManufacturerId(ObjectUtil.isNotEmpty(entries.get("manufacturer_id", Long.class)) ? entries.get("manufacturer_id", Long.class) : 999L);
                body.setManufacturerName(ObjectUtil.isNotEmpty(entries.get("manufacturer_name", String.class)) ? entries.get("manufacturer_name", String.class) : "设备未匹配到");
                body.setSiteId(ObjectUtil.isNotEmpty(entries.get("siteId", Long.class)) ? entries.get("siteId", Long.class) : 999L);
                body.setSiteName(ObjectUtil.isNotEmpty(entries.get("siteName", String.class)) ? entries.get("siteName", String.class) : "设备未匹配到");
                try {
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
        JSONObject obj = JSONUtil.createObj();
        obj.set("result", 0);
        obj.set("message", "ok");
        return obj;
    }


}
