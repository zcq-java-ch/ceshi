package com.hxls.system.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.system.convert.SysUserConvert;
import com.hxls.system.convert.TVehicleConvert;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.entity.TVehicleEntity;
import com.hxls.system.query.SysUserQuery;
import com.hxls.system.service.*;
import com.hxls.system.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;


/**
 * 用户管理
 *
 * @author
 */
@RestController
@RequestMapping("sys/user")
@AllArgsConstructor
@Tag(name = "用户管理")
public class SysUserController {
    private final SysUserService sysUserService;
    private final SysUserRoleService sysUserRoleService;
    private final SysUserPostService sysUserPostService;
    private final SysPostService sysPostService;
    private final PasswordEncoder passwordEncoder;
    private final SysOrgService sysOrgService;
    private final SysRoleDataScopeService sysRoleDataScopeService;
    private final TVehicleService tVehicleService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('sys:user:page')")
    public Result<PageResult<SysUserVO>> page(@ParameterObject @Valid SysUserQuery query) {
        PageResult<SysUserVO> page = sysUserService.page(query);
        return Result.ok(page);
    }


    @GetMapping("pageByGys")
    @Operation(summary = "供应商人员分页")
    @PreAuthorize("hasAuthority('sys:user:page')")
    public Result<PageResult<SysUserVO>> pageByGys(@ParameterObject @Valid SysUserQuery query) {
        UserDetail user = SecurityUser.getUser();
        if (user.getOrgId() != null) {
            query.setOrgId(user.getOrgId());
        }
        PageResult<SysUserVO> page = sysUserService.pageByGys(query);
        return Result.ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:user:info')")
    public Result<SysUserVO> get(@PathVariable("id") Long id) {
        SysUserEntity entity = sysUserService.getById(id);

        SysUserVO vo = SysUserConvert.INSTANCE.convert(entity);

        // 用户角色列表
        List<Long> roleIdList = sysUserRoleService.getRoleIdList(id);
        vo.setRoleIdList(roleIdList);

        // 用户岗位列表
        List<Long> postIdList = sysUserPostService.getPostIdList(id);
        vo.setPostIdList(postIdList);

        //查询组织名字
        if (entity.getOrgId() != null) {
            SysOrgEntity byId = sysOrgService.getById(entity.getOrgId());
            vo.setOrgName(byId.getName());
        }

        //查询所属车辆
        List<TVehicleEntity> result = tVehicleService.list(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getUserId , id));
        if (CollectionUtils.isNotEmpty(result)){
            vo.setTVehicleVOList(  TVehicleConvert.INSTANCE.convertList(result) );
        }


        return Result.ok(vo);
    }

    @GetMapping("info")
    @Operation(summary = "登录用户")
    public Result<SysUserVO> info() {
        SysUserVO user = SysUserConvert.INSTANCE.convert(SecurityUser.getUser());
        if (user == null) {
            throw new ServerException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        //查询机构名字
        user = SysUserConvert.INSTANCE.convert(sysUserService.getById(user.getId()));

        // 用户岗位列表
        List<Long> postIdList = sysUserPostService.getPostIdList(user.getId());
        user.setPostIdList(postIdList);

        // 用户岗位名称列表
        List<String> postNameList = sysPostService.getNameList(postIdList);
        user.setPostNameList(postNameList);

        //用户站点名字
        if (user.getStationId() != null) {
            SysOrgEntity byId = sysOrgService.getById(user.getStationId());
            if (byId !=null){
                user.setStationName(byId.getName());
            }
        }

        //查询组织名字  -- 修改为全路径
        if (user.getOrgId() != null) {
            SysOrgEntity byId = sysOrgService.getById(user.getOrgId());
            if (byId !=null){
                String orgName = byId.getName();
                user.setOrgName(orgName);
                user.setOrgCode(byId.getCode());
                String OverallOrgStructure = getOverallOrgStructure(byId.getPcode(), orgName);
                user.setOverallOrgStructure(OverallOrgStructure);
            }
        }
        //查询所属车辆
        List<TVehicleEntity> result = tVehicleService.list(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getUserId , user.getId()));
        if (CollectionUtils.isNotEmpty(result)){
            user.setTVehicleVOList(  TVehicleConvert.INSTANCE.convertList(result) );
        }

        //用户管理的站点数据权限
        List<SysOrgVO> orgList = sysRoleDataScopeService.getOrgList(user.getId());
        user.setOrgList(orgList);

        return Result.ok(user);
    }

    /**
     * 获取全组织结构
     *
     * @param orgCode
     * @param orgName
     * @return
     */
    private String getOverallOrgStructure(String orgCode, String orgName) {

        SysOrgEntity byId = sysOrgService.getByCode(orgCode);
        if (byId == null) {
            return orgName;
        }
        return getOverallOrgStructure(byId.getPcode(), byId.getName() + "/" + orgName);
    }

