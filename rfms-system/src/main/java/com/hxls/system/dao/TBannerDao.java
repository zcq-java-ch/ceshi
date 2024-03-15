package com.hxls.system.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.system.entity.TBannerEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* banner管理
*
* @author zhaohong 
* @since 1.0.0 2024-03-13
*/
@Mapper
public interface TBannerDao extends BaseDao<TBannerEntity> {
	
}