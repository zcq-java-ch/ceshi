package com.hxls.datasection.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.api.vo.PageResult;
import com.hxls.api.vo.TAppointmentVO;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.service.DataDashboardsService;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("datasection/dataDashboards")
@Tag(name = "数据看板")
@AllArgsConstructor
public class DataDashboardsController {
    private final DataDashboardsService dataDashboardsService;
    private final AppointmentFeign appointmentFeign;
    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;
    /**
     * 注入线程池
     */
    private final ThreadPoolExecutor executor;

    /**
      * @author: Mryang
      * @Description: PC端-数据看板-厂站看板
      * @Date: 15:05 2024/4/21
      * @Param:
      * @return:
      */
    @SneakyThrows
    @GetMapping("/factoryStationKanban")
    @Operation(summary = "数据看板-厂站看板")
    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:page')")
    public Result<JSONObject> factoryStationKanban(@RequestParam("stationId") Long stationId) {
        // 通过站点查询数据
        JSONObject jsonObject = new JSONObject();
        // 1. 人员信息部分

//        CompletableFuture<JSONObject> future1 = CompletableFuture.supplyAsync(() -> {
//            return dataDashboardsService.personnelInformationSection(stationId);
//        }, executor);
//
        JSONObject jsonper = dataDashboardsService.personnelInformationSection(stationId);
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

        // 6. 站点人员明细统计
        JSONObject jsonsiteTj = dataDashboardsService.sitePersonnelBreakdownSectionTj(jsonsite);
        jsonObject.put("sitePersonnelBreakdownSectionTj", jsonsiteTj);

        //替换人员信息部分
        jsonper.put("realTimeTotalNumberOfPeople" ,jsonsiteTj.getString("sum") );
        jsonper.put("companyPersonnel" ,jsonsiteTj.getString("company") );
        jsonper.put("residency" ,jsonsiteTj.getString("supplier") );
        jsonper.put("externalAppointments" ,jsonsiteTj.getString("other") );
        jsonObject.put("personnelInformationSection", jsonper);

        return Result.ok(jsonObject);

    }

    /**
      * @author Mryang
      * @description PC端-数据看板-公司看板
      * @date 11:02 2024/4/23
      * @param
      * @return
      */
    @GetMapping("/companyKanban")
    @Operation(summary = "数据看板-公司看板")
    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:page')")
    public Result<JSONObject> companyKanban() {

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

    @PostMapping("/guard")
    @Operation(summary = "数据看板-安保看板")
    @PreAuthorize("hasAuthority('datasection:guard:page')")
    public Result<PageResult<TAppointmentVO>> guardPage(@RequestBody AppointmentDTO data) {
        //配置查询权限
        UserDetail user = SecurityUser.getUser();
        if (user ==null){
            throw new ServerException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)){
            data.setDataScopeList(CollectionUtils.isNotEmpty(user.getDataScopeList()) ? user.getDataScopeList() : List.of(user.getOrgId()));
        }
        PageResult<TAppointmentVO> board = appointmentFeign.board(data);
        return Result.ok(board);
    }

    /**
      * @author Mryang
      * @description PC端-安保看板-获取详情
      * @date 10:15 2024/4/23
      * @param id 站点
      * @return JSONObject
      */
    @GetMapping("/guardInformation")
    @Operation(summary = "数据看板-安保看板-获取详情")
    @PreAuthorize("hasAuthority('datasection:guard:info')")
    public JSONObject guardInformation(@RequestParam Long id) {
        return appointmentFeign.guardInformation(id);
    }

    @PostMapping("/appointment")
    @Operation(summary = "数据看板-预约看板")
    @PreAuthorize("hasAuthority('datasection:appointment:page')")
    public Result<PageResult<TAppointmentVO>> appointmentPage(@RequestBody AppointmentDTO data) {
        PageResult<TAppointmentVO> board = appointmentFeign.board(data);
        return Result.ok(board);
    }

    @GetMapping("appointmentInformation")
    @Operation(summary = "数据看板-预约看板-获取详情")
    @PreAuthorize("hasAuthority('datasection:appointment:info')")
    public JSONObject appointmentInformation(@RequestParam  Long id) {
        return appointmentFeign.guardInformation(id);
    }

    @GetMapping("/delAppointment")
    @Operation(summary = "数据看板-删除预约")
    @PreAuthorize("hasAuthority('datasection:appointment:del')")
    public Result<Void> delAppointment(@RequestParam Long id) {
        appointmentFeign.delAppointment(id);
        return Result.ok();
    }

    @GetMapping("/appointmentSum")
    @Operation(summary = "数据看板-预约汇总")
    @PreAuthorize("hasAuthority('datasection:appointment:sum')")
    public Result< JSONObject> appointmentSum() {

        //配置查询权限
        UserDetail user = SecurityUser.getUser();

        if (user ==null){
            throw new ServerException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        String siteId = "1";

        if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)) {
            siteId =  user.getOrgId().toString();
        }

        if (CollectionUtils.isNotEmpty(user.getDataScopeList())){
            siteId = StrUtil.join("," , user.getDataScopeList());
        }

        JSONObject entries = appointmentFeign.appointmentSum(siteId , 2L);
        return Result.ok(entries);
    }

    @GetMapping("/guardSum")
    @Operation(summary = "数据看板-安保汇总")
    @PreAuthorize("hasAuthority('datasection:guard:sum')")
    public Result<JSONObject> guardSum() {

        //配置查询权限
        UserDetail user = SecurityUser.getUser();
        if (user ==null){
            throw new ServerException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        String siteId = "1";

        if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)) {
            siteId =  user.getOrgId().toString();
        }

        if (CollectionUtils.isNotEmpty(user.getDataScopeList())){
            siteId = StrUtil.join("," , user.getDataScopeList());
        }

        JSONObject entries = appointmentFeign.appointmentSum(siteId , 1L);
        return Result.ok(entries);
    }

    @PostMapping("/test")
    @Operation(summary = "测试随行人员生成数据")
    public Result<JSONObject> 测试随性人员生成记录(@RequestBody TVehicleAccessRecordsEntity entity) {
        tVehicleAccessRecordsService.retinuegenerateRecords(entity);
        return Result.ok();
    }
}
