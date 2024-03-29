package com.hxls.datasection.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.datasection.convert.TPersonAccessRecordsConvert;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.query.TPersonAccessRecordsQuery;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import com.hxls.datasection.dao.TPersonAccessRecordsDao;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 人员出入记录表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-29
 */
@Service
@AllArgsConstructor
public class TPersonAccessRecordsServiceImpl extends BaseServiceImpl<TPersonAccessRecordsDao, TPersonAccessRecordsEntity> implements TPersonAccessRecordsService {

    @Override
    public PageResult<TPersonAccessRecordsVO> page(TPersonAccessRecordsQuery query) {
        IPage<TPersonAccessRecordsEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(TPersonAccessRecordsConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TPersonAccessRecordsEntity> getWrapper(TPersonAccessRecordsQuery query){
        LambdaQueryWrapper<TPersonAccessRecordsEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.isNotBlank(query.getPersonName()),TPersonAccessRecordsEntity::getPersonName, query.getPersonName());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getManufacturerId()), TPersonAccessRecordsEntity::getManufacturerId, query.getManufacturerId());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getAccessType()), TPersonAccessRecordsEntity::getAccessType, query.getAccessType());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getChannelId()), TPersonAccessRecordsEntity::getChannelId, query.getChannelId());
        wrapper.between(ObjectUtils.isNotEmpty(query.getStartRecordTime()) && ObjectUtils.isNotEmpty(query.getEndRecordTime()), TPersonAccessRecordsEntity::getRecordTime, query.getStartRecordTime(), query.getEndRecordTime());
        wrapper.eq(TPersonAccessRecordsEntity::getStatus, 1);
        wrapper.eq(TPersonAccessRecordsEntity::getDeleted, 0);
        return wrapper;
    }

    @Override
    public void save(TPersonAccessRecordsVO vo) {
        TPersonAccessRecordsEntity entity = TPersonAccessRecordsConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(TPersonAccessRecordsVO vo) {
        TPersonAccessRecordsEntity entity = TPersonAccessRecordsConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

}