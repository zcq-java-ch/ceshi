package com.hxls.datasection.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
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

    }
}