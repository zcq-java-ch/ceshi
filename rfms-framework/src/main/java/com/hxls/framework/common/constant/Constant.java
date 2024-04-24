package com.hxls.framework.common.constant;

/**
 * 常量
 *
 * @author
 *
 */
public interface Constant {
    /**
     * 根节点标识
     */
    Long ROOT = 0L;
    /**
     * 当前页码
     */
    String PAGE = "page";
    /**
     * 数据权限
     */
    String DATA_SCOPE = "dataScope";
    /**
     * 超级管理员
     */
    Integer SUPER_ADMIN = 1;
    /**
     * 禁用
     */
    Integer DISABLE = 0;
    /**
     * 启用
     */
    Integer ENABLE = 1;
    /**
     * 失败
     */
    Integer FAIL = 0;
    /**
     * 成功
     */
    Integer SUCCESS = 1;
    /**
     * OK
     */
    String OK = "OK";

    /**
     * pgsql的driver
     */
    String PGSQL_DRIVER = "org.postgresql.Driver";

    /**
     * 数字 0
     */
    Integer ZERO = 0;

    String PASS = "1";


    String EXCHANGE = "_EXCHANGE";

    // 路由键 站点——类型 【平台-->站点】
    String SITE_ROUTING_FACE_TOAGENT = "_ROUTING_FACE_TOAGENT";
    String SITE_ROUTING_CAR_TOAGENT = "_ROUTING_CAR_TOAGENT";

    /**
     * 置空
     */
    Long EMPTY = -1L;

}
