package com.hxls.system.controller;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.convert.TVehicleConvert;
import com.hxls.system.entity.TVehicleEntity;
import com.hxls.system.service.TVehicleService;
import com.hxls.system.query.TVehicleQuery;
import com.hxls.system.vo.TVehicleVO;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('system:vehicle:page')")
    public Result<PageResult<TVehicleVO>> page(@ParameterObject @Valid TVehicleQuery query){
        PageResult<TVehicleVO> page = tVehicleService.page(query);

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



    @PostMapping("getByLicensePlates")
    @Operation(summary = "临时查询")
    public Result<List<TVehicleVO>> getByLicensePlates(@RequestBody List<String> data){
       return Result.ok( tVehicleService.getByLicensePlates(data));

    }

}
