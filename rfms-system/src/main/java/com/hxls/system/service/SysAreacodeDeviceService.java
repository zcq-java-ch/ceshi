package com.hxls.system.service;

import com.alibaba.fastjson.JSONArray;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysAreacodeDeviceEntity;
import com.hxls.system.query.SysAreacodeDeviceQuery;
import com.hxls.system.vo.SysAreacodeDeviceVO;

import java.util.List;
import java.util.Map;

/**
 * 区域通道随机码与设备中间表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-04-01
 */
public interface SysAreacodeDeviceService extends BaseService<SysAreacodeDeviceEntity> {

    PageResult<SysAreacodeDeviceVO> page(SysAreacodeDeviceQuery query);

    void save(SysAreacodeDeviceVO vo);

    void update(SysAreacodeDeviceVO vo);

    void delete(List<Long> idList);

    /**
     * 通过区域随机码查询所关联的设备
     * */
    JSONArray queryDeviceListByCode(String areaCode);

    /**
     * 通过区域随机码删除中间表数据
     * */
    boolean deleteDataByCode(String areaCode);
}