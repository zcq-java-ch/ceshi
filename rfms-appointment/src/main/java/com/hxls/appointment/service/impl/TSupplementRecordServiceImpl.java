package com.hxls.appointment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.appointment.convert.TAppointmentConvert;
import com.hxls.appointment.convert.TAppointmentPersonnelConvert;
import com.hxls.appointment.convert.TRecordSupplementConvert;
import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.entity.TAppointmentPersonnel;
import com.hxls.appointment.pojo.entity.TSupplementRecord;
import com.hxls.appointment.pojo.query.TSupplementRecordQuery;
import com.hxls.appointment.pojo.vo.TAppointmentPersonnelVO;
import com.hxls.appointment.pojo.vo.TSupplementRecordVO;
import com.hxls.appointment.service.TAppointmentPersonnelService;
import com.hxls.appointment.service.TSupplementRecordService;
import com.hxls.appointment.dao.TSupplementRecordMapper;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class TSupplementRecordServiceImpl extends BaseServiceImpl<TSupplementRecordMapper, TSupplementRecord>
        implements TSupplementRecordService {


    private final TAppointmentPersonnelService tAppointmentPersonnelService;

    @Override
    public PageResult<TSupplementRecordVO> page(TSupplementRecordQuery query) {
        IPage<TSupplementRecord> page = baseMapper.selectPage(getPage(query), getWrapper(query));
        List<TSupplementRecordVO> tSupplementRecordVOS = TRecordSupplementConvert.INSTANCE.convertList(page.getRecords());
        for (TSupplementRecordVO tSupplementRecordVO : tSupplementRecordVOS) {
            Long id = tSupplementRecordVO.getId();
            List<TAppointmentPersonnel> list = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, id));
            List<TAppointmentPersonnelVO> tAppointmentPersonnelVOS = TAppointmentPersonnelConvert.INSTANCE.convertList(list);
            tSupplementRecordVO.setRemark1(tAppointmentPersonnelVOS);
        }
        return new PageResult<>(tSupplementRecordVOS, page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(TSupplementRecordVO vo) {
        //主表转换
        TSupplementRecord entity = TRecordSupplementConvert.INSTANCE.convert(vo);
        //插入
        int insert = baseMapper.insert(entity);
        if (insert > 0) {
            List<TAppointmentPersonnelVO> remark = vo.getRemark1();
            if (CollectionUtils.isNotEmpty(remark)){
                List<TAppointmentPersonnel> appointmentPersonnels = remark.stream().map(item -> {
                    TAppointmentPersonnel tAppointmentPersonnel = new TAppointmentPersonnel();
                    BeanUtil.copyProperties(item, tAppointmentPersonnel);
                    tAppointmentPersonnel.setAppointmentId(entity.getId());
                    return tAppointmentPersonnel;
                }).toList();
                tAppointmentPersonnelService.saveBatch(appointmentPersonnels);
            }
        }
    }

    @Override
    public void update(TSupplementRecordVO vo) {
        //修改主表的信息
        TSupplementRecord entity = TRecordSupplementConvert.INSTANCE.convert(vo);
        updateById(entity);

        if (vo.getPerson()) {
            Long id = vo.getId();
            tAppointmentPersonnelService.remove(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, id));
            List<TAppointmentPersonnelVO> remark = vo.getRemark1();
            if (CollectionUtils.isNotEmpty(remark)) {
                List<TAppointmentPersonnel> appointmentPersonnels = remark.stream().map(item -> {
                    TAppointmentPersonnel tAppointmentPersonnel = new TAppointmentPersonnel();
                    BeanUtil.copyProperties(item, tAppointmentPersonnel);
                    tAppointmentPersonnel.setAppointmentId(vo.getId());
                    return tAppointmentPersonnel;
                }).toList();
                tAppointmentPersonnelService.saveBatch(appointmentPersonnels);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }


    private LambdaQueryWrapper<TSupplementRecord> getWrapper(TSupplementRecordQuery query) {
        LambdaQueryWrapper<TSupplementRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getSiteId() != null, TSupplementRecord::getSiteId, query.getSiteId());
//        wrapper.ge(query.getStartTime() != null, TSupplementRecord::getStartTime, query.getStartTime());
//        wrapper.le(query.getEndTime() != null, TSupplementRecord::getEndTime, query.getEndTime());
        wrapper.between(ArrayUtils.isNotEmpty(query.getSupplementTime()), TSupplementRecord::getSupplementTime, ArrayUtils.isNotEmpty(query.getSupplementTime()) ? query.getSupplementTime()[0] : null, ArrayUtils.isNotEmpty(query.getSupplementTime()) ? query.getSupplementTime()[1] : null);
        wrapper.eq(StringUtils.isNotEmpty(query.getAccessType()), TSupplementRecord::getAccessType, query.getAccessType());
        wrapper.eq(StringUtils.isNotEmpty(query.getSupplementType()), TSupplementRecord::getSupplementType, query.getSupplementType());
        return wrapper;
    }


    @Override
    public TSupplementRecordVO getDetailById(Long id) {

        TSupplementRecord byId = getById(id);
        TSupplementRecordVO convert = TRecordSupplementConvert.INSTANCE.convert(byId);
        if (ObjectUtil.isNull(byId)) {
            throw new ServerException("查找的数据已删除，或不存在");
        }
        List<TAppointmentPersonnel> list = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, id));
        convert.setRemark1(TAppointmentPersonnelConvert.INSTANCE.convertList(list));
        return convert;

    }

}




