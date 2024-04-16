package com.hxls.api.feign.system;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.hxls.api.feign.ServerNames;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ServerNames.SYSTEM_SERVER_NAME)
public interface DeviceFeign {

    @PostMapping(value = "api/system/queryAllDeviceList")
    JSONArray queryAllDeviceList();

    @PostMapping(value = "api/system/useTheAccountToQueryDeviceInformation")
    JSONObject useTheAccountToQueryDeviceInformation(@RequestParam("agentDeviceName") String agentDeviceName);
    @PostMapping(value = "api/system/useTheDeviceSnToQueryDeviceInformation")
    JSONObject useTheDeviceSnToQueryDeviceInformation(@RequestParam("deviceSn") String deviceSn);

    @PostMapping(value = "api/system/useTheIpaddressToQueryDeviceInformation")
    public JSONObject useTheIpaddressToQueryDeviceInformation(@RequestParam("ipAddress") String ipAddress);

}
