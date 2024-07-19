package com.hxls.system.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hxls.api.module.message.SmsApi;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.common.utils.TreeByCodeUtils;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.system.convert.SysOrgConvert;
import com.hxls.system.convert.SysSiteAreaConvert;
import com.hxls.system.convert.SysUserConvert;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.entity.SysSiteAreaEntity;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.entity.TVehicleEntity;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.query.SysUserQuery;
import com.hxls.system.service.*;
import com.hxls.system.vo.OrganizationVO;
import com.hxls.system.vo.SysOrgVO;
import com.hxls.system.vo.SysSiteAreaVO;
import com.hxls.system.vo.SysUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    /**
     * 引入密码修改
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 引入密码修改
     */
    private final TVehicleService tVehicleService;

    /**
     * 引入系统参数
     */
    private final SysParamsService sysParamsService;

    /**
     *
     */
    private final SysAreacodeDeviceService sysAreacodeDeviceService;



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


    @PutMapping
    @Operation(summary = "修改用户")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    public Result<String> update(@RequestBody @Valid SysUserVO vo) {
        // 如果密码不为空，则进行加密处理
        if (StrUtil.isBlank(vo.getPassword())) {
            vo.setPassword(null);
        } else {
            vo.setPassword(passwordEncoder.encode(vo.getPassword()));
        }
        sysUserService.updateByUser(vo);
        return Result.ok();
    }


    @GetMapping("area")
    @Operation(summary = "区域下拉")
    public Result<List<SysSiteAreaVO>> areaList(@RequestParam Long siteId) {
        LambdaQueryWrapper<SysSiteAreaEntity> wrapper = new LambdaQueryWrapper<>();
        List<SysSiteAreaEntity> list = sysSiteAreaService.list(wrapper.eq(SysSiteAreaEntity::getSiteId, siteId));
        List<SysSiteAreaVO> result = new ArrayList<>(SysSiteAreaConvert.INSTANCE.convertList(list));
        return Result.ok(result);

    }


    @GetMapping("site")
    @Operation(summary = "组织下拉")
    public Result<List<SysOrgVO>> siteList(@RequestParam Integer type) {

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
            }else {
                List<SysOrgEntity> list = sysOrgService.list(wrapper
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
        List<SysOrgEntity> list = sysOrgService.list( new LambdaQueryWrapper<SysOrgEntity>().eq(SysOrgEntity::getStatus , Constant.ENABLE));
        //List<SysOrgEntity> orgEntityList = list.stream().filter(item -> item.getPname().equals("四川华西集团有限公司")).collect(Collectors.toList());
        List<SysOrgEntity> orgEntityList  = new ArrayList<>();
        //写一个递归  最上级是不是   四川华西集团有限公司
        for (SysOrgEntity sysOrgEntity : list) {
            if (checkingData(sysOrgEntity ,list ) ){
                orgEntityList.add(sysOrgEntity);
            }
        }
        return Result.ok(TreeByCodeUtils.build(SysOrgConvert.INSTANCE.convertList(orgEntityList)));
    }

    private Boolean checkingData(SysOrgEntity sysOrgEntity, List<SysOrgEntity> list) {

        if (  sysOrgEntity.getPcode() != null && sysOrgEntity.getPcode().equals("HG")){
            return true;
        }
        for (SysOrgEntity item : list) {
            if (item.getCode().equals(sysOrgEntity.getPcode())) {
                return checkingData(item, list);
            }
        }
        return false;
    }

    @GetMapping("personAll")
    @Operation(summary = "全量人员下拉")
    public Result<PageResult<SysUserVO>> page(@ParameterObject @Valid SysUserQuery query) {
        PageResult<SysUserVO> page = sysUserService.pageByNoAuth(query);
        for (SysUserVO userVO : page.getList()){
            if(userVO.getOrgId() != null){
                SysOrgEntity byId = sysOrgService.getById(userVO.getOrgId());
                userVO.setOrgName(byId.getName());
            }
        }
        return Result.ok(page);
    }


    @GetMapping("fleetName")
    @Operation(summary = "关联厂站车队下拉")
    public Result<List<String>> listForFleetName() {
        //配置查询权限
        UserDetail user = SecurityUser.getUser();
        if (ObjectUtil.isNull(user)) {
            throw new ServerException(ErrorCode.FORBIDDEN);
        }
        List<TVehicleEntity> list = tVehicleService.list(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getSiteId,928));
        if (CollectionUtils.isNotEmpty(list)){
            return Result.ok(new ArrayList<>(list.stream().map(TVehicleEntity::getFleetName).filter(Objects::nonNull).distinct().toList()));
        }
        return Result.ok(new ArrayList<>());
    }

    /**
      * @author Mryang
      * @description 权限站点下拉 没有本身组织
      * @date 11:48 2024/5/10
      * @param
      * @return
      */
    @GetMapping("siteNoOrg")
    @Operation(summary = "站点下拉无组织")
    public Result<List<SysOrgVO>> siteNoOrgList(@RequestParam Integer type) {

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
                List<SysOrgEntity> list = sysOrgService.list(wrapper.in(SysOrgEntity::getId, dataScopeList));
                if (CollectionUtils.isNotEmpty(list)) {
                    result.addAll(SysOrgConvert.INSTANCE.convertList(list));
                }
            }else {
                List<SysOrgEntity> list = sysOrgService.list(wrapper
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


    @GetMapping("adviceNote")
    @Operation(summary = "获取预约告知书")
    public Result<String> adviceNote() {
        String adviceNote = sysParamsService.getString("advice_note");
        return Result.ok(adviceNote);
    }

    /**
     * 比对当前登陆人得组织是否是当前预约厂站得免审核组织
     * @param id 厂站id
     * @return 布尔值
     */
    @GetMapping("compareOrg")
    @Operation(summary = "比对是否免审核")
    public Result<Boolean> compareOrg(@RequestParam Long id) {

        UserDetail user = SecurityUser.getUser();
        if (user==null){
            throw new ServerException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        
        if (Objects.equals(user.getSuperAdmin(), Constant.SUPER_ADMIN)){
            return Result.ok(true);
        }
        SysOrgEntity byId = sysOrgService.getById(id);
        if (byId ==null){
            throw new ServerException(ErrorCode.NOT_FOUND);
        }
        if ( StringUtils.isNotEmpty( byId.getAuthOrgIdList() ) && byId.getAuthOrgIdList().contains(user.getOrgId().toString())) {
            return Result.ok(true);
        }
        return Result.ok(false);

    }


    @GetMapping("personList")
    @Operation(summary = "人员模糊查询下拉")
    public Result<List<SysUserVO>> personList(@RequestParam String name,@RequestParam String userType) {

        //配置查询权限
        List<SysUserVO> result = new ArrayList<>();
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StrUtil.isNotEmpty(userType),SysUserEntity::getUserType,userType);
        wrapper.like(StrUtil.isNotEmpty(name),SysUserEntity::getRealName , name);
        //获取是否是管理
        List<SysUserEntity> list = sysUserService.list(wrapper);
        result.addAll(SysUserConvert.INSTANCE.convertList(list));
        return Result.ok(result);
    }

    @GetMapping("getOrgSiteList")
    @Operation(summary = "获取厂站区域列表")
//    @PreAuthorize("hasAuthority('sys:org:getOrgSiteList')")
    public Result<List<SysOrgVO>> getOrgSiteList() {
        UserDetail user = SecurityUser.getUser();
//        List<Long> dataScopeList = user.getDataScopeList();
        List<SysOrgVO> orgSiteList = sysOrgService.getOrgSiteList(user);
        if (CollectionUtils.isNotEmpty(orgSiteList)){
            for (SysOrgVO sysOrgVO : orgSiteList) {
                LambdaQueryWrapper<SysSiteAreaEntity> sysSiteAreaEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
                sysSiteAreaEntityLambdaQueryWrapper.eq(SysSiteAreaEntity::getSiteId, sysOrgVO.getId());
                sysSiteAreaEntityLambdaQueryWrapper.eq(SysSiteAreaEntity::getStatus, 1);
                sysSiteAreaEntityLambdaQueryWrapper.eq(SysSiteAreaEntity::getDeleted, 0);
                List<SysSiteAreaEntity> sysSiteAreaEntityList = sysSiteAreaService.list(sysSiteAreaEntityLambdaQueryWrapper);
                if (CollectionUtils.isNotEmpty(sysSiteAreaEntityList)){
                    List<SysSiteAreaVO> returnList = new ArrayList<>();
                    for (int i = 0; i < sysSiteAreaEntityList.size(); i++) {
                        SysSiteAreaEntity entity = sysSiteAreaService.getById(sysSiteAreaEntityList.get(i).getId());
                        SysSiteAreaVO convert = SysSiteAreaConvert.INSTANCE.convert(entity);
                        String faceInCode = entity.getFaceInCode();
                        if (StringUtils.isNotEmpty(faceInCode)){
                            JSONArray objects = sysAreacodeDeviceService.queryDeviceListByCode(faceInCode);
                            convert.setFaceInCodeAndDevices(objects);
                        }
                        String faceOutCode = entity.getFaceOutCode();
                        if (StringUtils.isNotEmpty(faceOutCode)){
                            JSONArray objects = sysAreacodeDeviceService.queryDeviceListByCode(faceOutCode);
                            convert.setFaceOutCodeAndDevices(objects);
                        }
                        String carInCode = entity.getCarIntCode();
                        if (StringUtils.isNotEmpty(carInCode)){
                            JSONArray objects = sysAreacodeDeviceService.queryDeviceListByCode(carInCode);
                            convert.setCarIntCodeAndDevices(objects);
                        }
                        String carOutCode = entity.getCarOutCode();
                        if (StringUtils.isNotEmpty(carOutCode)){
                            JSONArray objects = sysAreacodeDeviceService.queryDeviceListByCode(carOutCode);
                            convert.setCarOutCodeAndDevices(objects);
                        }
                        convert.setName(convert.getAreaName());
                        returnList.add(convert);
                    }

                    if (CollectionUtils.isNotEmpty(returnList)){
                        for (SysSiteAreaVO sysSiteAreaVO : returnList) {
                            sysSiteAreaVO.setAreaId("A"+sysSiteAreaVO.getId());
                        }
                    }
                    sysOrgVO.setSysSiteAreaList(returnList);
                }else {
                    sysOrgVO.setSysSiteAreaList(new ArrayList<>());
                }
            }

            if (CollectionUtils.isNotEmpty(orgSiteList)){
                for (SysOrgVO sysOrgVO : orgSiteList) {
                    sysOrgVO.setAreaId("S"+sysOrgVO.getId());
                }
            }

            return Result.ok(orgSiteList);
        }
        return Result.ok(new ArrayList<>());
    }

}
