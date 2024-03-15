package com.hxls.message.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.message.entity.SmsPlatformEntity;
import com.hxls.message.query.SmsPlatformQuery;
import com.hxls.message.sms.config.SmsConfig;
import com.hxls.message.vo.SmsPlatformVO;

import java.util.List;

/**
 * 短信平台
 *
 * @author
 *
 */
public interface SmsPlatformService extends BaseService<SmsPlatformEntity> {

    PageResult<SmsPlatformVO> page(SmsPlatformQuery query);

    /**
     * 启用的短信平台列表
     */
    List<SmsConfig> listByEnable();

    void save(SmsPlatformVO vo);

    void update(SmsPlatformVO vo);

    void delete(List<Long> idList);

}
