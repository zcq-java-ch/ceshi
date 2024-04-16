package com.hxls.system.service;


import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysRoleMenuEntity;

import java.util.List;


/**
 * 角色与菜单对应关系
 *
 * @author
 *
 */
public interface SysRoleMenuService extends BaseService<SysRoleMenuEntity> {

	/**
	 * 根据角色ID和菜单分类，获取菜单ID列表
	 *  @param roleId 角色ID
	 * 	@param category   菜单分类(1：web端   2：移动端)
	 */
	List<Long> getMenuIdList(Long roleId,Integer category);

	/**
	 * 保存或修改
	 * @param roleId      角色ID
	 * @param menuIdList  菜单ID列表
	 * @param category   菜单分类(1：web端   2：移动端)
	 */
	void saveOrUpdate(Long roleId, List<Long> menuIdList,Integer category);

	/**
	 * 根据角色id列表，删除角色菜单关系
	 * @param roleIdList 角色id列表
	 */
	void deleteByRoleIdList(List<Long> roleIdList);

	/**
	 * 根据菜单id，删除角色菜单关系
	 * @param menuId 菜单id
	 */
	void deleteByMenuId(Long menuId);
}
