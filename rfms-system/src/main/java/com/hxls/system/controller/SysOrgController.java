package com.hxls.system.controller;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.query.SysUserQuery;
import com.hxls.system.service.SysOrgService;
import com.hxls.system.vo.SysOrgVO;
import com.hxls.system.vo.SysUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import com.hxls.system.convert.SysOrgConvert;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 机构管理
 *
 * @author
 *
 */
@RestController
@RequestMapping("sys/org")
@Tag(name = "机构管理")
@AllArgsConstructor
public class SysOrgController {
    private final SysOrgService sysOrgService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('sys:org:page')")
    public Result<PageResult<SysOrgVO>> page(@ParameterObject @Valid SysOrgQuery query) {
        //获取登录账户的数据权限
        UserDetail user = SecurityUser.getUser();
        if(user.getDataScopeList() != null ){
            query.setOrgList(user.getDataScopeList());
        }
        PageResult<SysOrgVO> page = sysOrgService.page(query);
        return Result.ok(page);
    }

    @GetMapping("pageByGys")
    @Operation(summary = "供应商组织")
    @PreAuthorize("hasAuthority('sys:org:page')")
    public Result<PageResult<SysOrgVO>> pageByGys(@ParameterObject @Valid SysOrgQuery query) {
        //获取登录账户的供应商
        UserDetail user = SecurityUser.getUser();
        if(user.getOrgId() != null ){
            query.setId(user.getOrgId());
        }
        PageResult<SysOrgVO> page = sysOrgService.page(query);
        return Result.ok(page);
    }

    @GetMapping("list")
    @Operation(summary = "列表")
    @PreAuthorize("hasAuthority('sys:org:list')")
    public Result<List<SysOrgVO>> list() {
        List<SysOrgVO> list = sysOrgService.getList();

        return Result.ok(list);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:org:info')")
    public Result<SysOrgVO> get(@PathVariable("id") Long id) {
        SysOrgEntity entity = sysOrgService.getById(id);
        SysOrgVO vo = SysOrgConvert.INSTANCE.convert(entity);

        // 获取上级机构名称
        if (entity.getPcode() != null) {
//            SysOrgEntity parentEntity = sysOrgService.getById(entity.getPcode());
            SysOrgEntity parentEntity = sysOrgService.getByCode(entity.getPcode());
            if (ObjectUtils.isNotEmpty(parentEntity)){
                vo.setPname(parentEntity.getName());
            }
        }

        return Result.ok(vo);
    }

    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('sys:org:save')")
    public Result<String> save(@RequestBody @Valid SysOrgVO vo) {
        sysOrgService.save(vo);

        return Result.ok();
    }

    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('sys:org:update')")
    public Result<String> update(@RequestBody @Valid SysOrgVO vo) {
        sysOrgService.update(vo);

        return Result.ok();
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
    @PreAuthorize("hasAuthority('sys:org:delete')")
    public Result<String> delete(@PathVariable("id") Long id) {
        sysOrgService.delete(id);

        return Result.ok();
    }

    @PostMapping("synOrg")
    @Operation(summary = "同步组织结构")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    public Result<String> synOrg() {
        sysOrgService.synOrg();

        return Result.ok();
    }

    @PostMapping("updateStatus")
    @Operation(summary = "批量修改状态")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('sys:org:update')")
    public Result<String> updateStatus(@RequestBody List<SysOrgVO> list) {
        sysOrgService.updateStatus(list);
        return Result.ok();
    }

}
