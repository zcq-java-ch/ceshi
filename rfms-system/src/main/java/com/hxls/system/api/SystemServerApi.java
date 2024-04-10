package com.hxls.system.api;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.system.entity.TDeviceManagementEntity;
import com.hxls.system.service.TDeviceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/system")
@Tag(name = "system api接口")
@AllArgsConstructor
public class SystemServerApi implements DeviceFeign {

    @Autowired
    private TDeviceManagementService tDeviceManagementService;

    @PostMapping("/queryAllDeviceList")
    @Operation(summary = "查询所有站点的编码和主ip")
    public JSONArray queryAllDeviceList(){
        LambdaQueryWrapper<TDeviceManagementEntity> tDeviceManagementEntityQueryWrapper = new LambdaQueryWrapper<>();
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getStatus, 1);
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getDeleted, 0);
        List<TDeviceManagementEntity> tDeviceManagementEntities = tDeviceManagementService.list(tDeviceManagementEntityQueryWrapper);

        // 站点编码 与 对应的主IP
        Map<String,String> siteDeviceMap = new HashMap<>();

        if (CollectionUtil.isNotEmpty(tDeviceManagementEntities)){
            for (TDeviceManagementEntity tDeviceManagementEntity : tDeviceManagementEntities) {
                String siteCode = tDeviceManagementEntity.getSiteCode();
                String ipAddress = tDeviceManagementEntity.getMasterIp();

                // 去除重复的站点编码信息
                String ipAddressDe = siteDeviceMap.getOrDefault(siteCode, ipAddress);
                siteDeviceMap.put(siteCode, ipAddressDe);
            }
        }

        // 构建最终的返回结果
        JSONArray returnA = new JSONArray();
        for (Map.Entry<String, String> entry : siteDeviceMap.entrySet()) {
            JSONObject siteDeviceGroup = new JSONObject();
            siteDeviceGroup.put("siteCode", entry.getKey());
            siteDeviceGroup.put("ipAddress", entry.getValue());
            returnA.add(siteDeviceGroup);
        }

        return returnA;
    }


}
