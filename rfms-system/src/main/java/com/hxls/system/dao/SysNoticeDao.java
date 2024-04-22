package com.hxls.system.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.system.entity.SysNoticeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 系统消息表
*
* @author zhaohong
* @since 1.0.0 2024-04-22
*/
@Mapper
public interface SysNoticeDao extends BaseDao<SysNoticeEntity> {

}