    @PutMapping("info")
    @Operation(summary = "修改登录用户信息")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    public Result<String> loginInfo(@RequestBody @Valid SysUserBaseVO vo) {
        UserDetail user = SecurityUser.getUser();
        if (user == null) {
            throw new ServerException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        sysUserService.updateLoginInfo(vo, user);
        return Result.ok();
    }

    @PutMapping("password")
    @Operation(summary = "修改密码")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    public Result<String> password(@RequestBody @Valid SysUserPasswordVO vo) {
        // 原密码不正确
        UserDetail user = SecurityUser.getUser();
        if (!passwordEncoder.matches(vo.getPassword(), user.getPassword())) {
            return Result.error("原密码不正确");
        }
        // 修改密码
        sysUserService.updatePassword(user.getId(), passwordEncoder.encode(vo.getNewPassword()));

        return Result.ok();
    }

    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('sys:user:save')")
    public Result<String> save(@RequestBody @Valid SysUserVO vo) {
        // 新增密码不能为空
        if (StrUtil.isBlank(vo.getPassword())) {
            return Result.error("密码不能为空");
        }

        // 密码加密
        vo.setPassword(passwordEncoder.encode(vo.getPassword()));

        // 保存
        sysUserService.save(vo);

        return Result.ok();
    }

    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('sys:user:update')")
    public Result<String> update(@RequestBody @Valid SysUserVO vo) {
        // 如果密码不为空，则进行加密处理
        if (StrUtil.isBlank(vo.getPassword())) {
            vo.setPassword(null);
        } else {
            vo.setPassword(passwordEncoder.encode(vo.getPassword()));
        }
        sysUserService.update(vo);
        return Result.ok();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public Result<String> delete(@RequestBody List<Long> idList) {
        Long userId = SecurityUser.getUserId();
        if (idList.contains(userId)) {
            return Result.error("不能删除当前登录用户");
        }

        sysUserService.delete(idList);

        return Result.ok();
    }

    @PostMapping("resetPassword")
    @Operation(summary = "重置密码")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('sys:user:update')")
    public Result<String> resetPassword(@RequestBody List<Long> idList) {
        for (Long id : idList) {
            // 重置密码
            sysUserService.updatePassword(id, passwordEncoder.encode("hxls123456"));
        }

        return Result.ok();
    }


    @PostMapping("import")
    @Operation(summary = "导入供应商用户")
    @OperateLog(type = OperateTypeEnum.IMPORT)
    @PreAuthorize("hasAuthority('sys:user:import')")
    public Result<String> importExcel(@RequestBody @Valid SysUserImportVO vo) {
        if (vo.getImageUrl().isEmpty()) {
            return Result.error("请选择需要上传的文件");
        }
        sysUserService.importByExcel(vo.getImageUrl(), passwordEncoder.encode("hxls123456"), vo.getOrgId());

        return Result.ok();
    }

    @GetMapping("export")
    @Operation(summary = "导出用户")
    @OperateLog(type = OperateTypeEnum.EXPORT)
    @PreAuthorize("hasAuthority('sys:user:export')")
    public void export() {
        sysUserService.export();
    }


    @GetMapping("queryByMainUsers")
    @Operation(summary = "主数据人员下拉数据")
    @PreAuthorize("hasAuthority('sys:user:page')")
    public Result<List<MainUserVO>> queryByMainUsers() {
        List<MainUserVO> userVOS = sysUserService.queryByMainUsers();
        return Result.ok(userVOS);
    }

    @PostMapping("updateStatus")
    @Operation(summary = "批量修改状态")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('sys:user:update')")
    public Result<String> updateStatus(@RequestBody List<SysUserVO> list) {
        sysUserService.updateStatus(list);
        return Result.ok();
    }

    @PostMapping("updateStationId")
    @Operation(summary = "批量修改所属站点")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    @PreAuthorize("hasAuthority('sys:user:updateStationId')")
    public Result<String> updateStationIdList(@RequestBody List<SysUserVO> list) {
        sysUserService.updateStationIdList(list);
        return Result.ok();
    }

    @PostMapping("synOrg")
    @Operation(summary = "同步组织结构")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    public Result<String> synOrg() {
        sysUserService.synOrg();
        return Result.ok();
    }

    @PostMapping("synUser")
    @Operation(summary = "同步主数据人员")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    public Result<String> synUser() {
        sysUserService.synUser();
        return Result.ok();
    }

    @PostMapping("importWithPictures")
    @Operation(summary = "导入供应商用户带图片")
    @OperateLog(type = OperateTypeEnum.IMPORT)
    @PreAuthorize("hasAuthority('sys:user:import')")
    public Result<String> importExcelWithPictures(@RequestBody @Valid SysUserImportVO vo) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        if (vo.getImageUrl().isEmpty()) {
            return Result.error("请选择需要上传的文件");
        }
        sysUserService.importByExcelWithPictures(vo.getImageUrl(), passwordEncoder.encode("hxls123456"), vo.getOrgId());

        return Result.ok();
    }



}
