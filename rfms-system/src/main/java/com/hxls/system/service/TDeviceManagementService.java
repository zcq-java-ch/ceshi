package com.hxls.system.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.vo.TDeviceManagementVO;
import com.hxls.system.query.TDeviceManagementQuery;
import com.hxls.system.entity.TDeviceManagementEntity;

import java.util.List;

/**
 * 设备管理表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-15
 */
public interface TDeviceManagementService extends BaseService<TDeviceManagementEntity> {

    PageResult<TDeviceManagementVO> page(TDeviceManagementQuery query);

    void save(TDeviceManagementVO vo);

    void update(TDeviceManagementVO vo);

    void delete(List<Long> idList);
}