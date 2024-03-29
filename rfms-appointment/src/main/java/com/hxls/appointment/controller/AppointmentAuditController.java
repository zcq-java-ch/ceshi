package com.hxls.appointment.controller;

import com.hxls.appointment.pojo.query.TAppointmentQuery;
import com.hxls.appointment.pojo.vo.TAppointmentPersonnelVO;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
import com.hxls.appointment.pojo.vo.TAppointmentVehicleVO;
import com.hxls.appointment.service.TAppointmentService;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("appointment/audit")
@Tag(name = "预约审核")
@AllArgsConstructor
public class AppointmentAuditController {


    private final TAppointmentService tAppointmentService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('audit:appointment:page')")
    public Result<PageResult<TAppointmentVO>> page(@ParameterObject @Valid TAppointmentQuery query) {
        PageResult<TAppointmentVO> page = tAppointmentService.pageByAuthority(query);
        return Result.ok(page);
    }

    @PutMapping
    @Operation(summary = "审核")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('audit:appointment:update')")
    public Result<String> update(@RequestBody @Valid TAppointmentVO vo){
        tAppointmentService.updateByAudit(vo);
        return Result.ok();
    }


    @GetMapping("{id}")
    @Operation(summary = "详情")
    @PreAuthorize("hasAuthority('audit:appointment:info')")
    public Result<List<TAppointmentPersonnelVO>> get(@PathVariable("id") Long id) {
        List<TAppointmentPersonnelVO> result = tAppointmentService.getListById(id);
        return Result.ok(result);
    }


    @GetMapping("/car/{id}")
    @Operation(summary = "车辆详情")
    @PreAuthorize("hasAuthority('audit:appointmentCar:info')")
    public Result<List<TAppointmentVehicleVO>> getInfo(@PathVariable("id") Long id) {
        List<TAppointmentVehicleVO> result = tAppointmentService.getVehicleListById(id);
        return Result.ok(result);
    }


    @PutMapping("car")
    @Operation(summary = "审核车辆")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('audit:appointmentCar:update')")
    public Result<String> updateCar(@RequestBody @Valid TAppointmentVO vo){
        tAppointmentService.updateByAudit(vo);
        return Result.ok();
    }

}
