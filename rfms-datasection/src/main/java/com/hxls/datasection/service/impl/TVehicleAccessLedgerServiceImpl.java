package com.hxls.datasection.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.datasection.convert.TVehicleAccessLedgerConvert;
import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import com.hxls.datasection.query.TVehicleAccessLedgerQuery;
import com.hxls.datasection.vo.TVehicleAccessLedgerVO;
import com.hxls.datasection.dao.TVehicleAccessLedgerDao;
import com.hxls.datasection.service.TVehicleAccessLedgerService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 车辆进出厂展示台账
 *
 * @author zhaohong 
 * @since 1.0.0 2024-04-18
 */
@Service
@AllArgsConstructor
public class TVehicleAccessLedgerServiceImpl extends BaseServiceImpl<TVehicleAccessLedgerDao, TVehicleAccessLedgerEntity> implements TVehicleAccessLedgerService {

    @Override
    public PageResult<TVehicleAccessLedgerVO> page(TVehicleAccessLedgerQuery query) {
        IPage<TVehicleAccessLedgerEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(TVehicleAccessLedgerConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TVehicleAccessLedgerEntity> getWrapper(TVehicleAccessLedgerQuery query){
        LambdaQueryWrapper<TVehicleAccessLedgerEntity> wrapper = Wrappers.lambdaQuery();
        return wrapper;
    }

    @Override
    public void save(TVehicleAccessLedgerVO vo) {
        TVehicleAccessLedgerEntity entity = TVehicleAccessLedgerConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(TVehicleAccessLedgerVO vo) {
        TVehicleAccessLedgerEntity entity = TVehicleAccessLedgerConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

}