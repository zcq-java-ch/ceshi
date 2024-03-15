package com.hxls.system.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysLogOperateEntity;
import com.hxls.system.query.SysLogOperateQuery;
import com.hxls.system.vo.SysLogOperateVO;
import com.hxls.framework.common.utils.PageResult;

/**
 * 操作日志
 *
 * @author
 *
 */
public interface SysLogOperateService extends BaseService<SysLogOperateEntity> {

    PageResult<SysLogOperateVO> page(SysLogOperateQuery query);
}
