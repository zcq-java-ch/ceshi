package com.hxls.appointment.controller.app;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
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
@RequestMapping("app/staff")
@Tag(name = "预约管理")
@AllArgsConstructor
public class AppStaffController {


    private final TAppointmentService tAppointmentService;

    @GetMapping("page")
    @Operation(summary = "分页")
    public Result<PageResult<TAppointmentVO>> page(@ParameterObject @Valid TAppointmentQuery query){

        //配置查询权限
        UserDetail user = SecurityUser.getUser();
        if (ObjectUtil.isNull(user)) {
            throw new ServerException(ErrorCode.FORBIDDEN);
        }
        if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)) {
            List<Long> dataScopeList = user.getDataScopeList();
            if (CollectionUtils.isNotEmpty(dataScopeList)){
                query.setSiteIds(dataScopeList);
            }else {
                query.setSiteIds(List.of(Constant.EMPTY));
            }
            query.setCreator(user.getId());
        }
        PageResult<TAppointmentVO> page = tAppointmentService.page(query);
        return Result.ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    public Result<TAppointmentVO> get(@PathVariable("id") Long id){
        TAppointmentVO vo = tAppointmentService.getDetailById(id,1L);
        return Result.ok(vo);
    }

    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    public Result<String> save(@RequestBody TAppointmentVO vo){
        tAppointmentService.save(vo);
        return Result.ok();
    }

    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    public Result<String> update(@RequestBody @Valid TAppointmentVO vo){
        tAppointmentService.update(vo);
        return Result.ok();
    }
}
