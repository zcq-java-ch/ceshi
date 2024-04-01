package com.hxls.framework.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 超级无敌雪花算法
 *  By_mryang
 *
 */
public class RandomSnowUtils {

    public static String getSnowRandom() {
        // 创建一个雪花算法对象
        Snowflake snowflake = IdUtil.getSnowflake(1, 1);

        // 生成随机码
        String idStr = snowflake.nextIdStr();
        // 生成指定长度的随机字符串
        String randomString = RandomUtil.randomString(10);

        return idStr+"_"+randomString;
    }
}