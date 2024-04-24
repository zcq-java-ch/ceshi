package com.hxls.api.feign.system;

import com.alibaba.fastjson.JSONObject;
import com.hxls.api.feign.ServerNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ServerNames.SYSTEM_SERVER_NAME, contextId = "user")
public interface UserFeign {

    @PostMapping(value = "api/system/queryInformationOnkanbanPersonnelStation")
    public JSONObject queryInformationOnkanbanPersonnelStation(@RequestParam("siteId") Long siteId);

    @PostMapping(value = "api/system/sendSystemMessage")
    public JSONObject sendSystemMessage(@RequestParam("type") String type,@RequestParam("siteId") Long siteId);

    @PostMapping(value = "api/system/QueryNumberVehiclesAndFacesOnlineAndOffline")
    JSONObject QueryNumberVehiclesAndFacesOnlineAndOffline();

    @PostMapping(value = "api/system/queryUserInformationThroughMobilePhoneNumber")
    JSONObject queryUserInformationThroughMobilePhoneNumber(@RequestParam("telephone") String telephone);
}
