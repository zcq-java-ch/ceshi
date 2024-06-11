package com.hxls.datasection.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.api.feign.system.VehicleFeign;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import com.hxls.framework.common.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("datasection/NoTokenThirdPartyCalls")
@Tag(name="无需认证-远程调用接口")
@AllArgsConstructor
@Slf4j
public class NoTokenController {
    private final DeviceFeign deviceFeign;
    private final VehicleFeign vehicleFeign;
    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;
    private final TPersonAccessRecordsService tPersonAccessRecordsService;
    /**
     * @author Mryang
     * @description 查询传入站点的 人脸和车辆最后一条记录的记录时间
     * @date 13:47 2024/6/11
     * @param
     * @return
     */
    @PostMapping("/getLastRecordTime")
    public Result<JSONObject> getLastRecordTime(@RequestBody JSONObject jsonObjectBody) {
        // 创建SimpleDateFormat对象，定义日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        JSONObject jsonObject1 = new JSONObject();

        // 客户端IP
        String agentIp = jsonObjectBody.getString("agentIp");
        JSONObject jsonObject = deviceFeign.queryTheSiteIDBySiteIP(agentIp);
        if (ObjectUtils.isNotEmpty(jsonObject)){
            Long siteId = jsonObject.getLong("siteId");
            LambdaQueryWrapper<TVehicleAccessRecordsEntity> objectQueryWrapper = new LambdaQueryWrapper<>();
            objectQueryWrapper.eq(TVehicleAccessRecordsEntity::getStatus, "1");
            objectQueryWrapper.eq(TVehicleAccessRecordsEntity::getDeleted, "0");
            objectQueryWrapper.eq(TVehicleAccessRecordsEntity::getSiteId, siteId);
            objectQueryWrapper.orderByDesc(true, TVehicleAccessRecordsEntity::getRecordTime);
            objectQueryWrapper.last("LIMIT 1");
            TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity = tVehicleAccessRecordsService.getOne(objectQueryWrapper);
            Date recordTime1 = new Date();
            if (ObjectUtils.isNotEmpty(tVehicleAccessRecordsEntity)){
                recordTime1 = tVehicleAccessRecordsEntity.getRecordTime();
            }else {
                // 获取当前日期
                LocalDate today = LocalDate.now();
                // 计算三天前的日期
                LocalDate threeDaysAgo = today.minusDays(3);
                // 设置时间为一天的开始时间（00:00:00）
                LocalDateTime startOfDay = LocalDateTime.of(threeDaysAgo, LocalTime.MIN);
                // 将LocalDateTime转换为Date
                recordTime1 = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
            }
            jsonObject1.put("tvehicleTime", sdf.format(recordTime1));

            LambdaQueryWrapper<TPersonAccessRecordsEntity> personAccessRecordsEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            personAccessRecordsEntityLambdaQueryWrapper.eq(TPersonAccessRecordsEntity::getStatus, "1");
            personAccessRecordsEntityLambdaQueryWrapper.eq(TPersonAccessRecordsEntity::getDeleted, "0");
            personAccessRecordsEntityLambdaQueryWrapper.eq(TPersonAccessRecordsEntity::getSiteId, siteId);
            personAccessRecordsEntityLambdaQueryWrapper.eq(TPersonAccessRecordsEntity::getCreateType, "1");
            personAccessRecordsEntityLambdaQueryWrapper.orderByDesc(true, TPersonAccessRecordsEntity::getRecordTime);
            personAccessRecordsEntityLambdaQueryWrapper.last("LIMIT 1");
            TPersonAccessRecordsEntity tPersonAccessRecordsEntity = tPersonAccessRecordsService.getOne(personAccessRecordsEntityLambdaQueryWrapper);
            Date recordTime = new Date();
            if (ObjectUtils.isNotEmpty(tPersonAccessRecordsEntity)){
                recordTime = tPersonAccessRecordsEntity.getRecordTime();
            }else {
                // 获取当前日期
                LocalDate today = LocalDate.now();
                // 计算三天前的日期
                LocalDate threeDaysAgo = today.minusDays(3);
                // 设置时间为一天的开始时间（00:00:00）
                LocalDateTime startOfDay = LocalDateTime.of(threeDaysAgo, LocalTime.MIN);
                // 将LocalDateTime转换为Date
                recordTime = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
            }
            jsonObject1.put("personTime", sdf.format(recordTime));
        }
        return Result.ok(jsonObject1);
    }

    /**
      * @author Mryang
      * @description 第三方调用，手动存储传回来的车辆识别记录
      * @date 15:36 2024/6/11
      * @param
      * @return
      */
    @PostMapping("/directlyInsterData")
    public Result<String> directlyInsterData(@RequestBody JSONObject jsonObjectBody) {
        JSONArray jsonArray = jsonObjectBody.getJSONArray("carRecords");
        if (CollectionUtils.isNotEmpty(jsonArray)) {
            int insterCount = 0;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObjectRecords = jsonArray.getJSONObject(i);
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
                            tVehicleAccessRecordsEntity.setImageUrl(jsonObject.getString("imageUrl"));
                            tVehicleAccessRecordsEntity.setLicenseImage(jsonObject.getString("licenseImage"));
                        }
                    }

                    tVehicleAccessRecordsService.save(tVehicleAccessRecordsEntity);
                    insterCount++;
                    // 存储车辆进出场展示台账
//                    log.info("通信记录存储完成，开始记录台账");
//                    tVehicleAccessRecordsService.saveLedger(tVehicleAccessRecordsEntity);

                }
            }
            // 如果插入数量不为空，那么就需要更新下精城车辆通行台账
            if (insterCount > 0){
                tVehicleAccessRecordsService.updateVehicleLedger("928");
            }
            return Result.ok("插入完成总条数为："+insterCount);
        }
        return Result.error("数据传入为空");
    }

}
