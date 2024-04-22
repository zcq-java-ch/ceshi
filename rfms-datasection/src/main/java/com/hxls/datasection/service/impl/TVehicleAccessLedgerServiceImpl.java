package com.hxls.datasection.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.security.user.UserDetail;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.datasection.convert.TVehicleAccessLedgerConvert;
import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import com.hxls.datasection.query.TVehicleAccessLedgerQuery;
import com.hxls.datasection.vo.TVehicleAccessLedgerVO;
import com.hxls.datasection.dao.TVehicleAccessLedgerDao;
import com.hxls.datasection.service.TVehicleAccessLedgerService;
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
    public PageResult<TVehicleAccessLedgerVO> page(TVehicleAccessLedgerQuery query, UserDetail baseUser) {
        IPage<TVehicleAccessLedgerEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query, baseUser));

        return new PageResult<>(TVehicleAccessLedgerConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private QueryWrapper<TVehicleAccessLedgerEntity> getWrapper(TVehicleAccessLedgerQuery query, UserDetail baseUser){
        QueryWrapper<TVehicleAccessLedgerEntity> wrapper = Wrappers.query();
        wrapper.eq("status",1);
        wrapper.eq("deleted",0);
        wrapper.eq(ObjectUtil.isNotEmpty(query.getSiteId()),"site_id",query.getSiteId());
        wrapper.eq(StringUtils.isNotBlank(query.getPlateNumber()), "plate_number",query.getPlateNumber());
        wrapper.eq(StringUtils.isNotBlank(query.getVehicleModel()), "vehicle_model",query.getVehicleModel());
        wrapper.like(StringUtils.isNotBlank(query.getFleetName()), "fleet_name",query.getFleetName());
        // 检查数组是否为空，如果不为空再调用 between 方法
        if (CollectionUtils.isNotEmpty(query.getInRecordTimeArr())) {
            wrapper.between("in_time", query.getInRecordTimeArr().get(0), query.getInRecordTimeArr().get(1));
        }

        // 检查数组是否为空，如果不为空再调用 between 方法
        if (CollectionUtils.isNotEmpty(query.getOutRecordTimeArr())) {
            wrapper.between("out_time", query.getOutRecordTimeArr().get(0), query.getOutRecordTimeArr().get(1));
        }
        if (baseUser.getSuperAdmin().equals(Constant.SUPER_ADMIN)){

        }else {
            List<Long> dataScopeList = baseUser.getDataScopeList();
            wrapper.in("site_id", dataScopeList);
        }
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