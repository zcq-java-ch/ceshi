package com.hxls.framework.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;

/**
 * 对象转换成 Map表
 */
public class ClassToMapUtils {

    /**
     * 转成对象map
     * @param obj
     * @return
     */
    public static Map<String, String> objectToStringMap(Object obj) {
        Map<String, String> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true); // 设置可访问私有字段

                Object value = field.get(obj);
                String stringValue = String.valueOf(value);
                map.put(field.getName(), stringValue);
            }
            return map;

        } catch (Exception e) {
            return map;
        }
    }
}
