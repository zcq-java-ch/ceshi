package com.hxls.datasection.entity;

import lombok.Data;

/**
 * //用于德服智慧 万众识别回调接口
 * */
@Data
public class DfWZCallBackDto {

    /**
     * 设备唯一标识码
     * */
    private String Mac_addr;

    /**
     *uuid
     * */
    private String id;
    /**
     *比对时间 yyyy-MM-dd HH:mm:ss
     * */
    private String time;
    /**
     *设备名称
     * */
    private String devicename;
    /**
     *安装位置
     * */
    private String location;
    /**
     *出入 0出口 1入口
     * */
    private Integer inout;
    /**
     *人员编号
     * */
    private String employee_number;
    /**
     *姓名
     * */
    private String name;
    /**
     *性别
     * */
    private String sex;
    /**
     *民族
     * */
    private String nation;
    /**
     *身份证号
     * */
    private String idNum;
    /**
     *IC卡号
     * */
    private String icNum;
    /**
     *出生年月如：1997年4月3日
     * */
    private String birthday;
    /**
     *电话
     * */
    private String telephone;
    /**
     *身份证上住址信息
     * */
    private String address;
    /**
     *签发机关
     * */
    private String depart;
    /**
     *有效期开始
     * */
    private String validStart;
    /**
     *有效期结束
     * */
    private String validEnd;
    /**识别方式（比对类型）：0人脸识别， 1黑名单识别（预留字段），2人证比对， 3IC卡识别
     *
     * */
    private Integer IdentifyType;
    /**
     *比对结果 1比对成功 0比对失败
     * */
    private Integer resultStatus;
    /**
     *比对抓拍照片 base64位字符串
     * */
    private String face_base64;
    /**
     *模板照片
     * */
    private String templatePhoto;
    /**
     *体温
     * */
    private String temperature;
    /**
     *健康码类型 0未开启 1绿码 2黄码 3红码
     * */
    private int healthCode;
    /**
     *其他检测信息
     * */
    private rnaJson rna;
    /**
     *
     * */
    public static class rnaJson {
        /**
         *核酸检测结果 0未开启 1阴性 2阳性
         * */
        private int ret;
        /**
         *核酸检测时间
         * */
        private String time;
    }
    /**接种记录
     *
     * */
    private antibodyJson antibody;
    /**
     *
     * */
    public static class antibodyJson {
        /**
         *是否接种 0未接种 1已接种
         * */
        private String ret;
        /**
         *接种时间 yyyy-MM-dd HH:mm:ss
         * */
        private String time;
        /**
         *接种记录
         * */
        private String src;
    }
}
