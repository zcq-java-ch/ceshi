package com.hxls.appointment.dao;

import com.hxls.appointment.pojo.entity.TPersonAccessRecords;
import com.hxls.framework.mybatis.dao.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
* @author admin
* @description 针对表【t_person_access_records(人员出入记录表)】的数据库操作Mapper
* @createDate 2024-03-26 13:35:49
* @Entity com.hxls.appointment.pojo.entity.TPersonAccessRecords
*/
@Mapper
public interface TPersonAccessRecordsMapper extends BaseDao<TPersonAccessRecords> {

}




