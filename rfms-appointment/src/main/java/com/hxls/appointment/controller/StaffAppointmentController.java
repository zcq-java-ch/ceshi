package com.hxls.appointment.controller;


import com.hxls.appointment.convert.TAppointmentConvert;
import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.query.TAppointmentQuery;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
import com.hxls.appointment.service.TAppointmentService;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("appointment/staff")
@Tag(name = "预约管理")
@AllArgsConstructor
public class StaffAppointmentController {


    private final TAppointmentService tAppointmentService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('system:appointment:page')")
    public Result<PageResult<TAppointmentVO>> page(@ParameterObject @Valid TAppointmentQuery query){
        PageResult<TAppointmentVO> page = tAppointmentService.page(query);
        return Result.ok(page);
    }


    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('system:appointment:info')")
    public Result<TAppointmentVO> get(@PathVariable("id") Long id){
        TAppointmentVO vo = tAppointmentService.getDetailById(id);
        return Result.ok(vo);
    }


    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('system:appointment:save')")
    public Result<String> save(@RequestBody TAppointmentVO vo){
        tAppointmentService.save(vo);
        return Result.ok();
    }


    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('system:appointment:update')")
    public Result<String> update(@RequestBody @Valid TAppointmentVO vo){
        tAppointmentService.update(vo);
        return Result.ok();
    }


    @DeleteMapping
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
    @PreAuthorize("hasAuthority('system:appointment:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tAppointmentService.delete(idList);
        return Result.ok();
    }


}
