package com.hxls.api.feign.system;

import cn.hutool.json.JSONObject;
import com.hxls.api.feign.ServerNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = ServerNames.SYSTEM_SERVER_NAME)
public interface DeviceFeign {

    @PostMapping(value = "api/system/queryAllDeviceList")
    JSONObject queryAllDeviceList();
}
