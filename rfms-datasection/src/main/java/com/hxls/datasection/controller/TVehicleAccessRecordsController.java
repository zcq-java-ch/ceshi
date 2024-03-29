package com.hxls.datasection.controller;

import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.datasection.convert.TVehicleAccessRecordsConvert;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import com.hxls.datasection.query.TVehicleAccessRecordsQuery;
import com.hxls.datasection.vo.TVehicleAccessRecordsVO;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
* 车辆出入记录表
*
* @author zhaohong 
* @since 1.0.0 2024-03-29
*/
@RestController
@RequestMapping("datasection/TVehicleAccessRecords")
@Tag(name="车辆出入记录表")
@AllArgsConstructor
public class TVehicleAccessRecordsController {
    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;

    @GetMapping("/pageTVehicleAccessRecords")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:page')")
    public Result<PageResult<TVehicleAccessRecordsVO>> page(@ParameterObject @Valid TVehicleAccessRecordsQuery query){
        PageResult<TVehicleAccessRecordsVO> page = tVehicleAccessRecordsService.page(query);

        return Result.ok(page);
    }

    @GetMapping("/TVehicleAccessRecords/{id}")
    @Operation(summary = "信息")
//    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:info')")
    public Result<TVehicleAccessRecordsVO> get(@PathVariable("id") Long id){
        TVehicleAccessRecordsEntity entity = tVehicleAccessRecordsService.getById(id);

        return Result.ok(TVehicleAccessRecordsConvert.INSTANCE.convert(entity));
    }

    @PostMapping("/saveTVehicleAccessRecords")
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
//    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:save')")
    public Result<String> save(@RequestBody TVehicleAccessRecordsVO vo){
        tVehicleAccessRecordsService.save(vo);

        return Result.ok();
    }

    @PutMapping("/updateTVehicleAccessRecords")
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
//    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:update')")
    public Result<String> update(@RequestBody @Valid TVehicleAccessRecordsVO vo){
        tVehicleAccessRecordsService.update(vo);

        return Result.ok();
    }

    @DeleteMapping("/delTVehicleAccessRecords")
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
//    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tVehicleAccessRecordsService.delete(idList);

        return Result.ok();
    }

}
