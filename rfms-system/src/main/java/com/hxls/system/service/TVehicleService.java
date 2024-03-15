package com.hxls.system.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.vo.TVehicleVO;
import com.hxls.system.query.TVehicleQuery;
import com.hxls.system.entity.TVehicleEntity;

import java.util.List;

/**
 * 通用车辆管理表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-15
 */
public interface TVehicleService extends BaseService<TVehicleEntity> {

    PageResult<TVehicleVO> page(TVehicleQuery query);

    void save(TVehicleVO vo);

    void update(TVehicleVO vo);

    void delete(List<Long> idList);
}