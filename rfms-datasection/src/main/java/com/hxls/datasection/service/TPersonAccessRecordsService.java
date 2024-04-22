package com.hxls.datasection.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
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
}