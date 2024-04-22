package com.hxls.system.controller;

import com.hxls.framework.security.user.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.convert.SysNoticeConvert;
import com.hxls.system.entity.SysNoticeEntity;
import com.hxls.system.service.SysNoticeService;
import com.hxls.system.query.SysNoticeQuery;
import com.hxls.system.vo.SysNoticeVO;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
* 系统消息表
*
* @author zhaohong
* @since 1.0.0 2024-04-22
*/
@RestController
@RequestMapping("system/sysNotice")
@Tag(name="系统消息表")
@AllArgsConstructor
public class SysNoticeController {
    private final SysNoticeService sysNoticeService;

    @GetMapping("page")
    @Operation(summary = "分页")
    public Result<PageResult<SysNoticeVO>> page(@ParameterObject @Valid SysNoticeQuery query){
        //获取登录用户的系统消息
        query.setReceiverId(SecurityUser.getUserId());
        PageResult<SysNoticeVO> page = sysNoticeService.page(query);

        return Result.ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('system:sysNotice:info')")
    public Result<SysNoticeVO> get(@PathVariable("id") Long id){
        SysNoticeEntity entity = sysNoticeService.getById(id);

        return Result.ok(SysNoticeConvert.INSTANCE.convert(entity));
    }

    @PostMapping
    @Operation(summary = "保存")
    public Result<String> save(@RequestBody SysNoticeVO vo){
        sysNoticeService.save(vo);

        return Result.ok();
    }

    @PutMapping
    @Operation(summary = "修改")
    public Result<String> update(@RequestBody @Valid SysNoticeVO vo){
        sysNoticeService.update(vo);

        return Result.ok();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @PreAuthorize("hasAuthority('system:sysNotice:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        sysNoticeService.delete(idList);

        return Result.ok();
    }
}
