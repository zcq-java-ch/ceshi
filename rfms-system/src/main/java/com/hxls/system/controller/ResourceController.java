package com.hxls.system.controller;


import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.service.SysDictTypeService;
import com.hxls.system.service.SysOrgService;
import com.hxls.system.vo.SysDictVO;
import com.hxls.system.vo.SysOrgVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("sys/resource")
@Tag(name = "资源下拉")
@AllArgsConstructor
public class ResourceController {

    private final SysOrgService sysOrgService;

    private final SysDictTypeService sysDictTypeService;


    @GetMapping("page")
    @Operation(summary = "场站分页")
    public Result<PageResult<SysOrgVO>> page(@ParameterObject @Valid SysOrgQuery query) {
        PageResult<SysOrgVO> page = sysOrgService.page(query);
        return Result.ok(page);
    }


    @GetMapping("all")
    @Operation(summary = "全部字典数据")
    public Result<List<SysDictVO>> all() {
        List<SysDictVO> dictList = sysDictTypeService.getDictList();
        return Result.ok(dictList);
    }

}
