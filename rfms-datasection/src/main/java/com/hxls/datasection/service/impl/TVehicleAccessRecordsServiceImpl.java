package com.hxls.datasection.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.datasection.dao.TVehicleAccessLedgerDao;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.datasection.convert.TVehicleAccessRecordsConvert;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.query.TVehicleAccessRecordsQuery;
import com.hxls.datasection.vo.TVehicleAccessRecordsVO;
import com.hxls.datasection.dao.TVehicleAccessRecordsDao;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 车辆出入记录表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-29
 */
@Service
@AllArgsConstructor
public class TVehicleAccessRecordsServiceImpl extends BaseServiceImpl<TVehicleAccessRecordsDao, TVehicleAccessRecordsEntity> implements TVehicleAccessRecordsService {

    private final TVehicleAccessLedgerDao tVehicleAccessLedgerDao;

    private final DeviceFeign deviceFeign;
    @Override
    public PageResult<TVehicleAccessRecordsVO> page(TVehicleAccessRecordsQuery query) {
        IPage<TVehicleAccessRecordsEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(TVehicleAccessRecordsConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TVehicleAccessRecordsEntity> getWrapper(TVehicleAccessRecordsQuery query){
        LambdaQueryWrapper<TVehicleAccessRecordsEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ObjectUtils.isNotEmpty(query.getManufacturerId()), TVehicleAccessRecordsEntity::getManufacturerId, query.getManufacturerId());
        wrapper.eq(StringUtils.isNotEmpty(query.getAccessType()), TVehicleAccessRecordsEntity::getAccessType, query.getAccessType());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getChannelId()), TVehicleAccessRecordsEntity::getChannelId, query.getChannelId());
        wrapper.between(StringUtils.isNotEmpty(query.getStartRecordTime()) && StringUtils.isNotEmpty(query.getEndRecordTime()), TVehicleAccessRecordsEntity::getRecordTime, query.getStartRecordTime(), query.getEndRecordTime());
        wrapper.like(StringUtils.isNotEmpty(query.getPlateNumber()), TVehicleAccessRecordsEntity::getPlateNumber, query.getPlateNumber());
        wrapper.like(StringUtils.isNotEmpty(query.getDriverName()), TVehicleAccessRecordsEntity::getDriverName, query.getDriverName());
        return wrapper;
    }

    @Override
    public void save(TVehicleAccessRecordsVO vo) {
        TVehicleAccessRecordsEntity entity = TVehicleAccessRecordsConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(TVehicleAccessRecordsVO vo) {
        TVehicleAccessRecordsEntity entity = TVehicleAccessRecordsConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

    @Override
    public boolean whetherItExists(String recordsId) {
        LambdaQueryWrapper<TVehicleAccessRecordsEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TVehicleAccessRecordsEntity::getRecordsId, recordsId);
        List<TVehicleAccessRecordsEntity> tPersonAccessRecordsEntities = baseMapper.selectList(objectLambdaQueryWrapper);
        if (CollectionUtil.isNotEmpty(tPersonAccessRecordsEntities)){
            return true;
        }else {
            return false;
        }

    }

    @Override
    public void saveLedger(TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity) {
        if (5 == tVehicleAccessRecordsEntity.getManufacturerId()){
            // 先只处理精诚元鸿的设备

            if ("1".equals(tVehicleAccessRecordsEntity.getAccessType())){
                // 如果是入的记录 则直接插入一条
                // 先通过车牌找到对应的平台车辆信息
                String plateNumber = tVehicleAccessRecordsEntity.getPlateNumber();
                JSONObject jsonObject = deviceFeign.queryVehicleInformationByLicensePlateNumber(plateNumber);

                TVehicleAccessLedgerEntity tVehicleAccessLedgerEntity = new TVehicleAccessLedgerEntity();
                tVehicleAccessLedgerEntity.setVehicleModel(jsonObject.get("carType", String.class));
                tVehicleAccessLedgerEntity.setEmissionStandard(jsonObject.get("emissionStandard", String.class));
                tVehicleAccessLedgerEntity.setLicenseImage(jsonObject.get("licenseImage", String.class));
                tVehicleAccessLedgerEntity.setEnvirList(jsonObject.get("images", String.class));
                tVehicleAccessLedgerEntity.setFleetName(jsonObject.get("fleetName", String.class));
                tVehicleAccessLedgerEntity.setVinNumber(jsonObject.get("vinNumber", String.class));
                tVehicleAccessLedgerEntity.setEngineNumber(jsonObject.get("engineNumber", String.class));

                tVehicleAccessLedgerEntity.setSiteId(tVehicleAccessRecordsEntity.getSiteId());
                tVehicleAccessLedgerEntity.setSiteName(tVehicleAccessRecordsEntity.getSiteName());
                tVehicleAccessLedgerEntity.setPlateNumber(tVehicleAccessRecordsEntity.getPlateNumber());
                tVehicleAccessLedgerEntity.setInTime(tVehicleAccessRecordsEntity.getRecordTime());
                tVehicleAccessLedgerEntity.setInPic(tVehicleAccessRecordsEntity.getCarUrl());
                tVehicleAccessLedgerEntity.setIsOver(0);
                tVehicleAccessLedgerEntity.setCreateTime(new Date());
                tVehicleAccessLedgerDao.insert(tVehicleAccessLedgerEntity);
            }else {
                // 如果是出的，则需要找到对应的最近一条入的台账
                String plateNumber = tVehicleAccessRecordsEntity.getPlateNumber();
                LambdaQueryWrapper<TVehicleAccessLedgerEntity> tVehicleAccessLedgerEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
                tVehicleAccessLedgerEntityLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getPlateNumber, plateNumber);
                tVehicleAccessLedgerEntityLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getStatus, 1);
                tVehicleAccessLedgerEntityLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getDeleted, 0);
                tVehicleAccessLedgerEntityLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getIsOver, 0);

                tVehicleAccessLedgerEntityLambdaQueryWrapper.orderByDesc(TVehicleAccessLedgerEntity::getInTime);
                tVehicleAccessLedgerEntityLambdaQueryWrapper.last("LIMIT 1"); // 限制只查询一条数据
                List<TVehicleAccessLedgerEntity> tVehicleAccessLedgerEntities = tVehicleAccessLedgerDao.selectList(tVehicleAccessLedgerEntityLambdaQueryWrapper);

                if(CollectionUtil.isNotEmpty(tVehicleAccessLedgerEntities)){
                    TVehicleAccessLedgerEntity tVehicleAccessLedgerEntity = tVehicleAccessLedgerEntities.get(0);
                    tVehicleAccessLedgerEntity.setIsOver(1);
                    tVehicleAccessLedgerEntity.setOutPic(tVehicleAccessRecordsEntity.getCarUrl());
                    tVehicleAccessLedgerEntity.setOutTime(tVehicleAccessRecordsEntity.getRecordTime());
                    tVehicleAccessLedgerEntity.setUpdateTime(new Date());
                    tVehicleAccessLedgerDao.updateById(tVehicleAccessLedgerEntity);
                }
            }
        }

    }
}