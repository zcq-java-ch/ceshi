package com.hxls.system.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.vo.SysOrgVO;

import java.util.List;

/**
 * 机构管理
 *
 * @author
 *
 */
public interface SysOrgService extends BaseService<SysOrgEntity> {

	PageResult<SysOrgVO> page(SysOrgQuery query);

	List<SysOrgVO> getList();

	void save(SysOrgVO vo);

	void update(SysOrgVO vo);

	void delete(Long id);

	/**
	 * 根据机构ID，获取子机构ID列表(包含本机构ID)
	 * @param id   机构ID
	 */
	List<Long> getSubOrgIdList(Long id);

    SysOrgEntity getByCode(String pcode);


	void updateStatus(List<SysOrgVO> list);

	/**
	 * 不关注状态的查询
	 * @param string
	 * @return
	 */
	SysOrgEntity getByCodeNoStatus(String string);

}
