package com.hxls.datasection.controller;

import com.hxls.datasection.query.TVehicleAccessRecordsQuery;
import com.hxls.datasection.vo.TVehicleAccessRecordsVO;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.datasection.convert.TPersonAccessRecordsConvert;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.query.TPersonAccessRecordsQuery;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
* 人员出入记录表
*
* @author zhaohong 
* @since 1.0.0 2024-03-29
*/
@RestController
@RequestMapping("datasection/TPersonAccessRecords")
@Tag(name="人员出入记录表")
@AllArgsConstructor
public class TPersonAccessRecordsController {
    private final TPersonAccessRecordsService tPersonAccessRecordsService;

    @GetMapping("/pageTpersonAccessRecords")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:page')")
    public Result<PageResult<TPersonAccessRecordsVO>> page(@ParameterObject @Valid TPersonAccessRecordsQuery query){
        PageResult<TPersonAccessRecordsVO> page = tPersonAccessRecordsService.page(query);

        return Result.ok(page);
    }

    @GetMapping("/tpersonAccessRecords/{id}")
    @Operation(summary = "信息")
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:info')")
    public Result<TPersonAccessRecordsVO> get(@PathVariable("id") Long id){
        TPersonAccessRecordsEntity entity = tPersonAccessRecordsService.getById(id);

        return Result.ok(TPersonAccessRecordsConvert.INSTANCE.convert(entity));
    }

    @PostMapping("/saveTpersonAccessRecords")
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:save')")
    public Result<String> saveTpersonAccessRecords(@RequestBody TPersonAccessRecordsVO vo){
        try {
            tPersonAccessRecordsService.save(vo);
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("添加失败！");
        }
    }

    @PutMapping("/updateTpersonAccessRecords")
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:update')")
    public Result<String> update(@RequestBody @Valid TPersonAccessRecordsVO vo){
        tPersonAccessRecordsService.update(vo);

        return Result.ok();
    }

    @DeleteMapping("/delTpersonAccessRecords")
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tPersonAccessRecordsService.delete(idList);

        return Result.ok();
    }


    @GetMapping("/pageUnidirectionalTpersonAccessRecords")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:unidirectional')")
    public Result<PageResult<TPersonAccessRecordsVO>> pageUnidirectionalTVehicleAccessRecords(@ParameterObject @Valid TPersonAccessRecordsQuery query){
        PageResult<TPersonAccessRecordsVO> page = tPersonAccessRecordsService.page(query);

        return Result.ok(page);
    }
}
