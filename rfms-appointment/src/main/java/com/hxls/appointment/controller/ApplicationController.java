package com.hxls.appointment.controller;

import cn.hutool.core.util.ObjectUtil;
import com.hxls.api.module.message.SmsApi;
import com.hxls.appointment.pojo.query.TAppointmentQuery;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
import com.hxls.appointment.service.TAppointmentService;
import com.hxls.framework.common.constant.Constant;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("supplier/person")
@Tag(name = "供应商人员派驻")
@AllArgsConstructor
public class ApplicationController {

    private final TAppointmentService tAppointmentService;


    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('supplier:person:page')")
    public Result<PageResult<TAppointmentVO>> page(@ParameterObject @Valid TAppointmentQuery query){

        UserDetail user = SecurityUser.getUser();
        if (ObjectUtil.isNull(user)) {
            throw new ServerException(ErrorCode.FORBIDDEN);
        }
        if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)) {

            Long orgId = user.getOrgId();
            if (orgId != null){
                query.setSupplierName(orgId.toString());
            }
        }
        PageResult<TAppointmentVO> page = tAppointmentService.page(query);
        return Result.ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('supplier:person:info')")
    public Result<TAppointmentVO> get(@PathVariable("id") Long id){
        TAppointmentVO vo = tAppointmentService.getDetailById(id , 1L);
        return Result.ok(vo);
    }

    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('supplier:person:save')")
    public Result<String> save(@RequestBody TAppointmentVO vo){
        tAppointmentService.save(vo);
        return Result.ok();
    }


    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('supplier:person:update')")
    public Result<String> update(@RequestBody @Valid TAppointmentVO vo){
        tAppointmentService.update(vo);
        return Result.ok();
    }


    @DeleteMapping
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
    @PreAuthorize("hasAuthority('supplier:person:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tAppointmentService.delete(idList);
        return Result.ok();
    }




}
