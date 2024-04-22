package com.hxls.appointment.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hxls.appointment.pojo.query.TSupplementRecordQuery;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
import com.hxls.appointment.pojo.vo.TSupplementRecordVO;
import com.hxls.appointment.service.TAppointmentService;
import com.hxls.appointment.service.TSupplementRecordService;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

        //配置查询权限
        UserDetail user = SecurityUser.getUser();
        if (ObjectUtil.isNull(user)) {
            throw new ServerException(ErrorCode.FORBIDDEN);
        }
        if (user.getSuperAdmin()<1) {
            List<Long> dataScopeList = user.getDataScopeList();
            if (CollectionUtils.isNotEmpty(dataScopeList)){
                query.setSiteIds(dataScopeList);
            }else {
                query.setCreator(user.getId());
            }
        }


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


    @PostMapping("import")
    @Operation(summary = "导入")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('record:appointment:import')")
    public Result<Long> importData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择需要上传的文件");
        }
        Long id = service.export(file);
        return Result.ok(id);
    }

}
