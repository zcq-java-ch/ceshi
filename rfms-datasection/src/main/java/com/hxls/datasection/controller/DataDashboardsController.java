package com.hxls.datasection.controller;

import com.alibaba.fastjson.JSONObject;
import com.hxls.datasection.query.TPersonAccessRecordsQuery;
import com.hxls.datasection.service.DataDashboardsService;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("datasection/dataDashboards")
@Tag(name="数据看板")
@AllArgsConstructor
public class DataDashboardsController {
    private final DataDashboardsService dataDashboardsService;
    @GetMapping("/factoryStationKanban")
    @Operation(summary = "数据看板-厂站看板")
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:page')")
    public Result<JSONObject> factoryStationKanban(@RequestParam("stationId") Long stationId){
        // 通过站点查询数据

        JSONObject jsonObject = new JSONObject();
        // 1. 人员信息部分
        JSONObject jsonper = dataDashboardsService.personnelInformationSection(stationId);
        jsonObject.put("personnelInformationSection", jsonper);
        // 2. 车辆信息部分
        JSONObject jsonveh = dataDashboardsService.vehicleInformationSection(stationId);
        jsonObject.put("vehicleInformationSection", jsonveh);
        // 3. 站点人员明细部分
        JSONObject jsonsite = dataDashboardsService.sitePersonnelBreakdownSection(stationId);
        jsonObject.put("sitePersonnelBreakdownSection", jsonsite);
        // 4. 车辆出入明细部分
        JSONObject jsonvehic = dataDashboardsService.vehicleAccessDetails(stationId);
        jsonObject.put("vehicleAccessDetails", jsonvehic);
        // 5. 外部预约人员明细部分
        JSONObject jsonbreak = dataDashboardsService.breakdownOfExternalAppointments(stationId);
        jsonObject.put("breakdownOfExternalAppointments", jsonbreak);

        return Result.ok(jsonObject);
    }

    @GetMapping("/companyKanban")
    @Operation(summary = "数据看板-公司看板")
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:page')")
    public Result<JSONObject> companyKanban(){
        JSONObject jsonObject = new JSONObject();
        // 1. 基本信息部分
        JSONObject jsonper = dataDashboardsService.basicInformationSection();
        jsonObject.put("basicInformationSection", jsonper);
        // 2. 实名制信息部分
        JSONObject jsonveh = dataDashboardsService.realNameInformationSection();
        jsonObject.put("realNameInformationSection", jsonveh);
        // 3. 地图部分
        JSONObject jsonsite = dataDashboardsService.mapSection();
        jsonObject.put("mapSection", jsonsite);

        return Result.ok(jsonObject);
    }
}
