package com.hxls.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hxls.storage.config.StorageConfiguration;
import com.hxls.storage.properties.StorageProperties;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.TBannerConvert;
import com.hxls.system.entity.TBannerEntity;
import com.hxls.system.query.TBannerQuery;
import com.hxls.system.vo.TBannerVO;
import com.hxls.system.dao.TBannerDao;
import com.hxls.system.service.TBannerService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * banner管理
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-13
 */
@Service
@AllArgsConstructor
public class TBannerServiceImpl extends BaseServiceImpl<TBannerDao, TBannerEntity> implements TBannerService {


    private final StorageProperties storageProperties;

    @Override
    public PageResult<TBannerVO> page(TBannerQuery query) {
        IPage<TBannerEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        List<TBannerEntity> records = page.getRecords();
        String domain = storageProperties.getConfig().getDomain();




        return new PageResult<>(TBannerConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TBannerEntity> getWrapper(TBannerQuery query){
        LambdaQueryWrapper<TBannerEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getId() != null, TBannerEntity::getId, query.getId());
        wrapper.like(StringUtils.isNotEmpty(query.getImageUrl()), TBannerEntity::getImageUrl, query.getImageUrl());
        wrapper.eq(query.getStatus() != null, TBannerEntity::getStatus, query.getStatus());
        return wrapper;
    }

    @Override
    public void save(TBannerVO vo) {
        TBannerEntity entity = TBannerConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(TBannerVO vo) {
        TBannerEntity entity = TBannerConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

}
