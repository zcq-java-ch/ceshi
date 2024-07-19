package com.hxls.system.controller;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.system.entity.SysOnlineLog;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.service.SysControlCarService;
import com.hxls.system.service.SysOnlineLogService;
import com.hxls.system.service.SysOrgService;
import com.hxls.system.vo.SysOrgVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import com.hxls.system.convert.SysOrgConvert;
import org.apache.commons.collections4.CollectionUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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
    private final SysControlCarService sysControlCarService;
    private final SysOnlineLogService sysOnlineLogService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('sys:org:page')")
    public Result<PageResult<SysOrgVO>> page(@ParameterObject @Valid SysOrgQuery query) {
        //获取登录账户的数据权限
        UserDetail user = SecurityUser.getUser();
        if( !user.getSuperAdmin().equals(1) &&  CollectionUtils.isNotEmpty(user.getDataScopeList())){
            query.setOrgList(user.getDataScopeList());
        }else if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)){
            query.setId(user.getOrgId());
        }
        PageResult<SysOrgVO> page = sysOrgService.page(query);

        //判断是否是管控厂站
        List<Long> control = sysControlCarService.getContro();
        if (CollectionUtils.isNotEmpty(page.getList()) && CollectionUtils.isNotEmpty(control)){
            for (SysOrgVO sysOrgVO : page.getList()) {
                if (control.contains(sysOrgVO.getId())){
                    sysOrgVO.setIsControl(1);
                }
            }
        }
        return Result.ok(page);
    }

    @GetMapping("pageByGys")
    @Operation(summary = "供应商组织")
    @PreAuthorize("hasAuthority('sys:org:page')")
    public Result<PageResult<SysOrgVO>> pageByGys(@ParameterObject @Valid SysOrgQuery query) {
        //获取登录账户的供应商
        UserDetail user = SecurityUser.getUser();
        if(com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(user.getDataScopeList())){
            query.setOrgList(user.getDataScopeList());
        }else if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)){
            query.setId(user.getOrgId());
        }
        query.setCreator(user.getId());
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

    @PostMapping("updateStatus")
    @Operation(summary = "批量修改状态")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('sys:org:update')")
    public Result<String> updateStatus(@RequestBody List<SysOrgVO> list) {
        sysOrgService.updateStatus(list);
        return Result.ok();
    }



    @GetMapping("setStationControl")
    @Operation(summary = "设置厂站管控")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('system:org:setStation')")
    public Result<String> setStation(@RequestParam Long id , @RequestParam Integer type) {

        List<Long> contro = sysControlCarService.getContro();

        if (Objects.equals(type, Constant.ENABLE)){
            //开启管控
            if (contro.contains(id)){
                throw new ServerException("此厂站正在管控中，无需再次管控");
            }
            sysOrgService.setStation(id);
            return Result.ok();
        }

        if (!contro.contains(id)){
            throw new ServerException("此厂站未在管控中，无需解除管控");
        }

        sysOrgService.offStationControl(id);
        return Result.ok();
    }

    @GetMapping("pageOffLine")
    @Operation(summary = "查看代理服务离线情况")
    //@PreAuthorize("hasAuthority('system:org:offStationControl')")
    public Result<PageResult<SysOnlineLog>> pageOffLine(@ParameterObject @Valid SysOrgQuery query) {

        PageResult<SysOnlineLog> sysOnlineLogPageResult = sysOnlineLogService.pageList(query);

        return Result.ok(sysOnlineLogPageResult);
    }



}
