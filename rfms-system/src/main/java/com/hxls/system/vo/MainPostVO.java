package com.hxls.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author zhaohong
 * @version 1.0
 * @class_name MainPostVO
 * @create_date 2024/4/15 17:20
 * @description 主数据岗位数据VO
 */
@Data
public class MainPostVO {

    @Schema(description = "id")
    private String id;

    @Schema(description = "岗位名字")
    private String name;
}
