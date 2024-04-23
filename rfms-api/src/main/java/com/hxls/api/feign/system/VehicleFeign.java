package com.hxls.api.feign.system;

import com.alibaba.fastjson.JSONObject;
import com.hxls.api.feign.ServerNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ServerNames.SYSTEM_SERVER_NAME, contextId = "vehicle")
public interface VehicleFeign {

    @PostMapping(value = "api/system/queryVehicleInformationByLicensePlateNumber")
    public JSONObject queryVehicleInformationByLicensePlateNumber(@RequestParam("licensePlates") String licensePlates);

    @PostMapping(value = "api/system/checkTheTotalNumberOfRegisteredVehicles")
    public JSONObject checkTheTotalNumberOfRegisteredVehicles(@RequestParam("siteId") Long siteId);
}
