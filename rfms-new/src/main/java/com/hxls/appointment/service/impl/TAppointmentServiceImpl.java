package com.hxls.appointment.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.appointment.convert.TAppointmentConvert;
import com.hxls.appointment.dao.TAppointmentDao;
import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.entity.TAppointmentPersonnel;
import com.hxls.appointment.pojo.entity.TAppointmentVehicle;
import com.hxls.appointment.pojo.query.TAppointmentQuery;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
import com.hxls.appointment.service.TAppointmentPersonnelService;
import com.hxls.appointment.service.TAppointmentService;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 预约信息表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-15
 */
@Service
@AllArgsConstructor
public class TAppointmentServiceImpl extends BaseServiceImpl<TAppointmentDao, TAppointmentEntity> implements TAppointmentService {


    /**
     * 注入预约人员数据层
     */
    private final TAppointmentPersonnelService tAppointmentPersonnelService;

    /**
     * 注入预约车辆数据层
     */
    private final TAppointmentVehicleServiceImpl tAppointmentVehicleService;



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

    /**
     * 插入主表单以及附属信息
     * @param vo
     */
    @Override
    public void save(TAppointmentVO vo) {
        //主表转换
        TAppointmentEntity entity = TAppointmentConvert.INSTANCE.convert(vo);
        //插入主预约信息单
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


    /**
     * 根据id获取详细表单信息
     * @param id id
     * @return
     */
    @Override
    public TAppointmentVO getDetailById(Long id) {
        //查询基础表单
        TAppointmentEntity byId = this.getById(id);
        if (ObjectUtil.isNull(byId)){
            throw new ServerException("查询得记录不存在或者已被删除");
        }

        TAppointmentVO vo = TAppointmentConvert.INSTANCE.convert(byId);

        //获取预约信息表单下面人员以及车辆的集合
        //1 人员
        List<TAppointmentPersonnel> personnelList = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId , id));

        //2 车辆
        List<TAppointmentVehicle> vehicleList = tAppointmentVehicleService.list(new LambdaQueryWrapper<TAppointmentVehicle>().eq(TAppointmentVehicle::getAppointmentId , id));

        vo.setPersonnelList(personnelList);
        vo.setVehicleList(vehicleList);

        return vo;

    }

}
