package com.hxls.api.dto.message;


import lombok.Data;

@Data
public class MessageDTO {

    /**
     * 预约审核短信
     */
    @Data
    public static class Appointment{
        /**
         * 站点名称
         */
        private String stationName;
        /**
         * 预约类型
         */
        private String appointmentType;
        /**
         * 公司名称
         */
        private String companyName;
        /**
         * 预约人姓名
         */
        private String name;

    }

    /**
     * 离线告警
     */
    @Data
    public static class Offline{
        /**
         * 站点名称
         */
        private String stationName;

        /**
         * 离线主机
         */
        private String masterName;
    }




}
