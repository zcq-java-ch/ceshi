package com.hxls.system.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.vo.SysOrgVO;

import java.util.List;

/**
 * 机构管理
 *
 * @author
 *
 */
public interface SysOrgService extends BaseService<SysOrgEntity> {

	List<SysOrgVO> getList();

	void save(SysOrgVO vo);

	void update(SysOrgVO vo);

	void delete(Long id);

	/**
	* @Author zhaohong
	* @Description  同步组织结构
	* @Date 18:08 2024/3/26
	**/
	void synOrg();

	/**
	 * 根据机构ID，获取子机构ID列表(包含本机构ID)
	 * @param id   机构ID
	 */
	List<Long> getSubOrgIdList(Long id);
}
