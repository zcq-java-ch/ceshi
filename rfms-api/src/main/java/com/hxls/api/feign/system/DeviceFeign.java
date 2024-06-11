package com.hxls.api.feign.system;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hxls.api.feign.ServerNames;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ServerNames.SYSTEM_SERVER_NAME, contextId = "device")
public interface DeviceFeign {

    @PostMapping(value = "api/system/queryAllDeviceList")
    JSONArray queryAllDeviceList();

    @PostMapping(value = "api/system/useTheAccountToQueryDeviceInformation")
    JSONObject useTheAccountToQueryDeviceInformation(@RequestParam("agentDeviceName") String agentDeviceName);
    @PostMapping(value = "api/system/useTheDeviceSnToQueryDeviceInformation")
    JSONObject useTheDeviceSnToQueryDeviceInformation(@RequestParam("deviceSn") String deviceSn);

    @PostMapping(value = "api/system/useTheIpaddressToQueryDeviceInformation")
    public JSONObject useTheIpaddressToQueryDeviceInformation(@RequestParam("ipAddress") String ipAddress);

    @PostMapping(value = "api/system/queryTheSiteIDBySiteIP")
    public JSONObject queryTheSiteIDBySiteIP(@RequestParam("ipAddress") String ipAddress);

}
