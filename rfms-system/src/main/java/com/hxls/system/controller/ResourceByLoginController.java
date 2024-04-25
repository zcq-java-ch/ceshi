package com.hxls.system.controller;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.common.utils.TreeByCodeUtils;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.system.convert.SysOrgConvert;
import com.hxls.system.convert.SysSiteAreaConvert;
import com.hxls.system.convert.SysUserConvert;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.entity.SysSiteAreaEntity;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.query.SysUserQuery;
import com.hxls.system.service.SysOrgService;
import com.hxls.system.service.SysSiteAreaService;
import com.hxls.system.service.SysUserService;
import com.hxls.system.vo.SysOrgVO;
import com.hxls.system.vo.SysSiteAreaVO;
import com.hxls.system.vo.SysUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("sys/resource/auth")
@Tag(name = "登录人员的资源下拉")
@AllArgsConstructor
public class ResourceByLoginController {

    /**
     * 引入用户业务层
     */
    private final SysUserService sysUserService;
    /**
     * 引入组织业务层
     */
    private final SysOrgService sysOrgService;
    /**
     * 引入区域业务层
     */
    private final SysSiteAreaService sysSiteAreaService;

    @GetMapping("person")
    @Operation(summary = "人员下拉")
    public Result<List<SysUserVO>> person() {

        //配置查询权限
        UserDetail user = SecurityUser.getUser();
        if (ObjectUtil.isNull(user)) {
            throw new ServerException(ErrorCode.FORBIDDEN);
        }

        List<SysUserVO> result = new ArrayList<>();
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();

        //获取是否是管理员
        if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)) {
            List<Long> dataScopeList = user.getDataScopeList();
            if (CollectionUtils.isNotEmpty(dataScopeList)) {
                //获取是所属数据权限的人员
                List<SysUserEntity> list = sysUserService.list(wrapper.in(SysUserEntity::getOrgId, dataScopeList)
                        .or()
                        .eq(SysUserEntity::getId, user.getId()));
                if (CollectionUtils.isNotEmpty(list)) {
                    result.addAll(SysUserConvert.INSTANCE.convertList(list));
                }
            }
            return Result.ok(result);
        }
        List<SysUserEntity> list = sysUserService.list();
        result.addAll(SysUserConvert.INSTANCE.convertList(list));
        return Result.ok(result);
    }


    @GetMapping("site")
    @Operation(summary = "区域下拉")
    public Result<List<SysSiteAreaVO>> siteList(@RequestParam Long siteId) {

//        //配置查询权限
//        UserDetail user = SecurityUser.getUser();
//        if (ObjectUtil.isNull(user)) {
//            throw new ServerException(ErrorCode.FORBIDDEN);
//        }
        //
//        LambdaQueryWrapper<SysSiteAreaEntity> wrapper = new LambdaQueryWrapper<>();
//        //获取是否是管理员
//        if ( !user.getSuperAdmin().equals(Constant.SUPER_ADMIN) ) {
//            List<Long> dataScopeList = user.getDataScopeList();
//            if (CollectionUtils.isNotEmpty(dataScopeList)){
//                //获取是所属数据权限的人员
//                List<SysSiteAreaEntity> list = sysSiteAreaService.list(wrapper.in(SysSiteAreaEntity::getSiteId, dataScopeList));
//                if (CollectionUtils.isNotEmpty(list)){
//                    result.addAll( SysSiteAreaConvert.INSTANCE.convertList(list));
//                }
//            }
//            return Result.ok(result);
//        }
        LambdaQueryWrapper<SysSiteAreaEntity> wrapper = new LambdaQueryWrapper<>();
        List<SysSiteAreaEntity> list = sysSiteAreaService.list(wrapper.eq(SysSiteAreaEntity::getSiteId, siteId));
        List<SysSiteAreaVO> result = new ArrayList<>(SysSiteAreaConvert.INSTANCE.convertList(list));
        return Result.ok(result);

    }


    @GetMapping("area")
    @Operation(summary = "组织下拉")
    public Result<List<SysOrgVO>> areaList(@RequestParam Integer type) {

        //配置查询权限
        UserDetail user = SecurityUser.getUser();
        if (ObjectUtil.isNull(user)) {
            throw new ServerException(ErrorCode.FORBIDDEN);
        }
        List<SysOrgVO> result = new ArrayList<>();

        LambdaQueryWrapper<SysOrgEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrgEntity::getProperty, type);
        //获取是否是管理员
        if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)) {
            List<Long> dataScopeList = user.getDataScopeList();
            if (CollectionUtils.isNotEmpty(dataScopeList)) {
                //获取是所属数据权限的人员
                wrapper.in(SysOrgEntity::getId, dataScopeList);
                List<SysOrgEntity> list = sysOrgService.list(wrapper.in(SysOrgEntity::getId, dataScopeList)
                        .or()
                        .eq(SysOrgEntity::getId, user.getOrgId()));
                if (CollectionUtils.isNotEmpty(list)) {
                    result.addAll(SysOrgConvert.INSTANCE.convertList(list));
                }
            }
            return Result.ok(result);
        }

        List<SysOrgEntity> list = sysOrgService.list(wrapper);
        result.addAll(SysOrgConvert.INSTANCE.convertList(list));
        return Result.ok(result);

    }

    @GetMapping("supplier")
    @Operation(summary = "供应商下拉")
    public Result<List<SysOrgVO>> supplierList(@RequestParam Integer type) {
        //配置查询权限
        UserDetail user = SecurityUser.getUser();
        if (ObjectUtil.isNull(user)) {
            throw new ServerException(ErrorCode.FORBIDDEN);
        }
        List<SysOrgVO> result = new ArrayList<>();

        LambdaQueryWrapper<SysOrgEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrgEntity::getProperty, type);
        //获取是否是管理员
        if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)) {
            List<SysOrgEntity> list = sysOrgService.list(wrapper.eq(SysOrgEntity::getId, user.getOrgId()));
            if (CollectionUtils.isNotEmpty(list)) {
                result.addAll(SysOrgConvert.INSTANCE.convertList(list));
            }
            return Result.ok(result);
        }
        List<SysOrgEntity> list = sysOrgService.list(wrapper);
        result.addAll(SysOrgConvert.INSTANCE.convertList(list));
        return Result.ok(result);
    }



    @GetMapping("orgAll")
    @Operation(summary = "全量组织下拉")
    public Result<List<SysOrgVO>> orgAll() {
        //配置查询权限
        List<SysOrgEntity> list = sysOrgService.list();
        return Result.ok(TreeByCodeUtils.build(SysOrgConvert.INSTANCE.convertList(list)));
    }

    @GetMapping("personAll")
    @Operation(summary = "全量人员下拉")
    public Result<PageResult<SysUserVO>> page(@ParameterObject @Valid SysUserQuery query) {
        PageResult<SysUserVO> page = sysUserService.page(query);
        for (SysUserVO userVO : page.getList()){
            if(userVO.getOrgId() != null){
                SysOrgEntity byId = sysOrgService.getById(userVO.getOrgId());
                userVO.setOrgName(byId.getName());
            }
        }
        return Result.ok(page);
    }


}
