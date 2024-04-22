package com.hxls.system.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.vo.SysNoticeVO;
import com.hxls.system.query.SysNoticeQuery;
import com.hxls.system.entity.SysNoticeEntity;

import java.util.List;

/**
 * 系统消息表
 *
 * @author zhaohong
 * @since 1.0.0 2024-04-22
 */
public interface SysNoticeService extends BaseService<SysNoticeEntity> {

    PageResult<SysNoticeVO> page(SysNoticeQuery query);

    void save(SysNoticeVO vo);

    void update(SysNoticeVO vo);

    void delete(List<Long> idList);
}
