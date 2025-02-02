package com.hxls.api.feign.system;

import com.alibaba.fastjson.JSONObject;
import com.hxls.api.feign.ServerNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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

    @PostMapping(value = "api/system/queryUserInformationUserId")
    JSONObject queryUserInformationUserId(@RequestParam("deviceUserId") String deviceUserId);

    @PostMapping(value = "api/system/queryUserInformationLicensePlate")
    JSONObject queryUserInformationLicensePlate(@RequestParam("LicensePlate") String LicensePlate);

    @PostMapping(value = "api/system/queryNbUserIdByUserIdS")
    JSONObject queryNbUserIdByUserIdS(@RequestParam("collect") List<Long> collect);

    @PostMapping(value = "api/system/queryIsStayByUser")
    boolean queryIsStayByUser(@RequestParam("personId") Long personId,@RequestParam("siteId") Long siteId);

    @PostMapping(value = "api/system/queryUserInformationUserName")
    JSONObject queryUserInformationUserName(@RequestParam("userName") String userName);

    @GetMapping(value = "api/system/userList")
    List<String> userList(@RequestParam("userType") String userType );

}
