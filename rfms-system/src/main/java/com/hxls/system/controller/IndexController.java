package com.hxls.system.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.framework.common.cache.RedisCache;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.entity.SysSiteAreaEntity;
import com.hxls.system.entity.TDeviceManagementEntity;
import com.hxls.system.service.SysAreacodeDeviceService;
import com.hxls.system.service.TDeviceManagementService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页 欢迎信息
 *
 * @author
 *
 */
@RestController
@Slf4j
public class IndexController {

    public static final String QUEUE_FACE_TOAGENT = "_TOAGENT";

    public static final String QUEUE_FACE_TOCLOUD = "_TOCLOUD";

    public static final String QUEUE_CAR_TOAGENT = "_TOAGENT";

    public static final String QUEUE_CAR_TOCLOUD = "_TOCLOUD";

    public final static String SITE_ROUTING_FACE_TOAGENT = "_ROUTING_FACE_TOAGENT";

    public final static String SITE_ROUTING_CAR_TOAGENT = "_ROUTING_CAR_TOAGENT";

    // 路由键 站点——类型 【站点到平台用的路由】
    public final static String SITE_ROUTING_FACE_TOCLOUD = "_ROUTING_FACE_TOCLOUD";
    public final static String SITE_ROUTING_CAR_TOCLOUD = "_ROUTING_CAR_TOCLOUD";

    @Resource
    private AppointmentFeign feign;
    @Resource
    private RedisCache redisCache;

    @Autowired
    private TDeviceManagementService tDeviceManagementService;

    @Autowired
    protected SysAreacodeDeviceService sysAreacodeDeviceService;

    @GetMapping("/")
    public String index() {
        return "您好，项目已启动，祝您使用愉快！";
    }


