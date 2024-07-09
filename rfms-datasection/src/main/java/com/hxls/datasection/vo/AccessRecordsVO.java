package com.hxls.datasection.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hxls.framework.common.utils.DateUtils;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * icps通行记录表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-29
 */
@Data
public class AccessRecordsVO {

    /**
     * 数据密钥
     */
    private String secretKey;

    /**
     * 人员进出记录表
     */
    private List<TPersonAccessRecords> personAccessRecordsVOList;

    /**
     * 车辆进出记录表
     */
    private List<TVehicleAccessRecords> tVehicleAccessRecordsVOList;


    @Data
    public static class TPersonAccessRecords{

        /**
         * 出入类型  1：进场   2：出场
         */
        private String accessType;
        /**
         * 人员名称
         */
        private String personName;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 记录时间
         */
        @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
        private Date recordTime;


        /**
         * 身份证号码
         */
        private String idCardNumber;


    }

    @Data
    public static class TVehicleAccessRecords{

        /**
         * 司机姓名
         */
        private String driverName;
        /**
         * 司机手机号码
         */
        private String driverPhone;

        /**
         * 出入类型  1：进场   2：出场
         */
        private String accessType;
        /**
         * 记录时间
         */
        @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
        private Date recordTime;
        /**
         * 车牌号
         */
        private String plateNumber;
    }

}
