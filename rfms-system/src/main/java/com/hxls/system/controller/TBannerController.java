package com.hxls.system.controller;

import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.convert.TBannerConvert;
import com.hxls.system.entity.TBannerEntity;
import com.hxls.system.service.TBannerService;
import com.hxls.system.query.TBannerQuery;
import com.hxls.system.vo.TBannerVO;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
* banner管理
*
* @author zhaohong
* @since 1.0.0 2024-03-13
*/
@RestController
@RequestMapping("system/banner")
@Tag(name="banner管理")
@AllArgsConstructor
public class TBannerController {
    private final TBannerService tBannerService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('system:banner:page')")
    public Result<PageResult<TBannerVO>> page(@ParameterObject @Valid TBannerQuery query){
        PageResult<TBannerVO> page = tBannerService.page(query);

        return Result.ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('system:banner:info')")
    public Result<TBannerVO> get(@PathVariable("id") Long id){
        TBannerEntity entity = tBannerService.getById(id);

        return Result.ok(TBannerConvert.INSTANCE.convert(entity));
    }

    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('system:banner:save')")
    public Result<String> save(@RequestBody TBannerVO vo){
        tBannerService.save(vo);

        return Result.ok();
    }

    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('system:banner:update')")
    public Result<String> update(@RequestBody @Valid TBannerVO vo){
        tBannerService.update(vo);

        return Result.ok();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
    @PreAuthorize("hasAuthority('system:banner:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tBannerService.delete(idList);

        return Result.ok();
    }
}
