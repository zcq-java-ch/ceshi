package com.hxls.system.controller;

import com.hxls.api.dto.AppointmentDTO;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.framework.common.cache.RedisCache;
import com.hxls.framework.common.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页 欢迎信息
 *
 * @author
 *
 */
@RestController
public class IndexController {

    @Resource
    private AppointmentFeign feign;
    @Resource
    private RedisCache redisCache;

    @GetMapping("/")
    public String index() {
        return "您好，项目已启动，祝您使用愉快！";
    }


    @GetMapping("/heartbeat")
    public Result<String> getheartbeat(@RequestParam String ip){
        System.out.println("接收到的ip为 ："+ip);

        try {
            AppointmentDTO establish = feign.establish(new AppointmentDTO().setExchangeName("jingcheng").setQueueName("jingcheng").setIp(ip));
            if (establish.getResult()){
                System.out.println("队列创建成功");
                redisCache.set( ip , "在线" ,60*3);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.ok("jingcheng");
    }


}
