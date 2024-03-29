package com.hxls.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.TDeviceManagementConvert;
import com.hxls.system.entity.TDeviceManagementEntity;
import com.hxls.system.query.TDeviceManagementQuery;
import com.hxls.system.vo.TDeviceManagementVO;
import com.hxls.system.dao.TDeviceManagementDao;
import com.hxls.system.service.TDeviceManagementService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 设备管理表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-15
 */
@Service
@AllArgsConstructor
public class TDeviceManagementServiceImpl extends BaseServiceImpl<TDeviceManagementDao, TDeviceManagementEntity> implements TDeviceManagementService {

    @Override
    public PageResult<TDeviceManagementVO> page(TDeviceManagementQuery query) {
        IPage<TDeviceManagementEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(TDeviceManagementConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TDeviceManagementEntity> getWrapper(TDeviceManagementQuery query){
        LambdaQueryWrapper<TDeviceManagementEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getSiteId() != null , TDeviceManagementEntity::getSiteId, query.getSiteId());
        wrapper.like(StringUtils.isNotEmpty(query.getDeviceName()), TDeviceManagementEntity::getDeviceName, query.getDeviceName());
        wrapper.like(StringUtils.isNotEmpty(query.getDeviceSn()), TDeviceManagementEntity::getDeviceSn, query.getDeviceSn());
        wrapper.eq(StringUtils.isNotEmpty(query.getDeviceType()), TDeviceManagementEntity::getDeviceType, query.getDeviceType());
        wrapper.eq(query.getManufacturerId() != null, TDeviceManagementEntity::getManufacturerId, query.getManufacturerId());
        wrapper.eq(query.getStatus() !=null, TDeviceManagementEntity::getStatus, query.getStatus());
        return wrapper;
    }

    @Override
    public void save(TDeviceManagementVO vo) {
        TDeviceManagementEntity entity = TDeviceManagementConvert.INSTANCE.convert(vo);
         baseMapper.insert(entity);
    }

    @Override
    public void update(TDeviceManagementVO vo) {
        TDeviceManagementEntity entity = TDeviceManagementConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }





}
