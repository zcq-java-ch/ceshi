package com.hxls.framework.common.utils;

import cn.hutool.http.HttpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 随机字符串
 *
 * @author
 *
 */
@Slf4j
public class RandomStringUtils {

    // 生成随机字符串的方法
    public static String generateRandomString(int length) {
        // 可以包含的字符集合
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();

        // 生成指定长度的随机字符串
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            stringBuilder.append(characters.charAt(index));
        }

        return stringBuilder.toString();
    }
}
