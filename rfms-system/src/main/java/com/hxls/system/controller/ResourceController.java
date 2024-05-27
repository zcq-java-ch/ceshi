package com.hxls.system.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.storage.properties.StorageProperties;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.entity.TVehicleEntity;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.query.TBannerQuery;
import com.hxls.system.service.*;
import com.hxls.system.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("sys/resource")
@Tag(name = "免登录资源下拉")
@AllArgsConstructor
public class ResourceController {

    private final SysOrgService sysOrgService;
    private final SysDictTypeService sysDictTypeService;
    private final TBannerService tBannerService;
    private final StorageProperties storageProperties;
    private final SysUserService sysUserService;
    private final TVehicleService tVehicleService;


    @GetMapping("page")
    @Operation(summary = "场站下拉")
    public Result<PageResult<SysOrgVO>> page(@ParameterObject SysOrgQuery query) {
        PageResult<SysOrgVO> page = sysOrgService.page(query);
        return Result.ok(page);
    }


    @GetMapping("getServerUrl")
    @Operation(summary = "获取服务器地址")
    public Result<String> page() {
        String domain = storageProperties.getConfig().getDomain();
        return Result.ok(domain);
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


    @PostMapping("setVehicleBindingByLicensePlates")
    @Operation(summary ="设置车辆绑定")
    public Result<String> setVehicleBindingByLicensePlates(@RequestBody TVehicleVO vo){
        //获取到得icps传输得数据
        //车牌号
        String licensePlate = vo.getLicensePlate();
        //驾驶员电话
        String driverPhone = vo.getDriverPhone();
        //通过电话找到对应得人员
        SysUserVO byMobile = sysUserService.getByMobile(driverPhone);
        if (ObjectUtil.isNull(byMobile)){
            throw new ServerException("没有此员工");
        }
        tVehicleService.setLicensePlates(byMobile,licensePlate);
        return Result.ok();
    }

    @GetMapping("start")
    public void start(){

        List<SysUserEntity> list = sysUserService.list(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUserType , "1"));
        List<SysUserEntity> sysUserEntities = list.stream().filter(item -> item.getLicensePlate() != null && StrUtil.isNotEmpty(item.getLicensePlate())).collect(Collectors.toList());

        List<TVehicleEntity> tVehicleEntityList = new ArrayList<>();

        for (SysUserEntity sysUserEntity : sysUserEntities) {
            Long id = sysUserEntity.getId();

            List<TVehicleEntity> list1 = tVehicleService.list(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getUserId, id));
            if (CollectionUtils.isNotEmpty(list1)){
                continue;
            }

            TVehicleEntity tVehicleEntity = new TVehicleEntity();
            tVehicleEntity.setCarType(sysUserEntity.getCarType());
            tVehicleEntity.setUserId(sysUserEntity.getId());
            tVehicleEntity.setLicensePlate(sysUserEntity.getLicensePlate());
            tVehicleEntity.setImageUrl(sysUserEntity.getImageUrl());
            tVehicleEntity.setEmissionStandard(sysUserEntity.getEmissionStandard());
            tVehicleEntity.setDriverId(sysUserEntity.getId());
            tVehicleEntity.setDriverMobile(sysUserEntity.getMobile());
            tVehicleEntity.setDriverName(sysUserEntity.getRealName());
            tVehicleEntity.setSiteId(sysUserEntity.getStationId());

            tVehicleEntityList.add(tVehicleEntity);
        }

        tVehicleService.saveBatch(tVehicleEntityList);

    }




}
