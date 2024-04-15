package com.hxls.system.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysPostEntity;
import com.hxls.system.query.SysPostQuery;
import com.hxls.system.vo.MainPostVO;
import com.hxls.system.vo.SysPostVO;
import com.hxls.framework.common.utils.PageResult;

import java.util.List;

/**
 * 岗位管理
 *
 * @author
 *
 */
public interface SysPostService extends BaseService<SysPostEntity> {

    PageResult<SysPostVO> page(SysPostQuery query);

    List<SysPostVO> getList();

    List<String> getNameList(List<Long> idList);

    void save(SysPostVO vo);

    void update(SysPostVO vo);

    void delete(List<Long> idList);

    List<MainPostVO> queryByMainPosts(SysPostQuery query);
}
