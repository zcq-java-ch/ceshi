package com.hxls.system.controller;

import cn.hutool.core.lang.Assert;
import com.hxls.framework.common.cache.RedisCache;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.system.convert.TDeviceManagementConvert;
import com.hxls.system.entity.TDeviceManagementEntity;
import com.hxls.system.query.TDeviceManagementQuery;
import com.hxls.system.service.TDeviceManagementService;
import com.hxls.system.vo.TDeviceManagementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Security;
import java.util.List;

/**
* 设备管理表
*
* @author zhaohong
* @since 1.0.0 2024-03-15
*/
@RestController
@RequestMapping("system/device")
@Tag(name="设备管理表")
@AllArgsConstructor
public class TDeviceManagementController {
    private final TDeviceManagementService tDeviceManagementService;
    private final RedisCache redisCache;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('system:device:page')")
    public Result<PageResult<TDeviceManagementVO>> page(@ParameterObject @Valid TDeviceManagementQuery query){

//        if (query.getSiteId() ==null){
//            UserDetail user = SecurityUser.getUser();
//            if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)){
////                user.getDataScopeList()
//                query.setSiteId(user.getOrgId());
//            }
//        }

        if ( query.getSiteId() != null  &&  query.getSiteId()==779) {
            query.setSiteId(1440L);
        }

        PageResult<TDeviceManagementVO> page = tDeviceManagementService.page(query);

        return Result.ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('system:device:info')")
    public Result<TDeviceManagementVO> get(@PathVariable("id") Long id){

        TDeviceManagementEntity entity = tDeviceManagementService.getById(id);

        return Result.ok(TDeviceManagementConvert.INSTANCE.convert(entity));
    }

    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('system:device:save')")
    public Result<String> save(@RequestBody TDeviceManagementVO vo){
        tDeviceManagementService.save(vo);

        return Result.ok();
    }

    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('system:device:update')")
    public Result<String> update(@RequestBody @Valid TDeviceManagementVO vo){
        tDeviceManagementService.update(vo);

        return Result.ok();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
    @PreAuthorize("hasAuthority('system:device:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tDeviceManagementService.delete(idList);

        return Result.ok();
    }


    @GetMapping("online")
    @Operation(summary = "在线状态")
    public void IsOnline(@RequestParam String ip){
        //接收心跳 -- 放入缓存
        redisCache.set(ip,"在线",20);

    }

    @PostMapping("updateStatus")
    @Operation(summary = "批量修改状态")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('system:device:update')")
    public Result<String> updateStatus(@RequestBody List<TDeviceManagementVO> list) {
        List<String> strings = tDeviceManagementService.updateStatus(list);
        if (CollectionUtils.isNotEmpty(strings)){
            return Result.error("以下设备已经被绑定不能更改状态："+strings.toString());
        }else {
            return Result.ok();
        }
    }
}
