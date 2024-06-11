package com.hxls.api.feign.system;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hxls.api.feign.ServerNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = ServerNames.SYSTEM_SERVER_NAME, contextId = "vehicle")
public interface VehicleFeign {

    @PostMapping(value = "api/system/queryVehicleInformationByLicensePlateNumber")
    public JSONObject queryVehicleInformationByLicensePlateNumber(@RequestParam("licensePlates") String licensePlates);

    @PostMapping(value = "api/system/checkTheTotalNumberOfRegisteredVehicles")
    public JSONObject checkTheTotalNumberOfRegisteredVehicles(@RequestParam("siteId") Long siteId);

    /**
      * @author Mryang
      * @description 查询车牌列表关联的车的 随车清单，行驶证等图片
      * @date 10:52 2024/6/11
      * @param
      * @return
      */
    @PostMapping(value = "api/system/queryVehiclePhotosThroughLicensePlateList")
    public JSONArray queryVehiclePhotosThroughLicensePlateList(@RequestBody JSONObject plateNumberList);
}
