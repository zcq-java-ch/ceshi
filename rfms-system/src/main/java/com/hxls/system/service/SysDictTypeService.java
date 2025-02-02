package com.hxls.system.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysDictTypeEntity;
import com.hxls.system.query.SysDictTypeQuery;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.system.vo.SysDictTypeVO;
import com.hxls.system.vo.SysDictVO;

import java.util.List;

/**
 * 数据字典
 *
 * @author
 *
 */
public interface SysDictTypeService extends BaseService<SysDictTypeEntity> {

    PageResult<SysDictTypeVO> page(SysDictTypeQuery query);

    void save(SysDictTypeVO vo);

    void update(SysDictTypeVO vo);

    void delete(List<Long> idList);

    /**
     * 获取动态SQL数据
     */
    List<SysDictVO.DictData> getDictSql(Long id);

    /**
     * 获取全部字典列表
     */
    List<SysDictVO> getDictList();

    /**
     * 刷新字典缓存
     */
    void refreshTransCache();

}
