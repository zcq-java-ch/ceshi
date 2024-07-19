package com.hxls.system.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.system.entity.SysOnlineLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author admin
* @description 针对表【sys_online_log】的数据库操作Mapper
* @createDate 2024-07-17 10:06:40
* @Entity com.hxls.system.entity.SysOnlineLog
*/
@Mapper
public interface SysOnlineLogMapper extends BaseDao<SysOnlineLog> {

}




