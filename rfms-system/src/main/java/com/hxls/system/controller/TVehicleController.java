package com.hxls.system.controller;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.system.convert.TVehicleConvert;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.entity.TVehicleEntity;
import com.hxls.system.query.TVehicleQuery;
import com.hxls.system.service.SysUserService;
import com.hxls.system.service.TVehicleService;
import com.hxls.system.vo.SysRoleVO;
import com.hxls.system.vo.TVehicleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* 通用车辆管理表
*
* @author zhaohong
* @since 1.0.0 2024-03-15
*/
@RestController
@RequestMapping("system/vehicle")
@Tag(name="通用车辆管理表")
@AllArgsConstructor
public class TVehicleController {
    private final TVehicleService tVehicleService;
    private final SysUserService sysUserService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('system:vehicle:page')")
    public Result<PageResult<TVehicleVO>> page(@ParameterObject @Valid TVehicleQuery query){
        PageResult<TVehicleVO> page = tVehicleService.page(query);

        //添加默认司机基础信息
        for (TVehicleVO tVehicleVO : page.getList()){
            SysUserEntity byId = sysUserService.getById(tVehicleVO.getDriverId());
            tVehicleVO.setDriverName(byId.getRealName());
            tVehicleVO.setDriverMobile(byId.getMobile());
        }
        return Result.ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('system:vehicle:info')")
    public Result<TVehicleVO> get(@PathVariable("id") Long id){
        TVehicleEntity entity = tVehicleService.getById(id);
        return Result.ok(TVehicleConvert.INSTANCE.convert(entity));
    }

    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('system:vehicle:save')")
    public Result<String> save(@RequestBody TVehicleVO vo){
        tVehicleService.save(vo);
        return Result.ok();
    }

    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('system:vehicle:update')")
    public Result<String> update(@RequestBody @Valid TVehicleVO vo){
        tVehicleService.update(vo);

        return Result.ok();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
    @PreAuthorize("hasAuthority('system:vehicle:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tVehicleService.delete(idList);

        return Result.ok();
    }


    /**
     * 隐蔽性企业查询接口
     * @param data
     * @return
     */
    @PostMapping("getByLicensePlates")
    @Operation(summary = "临时查询")
    public Result<List<TVehicleVO>> getByLicensePlates(@RequestBody List<String> data){
       return Result.ok( tVehicleService.getByLicensePlates(data));
    }

    /**
     *设置车辆绑定
     * @param licensePlates 车牌号
     * @return
     */
    @GetMapping("setVehicleBindingByLicensePlates")
    //@PreAuthorize("hasAuthority('system:vehicle:set')")
    @Operation(summary = "设置车辆绑定")
    public Result<Void> setByLicensePlates(@RequestParam String licensePlates,@RequestParam Integer type){
        Long userId = SecurityUser.getUserId();
        tVehicleService.setByLicensePlates(licensePlates , userId , type);
        return Result.ok();
    }

    /**
     *获取车辆归属
     * @param licensePlates 车牌号
     * @return
     */
    @GetMapping("getVehicleByLicensePlates")
    //@PreAuthorize("hasAuthority('system:vehicle:get')")
    @Operation(summary = "获取车辆归属")
    public Result<String> getVehicleByLicensePlates(@RequestParam String licensePlates){
        Long userId = SecurityUser.getUserId();
        String vehicleByLicensePlates = tVehicleService.getVehicleByLicensePlates(licensePlates, userId);
        return Result.ok(vehicleByLicensePlates);
    }

    @PostMapping("updateStatus")
    @Operation(summary = "批量修改状态")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('system:vehicle:update')")
    public Result<String> updateStatus(@RequestBody List<TVehicleVO> list) {
        tVehicleService.updateStatus(list);
        return Result.ok();
    }

}
