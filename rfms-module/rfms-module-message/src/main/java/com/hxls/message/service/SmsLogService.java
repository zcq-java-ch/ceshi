package com.hxls.message.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.message.entity.SmsLogEntity;
import com.hxls.message.query.SmsLogQuery;
import com.hxls.message.vo.SmsLogVO;

/**
 * 短信日志
 *
 * @author
 *
 */
public interface SmsLogService extends BaseService<SmsLogEntity> {

    PageResult<SmsLogVO> page(SmsLogQuery query);

}
