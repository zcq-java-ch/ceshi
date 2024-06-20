package com.hxls.system.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fhs.trans.service.impl.TransService;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.excel.ExcelFinishCallBack;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.DateUtils;
import com.hxls.framework.common.utils.ExcelUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.framework.security.cache.TokenStoreCache;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.framework.security.utils.TokenUtils;
import com.hxls.storage.properties.StorageProperties;
import com.hxls.system.cache.MainPlatformCache;
import com.hxls.system.config.BaseImageUtils;
import com.hxls.system.controller.ExcelController;
import com.hxls.system.convert.SysOrgConvert;
import com.hxls.system.convert.SysUserConvert;
import com.hxls.system.convert.TVehicleConvert;
import com.hxls.system.dao.SysUserDao;
import com.hxls.system.entity.*;
import com.hxls.system.enums.SuperAdminEnum;
import com.hxls.system.query.SysRoleUserQuery;
import com.hxls.system.query.SysUserQuery;
import com.hxls.system.service.*;
import com.hxls.system.vo.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.checkerframework.checker.units.qual.A;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理
 *
 * @author
 */
@Service
@AllArgsConstructor
@Slf4j
public class SysUserServiceImpl extends BaseServiceImpl<SysUserDao, SysUserEntity> implements SysUserService {
    private final SysUserRoleService sysUserRoleService;
    private final SysUserPostService sysUserPostService;
    private final SysUserTokenService sysUserTokenService;
    private final SysOrgService sysOrgService;
    private final TokenStoreCache tokenStoreCache;
    private final TransService transService;
    private final MainPlatformCache mainPlatformCache;
    private final AppointmentFeign appointmentFeign;
    private final StorageProperties properties;
    private final PasswordEncoder passwordEncoder;
    private final TVehicleService tVehicleService;
    private final SysSiteAreaService sysSiteAreaService;
    private final SysAreacodeDeviceService sysAreacodeDeviceService;
    private final TDeviceManagementService tDeviceManagementService;

    @Override
    public PageResult<SysUserVO> page(SysUserQuery query) {
        // 查询参数
        Map<String, Object> params = getParams(query);

        // 分页查询
        IPage<SysUserEntity> page = getPage(query);
        params.put(Constant.PAGE, page);

        // 数据列表
        List<SysUserEntity> list = baseMapper.getList(params);
        List<SysUserVO> sysUserVOS = SysUserConvert.INSTANCE.convertList(list);

        //查询车辆多归属
        for (SysUserVO sysUserVO : sysUserVOS) {
            Long id = sysUserVO.getId();
            List<TVehicleEntity> result = tVehicleService.list(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getUserId, id).in(TVehicleEntity::getCarClass, 1, 3));
            if (CollectionUtils.isNotEmpty(result)) {
                sysUserVO.setTVehicleVOList(TVehicleConvert.INSTANCE.convertList(result));
            }
        }

