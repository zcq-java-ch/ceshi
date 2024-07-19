package com.hxls.datasection.service;


import com.alibaba.fastjson.JSONObject;

import java.text.ParseException;
import java.util.Map;


public interface DataDashboardsService{

    JSONObject personnelInformationSection(Long stationId);

    JSONObject vehicleInformationSection(Long stationId);

    JSONObject sitePersonnelBreakdownSection(Long stationId);

    JSONObject vehicleAccessDetails(Long stationId);

    JSONObject breakdownOfExternalAppointments(Long stationId);

    JSONObject basicInformationSection();

    JSONObject realNameInformationSection();

    JSONObject mapSection();

    JSONObject sitePersonnelBreakdownSectionTj(JSONObject jsonsite);
}