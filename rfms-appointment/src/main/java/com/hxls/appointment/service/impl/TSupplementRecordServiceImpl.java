package com.hxls.appointment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.appointment.convert.TAppointmentConvert;
import com.hxls.appointment.convert.TRecordSupplementConvert;
import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.entity.TSupplementRecord;
import com.hxls.appointment.pojo.query.TSupplementRecordQuery;
import com.hxls.appointment.pojo.vo.TSupplementRecordVO;
import com.hxls.appointment.service.TSupplementRecordService;
import com.hxls.appointment.dao.TSupplementRecordMapper;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author admin
* @description 针对表【t_supplement_record(预约补录表)】的数据库操作Service实现
* @createDate 2024-03-26 14:47:54
*/
@Service
public class TSupplementRecordServiceImpl extends BaseServiceImpl<TSupplementRecordMapper, TSupplementRecord>
    implements TSupplementRecordService{

    @Override
    public PageResult<TSupplementRecordVO> page(TSupplementRecordQuery query) {
        IPage<TSupplementRecord> page = baseMapper.selectPage(getPage(query), getWrapper(query));
        return new PageResult<>(TRecordSupplementConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    @Override
    public void save(TSupplementRecordVO vo) {
        //主表转换
        TSupplementRecord entity = TRecordSupplementConvert.INSTANCE.convert(vo);
        //插入
        baseMapper.insert(entity);
    }

    @Override
    public void update(TSupplementRecordVO vo) {
        //修改主表的信息
        TSupplementRecord entity = TRecordSupplementConvert.INSTANCE.convert(vo);
        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

    private LambdaQueryWrapper<TSupplementRecord> getWrapper(TSupplementRecordQuery query){
        LambdaQueryWrapper<TSupplementRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getSiteId() != null, TSupplementRecord::getSiteId, query.getSiteId());
//        wrapper.ge(query.getStartTime() != null, TSupplementRecord::getStartTime, query.getStartTime());
//        wrapper.le(query.getEndTime() != null, TSupplementRecord::getEndTime, query.getEndTime());
        wrapper.between(ArrayUtils.isNotEmpty(query.getSupplementTime()), TSupplementRecord::getSupplementTime, ArrayUtils.isNotEmpty(query.getSupplementTime()) ? query.getSupplementTime()[0] : null, ArrayUtils.isNotEmpty(query.getSupplementTime()) ? query.getSupplementTime()[1] : null);
        wrapper.eq(StringUtils.isNotEmpty(query.getAccessType()), TSupplementRecord::getAccessType, query.getAccessType());
        wrapper.eq(StringUtils.isNotEmpty(query.getSupplementType()), TSupplementRecord::getSupplementType, query.getSupplementType());
        return wrapper;
    }
}




