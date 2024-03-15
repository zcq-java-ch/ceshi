package com.hxls.system.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysRoleEntity;
import com.hxls.system.query.SysRoleQuery;
import com.hxls.system.vo.SysRoleDataScopeVO;
import com.hxls.system.vo.SysRoleVO;
import com.hxls.framework.common.utils.PageResult;

import java.util.List;

/**
 * 角色
 *
 * @author
 *
 */
public interface SysRoleService extends BaseService<SysRoleEntity> {

	PageResult<SysRoleVO> page(SysRoleQuery query);

	List<SysRoleVO> getList(SysRoleQuery query);

	void save(SysRoleVO vo);

	void update(SysRoleVO vo);

	void dataScope(SysRoleDataScopeVO vo);

	void delete(List<Long> idList);
}
