package com.hxls.datasection.service;


import cn.hutool.json.JSONObject;

import java.util.List;

public interface DataDashboardsService{

    JSONObject personnelInformationSection(Long stationId);

    JSONObject vehicleInformationSection(Long stationId);

    JSONObject sitePersonnelBreakdownSection(Long stationId);

    JSONObject vehicleAccessDetails(Long stationId);

    JSONObject breakdownOfExternalAppointments(Long stationId);

    JSONObject basicInformationSection();

    JSONObject realNameInformationSection();

    JSONObject mapSection();
}