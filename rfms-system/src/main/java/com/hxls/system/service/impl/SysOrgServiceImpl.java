package com.hxls.system.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.api.vo.TAppointmentVehicleVO;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.TreeByCodeUtils;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.system.cache.MainPlatformCache;
import com.hxls.system.convert.SysOrgConvert;
import com.hxls.system.dao.SysOrgDao;
import com.hxls.system.dao.SysRoleDataScopeDao;
import com.hxls.system.dao.SysUserDao;
import com.hxls.system.entity.*;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.service.*;
import com.hxls.system.vo.MainPostVO;
import com.hxls.system.vo.OrganizationVO;
import com.hxls.system.vo.SysOrgVO;
import com.squareup.okhttp.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 机构管理
 *
 * @author
 */
@Service
@AllArgsConstructor
public class SysOrgServiceImpl extends BaseServiceImpl<SysOrgDao, SysOrgEntity> implements SysOrgService {
    private final SysUserDao sysUserDao;
    private final SysRoleDataScopeDao sysRoleDataScopeDao;
    private final TVehicleService tVehicleService;
    private final SysDictDataService sysDictDataService;
    private final AppointmentFeign appointmentFeign;
    private final SysControlCarService sysControlCarService;

    @Override
    public PageResult<SysOrgVO> page(SysOrgQuery query) {
        IPage<SysOrgEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(SysOrgConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<SysOrgEntity> getWrapper(SysOrgQuery query) {
        LambdaQueryWrapper<SysOrgEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getId() != null, SysOrgEntity::getId, query.getId());
        wrapper.eq(SysOrgEntity::getDeleted, 0);
        wrapper.eq(query.getStatus() != null, SysOrgEntity::getStatus, query.getStatus());
        wrapper.like(StringUtils.isNotEmpty(query.getCode()), SysOrgEntity::getCode, query.getCode());

        if (StringUtils.isNotEmpty(query.getPcode())) {
            List<String> subOrgCodeList = getSubOrgCodeList(query.getPcode());
            wrapper.in(CollectionUtils.isNotEmpty(subOrgCodeList), SysOrgEntity::getCode, subOrgCodeList);
        }

        wrapper.like(StringUtils.isNotEmpty(query.getName()), SysOrgEntity::getName, query.getName());
        wrapper.eq(query.getProperty() != null, SysOrgEntity::getProperty, query.getProperty());
        if (StrUtil.isEmpty(query.getPcode())) {
            wrapper.in(CollectionUtils.isNotEmpty(query.getOrgList()), SysOrgEntity::getId, query.getOrgList());
        }

        if (query.getCreator() != null) {
            wrapper.or().eq(SysOrgEntity::getCreator, query.getCreator());
        }
        return wrapper;
    }

    private Boolean checkData(SysOrgEntity sysOrgEntity, List<SysOrgEntity> list, String pcode) {

        if (sysOrgEntity.getPcode() == null) {
            return false;
        }
        if (sysOrgEntity.getPcode().equals(pcode)) {
            return true;
        }
        for (SysOrgEntity item : list) {
            if (item.getPcode().equals(sysOrgEntity.getCode())) {
                return checkData(item, list, pcode);
            }
        }
        return false;
    }

    @Override
    public List<SysOrgVO> getList() {
        Map<String, Object> params = new HashMap<>();

        // 数据权限
        params.put(Constant.DATA_SCOPE, getDataScopeByCreator("t1", "id"));


        // 机构列表
        List<SysOrgEntity> entityList = baseMapper.getList(params);

        // 获取全部组织
        List<SysOrgEntity> list = list();

        //添加上级组织
        checkUpData(entityList, list);


        return TreeByCodeUtils.build(SysOrgConvert.INSTANCE.convertList(entityList));
    }

    /**
     * 将上级组织添加进去
     *
     * @param entityList 返回组织
     * @param list       全量组织
     */
    private void checkUpData(List<SysOrgEntity> entityList, List<SysOrgEntity> list) {

        Set<String> code = new HashSet<>();
        List<String> codes = entityList.stream().map(SysOrgEntity::getCode).collect(Collectors.toList());
        for (SysOrgEntity sysOrgEntity : entityList) {
            if (StringUtils.isNotEmpty(sysOrgEntity.getPcode()) && !codes.contains(sysOrgEntity.getPcode())) {
                code.add(sysOrgEntity.getPcode());
            }
        }

        if (CollectionUtils.isNotEmpty(code)) {
            for (SysOrgEntity sysOrgEntity : list) {
                if (code.contains(sysOrgEntity.getCode())) {
                    entityList.add(sysOrgEntity);
                }
            }
            checkUpData(entityList, list);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SysOrgVO vo) {
        SysOrgEntity entity = SysOrgConvert.INSTANCE.convert(vo);
        baseMapper.insert(entity);

        //添加组织编码(RFMS + 组织ID)
        if (StringUtils.isEmpty(entity.getCode())) {
            entity.setCode("RFMS" + entity.getId());
            updateById(entity);
        }

        //新增完组织之后，数据权限应该自动添加
        String pcode = vo.getPcode();
        SysOrgEntity byCode = getByCode(pcode);
        Long pId = byCode.getId();

        List<SysRoleDataScopeEntity> list = sysRoleDataScopeDao.selectList(new LambdaQueryWrapper<SysRoleDataScopeEntity>().eq(SysRoleDataScopeEntity::getOrgId, pId));
        if (CollectionUtils.isNotEmpty(list)) {
            List<Long> result = list.stream().map(SysRoleDataScopeEntity::getRoleId).distinct().collect(Collectors.toList());
            for (Long roleId : result) {
                SysRoleDataScopeEntity add = new SysRoleDataScopeEntity();
                add.setRoleId(roleId);
                add.setOrgId(entity.getId());
                sysRoleDataScopeDao.insert(add);
            }
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysOrgVO vo) {

        //原有组织
        SysOrgEntity byId = getById(vo.getId());

        //修改组织
        SysOrgEntity entity = SysOrgConvert.INSTANCE.convert(vo);

        // 上级机构不能为自身
        if (entity.getCode().equals(entity.getPcode())) {
            throw new ServerException("上级机构不能为自身");
        }

        // 上级机构不能为下级
        List<String> subOrgList = getSubOrgCodeList(entity.getCode());
        if (subOrgList.contains(entity.getPcode())) {
            throw new ServerException("上级机构不能为下级");
        }

        updateById(entity);

        //修改成功后需要更换人员表中的名称
        List<SysUserEntity> sysUserEntities = sysUserDao.selectList(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getOrgId, entity.getId()));
        if (CollectionUtils.isNotEmpty(sysUserEntities)) {
            for (SysUserEntity item : sysUserEntities) {
                item.setOrgName(entity.getName());
                sysUserDao.updateById(item);
            }
        }

        //如果原有组织和现有组织的父级不一样，则需要修改数据权限
        if (!byId.getPcode().equals(vo.getPcode())) {
            //修改完之后，需要删除之前的数据权限，将新的组织添加的新的数据权限下边
            Long id = vo.getId();
            sysRoleDataScopeDao.delete(new LambdaQueryWrapper<SysRoleDataScopeEntity>().eq(SysRoleDataScopeEntity::getOrgId, id));

            String pcode = vo.getPcode();
            SysOrgEntity byCode = getByCode(pcode);
            Long pId = byCode.getId();

            List<SysRoleDataScopeEntity> list = sysRoleDataScopeDao.selectList(new LambdaQueryWrapper<SysRoleDataScopeEntity>().eq(SysRoleDataScopeEntity::getOrgId, pId));
            if (CollectionUtils.isNotEmpty(list)) {
                List<Long> result = list.stream().map(SysRoleDataScopeEntity::getRoleId).distinct().collect(Collectors.toList());
                for (Long roleId : result) {
                    SysRoleDataScopeEntity add = new SysRoleDataScopeEntity();
                    add.setRoleId(roleId);
                    add.setOrgId(entity.getId());
                    sysRoleDataScopeDao.insert(add);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 判断是否有子机构
        long orgCount = count(new QueryWrapper<SysOrgEntity>().eq("pid", id));
        if (orgCount > 0) {
            throw new ServerException("请先删除子机构");
        }

        // 判断机构下面是否有用户
        long userCount = sysUserDao.selectCount(new QueryWrapper<SysUserEntity>().eq("org_id", id));
        if (userCount > 0) {
            throw new ServerException("机构下面有用户，不能删除");
        }

        // 删除
        removeById(id);
    }


    public List<String> getSubOrgCodeList(String code) {
        // 所有机构的id、pid列表
        List<SysOrgEntity> orgList = baseMapper.getIdAndPidList();

        // 递归查询所有子机构ID列表
        List<String> subIdList = new ArrayList<>();
        getTreeByCode(code, orgList, subIdList);

        // 本机构也添加进去
        subIdList.add(code);

        return subIdList;
    }

    private void getTreeByCode(String code, List<SysOrgEntity> orgList, List<String> subIdList) {
        for (SysOrgEntity org : orgList) {
            if (ObjectUtil.equals(org.getPcode(), code)) {
                getTreeByCode(org.getCode(), orgList, subIdList);
                subIdList.add(org.getCode());
            }
        }
    }


    @Override
    public List<Long> getSubOrgIdList(Long id) {

        //根据id获取机构code
        SysOrgEntity sysOrgEntity = baseMapper.selectById(id);

        // 根据code获取所有下级code
        List<String> subOrgCodeList = getSubOrgCodeList(sysOrgEntity.getCode());

        //遍历所有的code获取所有的id
        List<Long> subIdList = baseMapper.getIdsByCodes(subOrgCodeList);

        return subIdList;
    }

    private void getTree(Long id, List<SysOrgEntity> orgList, List<Long> subIdList) {
        for (SysOrgEntity org : orgList) {
            if (ObjectUtil.equals(org.getPcode(), id)) {
                getTree(org.getId(), orgList, subIdList);

                subIdList.add(org.getId());
            }
        }
    }

    @Override
    public SysOrgEntity getByCode(String pcode) {
        LambdaQueryWrapper<SysOrgEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(SysOrgEntity::getCode, pcode);
        objectLambdaQueryWrapper.eq(SysOrgEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(SysOrgEntity::getDeleted, 0);
        List<SysOrgEntity> sysOrgEntities = baseMapper.selectList(objectLambdaQueryWrapper);
        if (CollectionUtils.isNotEmpty(sysOrgEntities)) {
            return sysOrgEntities.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void updateStatus(List<SysOrgVO> list) {
        for (SysOrgVO vo : list) {
            SysOrgEntity entity = new SysOrgEntity();
            entity.setId(vo.getId());
            if (vo.getStatus() != null) {
                entity.setStatus(vo.getStatus());
                // 更新实体
                baseMapper.updateById(entity);
            }
            if (vo.getDeleted() != null) {
                // 更新实体
                baseMapper.deleteById(entity);
            }

        }
    }

    @Override
    public SysOrgEntity getByCodeNoStatus(String code) {
        LambdaQueryWrapper<SysOrgEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrgEntity::getCode, code);
        List<SysOrgEntity> sysOrgEntities = baseMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(sysOrgEntities)) {
            return sysOrgEntities.get(0);
        } else {
            return null;
        }
    }


    @Override
    public List<SysOrgVO> getOrgSiteList(UserDetail user) {
        LambdaQueryWrapper<SysOrgEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrgEntity::getProperty, 4);
        List<SysOrgEntity> list = list(wrapper);

        return SysOrgConvert.INSTANCE.convertList(list);


    }

    @Override
    public void setStation(Long id) {

        UserDetail user = SecurityUser.getUser();

        List<SysControlCar> controlCars = new ArrayList<>();

        //开始管控车辆
        //第一步 查询 所有车辆表中 权限存在这个厂站的车辆
        List<TVehicleEntity> tVehicleEntities = tVehicleService
                .list(new LambdaQueryWrapper<TVehicleEntity>().like(TVehicleEntity::getAreaList, id.toString()));

        //第一步 查询 所有车辆表中 权限存在这个厂站的车辆
        List<SysUserEntity> sysUserEntities = sysUserDao
                .selectList(new LambdaQueryWrapper<SysUserEntity>().like(SysUserEntity::getAreaList, id.toString()));

        if (CollectionUtils.isNotEmpty(sysUserEntities)) {
            List<Long> userId = sysUserEntities.stream().map(SysUserEntity::getId).toList();
            List<TVehicleEntity> newList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(tVehicleEntities)) {
                newList.addAll(tVehicleEntities);
            }
            //查询到有权限管控的车辆
            List<TVehicleEntity> tVehicleEntityList = tVehicleService.list(new LambdaQueryWrapper<TVehicleEntity>().in(TVehicleEntity::getUserId, userId));
            if (CollectionUtils.isNotEmpty(tVehicleEntityList)) {
                newList.addAll(tVehicleEntityList);
            }

            //去除掉不需要管控的车辆
            List<SysDictDataEntity> dataEntities = sysDictDataService.list(new LambdaQueryWrapper<SysDictDataEntity>().eq(SysDictDataEntity::getDictTypeId, 38));

            if (CollectionUtils.isEmpty(dataEntities)) {
                throw new ServerException("请先维护所需管控车辆标准");
            }

            List<String> dataValues = dataEntities.stream().map(SysDictDataEntity::getDictValue).toList();


            //
            List<TVehicleEntity> result = newList.stream().filter(item -> {
                return item.getCarType() != null;
            }).filter(item -> item.getCarType().equals("2") || item.getCarType().equals("3")).filter(item -> {
                return item.getEmissionStandard() != null;
            }).filter(item -> dataValues.contains(item.getEmissionStandard())).toList();


            if (CollectionUtils.isNotEmpty(result)) {
                for (TVehicleEntity tVehicleEntity : result) {
                    SysControlCar sysControlCar = new SysControlCar();
                    sysControlCar.setType(1);
                    sysControlCar.setSiteId(id);
                    sysControlCar.setLicensePlate(tVehicleEntity.getLicensePlate());
                    controlCars.add(sysControlCar);
                }
            }
        }

        //第二步 查询今天之后  所有还在预约此厂站的车辆
        List<TAppointmentVehicleVO> appointmentCar = appointmentFeign.getAppointmentCar(id);

        if (CollectionUtils.isNotEmpty(appointmentCar)) {
            for (TAppointmentVehicleVO tVehicleEntity : appointmentCar) {
                SysControlCar sysControlCar = new SysControlCar();
                sysControlCar.setType(2);
                sysControlCar.setSiteId(id);
                sysControlCar.setLicensePlate(tVehicleEntity.getPlateNumber());
                sysControlCar.setRemark(tVehicleEntity.getAppointmentId().toString());
                controlCars.add(sysControlCar);
            }
        }

        if (CollectionUtils.isNotEmpty(controlCars)) {

            sysControlCarService.saveBatch(controlCars);

            for (SysControlCar controlCar : controlCars) {
                controlCar.setStationId(controlCar.getSiteId());
                controlCar.setPersonId(user.getId());
                controlCar.setIsControl("1");
                JSONObject vehicle = new JSONObject();
                vehicle.set("sendType", "2");
                vehicle.set("data", JSONUtil.toJsonStr(controlCar));
                vehicle.set("DELETE", "DELETE");
                appointmentFeign.issuedPeople(vehicle);
            }
        }

    }

    @Override
    @Transactional
    public void offStationControl(Long id) {

        UserDetail user = SecurityUser.getUser();

        List<SysControlCar> controlCars = sysControlCarService.list(new LambdaQueryWrapper<SysControlCar>()
                .eq(SysControlCar::getSiteId, id));
        //需要下发
        if (CollectionUtils.isNotEmpty(controlCars)) {
            //按照类型分组
            Map<Integer, List<SysControlCar>> integerListMap = controlCars.stream().collect(Collectors.groupingBy(SysControlCar::getType));
            for (Integer type : integerListMap.keySet()) {
                List<SysControlCar> controlCars1 = integerListMap.get(type);
                if (type.equals(2)) {
                    //预约类型
                    for (SysControlCar controlCar : controlCars1) {
                        com.alibaba.fastjson.JSONObject jsonObject = appointmentFeign.guardInformation(Long.parseLong(controlCar.getRemark()));
                        //获取时间
                        com.alibaba.fastjson.JSONObject data = jsonObject.getJSONObject("data");
                        String deadline = data.getString("endTime");
                        String startTime = data.getString("startTime");
                        controlCar.setStartTime(startTime);
                        controlCar.setDeadline(deadline);
                        controlCar.setStationId(controlCar.getSiteId());
                        controlCar.setPersonId(user.getId());
                        controlCar.setIsControl("1");
                        JSONObject vehicle = new JSONObject();
                        vehicle.set("sendType", "2");
                        vehicle.set("personId" , user.getId());
                        vehicle.set("isControl" ,"1" );
                        vehicle.set("data", JSONUtil.toJsonStr(controlCar));
                        appointmentFeign.issuedPeople(vehicle);
                    }
                } else {
                    for (SysControlCar controlCar : controlCars1) {
                        controlCar.setStationId(controlCar.getSiteId());
                        controlCar.setPersonId(user.getId());
                        controlCar.setIsControl("1");
                        JSONObject vehicle = new JSONObject();
                        vehicle.set("sendType", "2");
                        vehicle.set("personId" , user.getId());
                        vehicle.set("isControl" ,"1" );
                        vehicle.set("data", JSONUtil.toJsonStr(controlCar));
                        appointmentFeign.issuedPeople(vehicle);
                    }
                }
            }
            sysControlCarService.removeByIds(controlCars);
        }

    }


}
