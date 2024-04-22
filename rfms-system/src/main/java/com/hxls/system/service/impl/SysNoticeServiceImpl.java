package com.hxls.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.SysNoticeConvert;
import com.hxls.system.entity.SysNoticeEntity;
import com.hxls.system.query.SysNoticeQuery;
import com.hxls.system.vo.SysNoticeVO;
import com.hxls.system.dao.SysNoticeDao;
import com.hxls.system.service.SysNoticeService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统消息表
 *
 * @author zhaohong
 * @since 1.0.0 2024-04-22
 */
@Service
@AllArgsConstructor
public class SysNoticeServiceImpl extends BaseServiceImpl<SysNoticeDao, SysNoticeEntity> implements SysNoticeService {

    @Override
    public PageResult<SysNoticeVO> page(SysNoticeQuery query) {
        IPage<SysNoticeEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(SysNoticeConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<SysNoticeEntity> getWrapper(SysNoticeQuery query){
        LambdaQueryWrapper<SysNoticeEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq( query.getId() != null , SysNoticeEntity::getId, query.getId());
        wrapper.like(StringUtils.isNotEmpty(query.getNoticeTitle()), SysNoticeEntity::getNoticeTitle, query.getNoticeTitle());
        wrapper.eq(query.getReceiverId() != null , SysNoticeEntity::getReceiverId, query.getReceiverId());
        wrapper.between(ArrayUtils.isNotEmpty(query.getReadTime()), SysNoticeEntity::getReadTime, ArrayUtils.isNotEmpty(query.getReadTime()) ? query.getReadTime()[0] : null, ArrayUtils.isNotEmpty(query.getReadTime()) ? query.getReadTime()[1] : null);
        wrapper.eq(query.getStatus() != null , SysNoticeEntity::getStatus, query.getStatus());
        return wrapper;
    }

    @Override
    public void save(SysNoticeVO vo) {
        SysNoticeEntity entity = SysNoticeConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(SysNoticeVO vo) {
        SysNoticeEntity entity = SysNoticeConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

}
