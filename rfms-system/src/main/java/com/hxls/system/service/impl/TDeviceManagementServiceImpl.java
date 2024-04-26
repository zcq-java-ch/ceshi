package com.hxls.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.TDeviceManagementConvert;
import com.hxls.system.dao.TDeviceManagementDao;
import com.hxls.system.entity.SysSiteAreaEntity;
import com.hxls.system.entity.TDeviceManagementEntity;
import com.hxls.system.query.TDeviceManagementQuery;
import com.hxls.system.service.SysAreacodeDeviceService;
import com.hxls.system.service.TDeviceManagementService;
import com.hxls.system.vo.TDeviceManagementVO;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    private final SysAreacodeDeviceService sysAreacodeDeviceService;

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
        wrapper.eq(TDeviceManagementEntity::getDeleted, 0);
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

    @Override
    public List<String> updateStatus(List<TDeviceManagementVO> list) {
        List<String> notobjects = new ArrayList<>();

        for (TDeviceManagementVO vo : list) {
            TDeviceManagementEntity entity = new TDeviceManagementEntity();
            entity.setId(vo.getId());
            if(vo.getStatus() != null ){
                // 查询设备是否已经绑定了通道
                Long deviceId = vo.getId();
                SysSiteAreaEntity sysSiteAreaEntity = sysAreacodeDeviceService.queryChannelByDeviceId(deviceId);
                if (ObjectUtils.isNotEmpty(sysSiteAreaEntity)){
                    entity.setStatus(vo.getStatus());
                }else {
                    String deviceName = vo.getDeviceName();
                    notobjects.add(deviceName);
                }
            }
            // 更新实体
            this.updateById(entity);
        }

        return notobjects;
    }


}
