package com.hxls.message.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.message.entity.SmsLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 短信日志
*
* @author
*/
@Mapper
public interface SmsLogDao extends BaseDao<SmsLogEntity> {

}
