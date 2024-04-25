package com.hxls.system.api;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxls.framework.common.cache.RedisCache;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.entity.*;
import com.hxls.system.service.*;
import com.hxls.system.vo.SysNoticeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/system")
@Tag(name = "system api接口")
@AllArgsConstructor
public class SystemServerApi {

    /**
     * javadoc
     * */
    @Autowired
    private TDeviceManagementService tDeviceManagementService;
    /**
     * javadoc
     * */
    @Autowired
    protected SysAreacodeDeviceService sysAreacodeDeviceService;
    /**
     * javadoc
     * */
    @Autowired
    private TManufacturerService tManufacturerService;
    /**
     * javadoc
     * */
    @Autowired
    private TVehicleService tVehicleService;
    /**
     * javadoc
     * */
    @Autowired
    private SysUserService sysUserService;
    /**
     * javadoc
     * */
    @Autowired
    private SysNoticeService sysNoticeService;
    /**
     * javadoc
     * */
    @Autowired
    private SysOrgService sysOrgService;
    /**
     * javadoc
     * */
    @Autowired
    private SysSiteAreaService sysSiteAreaService;

    @Resource
    private RedisCache redisCache;
    /**
     * javadoc
     * */
    @PostMapping("/queryAllDeviceList")
    @Operation(summary = "查询所有站点的编码和主ip")
    public JSONArray queryAllDeviceList() {
        LambdaQueryWrapper<TDeviceManagementEntity> tDeviceManagementEntityQueryWrapper = new LambdaQueryWrapper<>();
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getStatus, 1);
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getDeleted, 0);
        List<TDeviceManagementEntity> tDeviceManagementEntities = tDeviceManagementService.list(tDeviceManagementEntityQueryWrapper);

        // 站点编码 与 对应的主IP
        Map<String, String> siteDeviceMap = new HashMap<>();

        if (CollectionUtil.isNotEmpty(tDeviceManagementEntities)) {
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
      * &#064;author:  Mryang
      * @Description: 准确的讲，传过来的并不是ip而是客户端的 设备名称
     *                  在万众设备下 我们需要将平台的ipAddress设置为和
     *                  客户端的设备名称一样，我们才能将客户端的设备与平台
     *                  设置的设备相对应起来
      * @Date: 23:34 2024/4/22
      * @param agentDeviceName 设备关联字段
      * @return entries 消息内容
      */
    @PostMapping("/useTheAccountToQueryDeviceInformation")
    @Operation(summary = "useTheAccountToQueryDeviceInformation")
    public JSONObject useTheAccountToQueryDeviceInformation(@RequestParam("agentDeviceName") final String agentDeviceName){
        LambdaQueryWrapper<TDeviceManagementEntity> tDeviceManagementEntityQueryWrapper = new LambdaQueryWrapper<>();
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getStatus, 1);
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getDeleted, 0);
        tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getAccount, agentDeviceName);
        List<TDeviceManagementEntity> tDeviceManagementEntities = tDeviceManagementService.list(tDeviceManagementEntityQueryWrapper);
        System.out.println("当前万众客户端传过来的设备名称是：" + agentDeviceName);
        System.out.println("是否在数据库中找到了对应的数据：" + tDeviceManagementEntities.toString());
        JSONObject entries = new JSONObject();
        if (CollectionUtil.isNotEmpty(tDeviceManagementEntities)){
            TDeviceManagementEntity tDeviceManagementEntity = tDeviceManagementEntities.get(0);

            // 通过设备id找到通道id和通道名字
            Long channelId = 1L;
            String channelName = "";
            SysSiteAreaEntity sysSiteAreaEntity = sysAreacodeDeviceService.queryChannelByDeviceId(tDeviceManagementEntity.getId());
            if (ObjectUtil.isNotEmpty(sysSiteAreaEntity)){
                channelId = sysSiteAreaEntity.getId();
                channelName = sysSiteAreaEntity.getAreaName();
            }else {
            }
            // 获取厂商名字
            Long manufacturerId = tDeviceManagementEntity.getManufacturerId();
            TManufacturerEntity manufacturerEntity = tManufacturerService.getById(manufacturerId);
            String manufactureName = manufacturerEntity != null ? manufacturerEntity.getManufacturerName() : "";

            entries.put("channel_id", channelId);
            entries.put("channel_name", channelName);
            entries.put("device_id", tDeviceManagementEntity.getId());
            entries.put("device_name", tDeviceManagementEntity.getDeviceName());
            entries.put("access_type", tDeviceManagementEntity.getType());
            entries.put("deviceStatus", tDeviceManagementEntity.getStatus());
            entries.put("manufacturer_id", tDeviceManagementEntity.getManufacturerId());
            entries.put("manufacturer_name", manufactureName);
            entries.put("account", tDeviceManagementEntity.getAccount());
            entries.put("siteId", tDeviceManagementEntity.getSiteId());
            entries.put("siteName", tDeviceManagementEntity.getSiteName());
        }
        return entries;
    }

    @PostMapping(value = "/useTheDeviceSnToQueryDeviceInformation")
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

            entries.put("channel_id", channel_id);
            entries.put("channel_name", channel_name);
            entries.put("device_id", tDeviceManagementEntity.getId());
            entries.put("device_name", tDeviceManagementEntity.getDeviceName());
            entries.put("access_type", tDeviceManagementEntity.getType());
            entries.put("deviceStatus", tDeviceManagementEntity.getStatus());
            entries.put("manufacturer_id", tDeviceManagementEntity.getManufacturerId());
            entries.put("manufacturer_name", manufactureName);
            entries.put("siteId", tDeviceManagementEntity.getSiteId());
            entries.put("siteName", tDeviceManagementEntity.getSiteName());
        }
        return entries;
    }

    @PostMapping(value = "/useTheIpaddressToQueryDeviceInformation")
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

            entries.put("channel_id", channel_id);
            entries.put("channel_name", channel_name);
            entries.put("device_id", tDeviceManagementEntity.getId());
            entries.put("device_name", tDeviceManagementEntity.getDeviceName());
            entries.put("access_type", tDeviceManagementEntity.getType());
            entries.put("deviceStatus", tDeviceManagementEntity.getStatus());
            entries.put("manufacturer_id", tDeviceManagementEntity.getManufacturerId());
            entries.put("manufacturer_name", manufactureName);
            entries.put("ipAddress", tDeviceManagementEntity.getIpAddress());
            entries.put("siteId", tDeviceManagementEntity.getSiteId());
            entries.put("siteName", tDeviceManagementEntity.getSiteName());
        }
        return entries;
    }

    @PostMapping(value = "/queryVehicleInformationByLicensePlateNumber")
    public JSONObject queryVehicleInformationByLicensePlateNumber(@RequestParam("licensePlates") String licensePlates){
        LambdaQueryWrapper<TVehicleEntity> tVehicleEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        tVehicleEntityLambdaQueryWrapper.eq(TVehicleEntity::getStatus, 1);
        tVehicleEntityLambdaQueryWrapper.eq(TVehicleEntity::getDeleted, 0);
        tVehicleEntityLambdaQueryWrapper.eq(TVehicleEntity::getLicensePlate, licensePlates);
        List<TVehicleEntity> tVehicleEntities = tVehicleService.list(tVehicleEntityLambdaQueryWrapper);
        JSONObject entries = new JSONObject();
        if (CollectionUtil.isNotEmpty(tVehicleEntities)){
            TVehicleEntity tVehicleEntity = tVehicleEntities.get(0);

            entries.put("carType", tVehicleEntity.getCarType());
            entries.put("emissionStandard", tVehicleEntity.getEmissionStandard());
            entries.put("licenseImage", tVehicleEntity.getLicenseImage());
            // 随车环报清单
            entries.put("images", tVehicleEntity.getImages());
            entries.put("fleetName", tVehicleEntity.getFleetName());
            entries.put("vinNumber", tVehicleEntity.getVinNumber());
            entries.put("engineNumber", tVehicleEntity.getEngineNumber());
            entries.put("driverId", tVehicleEntity.getDriverId());
            entries.put("driverName", tVehicleEntity.getDriverName());
            entries.put("driverMobile", tVehicleEntity.getDriverMobile());
        }
        return entries;
    }

    /**
     * 查询厂站看板人员信息
     * */
    @PostMapping(value = "/queryInformationOnkanbanPersonnelStation")
    public JSONObject queryInformationOnkanbanPersonnelStation(@RequestParam("siteId") Long siteId){
        LambdaQueryWrapper<SysUserEntity> sysUserEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysUserEntityLambdaQueryWrapper.eq(SysUserEntity::getStatus, 1);
        sysUserEntityLambdaQueryWrapper.eq(SysUserEntity::getDeleted, 0);
        sysUserEntityLambdaQueryWrapper.eq(SysUserEntity::getStationId, siteId);
        List<SysUserEntity> sysUserEntities = sysUserService.list(sysUserEntityLambdaQueryWrapper);
        JSONObject entries = new JSONObject();
        if (CollectionUtil.isNotEmpty(sysUserEntities)){
            // 在册人员数量=站点员工数量+派驻期内派驻该站点人数
            entries.put("numberOfPeopleRegistered", sysUserEntities.size());

            // 获取在册人员所有id
            List<Long> collect = sysUserEntities.stream().map(SysUserEntity::getId).collect(Collectors.toList());
            entries.put("numberOfPeopleRegisteredIdList", collect);

            // 按照 busis 字段进行分组
            Map<String, Long> typeCounts = sysUserEntities.stream()
                    .collect(Collectors.groupingBy(SysUserEntity::getBusis, Collectors.counting()));
            // 打印每个类型及其数量
            JSONObject objects = new JSONObject();
            for (Map.Entry<String, Long> entry : typeCounts.entrySet()) {
                System.out.println("类型：" + entry.getKey() + "，数量：" + entry.getValue());
                objects.put(entry.getKey(), entry.getValue());
            }
            entries.put("jobs", objects);

            // 按照 busis 字段进行分组
            Map<String, Long> postCounts = sysUserEntities.stream()
                    .collect(Collectors.groupingBy(SysUserEntity::getPostId, Collectors.counting()));
            // 打印每个类型及其数量
            JSONObject postobjects = new JSONObject();
            for (Map.Entry<String, Long> entry2 : postCounts.entrySet()) {
                System.out.println("岗位：" + entry2.getKey() + "，数量：" + entry2.getValue());
                postobjects.put(entry2.getKey(), entry2.getValue());
            }
            entries.put("postobjects", postobjects);
        }
        return entries;
    }

    /**
     * @author: Mryang
     * @Description: 用于查询 通用车辆管理表中，指定站点的车辆总数
     * @Date: 2024/4/21 22:53
     * @param siteId
     * @return: JSONObject
     */
    @PostMapping(value = "/checkTheTotalNumberOfRegisteredVehicles")
    public JSONObject checkTheTotalNumberOfRegisteredVehicles(@RequestParam("siteId") Long siteId){
        LambdaQueryWrapper<TVehicleEntity> tVehicleEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        tVehicleEntityLambdaQueryWrapper.eq(TVehicleEntity::getStatus, 1);
        tVehicleEntityLambdaQueryWrapper.eq(TVehicleEntity::getDeleted, 0);
        tVehicleEntityLambdaQueryWrapper.eq(TVehicleEntity::getSiteId, siteId);
        List<TVehicleEntity> tVehicleEntities = tVehicleService.list(tVehicleEntityLambdaQueryWrapper);
        JSONObject entries = new JSONObject();
        if (CollectionUtil.isNotEmpty(tVehicleEntities)){
            // 在册人员数量=站点员工数量+派驻期内派驻该站点人数
            entries.put("siteCarNumberTotal", tVehicleEntities.size());

        }
        return entries;
    }


    /**
     * @param type   预约类型
     * @param siteId 需要通知的站点（通过站点找到站点管理员）
     * @author: zhaohong
     * @Description: 根据站点和预约类型进行系统消息通知
     * @Date: 2024年4月22日17:38:32
     * @return: JSONObject
     */
    @PostMapping(value = "/sendSystemMessage")
    public cn.hutool.json.JSONObject sendSystemMessage(@RequestParam("type") String type, @RequestParam("siteId") Long siteId){
        //根据站点id获取站点管理员列表
        SysOrgEntity byId = sysOrgService.getById(siteId);
        //判断是否设置站点管理
        String siteAdminIds = byId.getSiteAdminIds();
        List<Long> adminIds = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(siteAdminIds);

        while (matcher.find()) {
            adminIds.add(Long.parseLong(matcher.group()));
        }

        if (adminIds != null && !adminIds.isEmpty()) {
            for (Long id : adminIds) {
                // 通知每一个站点管理员
                SysNoticeVO sysNoticeVO = new SysNoticeVO();
                sysNoticeVO.setStatus(0);
                sysNoticeVO.setReceiverId(id);
                sysNoticeVO.setNoticeTitle(type);
                sysNoticeService.save(sysNoticeVO);
            }
        }
        return new cn.hutool.json.JSONObject(Result.ok());
    }

    /**
      * @author: Mryang
      * @Description: 其他服务调用接口
     *                  查询站点数量，车辆通道数据量，人员通道数量
      * @Date: 22:57 2024/4/22
      * @Param:
      * @return:
      */
    @SuppressWarnings({"checkstyle:FinalParameters", "checkstyle:MagicNumber", "checkstyle:Indentation"})
    @PostMapping(value = "/querySiteNumAndChannelNum")
    public JSONObject querySiteNumAndChannelNum() {
        LambdaQueryWrapper<SysOrgEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(SysOrgEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(SysOrgEntity::getDeleted, 0);
        // 只查询组织类型为站点的
        objectLambdaQueryWrapper.eq(SysOrgEntity::getProperty, 3);
        List<SysOrgEntity> sysOrgEntities = sysOrgService.list(objectLambdaQueryWrapper);

        LambdaQueryWrapper<SysSiteAreaEntity> objectLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper1.eq(SysSiteAreaEntity::getStatus, 1);
        objectLambdaQueryWrapper1.eq(SysSiteAreaEntity::getDeleted, 0);
        List<SysSiteAreaEntity> areaEntities = sysSiteAreaService.list(objectLambdaQueryWrapper1);

        int faceChannelNum = 0;
        int carChannelNum = 0;
        for (int i = 0; i < areaEntities.size(); i++) {
            SysSiteAreaEntity sysSiteAreaEntity = areaEntities.get(i);
            if (StringUtils.isNotBlank(sysSiteAreaEntity.getFaceInCode()) && StringUtils.isNotBlank(sysSiteAreaEntity.getFaceOutCode())) {
                // 如果人脸进出设备不为空，那么人脸通道数量+1
                faceChannelNum += 1;
            }

            if (StringUtils.isNotBlank(sysSiteAreaEntity.getCarIntCode()) && StringUtils.isNotBlank(sysSiteAreaEntity.getCarOutCode())) {
                // 如果人脸进出设备不为空，那么人脸通道数量+1
                carChannelNum += 1;
            }

        }

        JSONObject entries = new JSONObject();
        entries.put("numberOfSites", sysOrgEntities.size());
        entries.put("vehicularAccess", faceChannelNum);
        entries.put("personnelAccess", carChannelNum);
        return entries;
    }

    /**
      * @author Mryang
      * @description 查询车牌设备和人脸设备 在线离线情况
      * @date 14:30 2024/4/23
      * @param 无
      * @return JSONObject
      */
    @PostMapping(value = "/QueryNumberVehiclesAndFacesOnlineAndOffline")
    public JSONObject QueryNumberVehiclesAndFacesOnlineAndOffline() {
        LambdaQueryWrapper<TDeviceManagementEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TDeviceManagementEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(TDeviceManagementEntity::getDeleted, 0);
        List<TDeviceManagementEntity> deviceManagementEntityList = tDeviceManagementService.list(objectLambdaQueryWrapper);
        List<TDeviceManagementEntity> facecollect = deviceManagementEntityList.stream()
                .filter(tDeviceManagementEntity -> "1".equals(tDeviceManagementEntity.getDeviceType()))
                .toList();
        List<TDeviceManagementEntity> carcollect = deviceManagementEntityList.stream()
                .filter(tDeviceManagementEntity -> "2".equals(tDeviceManagementEntity.getDeviceType()))
                .toList();

        int licensePlateRecognitionOnline = 0;
        int licensePlateRecognitionOffline = 0;
        int faceRecognitionOnline = 0;
        int faceRecognitionOffline = 0;

        Set<String> facekeys = redisCache.keys("DEVICES_STATUS:FACE:*");
        Set<String> carkeys = redisCache.keys("DEVICES_STATUS:CAR:*");
        licensePlateRecognitionOnline = carkeys.size();
        faceRecognitionOnline = facekeys.size();
        licensePlateRecognitionOffline = carcollect.size() - licensePlateRecognitionOnline;
        faceRecognitionOffline = facecollect.size() - faceRecognitionOnline;

        JSONObject entries = new JSONObject();
        entries.put("licensePlateRecognitionOnline", licensePlateRecognitionOnline);
        entries.put("licensePlateRecognitionOffline", licensePlateRecognitionOffline);
        entries.put("faceRecognitionOnline", faceRecognitionOnline);
        entries.put("faceRecognitionOffline", faceRecognitionOffline);
        return entries;
    }

    /**
      * @author Mryang
      * @description 查询全部站点的名字与id与经纬度
      * @date 16:02 2024/4/23
      * @param
      * @return
      */
    @PostMapping(value = "/querySiteCoordinates")
    public JSONArray querySiteCoordinates() {
        JSONArray objects = new JSONArray();

        LambdaQueryWrapper<SysOrgEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(SysOrgEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(SysOrgEntity::getDeleted, 0);
        // 只查询组织类型为站点的
        objectLambdaQueryWrapper.eq(SysOrgEntity::getProperty, 4);
        List<SysOrgEntity> sysOrgEntities = sysOrgService.list(objectLambdaQueryWrapper);
        if (CollectionUtils.isNotEmpty(sysOrgEntities)){
            for (int i = 0; i < sysOrgEntities.size(); i++) {
                SysOrgEntity sysOrgEntity = sysOrgEntities.get(i);
                JSONObject entries = new JSONObject();
                entries.put("siteName", sysOrgEntity.getName());
                entries.put("siteId", sysOrgEntity.getId());
                entries.put("siteCode", sysOrgEntity.getCode());
                // 经度
                entries.put("locationLon", sysOrgEntity.getSiteLocationLon());
                // 纬度
                entries.put("locationLat", sysOrgEntity.getSiteLocationLat());
                objects.add(entries);
            }
        }
        return objects;
    }

    /**
      * @author Mryang
      * @description 通过手机号查询用户信息
      * @date 11:40 2024/4/24
      * @param telephone
      * @return JSONObject
      */
    @PostMapping(value = "/queryUserInformationThroughMobilePhoneNumber")
    public JSONObject queryUserInformationThroughMobilePhoneNumber(@RequestParam("telephone") String telephone) {
        LambdaQueryWrapper<SysUserEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(SysUserEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(SysUserEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(SysUserEntity::getMobile, telephone);
        List<SysUserEntity> userEntities = sysUserService.list(objectLambdaQueryWrapper);
        JSONObject entries = new JSONObject();
        if (CollectionUtils.isNotEmpty(userEntities)){
            SysUserEntity sysUserEntity = userEntities.get(0);
            entries.put("orgId", sysUserEntity.getOrgId());
            entries.put("orgName", sysUserEntity.getOrgName());
            entries.put("supervisor", sysUserEntity.getSupervisor());
            entries.put("idCard", sysUserEntity.getIdCard());
            entries.put("mobile", sysUserEntity.getMobile());
            entries.put("postId", sysUserEntity.getPostId());
            entries.put("postName", sysUserEntity.getPostName());
            entries.put("busis", sysUserEntity.getBusis());
        }
        return entries;
    }

    /**
      * @author Mryang
      * @description 通过用户唯一编码查询用户信息
      * @date 10:10 2024/4/25
      * @param
      * @return
      */
    @PostMapping(value = "/queryUserInformationUserId")
    public JSONObject queryUserInformationUserId(@RequestParam("deviceUserId") String deviceUserId) {
        LambdaQueryWrapper<SysUserEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(SysUserEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(SysUserEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(SysUserEntity::getId, deviceUserId);
        List<SysUserEntity> userEntities = sysUserService.list(objectLambdaQueryWrapper);
        JSONObject entries = new JSONObject();
        if (CollectionUtils.isNotEmpty(userEntities)){
            SysUserEntity sysUserEntity = userEntities.get(0);
            entries.put("orgId", sysUserEntity.getOrgId());
            entries.put("orgName", sysUserEntity.getOrgName());
            entries.put("supervisor", sysUserEntity.getSupervisor());
            entries.put("idCard", sysUserEntity.getIdCard());
            entries.put("mobile", sysUserEntity.getMobile());
            entries.put("postId", sysUserEntity.getPostId());
            entries.put("postName", sysUserEntity.getPostName());
            entries.put("busis", sysUserEntity.getBusis());
        }
        return entries;
    }
}
