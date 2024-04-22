package com.hxls.datasection.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.datasection.vo.TVehicleAccessLedgerVO;
import com.hxls.datasection.query.TVehicleAccessLedgerQuery;
import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import com.hxls.framework.security.user.UserDetail;

import java.util.List;

/**
 * 车辆进出厂展示台账
 *
 * @author zhaohong 
 * @since 1.0.0 2024-04-18
 */
public interface TVehicleAccessLedgerService extends BaseService<TVehicleAccessLedgerEntity> {

    PageResult<TVehicleAccessLedgerVO> page(TVehicleAccessLedgerQuery query, UserDetail baseUser);

    void save(TVehicleAccessLedgerVO vo);

    void update(TVehicleAccessLedgerVO vo);

    void delete(List<Long> idList);
}