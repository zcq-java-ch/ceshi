package com.hxls.appointment.controller;

import com.hxls.appointment.pojo.query.TSupplementRecordQuery;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
import com.hxls.appointment.pojo.vo.TSupplementRecordVO;
import com.hxls.appointment.service.TAppointmentService;
import com.hxls.appointment.service.TSupplementRecordService;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("record/supplement")
@Tag(name = "记录补充")
@AllArgsConstructor
public class RecordSupplementController {

    private final TSupplementRecordService service;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('record:appointment:page')")
    public Result<PageResult<TSupplementRecordVO>> page(@ParameterObject @Valid TSupplementRecordQuery query) {
        PageResult<TSupplementRecordVO> page = service.page(query);
        return Result.ok(page);
    }

    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('record:appointment:save')")
    public Result<String> save(@RequestBody TSupplementRecordVO vo) {
        service.save(vo);
        return Result.ok();
    }



    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('record:appointment:info')")
    public Result<TSupplementRecordVO> get(@PathVariable("id") Long id){
        TSupplementRecordVO vo = service.getDetailById(id);
        return Result.ok(vo);
    }

    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('record:appointment:update')")
    public Result<String> update(@RequestBody @Valid TSupplementRecordVO vo) {
        service.update(vo);
        return Result.ok();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
    @PreAuthorize("hasAuthority('record:appointment:delete')")
    public Result<String> delete(@RequestBody List<Long> idList) {
        service.delete(idList);
        return Result.ok();
    }


}
