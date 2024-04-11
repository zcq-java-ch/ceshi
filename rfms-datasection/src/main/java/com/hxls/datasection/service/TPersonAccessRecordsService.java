package com.hxls.datasection.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import com.hxls.datasection.query.TPersonAccessRecordsQuery;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;

import java.util.List;

/**
 * 人员出入记录表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-29
 */
public interface TPersonAccessRecordsService extends BaseService<TPersonAccessRecordsEntity> {

    PageResult<TPersonAccessRecordsVO> page(TPersonAccessRecordsQuery query);

    void save(TPersonAccessRecordsVO vo);

    void update(TPersonAccessRecordsVO vo);

    void delete(List<Long> idList);

    PageResult<TPersonAccessRecordsVO> pageUnidirectionalTpersonAccessRecords(TPersonAccessRecordsQuery query);

    boolean whetherItExists(String recordsId);
}