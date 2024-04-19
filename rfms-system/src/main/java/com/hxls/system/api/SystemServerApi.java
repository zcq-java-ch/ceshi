package com.hxls.system.api;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.system.controller.TVehicleController;
import com.hxls.system.entity.*;
import com.hxls.system.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import oshi.driver.mac.net.NetStat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/system")
@Tag(name = "system api接口")
@AllArgsConstructor
public class SystemServerApi implements DeviceFeign {

    @Autowired
    private TDeviceManagementService tDeviceManagementService;
    @Autowired
    protected SysAreacodeDeviceService sysAreacodeDeviceService;
    @Autowired
    private TManufacturerService tManufacturerService;
    @Autowired
    private TVehicleService tVehicleService;
    @Autowired
    private SysUserService sysUserService;
    @PostMapping("/queryAllDeviceList")
    @Operation(summary = "查询所有站点的编码和主ip")
    @Override
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

    /**
     * 准确的讲，传过来的并不是ip而是客户端的 设备名称
     * 在万众设备下 我们需要将平台的ipAddress设置为和
     * 客户端的设备名称一样，我们才能将客户端的设备与平台
     * 设置的设备相对应起来
     * */
    @PostMapping("/useTheAccountToQueryDeviceInformation")
    @Operation(summary = "useTheAccountToQueryDeviceInformation")
    @Override
    public JSONObject useTheAccountToQueryDeviceInformation(@RequestParam("agentDeviceName") String agentDeviceName){
        LambdaQueryWrapper<TDeviceManagementEntity> tDeviceManagementEntityQueryWrapper = new LambdaQueryWrapper<>();
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getStatus, 1);
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getDeleted, 0);
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getAccount, agentDeviceName);
        List<TDeviceManagementEntity> tDeviceManagementEntities = tDeviceManagementService.list(tDeviceManagementEntityQueryWrapper);
        System.out.println("当前万众客户端传过来的设备名称是："+agentDeviceName);
        System.out.println("是否在数据库中找到了对应的数据："+tDeviceManagementEntities.toString());
        JSONObject entries = new JSONObject();
        if (CollectionUtil.isNotEmpty(tDeviceManagementEntities)){
            TDeviceManagementEntity tDeviceManagementEntity = tDeviceManagementEntities.get(0);

            // 通过设备id找到通道id和通道名字
            Long channel_id = 1L;
            String channel_name = "";
            SysSiteAreaEntity sysSiteAreaEntity = sysAreacodeDeviceService.queryChannelByDeviceId(tDeviceManagementEntity.getId());
            if (ObjectUtil.isNotEmpty(sysSiteAreaEntity)){
                channel_id = sysSiteAreaEntity.getId();
                channel_name = sysSiteAreaEntity.getAreaName();
            }else {
            }
            // 获取厂商名字
            Long manufacturerId = tDeviceManagementEntity.getManufacturerId();
            TManufacturerEntity manufacturerEntity = tManufacturerService.getById(manufacturerId);
            String manufactureName = manufacturerEntity != null ? manufacturerEntity.getManufacturerName() : "";

            entries.putOnce("channel_id", channel_id);
            entries.putOnce("channel_name", channel_name);
            entries.putOnce("device_id", tDeviceManagementEntity.getId());
            entries.putOnce("device_name", tDeviceManagementEntity.getDeviceName());
            entries.putOnce("access_type", tDeviceManagementEntity.getType());
            entries.putOnce("deviceStatus", tDeviceManagementEntity.getStatus());
            entries.putOnce("manufacturer_id", tDeviceManagementEntity.getManufacturerId());
            entries.putOnce("manufacturer_name", manufactureName);
            entries.putOnce("account", tDeviceManagementEntity.getAccount());
            entries.putOnce("siteId", tDeviceManagementEntity.getSiteId());
            entries.putOnce("siteName", tDeviceManagementEntity.getSiteName());
        }
        return entries;
    }

    @PostMapping(value = "/useTheDeviceSnToQueryDeviceInformation")
    @Override
    public JSONObject useTheDeviceSnToQueryDeviceInformation(@RequestParam("deviceSn") String deviceSn){
        LambdaQueryWrapper<TDeviceManagementEntity> tDeviceManagementEntityQueryWrapper = new LambdaQueryWrapper<>();
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getStatus, 1);
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getDeleted, 0);
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getDeviceSn, deviceSn);
        List<TDeviceManagementEntity> tDeviceManagementEntities = tDeviceManagementService.list(tDeviceManagementEntityQueryWrapper);
        System.out.println("当前海康客户端传过来的设备名称是："+deviceSn);
        System.out.println("是否在数据库中找到了对应的数据："+tDeviceManagementEntities.toString());
        JSONObject entries = new JSONObject();
        if (CollectionUtil.isNotEmpty(tDeviceManagementEntities)){
            TDeviceManagementEntity tDeviceManagementEntity = tDeviceManagementEntities.get(0);

            // 通过设备id找到通道id和通道名字
            Long channel_id = 1L;
            String channel_name = "";
            SysSiteAreaEntity sysSiteAreaEntity = sysAreacodeDeviceService.queryChannelByDeviceId(tDeviceManagementEntity.getId());
            if (ObjectUtil.isNotEmpty(sysSiteAreaEntity)){
                channel_id = sysSiteAreaEntity.getId();
                channel_name = sysSiteAreaEntity.getAreaName();
            }else {
            }
            // 获取厂商名字
            Long manufacturerId = tDeviceManagementEntity.getManufacturerId();
            TManufacturerEntity manufacturerEntity = tManufacturerService.getById(manufacturerId);
            String manufactureName = manufacturerEntity != null ? manufacturerEntity.getManufacturerName() : "";

            entries.putOnce("channel_id", channel_id);
            entries.putOnce("channel_name", channel_name);
            entries.putOnce("device_id", tDeviceManagementEntity.getId());
            entries.putOnce("device_name", tDeviceManagementEntity.getDeviceName());
            entries.putOnce("access_type", tDeviceManagementEntity.getType());
            entries.putOnce("deviceStatus", tDeviceManagementEntity.getStatus());
            entries.putOnce("manufacturer_id", tDeviceManagementEntity.getManufacturerId());
            entries.putOnce("manufacturer_name", manufactureName);
            entries.putOnce("siteId", tDeviceManagementEntity.getSiteId());
            entries.putOnce("siteName", tDeviceManagementEntity.getSiteName());
        }
        return entries;
    }

    @PostMapping(value = "/useTheIpaddressToQueryDeviceInformation")
    @Override
    public JSONObject useTheIpaddressToQueryDeviceInformation(@RequestParam("ipAddress") String ipAddress){
        LambdaQueryWrapper<TDeviceManagementEntity> tDeviceManagementEntityQueryWrapper = new LambdaQueryWrapper<>();
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getStatus, 1);
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getDeleted, 0);
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getIpAddress, ipAddress);
        List<TDeviceManagementEntity> tDeviceManagementEntities = tDeviceManagementService.list(tDeviceManagementEntityQueryWrapper);
        System.out.println("当前海康人脸客户端传过来的设备名称是："+ipAddress);
        System.out.println("是否在数据库中找到了对应的数据："+tDeviceManagementEntities.toString());
        JSONObject entries = new JSONObject();
        if (CollectionUtil.isNotEmpty(tDeviceManagementEntities)){
            TDeviceManagementEntity tDeviceManagementEntity = tDeviceManagementEntities.get(0);

            // 通过设备id找到通道id和通道名字
            Long channel_id = 1L;
            String channel_name = "";
            SysSiteAreaEntity sysSiteAreaEntity = sysAreacodeDeviceService.queryChannelByDeviceId(tDeviceManagementEntity.getId());
            if (ObjectUtil.isNotEmpty(sysSiteAreaEntity)){
                channel_id = sysSiteAreaEntity.getId();
                channel_name = sysSiteAreaEntity.getAreaName();
            }else {
            }
            // 获取厂商名字
            Long manufacturerId = tDeviceManagementEntity.getManufacturerId();
            TManufacturerEntity manufacturerEntity = tManufacturerService.getById(manufacturerId);
            String manufactureName = manufacturerEntity != null ? manufacturerEntity.getManufacturerName() : "";

            entries.putOnce("channel_id", channel_id);
            entries.putOnce("channel_name", channel_name);
            entries.putOnce("device_id", tDeviceManagementEntity.getId());
            entries.putOnce("device_name", tDeviceManagementEntity.getDeviceName());
            entries.putOnce("access_type", tDeviceManagementEntity.getType());
            entries.putOnce("deviceStatus", tDeviceManagementEntity.getStatus());
            entries.putOnce("manufacturer_id", tDeviceManagementEntity.getManufacturerId());
            entries.putOnce("manufacturer_name", manufactureName);
            entries.putOnce("ipAddress", tDeviceManagementEntity.getIpAddress());
            entries.putOnce("siteId", tDeviceManagementEntity.getSiteId());
            entries.putOnce("siteName", tDeviceManagementEntity.getSiteName());
        }
        return entries;
    }

    @PostMapping(value = "/queryVehicleInformationByLicensePlateNumber")
    @Override
    public JSONObject queryVehicleInformationByLicensePlateNumber(@RequestParam("licensePlates") String licensePlates){
        LambdaQueryWrapper<TVehicleEntity> tVehicleEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        tVehicleEntityLambdaQueryWrapper.eq(TVehicleEntity::getStatus, 1);
        tVehicleEntityLambdaQueryWrapper.eq(TVehicleEntity::getDeleted, 0);
        tVehicleEntityLambdaQueryWrapper.eq(TVehicleEntity::getLicensePlate, licensePlates);
        List<TVehicleEntity> tVehicleEntities = tVehicleService.list(tVehicleEntityLambdaQueryWrapper);
        JSONObject entries = new JSONObject();
        if (CollectionUtil.isNotEmpty(tVehicleEntities)){
            TVehicleEntity tVehicleEntity = tVehicleEntities.get(0);

            entries.putOnce("carType", tVehicleEntity.getCarType());
            entries.putOnce("emissionStandard", tVehicleEntity.getEmissionStandard());
            entries.putOnce("licenseImage", tVehicleEntity.getLicenseImage());
            entries.putOnce("images", tVehicleEntity.getImages()); // 随车环报清单
            entries.putOnce("fleetName", tVehicleEntity.getFleetName());
            entries.putOnce("vinNumber", tVehicleEntity.getVinNumber());
            entries.putOnce("engineNumber", tVehicleEntity.getEngineNumber());
        }
        return entries;
    }

    /**
     * 查询厂站看板人员信息
     * */
    @PostMapping(value = "/queryInformationOnkanbanPersonnelStation")
    @Override
    public JSONObject queryInformationOnkanbanPersonnelStation(@RequestParam("siteId") Long siteId){
        LambdaQueryWrapper<SysUserEntity> sysUserEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysUserEntityLambdaQueryWrapper.eq(SysUserEntity::getStatus, 1);
        sysUserEntityLambdaQueryWrapper.eq(SysUserEntity::getDeleted, 0);
        sysUserEntityLambdaQueryWrapper.eq(SysUserEntity::getStationId, siteId);
        List<SysUserEntity> sysUserEntities = sysUserService.list(sysUserEntityLambdaQueryWrapper);
        JSONObject entries = new JSONObject();
        if (CollectionUtil.isNotEmpty(sysUserEntities)){
            // 在册人员数量=站点员工数量+派驻期内派驻该站点人数
            entries.putOnce("numberOfPeopleRegistered", sysUserEntities.size());

            // 获取在册人员所有id
            List<Long> collect = sysUserEntities.stream().map(SysUserEntity::getId).collect(Collectors.toList());
            entries.putOnce("numberOfPeopleRegisteredIdList", collect);

            // 按照 busis 字段进行分组
            Map<String, Long> typeCounts = sysUserEntities.stream()
                    .collect(Collectors.groupingBy(SysUserEntity::getBusis, Collectors.counting()));
            // 打印每个类型及其数量
            JSONObject objects = new JSONObject();
            for (Map.Entry<String, Long> entry : typeCounts.entrySet()) {
                System.out.println("类型：" + entry.getKey() + "，数量：" + entry.getValue());
                objects.putOnce(entry.getKey(), entry.getValue());
            }
            entries.putOnce("jobs", objects);

            // 按照 busis 字段进行分组
            Map<String, Long> postCounts = sysUserEntities.stream()
                    .collect(Collectors.groupingBy(SysUserEntity::getPostId, Collectors.counting()));
            // 打印每个类型及其数量
            JSONObject postobjects = new JSONObject();
            for (Map.Entry<String, Long> entry2 : postCounts.entrySet()) {
                System.out.println("岗位：" + entry2.getKey() + "，数量：" + entry2.getValue());
                postobjects.putOnce(entry2.getKey(), entry2.getValue());
            }
            entries.putOnce("postobjects", postobjects);
        }
        return entries;
    }

}
