package com.hxls.appointment.server;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxls.framework.common.cache.RedisCache;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
@AllArgsConstructor
public class MqServer {

    private final RedisCache redisCache;

    /**
     * 检查是否有新工厂产生
     */
    private void checkNewStation(){
        //检查缓存里面的站点编号;
        Object station = redisCache.get("Station");
        if (station !=null ){
            List<String> stationList = Arrays.stream(station.toString().split(",")).toList();

            return;
        }

        //todo 拿去新建路由器和队列

    }
}
