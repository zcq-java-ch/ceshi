package com.hxls.system.dao;

import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.system.entity.SysOrgEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 机构管理
 *
 * @author
 *
 */
@Mapper
public interface SysOrgDao extends BaseDao<SysOrgEntity> {

    List<SysOrgEntity> getList(Map<String, Object> params);

    /**
     * 获取所有机构的id、pid列表
     */
    List<SysOrgEntity> getIdAndPidList();

    /**
    * @Author zhaohong
    * @Description  根据编号查询id
    * @Date 15:02 2024/4/23
    **/
    List<Long> getIdsByCodes(List<String> subOrgCodeList);

}
