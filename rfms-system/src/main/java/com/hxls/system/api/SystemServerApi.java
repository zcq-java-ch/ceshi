package com.hxls.system.api;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.system.entity.TDeviceManagementEntity;
import com.hxls.system.service.TDeviceManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/system")
@Tag(name = "system api接口")
@AllArgsConstructor
public class SystemServerApi implements DeviceFeign {

    @Autowired
    private TDeviceManagementService tDeviceManagementService;

    @PostMapping("/queryAllDeviceList")
    @Operation(summary = "查询所有设备的ip和sn")
    public JSONObject queryAllDeviceList(){
        LambdaQueryWrapper<TDeviceManagementEntity> tDeviceManagementEntityQueryWrapper = new LambdaQueryWrapper<>();
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getStatus, 1);
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getDeleted, 0);
        List<TDeviceManagementEntity> tDeviceManagementEntities = tDeviceManagementService.list(tDeviceManagementEntityQueryWrapper);
        JSONArray returnA = new JSONArray();
        if (CollectionUtil.isNotEmpty(tDeviceManagementEntities)){
            for (int i = 0; i < tDeviceManagementEntities.size(); i++) {
                TDeviceManagementEntity tDeviceManagementEntity = tDeviceManagementEntities.get(i);
                String ipAddress = tDeviceManagementEntity.getIpAddress();
                String deviceSn = tDeviceManagementEntity.getDeviceSn();
                JSONObject jsonObject = new JSONObject();
                jsonObject.putOnce("ipAddree", ipAddress);
                jsonObject.putOnce("deviceSn", deviceSn);
                returnA.add(jsonObject);
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOnce("jsonA", returnA);
        return jsonObject;
    }


}
