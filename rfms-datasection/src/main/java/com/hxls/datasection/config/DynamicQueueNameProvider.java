package com.hxls.datasection.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.hxls.api.feign.system.DeviceFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitAdmin;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DynamicQueueNameProvider {

    @Autowired
    private DeviceFeign deviceFeign;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    /**
     * Mryang
     * 获取当前从客户端到平台端 人脸的 静态 路由名称
     * */
    public List<String> getDynamicFaceQueueNameFromCloud() {
        // 可以根据业务逻辑动态生成队列名称

        /**
         * 通过fegin 查询system中的设备，将所有合格的设备，获取sn和ip全部进行监听
         * */
        JSONObject objects = deviceFeign.queryAllDeviceList();
        log.info("来自系统服务的查询设备的结果：{}",objects);
        List<String> all = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(objects)){
            JSONArray objects1 = objects.get("jsonA", JSONArray.class);
            if (CollectionUtil.isNotEmpty(objects1)){
                for (int i = 0; i < objects1.size(); i++) {
                    JSONObject jsonObject = objects1.get(i, JSONObject.class);

                    String ipAddress = jsonObject.get("ipAddress", String.class);
                    String deviceSn = jsonObject.get("deviceSn", String.class);

                    String faceQueueName = deviceSn + "_FACE_" + ipAddress + "_TOCLOUD";
                    log.info("获取到来自系统服务的设备，并开始监听人脸队列{}",faceQueueName);
                    String carQueueName = deviceSn + "_CAR_" + ipAddress + "_TOCLOUD";
                    log.info("获取到来自系统服务的设备，并开始监听车辆队列{}",carQueueName);
                    all.add(faceQueueName);
                    all.add(carQueueName);
                }
            }
        }

        log.debug("最终监听到的队列集合是：{}",all);
        return all;

    }

    /**
     * Mryang
     * 获取当前从客户端到平台端 车辆的 静态 路由名称
     * */
    public String getDynamicCarQueueNameFromCloud() {
        // 可以根据业务逻辑动态生成队列名称
        // 从数据库中根据当前电脑IP获取站点名称，这里假设有一个方法 getSiteNameByIp 实现此逻辑
        String siteName = "XICHANG";
        String ip = "192.102.0.76";
        String all = siteName + "_CAR_" + ip + "_TOCLOUD";
        System.out.println(
                "全队列名称"+all
        );
        return all;

    }

    // 判断队列是否存在的方法
//    public boolean isQueueExist(String queueName) {
//        // 使用 RabbitAdmin 查询队列信息，如果存在则返回 true，否则返回 false
//        return rabbitAdmin.getQueueProperties(queueName) != null;
//    }


}
