package com.hxls.rabbitmq.demoExchange;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class DynamicQueueNameProvider {



    /**
     * Mryang
     * 获取当前从平台到客户端的 静态 路由名称
     * */
    public String getDynamicQueueNameFromCloud() {
        // 可以根据业务逻辑动态生成队列名称
        // 从数据库中根据当前电脑IP获取站点名称，这里假设有一个方法 getSiteNameByIp 实现此逻辑
        String siteName = "XICHANG";
        String ip = "192.102.0.76";
        String all = siteName + "_FACE_" + ip + "_TOCLOUD";
        System.out.println(
                "全队列名称"+all
        );
        return all;

    }


}
