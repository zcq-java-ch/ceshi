package com.hxls.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.TransPojo;
import com.hxls.framework.common.utils.DateUtils;
import com.hxls.system.entity.SysOrgEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户
 *
 * @author
 *
 */
@Data
@Schema(description = "用户")
public class SysUserVO implements Serializable, TransPojo {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    @Schema(description = "员工编码")
    private String code;

    @Schema(description = "用户类型: 数据字典（内部/客户/供应商）")
    private String userType;

    @Schema(description = "所属站点（组织类型为站点的组织id）")
    private String stationId;


    @Schema(description = "业务：数据字典")
    private String busis;


    @Schema(description = "身份证")
    private String idCard;


    @Schema(description = "岗位编码")
    private String postId;


    @Schema(description = "岗位名称")
    private String postName;

    @Schema(description = "职级：数据字典")
    private Integer ranks;


    @Schema(description = "NC编号")
    private String ncNo;


    @Schema(description = "登录方式：数据字典（1：用户名；2：手机号；3：用户名+手机号）")
    private Integer loginType;


    @Schema(description = "是否支持创建子用户：1：支持；0：不支持")
    private Integer createChindAccount;

    @Schema(description = "家庭住址")
    private String address;

    @Schema(description = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(description = "姓名", required = true)
    @NotBlank(message = "姓名不能为空")
    private String realName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别 0：男   1：女   2：未知", required = true)
    @Range(min = 0, max = 2, message = "性别不正确")
    private Integer gender;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "机构ID", required = true)
    @NotNull(message = "机构ID不能为空")
    @Trans(type = TransType.SIMPLE, target = SysOrgEntity.class, fields = "name", ref = "orgName")
    private Long orgId;

    @Schema(description = "状态 0：停用    1：正常", required = true)
    @Range(min = 0, max = 1, message = "用户状态不正确")
    private Integer status;

    @Schema(description = "角色ID列表")
    private List<Long> roleIdList;

    @Schema(description = "岗位ID列表")
    private List<Long> postIdList;

    @Schema(description = "岗位名称列表")
    private List<String> postNameList;

    @Schema(description = "超级管理员   0：否   1：是")
    private Integer superAdmin;

    @Schema(description = "机构名称")
    private String orgName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createTime;

    @Schema(description = "车牌号")
    private String licensePlate;

    @Schema(description = "车辆照片")
    private String imageUrl;

    @Schema(description = "排放标准（数据字典）")
    private String emissionStandard;

    @Schema(description = "车辆类型（数据字典）")
    private String carType;

    @Schema(description = "三方关联id（数据字典）")
    private String openId;


    @Schema(description = "排序")
    private Integer sort;





}
