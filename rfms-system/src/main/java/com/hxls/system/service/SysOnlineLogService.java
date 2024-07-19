package com.hxls.system.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysOnlineLog;
import com.hxls.system.query.SysOrgQuery;


/**
* @author admin
* @description 针对表【sys_online_log】的数据库操作Service
* @createDate 2024-07-17 10:06:40
*/
public interface SysOnlineLogService extends BaseService<SysOnlineLog> {

    void save(String ip, Long siteId);

    PageResult<SysOnlineLog> pageList(SysOrgQuery query);


}
