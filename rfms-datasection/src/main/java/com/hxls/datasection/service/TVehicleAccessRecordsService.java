package com.hxls.datasection.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.datasection.vo.TVehicleAccessRecordsVO;
import com.hxls.datasection.query.TVehicleAccessRecordsQuery;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.framework.security.user.UserDetail;

import java.util.List;

/**
 * 车辆出入记录表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-29
 */
public interface TVehicleAccessRecordsService extends BaseService<TVehicleAccessRecordsEntity> {

    PageResult<TVehicleAccessRecordsVO> page(TVehicleAccessRecordsQuery query, UserDetail baseUser);

    void save(TVehicleAccessRecordsVO vo);

    void update(TVehicleAccessRecordsVO vo);

    void delete(List<Long> idList);

    boolean whetherItExists(String recordsId);

    void saveLedger(TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity);

    JSONObject QueryRealtimeTotalAndNumberVariousClasses(Long stationId);

    JSONArray queryTheDetailsOfSiteCar(Long stationId);

    void jingchengMakeTaz(String siteId);
}
