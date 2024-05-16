package com.hxls.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.mybatis.entity.BaseEntity;
import com.hxls.system.enums.UserStatusEnum;

/**
 * 用户
 *
 * @author
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
public class SysUserEntity extends BaseEntity {

    /**
     * 员工编码
     **/
    private String code;

    /**
    * 用户类型: 数据字典 内部/客户/供应商
    **/
    private String userType;

    /**
     * 所属站点
     **/
    private Long stationId;

    /**
     * 所属多站点
     **/
    private String stationIds;


    /**
     * 是否留宿
     **/
    private Integer isStay;

    /**
     * 业务
     **/
    private String busis;



    /**
     * 身份证
     **/
    private String idCard;



    /**
     * 岗位编码
     **/
    private String postId;


    /**
     * 岗位名称
     **/
    private String postName;

    /**
     * 职级（数据字典）
     **/
    private Integer ranks;



    /**
     * NC编号
     **/
    private String ncNo;



    /**
     * 登录方式：1：用户名；2：手机号；3：用户名+手机号
     **/
    private Integer loginType;


    /**
     * 是否支持创建子用户：1：支持；0：不支持
     **/
    private Integer createChindAccount;

    /**
     * 家庭住址
     **/
    private String address;


    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 姓名
     */
    private String realName;

    /**
     * 代班负责人
     */
    private String  supervisor;

    /**
     * 头像
     */
    private String avatar;
    /**
     * 性别   0：男   1：女   2：未知
     */
    private Integer gender;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 公司ID
     */
    private Long orgId;
    /**
     * 公司名字
     */
    private String orgName;
    /**
     * 超级管理员   0：否   1：是
     */
    private Integer superAdmin;
    /**
     * 状态  {@link UserStatusEnum}
     */
    private Integer status;
    /**
     * 组织名称
     */
//    @TableField(exist = false)
//    private String orgName;

    /**
     * 车牌号
     */
    private String licensePlate;

    /**
     * 车辆照片
     */
    private String imageUrl;

    /**
     * 排放标准（数据字典）
     */
    private String emissionStandard;
    /**
     * 车辆类型（数据字典）
     */
    private String carType;
    /**
     * 三方关联id（数据字典）
     */
    private String openId;

    /**
    * 排序
    **/
    private Integer sort;
}
