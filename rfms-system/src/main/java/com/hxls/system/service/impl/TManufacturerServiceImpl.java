package com.hxls.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hxls.framework.common.cache.RedisCache;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.TManufacturerConvert;
import com.hxls.system.entity.TManufacturerEntity;
import com.hxls.system.query.TManufacturerQuery;
import com.hxls.system.vo.TManufacturerVO;
import com.hxls.system.dao.TManufacturerDao;
import com.hxls.system.service.TManufacturerService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 厂家管理表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-15
 */
@Service
@AllArgsConstructor
public class TManufacturerServiceImpl extends BaseServiceImpl<TManufacturerDao, TManufacturerEntity> implements TManufacturerService {

    @Override
    public PageResult<TManufacturerVO> page(TManufacturerQuery query) {
        IPage<TManufacturerEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(TManufacturerConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TManufacturerEntity> getWrapper(TManufacturerQuery query){
        LambdaQueryWrapper<TManufacturerEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getId() != null, TManufacturerEntity::getId, query.getId());
        wrapper.like(StringUtils.isNotEmpty(query.getManufacturerCode()), TManufacturerEntity::getManufacturerCode, query.getManufacturerCode());
        wrapper.like(StringUtils.isNotEmpty(query.getManufacturerName()), TManufacturerEntity::getManufacturerName, query.getManufacturerName());
        wrapper.eq(query.getStatus() != null , TManufacturerEntity::getStatus, query.getStatus());
        return wrapper;
    }

    @Override
    public void save(TManufacturerVO vo) {
        TManufacturerEntity entity = TManufacturerConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(TManufacturerVO vo) {
        TManufacturerEntity entity = TManufacturerConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }



}
