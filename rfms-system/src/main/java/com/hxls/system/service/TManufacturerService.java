package com.hxls.system.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.vo.TManufacturerVO;
import com.hxls.system.query.TManufacturerQuery;
import com.hxls.system.entity.TManufacturerEntity;

import java.util.List;

/**
 * 厂家管理表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-15
 */
public interface TManufacturerService extends BaseService<TManufacturerEntity> {

    PageResult<TManufacturerVO> page(TManufacturerQuery query);

    void save(TManufacturerVO vo);

    void update(TManufacturerVO vo);

    void delete(List<Long> idList);
}