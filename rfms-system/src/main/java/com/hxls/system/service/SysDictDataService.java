package com.hxls.system.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysDictDataEntity;
import com.hxls.system.query.SysDictDataQuery;
import com.hxls.system.vo.SysDictDataVO;
import com.hxls.framework.common.utils.PageResult;

import java.util.List;

/**
 * 数据字典
 *
 * @author
 *
 */
public interface SysDictDataService extends BaseService<SysDictDataEntity> {

    PageResult<SysDictDataVO> page(SysDictDataQuery query);

    void save(SysDictDataVO vo);

    void update(SysDictDataVO vo);

    void delete(List<Long> idList);

}
