package com.hxls.appointment.service.impl;

import com.hxls.appointment.convert.TAppointmentConvert;
import com.hxls.appointment.convert.TAppointmentPersonnelConvert;
import com.hxls.appointment.dao.TAppointmentPersonnelDao;
import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.entity.TAppointmentPersonnel;
import com.hxls.appointment.pojo.vo.TAppointmentPersonnelVO;
import com.hxls.appointment.service.TAppointmentPersonnelService;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【t_appointment_personnel(预约人员信息表)】的数据库操作Service实现
* @createDate 2024-03-21 16:57:24
*/
@Service
@AllArgsConstructor
public class TAppointmentPersonnelServiceImpl extends BaseServiceImpl<TAppointmentPersonnelDao, TAppointmentPersonnel>
    implements TAppointmentPersonnelService {

    @Override
    public void save(TAppointmentPersonnelVO vo) {
        //主表转换
        TAppointmentPersonnel entity = TAppointmentPersonnelConvert.INSTANCE.convert(vo);
        //插入主预约信息单
        int insert = baseMapper.insert(entity);
    }
}




