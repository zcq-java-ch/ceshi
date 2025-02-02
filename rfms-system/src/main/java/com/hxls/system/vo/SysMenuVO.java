package com.hxls.system.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.hxls.framework.common.utils.DateUtils;
import com.hxls.framework.common.utils.TreeNode;
import org.hibernate.validator.constraints.Range;

import java.util.Date;

/**
 * 菜单管理
 *
 * @author
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "菜单")
public class SysMenuVO extends TreeNode<SysMenuVO> {

    @Schema(description = "菜单名称")
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    @Schema(description = "菜单URL")
    private String url;

    @Schema(description = "类别  1：web端   2：移动端")
    @Range(min = 1, max = 2, message = "类别不正确")
    private Integer category;

    @Schema(description = "类型  0：菜单   1：按钮   2：接口")
    @Range(min = 0, max = 2, message = "类型不正确")
    private Integer type;

    @Schema(description = "打开方式   0：内部   1：外部")
    @Range(min = 0, max = 1, message = "打开方式不正确")
    private Integer openStyle;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "授权标识(多个用逗号分隔，如：sys:menu:list,sys:menu:save)")
    private String authority;

    @Schema(description = "排序")
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createTime;

    @Schema(description = "上级菜单名称")
    private String parentName;

    @TableField(exist = false)
    private Long ppid;
}
