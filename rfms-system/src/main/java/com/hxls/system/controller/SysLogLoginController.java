package com.hxls.system.controller;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.system.query.SysLogLoginQuery;
import com.hxls.system.service.SysLogLoginService;
import com.hxls.system.vo.SysLogLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.Result;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录日志
 *
 * @author
 *
 */
@RestController
@RequestMapping("sys/log/login")
@Tag(name = "登录日志")
@AllArgsConstructor
public class SysLogLoginController {
    private final SysLogLoginService sysLogLoginService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('sys:log:login')")
    public Result<PageResult<SysLogLoginVO>> page(@ParameterObject @Valid SysLogLoginQuery query) {
        PageResult<SysLogLoginVO> page = sysLogLoginService.page(query);
        return Result.ok(page);
    }

    @GetMapping("export")
    @Operation(summary = "导出excel")
    @OperateLog(type = OperateTypeEnum.EXPORT)
    @PreAuthorize("hasAuthority('sys:log:login')")
    public void export() {
        sysLogLoginService.export();
    }

}
