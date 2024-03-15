package com.hxls.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.TAppointmentConvert;
import com.hxls.system.entity.TAppointmentEntity;
import com.hxls.system.query.TAppointmentQuery;
import com.hxls.system.vo.TAppointmentVO;
import com.hxls.system.dao.TAppointmentDao;
import com.hxls.system.service.TAppointmentService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 预约信息表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-15
 */
@Service
@AllArgsConstructor
public class TAppointmentServiceImpl extends BaseServiceImpl<TAppointmentDao, TAppointmentEntity> implements TAppointmentService {

    @Override
    public PageResult<TAppointmentVO> page(TAppointmentQuery query) {
        IPage<TAppointmentEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(TAppointmentConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TAppointmentEntity> getWrapper(TAppointmentQuery query){
        LambdaQueryWrapper<TAppointmentEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StringUtils.isNotEmpty(query.getAppointmentType()), TAppointmentEntity::getAppointmentType, query.getAppointmentType());
        wrapper.like(StringUtils.isNotEmpty(query.getSubmitter()), TAppointmentEntity::getSubmitter, query.getSubmitter());
        wrapper.eq(query.getSiteId() != null, TAppointmentEntity::getSiteId, query.getSiteId());
        wrapper.like(StringUtils.isNotEmpty(query.getSiteName()), TAppointmentEntity::getSiteName, query.getSiteName());
        wrapper.ge(query.getStartTime() != null, TAppointmentEntity::getStartTime, query.getStartTime());
        wrapper.le(query.getEndTime() != null, TAppointmentEntity::getEndTime, query.getEndTime());
        wrapper.between(ArrayUtils.isNotEmpty(query.getReviewTime()), TAppointmentEntity::getReviewTime, ArrayUtils.isNotEmpty(query.getReviewTime()) ? query.getReviewTime()[0] : null, ArrayUtils.isNotEmpty(query.getReviewTime()) ? query.getReviewTime()[1] : null);
        wrapper.eq(StringUtils.isNotEmpty(query.getReviewResult()), TAppointmentEntity::getReviewResult, query.getReviewResult());
        wrapper.eq(StringUtils.isNotEmpty(query.getReviewStatus()), TAppointmentEntity::getReviewStatus, query.getReviewStatus());
        return wrapper;
    }

    @Override
    public void save(TAppointmentVO vo) {
        TAppointmentEntity entity = TAppointmentConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(TAppointmentVO vo) {
        TAppointmentEntity entity = TAppointmentConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

}
