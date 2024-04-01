package com.hxls.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.RandomSnowUtils;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.SysSiteAreaConvert;
import com.hxls.system.dao.SysSiteAreaDao;
import com.hxls.system.entity.SysAreacodeDeviceEntity;
import com.hxls.system.entity.SysSiteAreaEntity;
import com.hxls.system.query.SysSiteAreaQuery;
import com.hxls.system.service.SysAreacodeDeviceService;
import com.hxls.system.service.SysSiteAreaService;
import com.hxls.system.vo.SysSiteAreaVO;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 站点区域表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-04-01
 */
@Service
@AllArgsConstructor
public class SysSiteAreaServiceImpl extends BaseServiceImpl<SysSiteAreaDao, SysSiteAreaEntity> implements SysSiteAreaService {

    private final SysAreacodeDeviceService sysAreacodeDeviceService;
    @Override
    public PageResult<SysSiteAreaVO> page(SysSiteAreaQuery query) {
        IPage<SysSiteAreaEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(SysSiteAreaConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<SysSiteAreaEntity> getWrapper(SysSiteAreaQuery query){
        LambdaQueryWrapper<SysSiteAreaEntity> wrapper = Wrappers.lambdaQuery();
        return wrapper;
    }

    @Override
    public void save(SysSiteAreaVO vo) {
        vo.setCreateTime(new Date());
        SysSiteAreaEntity entity = SysSiteAreaConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(SysSiteAreaVO vo) {
        vo.setUpdateTime(new Date());
        SysSiteAreaEntity entity = SysSiteAreaConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

    @Override
    public String resetCodeAndDeleteData(String areaCode) {
        sysAreacodeDeviceService.deleteDataByCode(areaCode);
        return RandomSnowUtils.getSnowRandom();
    }

    @Override
    public void addNewDevices(List<Long> deviceIds, String areaCode) {
        if (CollectionUtils.isNotEmpty(deviceIds)) {
            for (Long deviceId : deviceIds) {
                SysAreacodeDeviceEntity entity = new SysAreacodeDeviceEntity();
                entity.setAreaDeviceCode(areaCode);
                entity.setDeviceId(deviceId);
                entity.setCreateTime(new Date());
                sysAreacodeDeviceService.save(entity);
            }
        }
    }

}