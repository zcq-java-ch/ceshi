package com.hxls.system.controller;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.storage.properties.StorageProperties;
import com.hxls.system.convert.SysUserConvert;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.query.TBannerQuery;
import com.hxls.system.service.SysDictTypeService;
import com.hxls.system.service.SysOrgService;
import com.hxls.system.service.SysUserService;
import com.hxls.system.service.TBannerService;
import com.hxls.system.vo.SysDictVO;
import com.hxls.system.vo.SysOrgVO;
import com.hxls.system.vo.SysUserVO;
import com.hxls.system.vo.TBannerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("sys/resource")
@Tag(name = "免登录资源下拉")
@AllArgsConstructor
public class ResourceController {

    private final SysOrgService sysOrgService;

    private final SysDictTypeService sysDictTypeService;

    private final TBannerService tBannerService;

    private final StorageProperties storageProperties;



    @GetMapping("page")
    @Operation(summary = "场站下拉")
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



    @GetMapping("pageBanner")
    @Operation(summary = "分页")
    public Result<PageResult<TBannerVO>> page(@ParameterObject @Valid TBannerQuery query){
        PageResult<TBannerVO> page = tBannerService.page(query);

        return Result.ok(page);
    }

    @GetMapping("export")
    @Operation(summary = "下载补录导入模板")
    public Result<String> export() {

        String domain = storageProperties.getConfig().getDomain();
        String path = storageProperties.getLocal().getUrl();

        String dataUrl = domain + "/" + path + "/补录导入模板.xlsx";
        return Result.ok(dataUrl);
    }

    @GetMapping("exportSupplierCar")
    @Operation(summary = "下载车辆入场导入模板")
    public Result<String> exportSupplierCar() {

        String domain = storageProperties.getConfig().getDomain();

        String path = storageProperties.getLocal().getUrl();

        String dataUrl = domain + "/" + path + "/车辆入场导入模板.xlsx";
        return Result.ok(dataUrl);
    }

}
