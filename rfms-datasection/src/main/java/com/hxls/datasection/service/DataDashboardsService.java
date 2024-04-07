package com.hxls.datasection.service;

import com.alibaba.fastjson.JSONObject;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.query.TPersonAccessRecordsQuery;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;

import java.util.List;

public interface DataDashboardsService{

    JSONObject personnelInformationSection(Long stationId);

    JSONObject vehicleInformationSection(Long stationId);

    JSONObject sitePersonnelBreakdownSection(Long stationId);

    JSONObject vehicleAccessDetails(Long stationId);

    JSONObject breakdownOfExternalAppointments(Long stationId);
}