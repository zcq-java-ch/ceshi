package com.hxls.system.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysSiteAreaEntity;
import com.hxls.system.query.SysSiteAreaQuery;
import com.hxls.system.vo.SysSiteAreaVO;

import java.util.List;

/**
 * 站点区域表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-04-01
 */
public interface SysSiteAreaService extends BaseService<SysSiteAreaEntity> {

    PageResult<SysSiteAreaVO> page(SysSiteAreaQuery query);

    void save(SysSiteAreaVO vo);

    void update(SysSiteAreaVO vo);

    void delete(List<Long> idList);

    String resetCodeAndDeleteData(String carIntCode);

    void addNewDevices(List<Long> faceInCodeAddDevices, String faceInCode);
}