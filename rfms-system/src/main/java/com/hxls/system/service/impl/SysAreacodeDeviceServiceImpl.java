package com.hxls.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.SysAreacodeDeviceConvert;
import com.hxls.system.dao.SysAreacodeDeviceDao;
import com.hxls.system.dao.SysSiteAreaDao;
import com.hxls.system.dao.TDeviceManagementDao;
import com.hxls.system.entity.SysAreacodeDeviceEntity;
import com.hxls.system.entity.SysSiteAreaEntity;
import com.hxls.system.entity.TDeviceManagementEntity;
import com.hxls.system.query.SysAreacodeDeviceQuery;
import com.hxls.system.service.SysAreacodeDeviceService;
import com.hxls.system.vo.SysAreacodeDeviceVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 区域通道随机码与设备中间表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-04-01
 */
@Service
@AllArgsConstructor
public class SysAreacodeDeviceServiceImpl extends BaseServiceImpl<SysAreacodeDeviceDao, SysAreacodeDeviceEntity> implements SysAreacodeDeviceService {

    private final TDeviceManagementDao tDeviceManagementDao;

    private final SysSiteAreaDao sysSiteAreaDao;
    @Override
    public PageResult<SysAreacodeDeviceVO> page(SysAreacodeDeviceQuery query) {
        IPage<SysAreacodeDeviceEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(SysAreacodeDeviceConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<SysAreacodeDeviceEntity> getWrapper(SysAreacodeDeviceQuery query){
        LambdaQueryWrapper<SysAreacodeDeviceEntity> wrapper = Wrappers.lambdaQuery();
        return wrapper;
    }

    @Override
    public void save(SysAreacodeDeviceVO vo) {
        SysAreacodeDeviceEntity entity = SysAreacodeDeviceConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(SysAreacodeDeviceVO vo) {
        SysAreacodeDeviceEntity entity = SysAreacodeDeviceConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

    @Override
    public JSONArray queryDeviceListByCode(String areaCode) {
        LambdaQueryWrapper<SysAreacodeDeviceEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(SysAreacodeDeviceEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(SysAreacodeDeviceEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(SysAreacodeDeviceEntity::getAreaDeviceCode, areaCode);
        List<SysAreacodeDeviceEntity> sysAreacodeDeviceEntities = baseMapper.selectList(objectLambdaQueryWrapper);

        if (CollectionUtils.isNotEmpty(sysAreacodeDeviceEntities)){
            JSONArray objectsArray = new JSONArray();
            for (int i = 0; i < sysAreacodeDeviceEntities.size(); i++) {
                SysAreacodeDeviceEntity sysAreacodeDeviceEntity = sysAreacodeDeviceEntities.get(i);
                Long deviceId = sysAreacodeDeviceEntity.getDeviceId();
                TDeviceManagementEntity tDeviceManagementEntity = tDeviceManagementDao.selectById(deviceId);
                if (ObjectUtils.isNotEmpty(tDeviceManagementEntity)){
                    String deviceName = tDeviceManagementEntity.getDeviceName();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("deviceId", deviceId);
                    jsonObject.put("deviceName", deviceName);
                    objectsArray.add(jsonObject);
                }
            }
            return objectsArray;
        }else {
            return new JSONArray();
        }
    }

    @Override
    public boolean deleteDataByCode(String areaCode) {
        // 构建查询条件
        LambdaQueryWrapper<SysAreacodeDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysAreacodeDeviceEntity::getStatus, 1)
                .eq(SysAreacodeDeviceEntity::getDeleted, 0)
                .eq(SysAreacodeDeviceEntity::getAreaDeviceCode, areaCode);

        // 查询数据库
        List<SysAreacodeDeviceEntity> entities = baseMapper.selectList(queryWrapper);

        // 如果查询结果为空，则直接返回true
        if (CollectionUtils.isEmpty(entities)) {
            return true;
        }

        // 更新数据库记录
        try {
//            entities.forEach(this::updateById);
            for (int i = 0; i < entities.size(); i++) {
                SysAreacodeDeviceEntity sysAreacodeDeviceEntity = entities.get(i);
                baseMapper.deleteById(sysAreacodeDeviceEntity);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public SysSiteAreaEntity queryChannelByDeviceId(Long deviceId) {

        SysSiteAreaEntity sysSiteAreaEntity = new SysSiteAreaEntity();

        LambdaQueryWrapper<SysAreacodeDeviceEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(SysAreacodeDeviceEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(SysAreacodeDeviceEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(SysAreacodeDeviceEntity::getDeviceId, deviceId);
        List<SysAreacodeDeviceEntity> sysAreacodeDeviceEntities = baseMapper.selectList(objectLambdaQueryWrapper);

        if (CollectionUtils.isNotEmpty(sysAreacodeDeviceEntities)){
            JSONArray objectsArray = new JSONArray();
            SysAreacodeDeviceEntity sysAreacodeDeviceEntity1 = sysAreacodeDeviceEntities.get(0);
            String areaDeviceCode = sysAreacodeDeviceEntity1.getAreaDeviceCode();
            LambdaQueryWrapper<SysSiteAreaEntity> sysSiteAreaEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            sysSiteAreaEntityLambdaQueryWrapper.eq(SysSiteAreaEntity::getStatus, 1)
                    .eq(SysSiteAreaEntity::getDeleted, 0)
                    .and(wrapper -> wrapper
                            .eq(SysSiteAreaEntity::getCarIntCode, areaDeviceCode)
                            .or()
                            .eq(SysSiteAreaEntity::getCarOutCode, areaDeviceCode)
                            .or()
                            .eq(SysSiteAreaEntity::getFaceInCode, areaDeviceCode)
                            .or()
                            .eq(SysSiteAreaEntity::getFaceOutCode, areaDeviceCode)
                    );
            List<SysSiteAreaEntity> sysAreacodeDeviceEntities1 = sysSiteAreaDao.selectList(sysSiteAreaEntityLambdaQueryWrapper);

            if (CollectionUtil.isNotEmpty(sysAreacodeDeviceEntities1)){
                sysSiteAreaEntity = sysAreacodeDeviceEntities1.get(0);
            }
        }else {
        }
        return sysSiteAreaEntity;
    }
}