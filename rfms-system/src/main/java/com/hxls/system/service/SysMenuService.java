package com.hxls.system.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysMenuEntity;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.system.vo.SysMenuVO;

import java.util.List;
import java.util.Set;


/**
 * 菜单管理
 *
 * @author
 *
 */
public interface SysMenuService extends BaseService<SysMenuEntity> {

	void save(SysMenuVO vo);

	void update(SysMenuVO vo);

	void delete(Long id);

	/**
	 * 菜单列表
	 *
	 * @param type 菜单类型	0：全部  1：目录   2：菜单   3：按钮
	 *  @param category   菜单分类(1：web端   2：移动端)
	 */
	List<SysMenuVO> getMenuList(Integer type,Integer category);


	/**
	 * 用户菜单列表
	 *
	 * @param user  用户
	 * @param type 菜单类型
	 */
	List<SysMenuVO> getUserMenuList(UserDetail user, Integer type,Integer category);

	/**
	 * 获取子菜单的数量
	 * @param pid  父菜单ID
	 */
	Long getSubMenuCount(Long pid);

	/**
	 * 获取用户权限列表
	 */
	Set<String> getUserAuthority(UserDetail user);
}
