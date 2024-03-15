package com.hxls.system.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.vo.TBannerVO;
import com.hxls.system.query.TBannerQuery;
import com.hxls.system.entity.TBannerEntity;

import java.util.List;

/**
 * banner管理
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-13
 */
public interface TBannerService extends BaseService<TBannerEntity> {

    PageResult<TBannerVO> page(TBannerQuery query);

    void save(TBannerVO vo);

    void update(TBannerVO vo);

    void delete(List<Long> idList);
}