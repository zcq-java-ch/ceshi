package com.hxls.system.controller;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.system.convert.SysAreacodeDeviceConvert;
import com.hxls.system.entity.SysAreacodeDeviceEntity;
import com.hxls.system.query.SysAreacodeDeviceQuery;
import com.hxls.system.service.SysAreacodeDeviceService;
import com.hxls.system.vo.SysAreacodeDeviceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
* 区域通道随机码与设备中间表
*
* @author zhaohong 
* @since 1.0.0 2024-04-01
*/
@RestController
@RequestMapping("hxls/device")
@Tag(name="区域通道随机码与设备中间表")
@AllArgsConstructor
public class SysAreacodeDeviceController {
    private final SysAreacodeDeviceService sysAreacodeDeviceService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('spd:device:page')")
    public Result<PageResult<SysAreacodeDeviceVO>> page(@ParameterObject @Valid SysAreacodeDeviceQuery query){
        PageResult<SysAreacodeDeviceVO> page = sysAreacodeDeviceService.page(query);

        return Result.ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('spd:device:info')")
    public Result<SysAreacodeDeviceVO> get(@PathVariable("id") Long id){
        SysAreacodeDeviceEntity entity = sysAreacodeDeviceService.getById(id);

        return Result.ok(SysAreacodeDeviceConvert.INSTANCE.convert(entity));
    }

    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('spd:device:save')")
    public Result<String> save(@RequestBody SysAreacodeDeviceVO vo){
        sysAreacodeDeviceService.save(vo);

        return Result.ok();
    }

    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('spd:device:update')")
    public Result<String> update(@RequestBody @Valid SysAreacodeDeviceVO vo){
        sysAreacodeDeviceService.update(vo);

        return Result.ok();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
    @PreAuthorize("hasAuthority('spd:device:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        sysAreacodeDeviceService.delete(idList);

        return Result.ok();
    }
}
