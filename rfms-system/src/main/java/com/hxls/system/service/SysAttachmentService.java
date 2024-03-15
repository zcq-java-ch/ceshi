package com.hxls.system.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysAttachmentEntity;
import com.hxls.system.query.SysAttachmentQuery;
import com.hxls.system.vo.SysAttachmentVO;
import com.hxls.framework.common.utils.PageResult;

import java.util.List;

/**
 * 附件管理
 *
 * @author
 *
 */
public interface SysAttachmentService extends BaseService<SysAttachmentEntity> {

    PageResult<SysAttachmentVO> page(SysAttachmentQuery query);

    void save(SysAttachmentVO vo);

    void update(SysAttachmentVO vo);

    void delete(List<Long> idList);
}
