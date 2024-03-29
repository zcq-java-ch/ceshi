package com.hxls.system.cache;

import com.hxls.framework.common.cache.RedisCache;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author zhaohong
 * @version 1.0
 * @class_name MainPlatformCache
 * @create_date 2024/3/29 16:24
 * @description 华西主数据 Cache
 */
@Service
@AllArgsConstructor
public class MainPlatformCache {
    private final RedisCache redisCache;

    /**
     * 主数据accessToken
     */
    private final String MAIN_ACCESS_TOKEN = "main:accessToken";

    public void saveAccessToken(String accessToken) {
        // 保存到Redis，有效期90分钟
        redisCache.set(MAIN_ACCESS_TOKEN, accessToken, 90 * 60);
    }

    public String getAccessToken() {
        return (String) redisCache.get(MAIN_ACCESS_TOKEN);
    }

}
