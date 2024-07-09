package com.hxls.api.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

@Data
public class TIssueEigenvalueDTO implements Serializable {


    @Schema(description = "厂站id")
    private Long siteId;

    @Schema(description = "预约类型（数据字典）")
    private Long areaId;

    @Schema(description = "提交时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private String[] creatTime;

    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小值为 1")
    @Schema(description = "当前页码", required = true)
    Integer page;

    @NotNull(message = "每页条数不能为空")
    @Schema(description = "每页条数", required = true)
    Integer limit;

    @Schema(description = "人车类型（1人 2车）")
    private Integer type;

    @Schema(description = "状态")
    private Integer status;



}
