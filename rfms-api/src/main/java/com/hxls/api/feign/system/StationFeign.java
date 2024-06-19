package com.hxls.api.feign.system;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hxls.api.feign.ServerNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ServerNames.SYSTEM_SERVER_NAME, contextId = "station")
public interface StationFeign {

//    @PostMapping(value = "api/system/queryAllDeviceList")
//    JSONArray queryAllDeviceList();
//
//    @PostMapping(value = "api/system/useTheAccountToQueryDeviceInformation")
//    JSONObject useTheAccountToQueryDeviceInformation(@RequestParam("agentDeviceName") String agentDeviceName);
//    @PostMapping(value = "api/system/useTheDeviceSnToQueryDeviceInformation")
//    JSONObject useTheDeviceSnToQueryDeviceInformation(@RequestParam("deviceSn") String deviceSn);
//
//    @PostMapping(value = "api/system/useTheIpaddressToQueryDeviceInformation")
//    public JSONObject useTheIpaddressToQueryDeviceInformation(@RequestParam("ipAddress") String ipAddress);
//
//    @PostMapping(value = "api/system/queryVehicleInformationByLicensePlateNumber")
//    public JSONObject queryVehicleInformationByLicensePlateNumber(@RequestParam("licensePlates") String licensePlates);
//
//    @PostMapping(value = "api/system/queryInformationOnkanbanPersonnelStation")
//    public JSONObject queryInformationOnkanbanPersonnelStation(@RequestParam("siteId") Long siteId);
//
//    @PostMapping(value = "api/system/checkTheTotalNumberOfRegisteredVehicles")
//    public JSONObject checkTheTotalNumberOfRegisteredVehicles(@RequestParam("siteId") Long siteId);
//
//    @PostMapping(value = "api/system/sendSystemMessage")
//    public JSONObject sendSystemMessage(@RequestParam("type") String type,@RequestParam("siteId") Long siteId);

    @PostMapping(value = "api/system/querySiteNumAndChannelNum")
    JSONObject querySiteNumAndChannelNum();

    @PostMapping(value = "api/system/querySiteCoordinates")
    JSONArray querySiteCoordinates();

    @PostMapping(value = "api/system/queryStationInfoByStationId")
    JSONObject queryStationInfoByStationId(@RequestParam("stationId") Long stationId);

}
