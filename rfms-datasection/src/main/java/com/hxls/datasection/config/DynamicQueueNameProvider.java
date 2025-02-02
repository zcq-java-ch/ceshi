package com.hxls.datasection.config;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.api.feign.system.DeviceFeign;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DynamicQueueNameProvider {

    @Autowired
    private DeviceFeign deviceFeign;

    @Resource
    private AppointmentFeign appointmentFeign;
    public static final String QUEUE_FACE_TOCLOUD = "_TOCLOUD";
    public static final String QUEUE_CAR_TOCLOUD = "_TOCLOUD";
    public final static String SITE_ROUTING_FACE_TOCLOUD = "_ROUTING_FACE_TOCLOUD";
    public final static String SITE_ROUTING_CAR_TOCLOUD = "_ROUTING_CAR_TOCLOUD";
    /**
     * Mryang
     * 获取当前从客户端到平台端 人脸的 静态 路由名称
     * */
    public List<String> getDynamicFaceQueueNameFromCloud() {
        // 可以根据业务逻辑动态生成队列名称

        /**
         * 通过fegin 查询system中的设备，将所有合格的站点
         * */
        JSONArray objects = deviceFeign.queryAllDeviceList();
        log.info("来自系统服务的查询设备的结果：{}",objects);
        List<String> all = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(objects)){
            for (int i = 0; i < objects.size(); i++) {
                JSONObject jsonObject = objects.getJSONObject(i);
                String siteCode = jsonObject.getString("siteCode");
                String ipAddress = jsonObject.getString("ipAddress");

                // 创建交换机 和 队列
                String exchangeName = siteCode + "_EXCHANGE";
                String toFaceCloudQueueName = siteCode + "_FACE_" + ipAddress + QUEUE_FACE_TOCLOUD;
                String toFaceCloudRoutingName = siteCode + SITE_ROUTING_FACE_TOCLOUD;
                AppointmentDTO appointmentDTO = new AppointmentDTO();
                appointmentDTO.setExchangeName(exchangeName);
                appointmentDTO.setFaceToCloudQueueName(toFaceCloudQueueName);
                appointmentDTO.setFaceToCloudroutingKey(toFaceCloudRoutingName);
                appointmentFeign.establishAgentToCloud(appointmentDTO);
                log.info("获取到来自系统服务的设备，并开始监听人脸队列{}",toFaceCloudQueueName);
                all.add(toFaceCloudQueueName);
            }
        }

        log.debug("最终监听到的队列集合是：{}",all);
        return all;
    }

    /**
     * Mryang
     * 获取当前从客户端到平台端 车辆的 静态 路由名称
     * */
    public List<String> getDynamicCarQueueNameFromCloud() {
        // 可以根据业务逻辑动态生成队列名称

        /**
         * 通过fegin 查询system中的设备，将所有合格的站点
         * */
        JSONArray objects = deviceFeign.queryAllDeviceList();
        log.info("来自系统服务的查询设备的结果：{}",objects);
        List<String> all = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(objects)){
            for (int i = 0; i < objects.size(); i++) {
                JSONObject jsonObject = objects.getJSONObject(i);
                String siteCode = jsonObject.getString("siteCode");
                String ipAddress = jsonObject.getString("ipAddress");

                // 创建交换机 和 队列
                String exchangeName = siteCode + "_EXCHANGE";
                String toCarCloudQueueName = siteCode + "_CAR_" + ipAddress + QUEUE_CAR_TOCLOUD;
                String toCarCloudRoutingName = siteCode + SITE_ROUTING_CAR_TOCLOUD;
                AppointmentDTO appointmentDTO = new AppointmentDTO();
                appointmentDTO.setExchangeName(exchangeName);
                appointmentDTO.setCarToCloudQueueName(toCarCloudQueueName);
                appointmentDTO.setCarToCloudroutingKey(toCarCloudRoutingName);
                appointmentFeign.establishAgentToCloud(appointmentDTO);
                log.info("获取到来自系统服务的设备，并开始监听车辆队列{}",toCarCloudQueueName);
                all.add(toCarCloudQueueName);
            }
        }

        log.debug("最终监听到的队列集合是：{}",all);
        return all;

    }

    // 判断队列是否存在的方法
//    public boolean isQueueExist(String queueName) {
//        // 使用 RabbitAdmin 查询队列信息，如果存在则返回 true，否则返回 false
//        return rabbitAdmin.getQueueProperties(queueName) != null;
//    }
}
