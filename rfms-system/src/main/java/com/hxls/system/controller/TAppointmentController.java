package com.hxls.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.convert.TAppointmentConvert;
import com.hxls.system.entity.TAppointmentEntity;
import com.hxls.system.service.TAppointmentService;
import com.hxls.system.query.TAppointmentQuery;
import com.hxls.system.vo.TAppointmentVO;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
* 预约信息表
*
* @author zhaohong
* @since 1.0.0 2024-03-15
*/
@RestController
@RequestMapping("system/appointment")
@Tag(name="预约信息表")
@AllArgsConstructor
public class TAppointmentController {
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
        TAppointmentEntity entity = tAppointmentService.getById(id);

        return Result.ok(TAppointmentConvert.INSTANCE.convert(entity));
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
