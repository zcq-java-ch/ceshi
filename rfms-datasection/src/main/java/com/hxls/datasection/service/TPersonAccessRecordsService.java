package com.hxls.datasection.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import com.hxls.datasection.query.TPersonAccessRecordsQuery;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.framework.security.user.UserDetail;

import java.util.List;

/**
 * 人员出入记录表
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-29
 */
public interface TPersonAccessRecordsService extends BaseService<TPersonAccessRecordsEntity> {

    PageResult<TPersonAccessRecordsVO> page(TPersonAccessRecordsQuery query, UserDetail baseUser);

    void save(TPersonAccessRecordsVO vo);

    void update(TPersonAccessRecordsVO vo);

    void delete(List<Long> idList);

    PageResult<TPersonAccessRecordsVO> pageUnidirectionalTpersonAccessRecords(TPersonAccessRecordsQuery query, UserDetail baseUser);

    boolean whetherItExists(String recordsId);

    JSONObject queryInformationOnkanbanPersonnelStation(Long stationId, List<Long> nbNumids,List<Long> pzNumIds, Integer numberOfPeopleRegistered);

    JSONArray queryTheDetailsOfSitePersonnel(Long stationId);

    /**
    * @Aouthor: Mryang
    * @Date: 2024/4/23 16:15
    * @describe: 查询所有站点的在厂人员数量和车辆数量，并将
     *              人员按照业务类型分组获取数量
     *              车辆按照车辆类型来分组
    *
    */
    JSONObject queryAllVehicleAndPersonStatistics();

   /**
   * @Aouthor: Mryang
   * @Date: 2024/4/23 16:15
   * @describe: 给传入的所有站点数据，加上每个站的在线人员数量和车辆数量
   *
   */
    JSONArray numberOfAssemblersAndVehicles(JSONArray siteCoor);
}