    @GetMapping("/heartbeat")
    public Result<JSONObject> getheartbeat(@RequestParam String ip){
        System.out.println("接收到的ip为 ："+ip);

        try {
            /**
             * 1. 现通过ip找到对应的设备
             * 2. 如果有设备—— 执行创建队列等操作，并返回
             *       1： 当前站点的队列前缀【XICHANG】
             *       2: 返回当前站点应该执行哪个厂商的方法类型值【1】(比如执行华安视讯的方法)
             *       3： 返回当前ip关联的子设备ip
             * 3. 如果没有设备——返回空
             *
             * */
            JSONObject jsonObject = new JSONObject();
            JSONArray faceJsonA = new JSONArray();
            JSONArray carJsonA = new JSONArray();


            LambdaQueryWrapper<TDeviceManagementEntity> tDeviceManagementEntityQueryWrapper = new LambdaQueryWrapper<>();
            tDeviceManagementEntityQueryWrapper.eq(TDeviceManagementEntity::getMasterIp, ip);
            List<TDeviceManagementEntity> deviceManagementEntityList = tDeviceManagementService.list(tDeviceManagementEntityQueryWrapper);
            if (CollectionUtil.isNotEmpty(deviceManagementEntityList)){
                TDeviceManagementEntity tDeviceManagementEntity = deviceManagementEntityList.get(0);
                String siteCode = tDeviceManagementEntity.getSiteCode();
                jsonObject.put("siteFront", siteCode);
                for (int i = 0; i < deviceManagementEntityList.size(); i++) {
                    TDeviceManagementEntity tDeviceManagementEntity1 = deviceManagementEntityList.get(i);
                    String deviceType = tDeviceManagementEntity1.getDeviceType();
                    String manufacturerCode = tDeviceManagementEntity1.getManufacturerCode();
                    String ipAddress = tDeviceManagementEntity1.getIpAddress();
                    String inoutType = tDeviceManagementEntity1.getType();

                    // 其他字段
                    Long id = tDeviceManagementEntity1.getId();   // 设备ID
                    String deviceName = tDeviceManagementEntity1.getDeviceName(); // // 设备名字

                    // 通过设备id找到通道id和通道名字
                    Long channel_id = 1L;
                    String channel_name = "";
                    SysSiteAreaEntity sysSiteAreaEntity = sysAreacodeDeviceService.queryChannelByDeviceId(id);
                    if (ObjectUtil.isNotEmpty(sysSiteAreaEntity)){
                        channel_id = sysSiteAreaEntity.getId();
                        channel_name = sysSiteAreaEntity.getAreaName();
                    }else {
                    }

                    if ("1".equals(deviceType)){
                        // 人脸
                        JSONObject facejsonObject = new JSONObject();
                        facejsonObject.putOnce("manufacturerCode", manufacturerCode);
                        facejsonObject.putOnce("ipAddress", ipAddress);
                        facejsonObject.putOnce("inoutType", inoutType);
                        facejsonObject.putOnce("channel_id", channel_id);
                        facejsonObject.putOnce("channel_name", channel_name);
                        facejsonObject.putOnce("device_id", id);
                        facejsonObject.putOnce("deviceName", deviceName);
                        faceJsonA.add(facejsonObject);
                    }else if("2".equals(deviceType)){
                        // 车辆
                        JSONObject facejsonObject = new JSONObject();
                        facejsonObject.putOnce("manufacturerCode", manufacturerCode);
                        facejsonObject.putOnce("ipAddress", ipAddress);
                        facejsonObject.putOnce("inoutType", inoutType);
                        facejsonObject.putOnce("channel_id", channel_id);
                        facejsonObject.putOnce("channel_name", channel_name);
                        facejsonObject.putOnce("device_id", id);
                        facejsonObject.putOnce("deviceName", deviceName);
                        faceJsonA.add(facejsonObject);
                    }else {
                        // 数据错误
                    }
                }

                String exchangeName = siteCode + "_EXCHANGE";
                String toFaceAgentQueueName = siteCode + "_FACE_" + ip + QUEUE_FACE_TOAGENT;
                String toFaceCloudQueueName = siteCode + "_FACE_" + ip + QUEUE_FACE_TOCLOUD;
                String toFaceAgentRoutingName = siteCode + SITE_ROUTING_FACE_TOAGENT;
                String toFaceCloudRoutingName = siteCode + SITE_ROUTING_FACE_TOCLOUD;

                String toCarAgentQueueName = siteCode + "_CAR_" + ip + QUEUE_CAR_TOAGENT;
                String toCarCloudQueueName = siteCode + "_CAR_" + ip + QUEUE_CAR_TOCLOUD;
                String toCarAgentRoutingName = siteCode + SITE_ROUTING_CAR_TOAGENT;
                String toCarCloudRoutingName = siteCode + SITE_ROUTING_CAR_TOCLOUD;

                AppointmentDTO establish1 = feign.establish(new AppointmentDTO().setExchangeName(exchangeName).setQueueName(toFaceAgentQueueName).setRoutingKey(toFaceAgentRoutingName).setIp(ip));
                if (establish1.getResult()){
                    log.info("平台下发【人脸】指令到客户端的队列创建成功：{}，路由键是：{}", toFaceAgentQueueName, toFaceAgentRoutingName);
                    redisCache.set( ip , "在线" ,60*3);
                }
                AppointmentDTO establish2 = feign.establish(new AppointmentDTO().setExchangeName(exchangeName).setQueueName(toFaceCloudQueueName).setRoutingKey(toFaceCloudRoutingName).setIp(ip));
                if (establish2.getResult()){
                    log.info("客户端上行【人脸】指令到平台的队列创建成功：{}，路由键是：{}", toFaceCloudQueueName, toFaceCloudRoutingName);
                    redisCache.set( ip , "在线" ,60*3);
                }
                AppointmentDTO establish3 = feign.establish(new AppointmentDTO().setExchangeName(exchangeName).setQueueName(toCarAgentQueueName).setRoutingKey(toCarAgentRoutingName).setIp(ip));
                if (establish3.getResult()){
                    log.info("平台下发【车辆】指令到客户端的队列创建成功：{}，路由键是：{}", toCarAgentQueueName, toCarAgentRoutingName);
                    redisCache.set( ip , "在线" ,60*3);
                }
                AppointmentDTO establish4 = feign.establish(new AppointmentDTO().setExchangeName(exchangeName).setQueueName(toCarCloudQueueName).setRoutingKey(toCarCloudRoutingName).setIp(ip));
                if (establish4.getResult()){
                    log.info("客户端上行【车辆】指令到平台的队列创建成功：{}，路由键是：{}", toCarCloudQueueName, toCarCloudRoutingName);
                    redisCache.set( ip , "在线" ,60*3);
                }
            }else {
                return null;
            }
            jsonObject.putOnce("faceJsonA", faceJsonA);
            jsonObject.putOnce("carJsonA", carJsonA);

            return Result.ok(jsonObject);
        } catch (Exception e) {
//            throw new RuntimeException(e);
            e.printStackTrace();
            return Result.ok(new JSONObject());
        }
    }


}