        return new PageResult<>(sysUserVOS, page.getTotal());
    }


    @Override
    public PageResult<SysUserVO> pageByGys(SysUserQuery query) {
        // 查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("username", query.getUsername());
        params.put("mobile", query.getMobile());
        params.put("gender", query.getGender());
        params.put("userType", query.getUserType());
        params.put("supervisor", query.getSupervisor());
        params.put("licensePlate", query.getLicensePlate());
        params.put("code", query.getCode());
        params.put("status", query.getStatus());
        params.put("realName", query.getRealName());
        params.put("orgName", query.getOrgName());
        params.put("orgId", query.getOrgId());
        params.put("orgList", query.getOrgList());
        // 分页查询
        IPage<SysUserEntity> page = getPage(query);
        params.put(Constant.PAGE, page);

        // 数据列表
        List<SysUserEntity> list = baseMapper.getList(params);

        List<SysUserVO> sysUserVOS = SysUserConvert.INSTANCE.convertList(list);

        //查询车辆多归属
        for (SysUserVO sysUserVO : sysUserVOS) {
            Long id = sysUserVO.getId();
            List<TVehicleEntity> result = tVehicleService.list(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getUserId, id).in(TVehicleEntity::getCarClass, 1,3));
            if (CollectionUtils.isNotEmpty(result)) {
                sysUserVO.setTVehicleVOList(TVehicleConvert.INSTANCE.convertList(result));
            }
        }

        return new PageResult<>(sysUserVOS, page.getTotal());
    }


    private Map<String, Object> getParams(SysUserQuery query) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", query.getUsername());
        params.put("mobile", query.getMobile());
        params.put("gender", query.getGender());
        params.put("userType", query.getUserType());
        params.put("supervisor", query.getSupervisor());
        params.put("licensePlate", query.getLicensePlate());
        params.put("code", query.getCode());
        params.put("status", query.getStatus());
        params.put("realName", query.getRealName());
        params.put("orgName", query.getOrgName());
        params.put("stationId", query.getStationId());


        if (query.getOrgId() != null) {
            List<Long> subOrgIdList = sysOrgService.getSubOrgIdList(query.getOrgId());
            params.put("orgList", subOrgIdList);
        }

        // 数据权限
        params.put(Constant.DATA_SCOPE, getDataScope("t1", null));

        return params;
    }

    private Map<String, Object> getParamsByNoAuth(SysUserQuery query) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", query.getUsername());
        params.put("mobile", query.getMobile());
        params.put("gender", query.getGender());
        params.put("userType", query.getUserType());
        params.put("supervisor", query.getSupervisor());
        params.put("licensePlate", query.getLicensePlate());
        params.put("code", query.getCode());
        params.put("status", query.getStatus());
        params.put("realName", query.getRealName());
        params.put("orgName", query.getOrgName());
        params.put("orgId", query.getOrgId());

        return params;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SysUserVO vo) {
        SysUserEntity entity = SysUserConvert.INSTANCE.convert(vo);
        entity.setSuperAdmin(SuperAdminEnum.NO.getValue());

        // 判断用户名是否存在
        SysUserEntity user = baseMapper.getByUsername(entity.getUsername());
        if (user != null) {
            throw new ServerException("用户名已经存在");
        }

        // 判断用户名是否存在
        user = baseMapper.getByMobile(entity.getUsername());
        if (user != null) {
            throw new ServerException("用户名已经存在");
        }

        // 判断手机号是否存在
        user = baseMapper.getByUsername(entity.getMobile());
        if (user != null) {
            throw new ServerException("手机号已经存在");
        }

        // 判断手机号是否存在
        user = baseMapper.getByMobile(entity.getMobile());
        if (user != null) {
            throw new ServerException("手机号已经存在");
        }

        // 判断车牌号是否存在
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(vo.getTVehicleVOList())) {
            for (TVehicleVO tVehicleVO : vo.getTVehicleVOList()) {
                List<TVehicleEntity> list = tVehicleService.list(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getLicensePlate, tVehicleVO.getLicensePlate()));
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(list)) {
                    throw new ServerException("车牌号已存在");
                }
            }
        }

        // 保存用户
        baseMapper.insert(entity);

        if (CollectionUtils.isNotEmpty(vo.getTVehicleVOList())) {

            List<TVehicleEntity> tVehicleEntities = TVehicleConvert.INSTANCE.convertToEntityList(vo.getTVehicleVOList());
            for (TVehicleEntity tVehicleEntity : tVehicleEntities) {
                tVehicleEntity.setSiteId(entity.getStationId());
                tVehicleEntity.setStationId(entity.getStationId());
                tVehicleEntity.setUserId(entity.getId());
                tVehicleEntity.setDriverId(entity.getId());
                tVehicleEntity.setDriverName(vo.getRealName());
                tVehicleEntity.setDriverMobile(vo.getMobile());
                if ("1".equals(vo.getUserType())){
                    // 如果是内部员工 设置车辆所属为内部私家车
                    tVehicleEntity.setCarClass("1");
                }else {
                    // 如果是外部员工  设置车辆所属为外部私家车
                    tVehicleEntity.setCarClass("3");
                    tVehicleEntity.setSupplierId(vo.getOrgId());
                    tVehicleEntity.setSupplierName(vo.getOrgName());
                }
            }
            tVehicleService.saveBatch(tVehicleEntities);
        }


        //TODO  添加用户的时候人脸下发  还需要判断是否有场站
        if (entity.getStationId() != null && entity.getStationId().equals(Constant.EMPTY)) {
            JSONObject person = new JSONObject();
            person.set("sendType", "1");
            person.set("data", JSONUtil.toJsonStr(entity));
            appointmentFeign.issuedPeople(person);
            //TODO 添加用户的时候车辆下发 ---判断是否有值
            if (StringUtils.isNotEmpty(entity.getLicensePlate())) {
                JSONObject vehicle = new JSONObject();
                vehicle.set("sendType", "2");
                vehicle.set("data", JSONUtil.toJsonStr(entity));
                appointmentFeign.issuedPeople(vehicle);
            }
            //下发其他站点
            sendOtherStation(entity);

        }

        // 保存用户角色关系
        sysUserRoleService.saveOrUpdate(entity.getId(), vo.getRoleIdList());

        // 更新用户岗位关系
        sysUserPostService.saveOrUpdate(entity.getId(), vo.getPostIdList());
    }

    private void sendOtherStation(SysUserEntity entity) {

        if (StringUtils.isNotEmpty(entity.getStationIds())) {
            for (String stationId : entity.getStationIds().split(",")) {
                if (Long.getLong(stationId).equals(entity.getStationId())) {
                    continue;
                }
                JSONObject person = new JSONObject();
                entity.setStationId(Long.getLong(stationId));
                person.set("data", JSONUtil.toJsonStr(entity));
                appointmentFeign.issuedPeople(person);
                if (StringUtils.isNotEmpty(entity.getLicensePlate())) {
                    String[] licensePlates = entity.getLicensePlate().split(",");
                    for (String licensePlate : licensePlates) {
                        JSONObject vehicle = new JSONObject();
                        vehicle.set("sendType", "2");
                        entity.setLicensePlate(licensePlate);
                        vehicle.set("data", JSONUtil.toJsonStr(entity));
                        appointmentFeign.issuedPeople(vehicle);
                    }
                }
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserVO vo) {
        SysUserEntity entity = SysUserConvert.INSTANCE.convert(vo);         if (entity.getStationId() == null) {
            entity.setStationId(Constant.EMPTY);
        }
        //判断是否需要删除原有厂站的数据
        Long id = vo.getId();
        SysUserEntity byId = baseMapper.getById(id);
        if (byId == null) {
            throw new ServerException(ErrorCode.NOT_FOUND);
        }

        //TODO 修改需要更改方式
        //判断是否更换区域,删除之前得老区域，下发新站点和区域
        //更换新得删除下发方式 --- 2024-06-07
        String oldAreaList = byId.getAreaList();
        String newAreaList = vo.getAreaList();

        compareDeleteSite(oldAreaList , newAreaList);



        if (StringUtils.isNotEmpty(byId.getAreaList())&& StringUtils.isNotEmpty(vo.getAreaList()) && !byId.getAreaList().equals(vo.getAreaList())){
            String areaList = byId.getAreaList();
            List<String> areas = JSONUtil.toList(areaList, String.class);
            List<Long> result = areas.stream().filter(item -> item.contains("S")).map(item -> {
                return Long.parseLong(item.substring(1));
            }).toList();
            for (Long stationId : result) {
                //删除老站点ID
                byId.setStationId(stationId);
                deleteInfoToAgent(byId, byId);
            }
        }

        // 判断用户名是否存在
        SysUserEntity user = baseMapper.getByUsername(entity.getUsername());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("用户名已经存在");
        }

        // 判断手机号是否存在
        user = baseMapper.getByMobile(entity.getMobile());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("手机号已经存在");
        }

        // 判断用户名是否存在
        user = baseMapper.getByMobile(entity.getUsername());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("用户名已经存在");
        }

        // 判断手机号是否存在
        user = baseMapper.getByUsername(entity.getMobile());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("手机号已经存在");
        }

        // 判断车牌号是否存在
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(vo.getTVehicleVOList())) {
            for (TVehicleVO tVehicleVO : vo.getTVehicleVOList()) {
                List<TVehicleEntity> list = tVehicleService.list(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getLicensePlate, tVehicleVO.getLicensePlate()));
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(list)) {
                    if (!list.get(0).getUserId().equals(id)) {
                        throw new ServerException("车牌号已存在");
                    }
                }
            }
        }

        // 更新用户
        updateById(entity);

        //更换车辆存储位置
        if (vo.getVehicle()) {
            // 删除这个用户关联的车辆中，内部私家车 外部私家车
            tVehicleService.remove(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getUserId, vo.getId()).in(TVehicleEntity::getCarClass, "1","3"));
            if (CollectionUtils.isNotEmpty(vo.getTVehicleVOList())) {
                List<TVehicleEntity> tVehicleEntities = TVehicleConvert.INSTANCE.convertToEntityList(vo.getTVehicleVOList());
                for (TVehicleEntity tVehicleEntity : tVehicleEntities) {
                    tVehicleEntity.setSiteId(entity.getStationId());
                    tVehicleEntity.setStationId(entity.getStationId());
                    tVehicleEntity.setUserId(vo.getId());
                    tVehicleEntity.setDriverId(vo.getId());
                    tVehicleEntity.setDriverName(vo.getRealName());
                    tVehicleEntity.setDriverMobile(vo.getMobile());
                    if ("1".equals(vo.getUserType())){
                        // 如果是内部员工 设置车辆所属为内部私家车
                        tVehicleEntity.setCarClass("1");
                    }else {
                        // 如果是外部员工  设置车辆所属为外部私家车
                        tVehicleEntity.setCarClass("3");
                        tVehicleEntity.setSupplierId(vo.getOrgId());
                        tVehicleEntity.setSupplierName(vo.getOrgName());
                    }
                }
                tVehicleService.saveBatch(tVehicleEntities);
            }
        }

        //修改下发 TODO
        if (StringUtils.isNotEmpty(vo.getAreaList())){

            String areaList = vo.getAreaList();
            List<String> areas = JSONUtil.toList(areaList, String.class);

            List<Long> result = areas.stream().filter(item -> item.contains("A")).map(item -> {
                return Long.parseLong(item.substring(1));
            }).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(result)) {
                //获取到所有的区域
                List<SysSiteAreaEntity> sysSiteAreaEntities = sysSiteAreaService.listByIds(result);
                //获取到人脸设备
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(sysSiteAreaEntities)) {
                    //获取到设备编码
                    List<String> deviceIds = new ArrayList<>();
                    for (SysSiteAreaEntity sysSiteAreaEntity : sysSiteAreaEntities) {
                        deviceIds.add(sysSiteAreaEntity.getFaceInCode());
                        deviceIds.add(sysSiteAreaEntity.getFaceOutCode());
                        deviceIds.add(sysSiteAreaEntity.getCarOutCode());
                        deviceIds.add(sysSiteAreaEntity.getCarIntCode());
                    }
                    //通过中间表获取设备id
                    List<SysAreacodeDeviceEntity> areacodeDeviceEntities = sysAreacodeDeviceService.list(new LambdaQueryWrapper<SysAreacodeDeviceEntity>().in(SysAreacodeDeviceEntity::getAreaDeviceCode, deviceIds));
                    if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(areacodeDeviceEntities)) {
                        List<Long> ids = areacodeDeviceEntities.stream().map(SysAreacodeDeviceEntity::getDeviceId).toList();
//                        List<TDeviceManagementEntity> tDeviceManagementEntities = tDeviceManagementService.listByIds(ids);
                        //下发设备
                        JSONObject person = new JSONObject();
                        person.set("sendType", "1");
                        person.set("data", JSONUtil.toJsonStr(byId));
                        person.set("ids", JSONUtil.toJsonStr(ids));
                        appointmentFeign.issuedPeople(person);
                        if (StringUtils.isNotEmpty(entity.getLicensePlate())) {
                            String[] licensePlates = entity.getLicensePlate().split(",");
                            for (String licensePlate : licensePlates) {
                                JSONObject vehicle = new JSONObject();
                                vehicle.set("sendType", "2");
                                entity.setLicensePlate(licensePlate);
                                vehicle.set("data", JSONUtil.toJsonStr(entity));
                                vehicle.set("ids", JSONUtil.toJsonStr(ids));
                                appointmentFeign.issuedPeople(vehicle);
                            }
                        }
                    }
                }
            }
        }

        // 更新用户角色关系
        sysUserRoleService.saveOrUpdate(entity.getId(), vo.getRoleIdList());

        // 更新用户岗位关系
        sysUserPostService.saveOrUpdate(entity.getId(), vo.getPostIdList());

        // 更新用户缓存权限
        sysUserTokenService.updateCacheAuthByUserId(entity.getId());
    }

    /**
     * 比较该删除的权区域
     * @param oldAreaList 老权限区域
     * @param newAreaList 新权限区域
     */
    private void compareDeleteSite(String oldAreaList, String newAreaList) {





    }

    private void deleteInfoToAgent(SysUserEntity byId, SysUserEntity entity) {
        JSONObject person = new JSONObject();
        person.set("sendType", "1");
        person.set("data", JSONUtil.toJsonStr(byId));
        person.set("DELETE", "DELETE");
        appointmentFeign.issuedPeople(person);

        if (StringUtils.isNotEmpty(entity.getLicensePlate())) {
            String[] licensePlates = entity.getLicensePlate().split(",");
            for (String licensePlate : licensePlates) {
                JSONObject vehicle = new JSONObject();
                vehicle.set("sendType", "2");
                entity.setLicensePlate(licensePlate);
                vehicle.set("data", JSONUtil.toJsonStr(entity));
                vehicle.set("DELETE", "DELETE");
                appointmentFeign.issuedPeople(vehicle);
            }
        }
    }

    @Override
    public void updateLoginInfo(SysUserBaseVO vo, UserDetail LoginUser) {
        SysUserEntity entity = SysUserConvert.INSTANCE.convert(vo);
        // 设置登录用户ID
        entity.setId(LoginUser.getId());

        // 判断手机号是否存在
        SysUserEntity user = baseMapper.getByMobile(entity.getMobile());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("手机号已经存在");
        }
        // 更新用户
        updateById(entity);

        // 删除用户缓存
        tokenStoreCache.deleteUser(TokenUtils.getAccessToken());
    }

    @Override
    public void delete(List<Long> idList) {

        List<SysUserEntity> sysUserEntities = listByIds(idList);
        for (SysUserEntity byId : sysUserEntities) {
            JSONObject person = new JSONObject();
            person.set("sendType", "1");
            person.set("data", JSONUtil.toJsonStr(byId));
            person.set("DELETE", "DELETE");
            appointmentFeign.issuedPeople(person);
        }


        // 删除用户
        removeByIds(idList);

        // 删除用户角色关系
        sysUserRoleService.deleteByUserIdList(idList);

        // 删除用户岗位关系
        sysUserPostService.deleteByUserIdList(idList);
    }

    @Override
    public SysUserVO getByMobile(String mobile) {
        SysUserEntity user = baseMapper.getByMobile(mobile);

        return SysUserConvert.INSTANCE.convert(user);
    }

    @Override
    public void updatePassword(Long id, String newPassword) {
        // 修改密码
        SysUserEntity user = getById(id);
        user.setPassword(newPassword);

        updateById(user);
    }

    @Override
    public PageResult<SysUserVO> roleUserPage(SysRoleUserQuery query) {
        // 查询参数
        Map<String, Object> params = getParams(query);
        params.put("roleId", query.getRoleId());

        // 分页查询
        IPage<SysUserEntity> page = getPage(query);
        params.put(Constant.PAGE, page);

        // 数据列表
        List<SysUserEntity> list = baseMapper.getRoleUserList(params);

        return new PageResult<>(SysUserConvert.INSTANCE.convertList(list), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importByExcel(String file, String password, Long orgId) {
        try {
            SysOrgEntity byId = sysOrgService.getById(orgId);

            //导入时候获取的地址是相对路径 需要拼接服务器路径
            String domain = properties.getConfig().getDomain();
            ExcelUtils.readAnalysis(ExcelUtils.convertToMultipartFile(domain + file), SysUserGysExcelVO.class, new ExcelFinishCallBack<SysUserGysExcelVO>() {
                @Override
                public void doAfterAllAnalysed(List<SysUserGysExcelVO> result) {
                    saveUser(result);
                }

                @Override
                public void doSaveBatch(List<SysUserGysExcelVO> result) {
                    saveUser(result);
                }

                private void saveUser(List<SysUserGysExcelVO> result) {
                    ExcelUtils.parseDict(result);
                    List<SysUserEntity> sysUserEntities = SysUserConvert.INSTANCE.convertListEntity(result);
                    sysUserEntities.forEach(user -> {
                        // 判断手机号是否存在
                        SysUserEntity olduser = baseMapper.getByUsername(user.getMobile());
                        if (olduser != null) {
                            throw new ServerException("手机号已经存在");
                        }
                        // 判断手机号是否存在
                        olduser = baseMapper.getByMobile(user.getMobile());
                        if (olduser != null) {
                            throw new ServerException("手机号已经存在");
                        }
                        user.setUserType("2");
                        user.setPassword(password);
                        user.setOrgId(orgId);
                        user.setUsername(user.getMobile());
                        user.setOrgName(byId.getName());
                        user.setStatus(1);
                        user.setSuperAdmin(0);
                    });
                    saveBatch(sysUserEntities);
                }
            });
        } catch (Exception e) {
            throw new ServerException("导入数据不正确");
        }


    }

    @Override
    @SneakyThrows
    public void export() {
        List<SysUserEntity> list = list(Wrappers.lambdaQuery(SysUserEntity.class).eq(SysUserEntity::getSuperAdmin, SuperAdminEnum.NO.getValue()));
        List<SysUserExcelVO> userExcelVOS = SysUserConvert.INSTANCE.convert2List(list);
        transService.transBatch(userExcelVOS);
        // 写到浏览器打开
        ExcelUtils.excelExport(SysUserExcelVO.class, "system_user_excel" + DateUtils.format(new Date()), null, userExcelVOS);
    }

//    @Override
//    public void cardLogin() {
//        //请求小基础数据 1、请求登录接口，获取返回的token   2、根据token去请求小基础组织数据
//        OkHttpClient client = new OkHttpClient();
//        String jsonBody = "{\"idCard\":\"rfms\"}";
//        // 创建RequestBody实例
//        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);
//
//        // 构建请求
//        Request request = new Request.Builder()
//                .url("https://jcmdm.huashijc.com/MainPlatform/userLogin/cardLogin")
//                .post(body) // 设置POST方法
//                .addHeader("Content-Type", "application/json") // 通常OkHttp会自动设置，这里可以省略
//                .build();
//
//        // 发送请求并处理响应
//        try {
//            Response firstResponse = client.newCall(request).execute();
//
//            if (firstResponse.isSuccessful()) {
//                // 请求成功，处理响应数据
//                String responseBody = firstResponse.body().string();
//                // 解析响应体为Map，这里假设JSON结构是标准的
//                JSONObject json1 = new JSONObject(responseBody);
//                JSONObject json2 = new JSONObject(json1.get("data"));
//                // 从响应中提取accessToken
//                String accessToken = json2.get("accessToken").toString();
//                //将accessToken存到缓存中，有效期一个半小时
//                mainPlatformCache.saveAccessToken(accessToken);
//            }
//
//        }catch (Exception e) {
//            // 网络异常处理
//            e.printStackTrace();
//        }
//
//    }

    @Override
    public void cardLogin() {
        String url = "https://jcmdm.huashijc.com/MainPlatform/userLogin/cardLogin";
        String jsonBody = "{\"idCard\":\"rfms\"}";

        // 发送POST请求
        HttpResponse response = HttpUtil.createPost(url)
                .body(jsonBody, ContentType.JSON.toString())
                .execute();

        // 处理响应
        if (response.isOk()) {
            // 请求成功，处理响应数据
            String responseBody = response.body();
            JSONObject json1 = new JSONObject(responseBody);
            JSONObject json2 = json1.getJSONObject("data");
            String accessToken = json2.get("accessToken").toString();
            //将accessToken存到缓存中，有效期一个半小时
            mainPlatformCache.saveAccessToken(accessToken);
        } else {
            // 请求失败，处理错误信息
            System.out.println("Request failed: " + response.getStatus() + ", " + response.body());
        }
    }


    @Override
    public List<MainUserVO> queryByMainUsers() {
        //获取mainAccessToken
        String accessToken = mainPlatformCache.getAccessToken();
        if (!StringUtils.isNotEmpty(accessToken)) {//如果mainAccessToken过期的话，就重新更新mainAccessToken
            cardLogin();
            //更新accessToken
            accessToken = mainPlatformCache.getAccessToken();
        }

        String secondRequestUrl = "https://jcmdm.huashijc.com/MainPlatform/travel/employee/queryList";
        HttpResponse httpResponse = HttpUtil.createPost(secondRequestUrl)
                .header("access-token", accessToken)  // 设置请求头
                .execute();

        // 处理响应
        if (httpResponse.isOk()) {
            // 获取响应体
            String body = httpResponse.body();
            JSONObject rel1 = new JSONObject(body);
            List<MainUserVO> mainUserVOS = JSONUtil.toBean(rel1.get("data").toString(), new TypeReference<List<MainUserVO>>() {
            }, true);
            return mainUserVOS;
        } else {
            throw new ServerException("请求主数据人员异常");
        }
    }

    @Override
    public void updateStatus(List<SysUserVO> list) {
        for (SysUserVO vo : list) {
            SysUserEntity entity = new SysUserEntity();
            entity.setId(vo.getId());

            //查询人员详情
            SysUserEntity byId = getById(vo.getId());

            if (vo.getStatus() != null) {
                entity.setStatus(vo.getStatus());
            }
            // 更新实体
            this.updateById(entity);
            //更新成功后 - 下发设备指令
            JSONObject person = new JSONObject();
            person.set("sendType", "1");
            person.set("data", JSONUtil.toJsonStr(byId));
            if (!vo.getStatus().equals(Constant.ENABLE)) {
                log.info("此时发起禁用删除");
                person.set("DELETE", "DELETE");
            }
            appointmentFeign.issuedPeople(person);
            if (StringUtils.isNotEmpty(entity.getLicensePlate())) {
                JSONObject vehicle = new JSONObject();
                vehicle.set("sendType", "2");
                vehicle.set("data", JSONUtil.toJsonStr(byId));
                if (!vo.getStatus().equals(Constant.ENABLE)) {
                    log.info("此时发起禁用删除");
                    vehicle.set("DELETE", "DELETE");
                }
                appointmentFeign.issuedPeople(vehicle);
            }
        }
    }

    @Override
    public PageResult<SysUserVO> pageByNoAuth(SysUserQuery query) {
        // 查询参数
        Map<String, Object> params = getParamsByNoAuth(query);

        // 分页查询
        IPage<SysUserEntity> page = getPage(query);
        params.put(Constant.PAGE, page);

        // 数据列表
        List<SysUserEntity> list = baseMapper.getList(params);

        return new PageResult<>(SysUserConvert.INSTANCE.convertList(list), page.getTotal());
    }

    @Override
    public void updateByUser(SysUserVO vo) {
        SysUserEntity entity = SysUserConvert.INSTANCE.convert(vo);

        //判断是否需要删除原有厂站的数据
        Long id = vo.getId();
        SysUserEntity byId = baseMapper.getById(id);
        if (byId == null) {
            throw new ServerException(ErrorCode.NOT_FOUND);
        }

        if (byId.getStationId() != null && !byId.getStationId().equals(Constant.EMPTY) && !byId.getStationId().equals(entity.getStationId())) {
            System.out.println("开始删除之前的");
            //删除人员信息
            deleteInfoToAgent(byId, entity);
        }

        // 判断用户名是否存在
        SysUserEntity user = baseMapper.getByUsername(entity.getUsername());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("用户名已经存在");
        }

        // 判断手机号是否存在
        user = baseMapper.getByMobile(entity.getMobile());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("手机号已经存在");
        }

        // 判断车牌号是否存在
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(vo.getTVehicleVOList())) {
            for (TVehicleVO tVehicleVO : vo.getTVehicleVOList()) {
                List<TVehicleEntity> list = tVehicleService.list(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getLicensePlate, tVehicleVO.getLicensePlate()));
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(list)) {
                    if (!list.get(0).getUserId().equals(id)) {
                        throw new ServerException("车牌号已存在");
                    }
                }
            }
        }

        // 更新用户
        updateById(entity);

        //更换车辆存储位置
        if (vo.getVehicle()){
            tVehicleService.remove(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getUserId, vo.getId()));
            if (CollectionUtils.isNotEmpty(vo.getTVehicleVOList())) {
                List<TVehicleEntity> tVehicleEntities = TVehicleConvert.INSTANCE.convertToEntityList(vo.getTVehicleVOList());
                for (TVehicleEntity tVehicleEntity : tVehicleEntities) {
                    tVehicleEntity.setUserId(vo.getId());
                    tVehicleEntity.setDriverId(vo.getId());
                    tVehicleEntity.setDriverName(vo.getRealName());
                    tVehicleEntity.setDriverMobile(vo.getMobile());
                    tVehicleEntity.setId(null);
                }
                tVehicleService.saveBatch(tVehicleEntities);
            }
        }



        if (entity.getStationId() != null && !entity.getStationId().equals(Constant.EMPTY)) {
            JSONObject person = new JSONObject();
            person.set("sendType", "1");
            person.set("data", JSONUtil.toJsonStr(entity));
            appointmentFeign.issuedPeople(person);

            if (StringUtils.isNotEmpty(entity.getLicensePlate())) {
                String[] licensePlates = entity.getLicensePlate().split(",");
                for (String licensePlate : licensePlates) {
                    JSONObject vehicle = new JSONObject();
                    vehicle.set("sendType", "2");
                    entity.setLicensePlate(licensePlate);
                    vehicle.set("data", JSONUtil.toJsonStr(entity));
                    appointmentFeign.issuedPeople(vehicle);
                }
            }
        }
    }

    @Override
    public void synOrg() {
        //获取mainAccessToken
        String accessToken = mainPlatformCache.getAccessToken();
        if (!com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotEmpty(accessToken)) {//如果mainAccessToken过期的话，就重新更新mainAccessToken
            cardLogin();
            //更新accessToken
            accessToken = mainPlatformCache.getAccessToken();
        }

        String secondRequestUrl = "https://jcmdm.huashijc.com/MainPlatform/travel/administrative_organization/query_page";
        JSONObject params = new JSONObject();
        params.set("page", 1);
        params.set("pageSize", 1000000);
        params.set("status", 1);

        // 发送POST请求
        HttpResponse response = HttpUtil.createPost(secondRequestUrl)
                .header("access-token", accessToken)
                .body(params.toString())
                .execute();

        // 处理响应
        if (response.isOk()) {
            JSONObject rel1 = JSONUtil.parseObj(response.body());
            JSONObject rel2 = JSONUtil.parseObj(rel1.get("data"));
            List<OrganizationVO> organizationList = JSONUtil.toBean(rel2.get("data").toString(), new TypeReference<List<OrganizationVO>>() {
            }, true);
            // 处理解析后的数据
            for (OrganizationVO organization : organizationList) {
                SysOrgVO sysOrgEntity = new SysOrgVO();
                sysOrgEntity.setCode(organization.getCode());
                sysOrgEntity.setName(organization.getName());
                sysOrgEntity.setPcode(organization.getPcode());
                sysOrgEntity.setPname(organization.getPname());
                sysOrgEntity.setSort(1);
                sysOrgEntity.setStatus(1);
                sysOrgEntity.setProperty(Integer.parseInt(organization.getProperty() + ""));
                sysOrgEntity.setVirtualFlag(0);

                //如果存在，则需要修改组织
                SysOrgEntity byCode = sysOrgService.getByCodeNoStatus(organization.getCode());
                if (ObjectUtil.isNotNull(byCode)) {
                    sysOrgEntity.setId(byCode.getId());
                    sysOrgEntity.setStatus(byCode.getStatus());
                    sysOrgEntity.setDeleted(byCode.getDeleted());
                    SysOrgEntity entity = SysOrgConvert.INSTANCE.convert(sysOrgEntity);
                    sysOrgService.updateById(entity);

                    //修改成功后需要更换人员表中的名称
                    List<SysUserEntity> sysUserEntities = list(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getOrgId, entity.getId()));
                    if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(sysUserEntities)) {
                        for (SysUserEntity item : sysUserEntities) {
                            item.setOrgName(entity.getName());
                            updateById(item);
                        }
                    }
                } else {
                    sysOrgService.save(sysOrgEntity);
                }
            }
        } else {
            throw new ServerException("请求主数据岗位异常");
        }
    }

    @Override
    public void synUser() {
        //获取mainAccessToken
        String accessToken = mainPlatformCache.getAccessToken();
        //如果mainAccessToken过期的话，就重新更新mainAccessToken
        if (!StringUtils.isNotEmpty(accessToken)) {
            cardLogin();
            //更新accessToken
            accessToken = mainPlatformCache.getAccessToken();
        }

        String secondRequestUrl = "https://jcmdm.huashijc.com/MainPlatform/travel/employee/query_page";
        JSONObject params = new JSONObject();
        params.set("page", 1);
        params.set("pageSize", 1000000);
        params.set("status", 1);

        HttpResponse httpResponse = HttpUtil.createPost(secondRequestUrl)
                .header("access-token", accessToken)
                .body(params.toString())
                .execute();

        // 处理响应
        if (httpResponse.isOk()) {
            // 获取响应体
            String body = httpResponse.body();
            JSONObject rel1 = new JSONObject(body);
            JSONObject rel2 = JSONUtil.parseObj(rel1.get("data"));
            List<MainUserVO> mainUserVOS = JSONUtil.toBean(rel2.get("data").toString(), new TypeReference<List<MainUserVO>>() {
            }, true);
            for (MainUserVO mainUserVO : mainUserVOS) {
                //判断员工是在职状态
                if ("001".equals(mainUserVO.getRelationStatusCode()) || "002".equals(mainUserVO.getRelationStatusCode())) {
                    SysUserEntity one = baseMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUsername, mainUserVO.getPhone()));
                    if (one == null) {
                        one = baseMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getMobile, mainUserVO.getPhone()));
                    }

                    String code = "";
                    for (Map<String, String> sysRef : mainUserVO.getSysRefs()) {
                        if (!StrUtil.isEmpty(sysRef.get("isMain"))  && sysRef.get("isMain").equals("1")) {
                            code = sysRef.get("deptCode");
                            break;
                        }
                    }
                    List<SysOrgEntity> orgs = sysOrgService.list(new LambdaQueryWrapper<SysOrgEntity>().eq(SysOrgEntity::getCode, code));
                    //如果已经存在的用户，更新组织及员工编码
                    if (one != null) {
                        if (CollectionUtils.isNotEmpty(orgs)) {
                            one.setCode(mainUserVO.getCode());
                            one.setOrgId(orgs.get(0).getId());
                            one.setOrgName(orgs.get(0).getName());
                            one.setPostName(mainUserVO.getSysRefs().get(0).get("positionName"));
                            baseMapper.updateById(one);
                        }
                    } else {
                        SysUserEntity user = new SysUserEntity();
                        user.setCode(mainUserVO.getCode());
                        user.setUserType("1");
                        user.setUsername(mainUserVO.getPhone());
                        user.setRealName(mainUserVO.getName());
                        user.setPassword(passwordEncoder.encode("hxls123456"));
                        user.setMobile(mainUserVO.getPhone());
                        user.setStatus(1);
                        user.setSuperAdmin(0);

                        if (CollectionUtils.isNotEmpty(orgs)) {
                            user.setOrgId(orgs.get(0).getId());
                            user.setOrgName(orgs.get(0).getName());
                            user.setPostName(mainUserVO.getSysRefs().get(0).get("positionName"));
                        } else {
                            user.setOrgName("无组织");
                        }
                        baseMapper.insert(user);
                    }
                }

            }
        } else {
            throw new ServerException("同步主数据人员异常");
        }
    }

    /**
     * 批量修改所属站点
     *
     * @param list
     */
    @Override
    public void updateStationIdList(List<SysUserVO> list) {

        for (SysUserVO vo : list) {
//            Boolean issued = false;
            SysUserEntity entity = new SysUserEntity();
            //设置id和站点所属
            entity.setId(vo.getId());
            //查询车辆  --
            Long id = vo.getId();

            List<TVehicleEntity> carNum = tVehicleService.list(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getUserId,id));
            String join = String.join(",", carNum.stream().map(TVehicleEntity::getLicensePlate).toList());

            entity.setLicensePlate(join);
            entity.setStationId(vo.getStationId());
            entity.setAreaList(vo.getAreaList());
            //查询人员详情
            SysUserEntity byId = getById(vo.getId());

            //判断是否更换区域,删除之前得老区域，下发新站点和区域
            if (StringUtils.isNotEmpty(byId.getAreaList())){
                 String areaList = byId.getAreaList();
                 List<String> areas = JSONUtil.toList(areaList, String.class);
                 List<Long> result = areas.stream().filter(item -> item.contains("S")).map(item -> {
                     return Long.parseLong(item.substring(1));
                 }).toList();
                 if (CollectionUtils.isNotEmpty(result)){
                     for (Long stationId : result) {
                         //删除老站点ID
                         byId.setStationId(stationId);
                         deleteInfoToAgent(byId, byId);
                     }
                 }
            }
            // 更新实体
            this.updateById(entity);

            //下发
            if (StringUtils.isNotEmpty(vo.getAreaList())){

                String areaList = vo.getAreaList();
                List<String> areas = JSONUtil.toList(areaList, String.class);

                List<Long> result = areas.stream().filter(item -> item.contains("A")).map(item -> {
                    return Long.parseLong(item.substring(1));
                }).collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(result)) {
                    //获取到所有的区域
                    List<SysSiteAreaEntity> sysSiteAreaEntities = sysSiteAreaService.listByIds(result);
                    //获取到人脸设备
                    if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(sysSiteAreaEntities)) {
                        //获取到设备编码
                        List<String> deviceIds = new ArrayList<>();
                        for (SysSiteAreaEntity sysSiteAreaEntity : sysSiteAreaEntities) {
                            deviceIds.add(sysSiteAreaEntity.getFaceInCode());
                            deviceIds.add(sysSiteAreaEntity.getFaceOutCode());
                            deviceIds.add(sysSiteAreaEntity.getCarIntCode());
                            deviceIds.add(sysSiteAreaEntity.getCarOutCode());
                        }
                        //通过中间表获取设备id
                        List<SysAreacodeDeviceEntity> areacodeDeviceEntities = sysAreacodeDeviceService.list(new LambdaQueryWrapper<SysAreacodeDeviceEntity>().in(SysAreacodeDeviceEntity::getAreaDeviceCode, deviceIds));

                        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(areacodeDeviceEntities)) {
                            List<Long> ids = areacodeDeviceEntities.stream().map(SysAreacodeDeviceEntity::getDeviceId).toList();
//                        List<TDeviceManagementEntity> tDeviceManagementEntities = tDeviceManagementService.listByIds(ids);
                            //下发设备
                            JSONObject person = new JSONObject();
                            person.set("sendType", "1");
                            person.set("data", JSONUtil.toJsonStr(byId));
                            person.set("ids", JSONUtil.toJsonStr(ids));
                            appointmentFeign.issuedPeople(person);
                            if (StringUtils.isNotEmpty(entity.getLicensePlate())) {
                                String[] licensePlates = entity.getLicensePlate().split(",");
                                for (String licensePlate : licensePlates) {
                                    JSONObject vehicle = new JSONObject();
                                    vehicle.set("sendType", "2");
                                    entity.setLicensePlate(licensePlate);
                                    vehicle.set("data", JSONUtil.toJsonStr(entity));
                                    vehicle.set("ids", JSONUtil.toJsonStr(ids));
                                    appointmentFeign.issuedPeople(vehicle);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importByExcelWithPictures(String excelUrl, String hxls123456, Long orgId) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        SysOrgEntity byId = sysOrgService.getById(orgId);
        //导入时候获取的地址是相对路径 需要拼接服务器路径
        String domain = properties.getConfig().getDomain();
        String allUrl = domain + excelUrl;

        // 初始化图片容器
        HashMap<ExcelController.PicturePosition, String> pictureMap = new HashMap<>();


        disableSslVerification();
        // 下载Excel文件到本地临时文件
        File tempFile = downloadFile(allUrl);

        try (InputStream inputStream = new FileInputStream(tempFile)) {
            Workbook workbook;
            String fileFormat = allUrl.substring(allUrl.lastIndexOf('.') + 1);
            try {
                if (ExcelController.ExcelFormatEnum.XLS.getValue().equalsIgnoreCase(fileFormat)) {
                    workbook = new HSSFWorkbook(inputStream);
                } else if (ExcelController.ExcelFormatEnum.XLSX.getValue().equalsIgnoreCase(fileFormat)) {
                    workbook = new XSSFWorkbook(inputStream);
                } else {
                    throw new ServerException("Unsupported file format.");
                }

                //读取excel所有图片
                if (ExcelController.ExcelFormatEnum.XLS.getValue().equals(fileFormat)) {
                    getPicturesXLS(workbook, pictureMap);
                } else {
                    getPicturesXLSX(workbook, pictureMap);
                }

                List<SysUserGysExcelVO> transferList = new ArrayList<>();


                Sheet sheet = workbook.getSheetAt(0);
                int rows = sheet.getLastRowNum();
                for (int i = 1; i <= rows; i++) {
                    Row row = sheet.getRow(i);
                    SysUserGysExcelVO sysUserGysExcelVO = new SysUserGysExcelVO();
                    if (row.getCell(0) != null) {
                        sysUserGysExcelVO.setSupervisor(this.getCellValue(row.getCell(0)));
                    }
                    if (row.getCell(1) != null) {
                        sysUserGysExcelVO.setRealName(this.getCellValue(row.getCell(1)));
                    }
                    if (row.getCell(2) != null) {
                        sysUserGysExcelVO.setIdCard(this.getCellValue(row.getCell(2)));
                    }
                    if (row.getCell(3) != null) {
                        sysUserGysExcelVO.setMobile(this.getCellValue(row.getCell(3)));
                    }
                    if (row.getCell(4) != null) {
                        sysUserGysExcelVO.setPostName(this.getCellValue(row.getCell(4)));
                    }
                    if (row.getCell(5) != null) {
                        sysUserGysExcelVO.setAvatar(String.valueOf(pictureMap.get(ExcelController.PicturePosition.newInstance(i, 5))));
                    }
                    if (row.getCell(6) != null) {
                        sysUserGysExcelVO.setImageUrl(String.valueOf(pictureMap.get(ExcelController.PicturePosition.newInstance(i, 6))));
                    }
                    if (row.getCell(7) != null) {
                        sysUserGysExcelVO.setLicensePlate(this.getCellValue(row.getCell(7)));
                    }
                    if (row.getCell(8) != null) {
                        sysUserGysExcelVO.setCarTypeName(this.getCellValue(row.getCell(8)));
                    }
                    if (row.getCell(9) != null) {
                        sysUserGysExcelVO.setEmissionStandardName(this.getCellValue(row.getCell(9)));
                    }

                    transferList.add(sysUserGysExcelVO);
                }

                String password = passwordEncoder.encode("hxls123456");
                // 执行数据处理逻辑
                ExcelUtils.parseDict(transferList);
                List<SysUserEntity> sysUserEntities = SysUserConvert.INSTANCE.convertListEntity(transferList);

                /**
                 * 2024年5月27日
                 * 新增逻辑
                 * 一个用户可能有多个车辆，再导入的时候就是多条数据
                 * 通过当前用户手机号判断是不是已经存在user中，
                 * 如果存在
                 *  -添加对应的车辆即可
                 * 如果不存在
                 *  -添加用户，以及对应的车辆
                 * */
//                List<SysUserEntity> saveUserLists = new ArrayList<>();
                List<TVehicleEntity> saveCarLists = new ArrayList<>();
                for (int i = 0; i < sysUserEntities.size(); i++) {
                    SysUserEntity sysUserEntity = sysUserEntities.get(i);

                    // 数据规则校验
                    checkData(sysUserEntity);


                    //判断车牌号有没有，车牌号只能被创建一次
                    if (org.apache.commons.lang3.StringUtils.isNotEmpty(sysUserEntity.getLicensePlate())){
                        long valusCount = tVehicleService.count(new QueryWrapper<TVehicleEntity>()
                                .eq("license_plate", sysUserEntity.getLicensePlate())
                                .eq("deleted", 0));
                        if (valusCount > 0) {
                            throw new ServerException("车辆"+sysUserEntity.getLicensePlate()+"已存在，不能重复添加");
                        }
                        SysUserEntity oldusermobile = baseMapper.getByMobile(sysUserEntity.getMobile());
                        if (oldusermobile != null) {
                            //  如果用户已经存在，则只需要添加车辆信息即可

                            TVehicleEntity tVehicleEntity = new TVehicleEntity();
                            tVehicleEntity.setUserId(oldusermobile.getId());
                            tVehicleEntity.setLicensePlate(sysUserEntity.getLicensePlate());
                            tVehicleEntity.setImageUrl(sysUserEntity.getImageUrl());
                            tVehicleEntity.setEmissionStandard(sysUserEntity.getEmissionStandard());
                            // 设置供应商人员导入的车辆，默认是小客车
                            tVehicleEntity.setCarType("1");
                            tVehicleEntity.setDriverId(oldusermobile.getId());
                            tVehicleEntity.setDriverMobile(sysUserEntity.getMobile());
                            tVehicleEntity.setDriverName(sysUserEntity.getRealName());
                            tVehicleEntity.setSupplierId(orgId);
                            tVehicleEntity.setSupplierName(byId.getName());
                            tVehicleEntity.setCarClass("3");
                            saveCarLists.add(tVehicleEntity);
                        }else {
                            sysUserEntity.setUserType("2");
                            sysUserEntity.setPassword(password);
                            sysUserEntity.setOrgId(orgId);
                            sysUserEntity.setUsername(sysUserEntity.getMobile());
                            sysUserEntity.setOrgName(byId.getName());
                            sysUserEntity.setStatus(1);
                            sysUserEntity.setSuperAdmin(0);
                            save(sysUserEntity);

                            TVehicleEntity tVehicleEntity = new TVehicleEntity();
                            tVehicleEntity.setUserId(9999L);
                            tVehicleEntity.setLicensePlate(sysUserEntity.getLicensePlate());
                            tVehicleEntity.setImageUrl(sysUserEntity.getImageUrl());
                            tVehicleEntity.setEmissionStandard(sysUserEntity.getEmissionStandard());
                            // 设置供应商人员导入的车辆，默认是小客车
                            tVehicleEntity.setCarType("1");
                            tVehicleEntity.setDriverId(9999L);
                            tVehicleEntity.setDriverMobile(sysUserEntity.getMobile());
                            tVehicleEntity.setDriverName(sysUserEntity.getRealName());
                            tVehicleEntity.setSupplierId(orgId);
                            tVehicleEntity.setSupplierName(byId.getName());
                            tVehicleEntity.setCarClass("3");
                            saveCarLists.add(tVehicleEntity);
                        }

                    }else {
                        // 没有车牌号
                        sysUserEntity.setUserType("2");
                        sysUserEntity.setPassword(password);
                        sysUserEntity.setOrgId(orgId);
                        sysUserEntity.setUsername(sysUserEntity.getMobile());
                        sysUserEntity.setOrgName(byId.getName());
                        sysUserEntity.setStatus(1);
                        sysUserEntity.setSuperAdmin(0);
                        save(sysUserEntity);
                    }
                }
                // 用户添加完成后，需要反向给车辆表设置司机ID
                if(CollectionUtils.isNotEmpty(saveCarLists)){
                    saveCarLists.forEach(car -> {
                        String driverMobile = car.getDriverMobile();
                        SysUserEntity oldusermobile = baseMapper.getByMobile(driverMobile);
                        if (oldusermobile != null) {
                            car.setUserId(oldusermobile.getId());
                            car.setDriverId(oldusermobile.getId());
                        }

                    });
                    tVehicleService.saveBatch(saveCarLists);
                }
            } finally {
                // 清理临时文件
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } catch (IOException e) {
            throw new ServerException("Error reading Excel from URL.");
        }
    }

    private void checkData(SysUserEntity sysUserEntity) {
        String Mobile1 = sysUserEntity.getMobile();
        if (org.apache.commons.lang3.StringUtils.isEmpty(Mobile1)) {
            throw new ServerException("手机号不能为空，请调整数据");
        }

        // 手机号11位判断
        int phoneminLength = 11; // 一般手机号长度
        if (phoneminLength != Mobile1.length()) {
            throw new ServerException("手机号长度不对，请调整数据");
        }

        String realName = sysUserEntity.getRealName();
        if (org.apache.commons.lang3.StringUtils.isEmpty(realName)) {
            throw new ServerException("用户姓名不能为空，请调整数据");
        }

        String idCard = sysUserEntity.getIdCard();
        if (org.apache.commons.lang3.StringUtils.isEmpty(idCard)) {
            throw new ServerException("用户身份证号码不能为空，请调整数据");
        }

        // 身份证18位判断
        int idcardLength = 18;
        if (idcardLength != idCard.length()) {
            throw new ServerException("身份证号码长度不对，请调整数据");
        }

        String licensePlate = sysUserEntity.getLicensePlate();
        // 车牌号不能为空
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(licensePlate)) {
            // 车牌号去空格
            sysUserEntity.setLicensePlate(licensePlate.trim());

            int minLength = 6; // 一般车牌号的最小长度
            int maxLength = 10; // 一般车牌号的最大长度
            // 验证长度是否在指定范围内
            int length = licensePlate.length();
            if (length >= minLength && length <= maxLength) {
//                        System.out.println("车牌号长度符合要求。");
            } else {
                throw new ServerException("车辆" + sysUserEntity.getLicensePlate() + "车牌号不符合长度，不能添加");
            }
        }

    }

    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // Always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String fileName = "";
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10, disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1, fileUrl.length());
            }

            InputStream inputStream = httpConn.getInputStream();
            File tempFile = File.createTempFile(fileName, ".tmp");
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } else {
            throw new IOException("No file to download. Server replied HTTP code: " + responseCode);
        }
    }

    // 在下载文件方法之前添加此代码
    public static void disableSslVerification() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    /**
     * cell数据格式转换
     *
     * @param cell
     * @return
     */
    private static String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case NUMERIC: // 数字
                //如果为时间格式的内容
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    //注：format格式 yyyy-MM-dd hh:mm:ss 中小时为12小时制，若要24小时制，则把小h变为H即可，yyyy-MM-dd HH:mm:ss
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    return sdf.format(HSSFDateUtil.getJavaDate(cell.
                            getNumericCellValue()));
                } else {
                    return new DecimalFormat("0").format(cell.getNumericCellValue());
                }
            case STRING: // 字符串
                return cell.getStringCellValue();
            case BOOLEAN: // Boolean
                return cell.getBooleanCellValue() + "";
            case FORMULA: // 公式
                return cell.getCellFormula() + "";
            case BLANK: // 空值
                return "";
            case ERROR: // 故障
                return null;
            default:
                return null;
        }
    }

    /**
     * 获取Excel2003的图片
     *
     * @param workbook
     */
    private static void getPicturesXLS(Workbook workbook, HashMap<ExcelController.PicturePosition, String> pictureMap) {
        List<HSSFPictureData> pictures = (List<HSSFPictureData>) workbook.getAllPictures();
        HSSFSheet sheet = (HSSFSheet) workbook.getSheetAt(0);
        if (pictures.size() != 0) {
            for (HSSFShape shape : sheet.getDrawingPatriarch().getChildren()) {
                HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
                if (shape instanceof HSSFPicture) {
                    HSSFPicture pic = (HSSFPicture) shape;
                    int pictureIndex = pic.getPictureIndex() - 1;
                    HSSFPictureData picData = pictures.get(pictureIndex);
                    ExcelController.PicturePosition picturePosition = ExcelController.PicturePosition.newInstance(anchor.getRow1(), anchor.getCol1());
                    pictureMap.put(picturePosition, printImg(picData));
                }
            }
        }
    }

    /**
     * 获取Excel2007的图片
     *
     * @param workbook
     */
    private static void getPicturesXLSX(Workbook workbook, HashMap<ExcelController.PicturePosition, String> pictureMap) {
        XSSFSheet xssfSheet = (XSSFSheet) workbook.getSheetAt(0);
        for (POIXMLDocumentPart dr : xssfSheet.getRelations()) {
            if (dr instanceof XSSFDrawing) {
                XSSFDrawing drawing = (XSSFDrawing) dr;
                List<XSSFShape> shapes = drawing.getShapes();
                for (XSSFShape shape : shapes) {
                    XSSFPicture pic = (XSSFPicture) shape;
                    try {
                        XSSFClientAnchor anchor = pic.getPreferredSize();
                        if (anchor != null) {
                            CTMarker ctMarker = anchor.getFrom();
                            ExcelController.PicturePosition picturePosition = ExcelController.PicturePosition.newInstance(ctMarker.getRow(), ctMarker.getCol());
                            pictureMap.put(picturePosition, printImg(pic.getPictureData()));
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        }
    }

    /**
     * 保存图片并返回存储地址
     *
     * @param pic
     * @return
     */
    public static String printImg(PictureData pic) {
        try {
            String filePath = UUID.randomUUID().toString() + "." + pic.suggestFileExtension();
            byte[] data = pic.getData();
            // 将二进制数据转换为 Base64 格式
            String base64String = Base64.getEncoder().encodeToString(data);
            String faceUrl = "";
            faceUrl = BaseImageUtils.base64ToUrl(base64String, "GYS/IMAGES", "autoimport");
            return faceUrl;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 图片位置
     * 行row 列 col
     */
    @Data
    public static class PicturePosition {
        private int row;
        private int col;

        public static ExcelController.PicturePosition newInstance(int row, int col) {
            ExcelController.PicturePosition picturePosition = new ExcelController.PicturePosition();
            picturePosition.setRow(row);
            picturePosition.setCol(col);
            return picturePosition;
        }
    }

    /**
     * 枚举excel格式
     */
    public enum ExcelFormatEnum {
        XLS(0, "xls"),
        XLSX(1, "xlsx");

        private final Integer key;
        private final String value;

        ExcelFormatEnum(Integer key, String value) {
            this.key = key;
            this.value = value;
        }

        public Integer getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
