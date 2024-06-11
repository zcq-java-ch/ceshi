package com.hxls.datasection.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import com.hxls.framework.common.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Result<JSONObject> directlyInsterData(@RequestBody JSONObject jsonObjectBody) {
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
}
