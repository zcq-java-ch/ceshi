package com.hxls.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.convert.TManufacturerConvert;
import com.hxls.system.entity.TManufacturerEntity;
import com.hxls.system.service.TManufacturerService;
import com.hxls.system.query.TManufacturerQuery;
import com.hxls.system.vo.TManufacturerVO;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
* 厂家管理表
*
* @author zhaohong
* @since 1.0.0 2024-03-15
*/
@RestController
@RequestMapping("system/manufacturer")
@Tag(name="厂家管理表")
@AllArgsConstructor
public class TManufacturerController {
    private final TManufacturerService tManufacturerService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('system:manufacturer:page')")
    public Result<PageResult<TManufacturerVO>> page(@ParameterObject @Valid TManufacturerQuery query){
        PageResult<TManufacturerVO> page = tManufacturerService.page(query);

        return Result.ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('system:manufacturer:info')")
    public Result<TManufacturerVO> get(@PathVariable("id") Long id){
        TManufacturerEntity entity = tManufacturerService.getById(id);

        return Result.ok(TManufacturerConvert.INSTANCE.convert(entity));
    }

    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('system:manufacturer:save')")
    public Result<String> save(@RequestBody TManufacturerVO vo){
        tManufacturerService.save(vo);

        return Result.ok();
    }

    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('system:manufacturer:update')")
    public Result<String> update(@RequestBody @Valid TManufacturerVO vo){
        tManufacturerService.update(vo);

        return Result.ok();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
    @PreAuthorize("hasAuthority('system:manufacturer:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tManufacturerService.delete(idList);

        return Result.ok();
    }
}
