package com.hxls.api.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@Tag(name="预约模块对象")
@Accessors(chain = true)
public class AppointmentDTO  implements Serializable {

    private String routingKey ;

    private String exchangeName ;

    private String faceToAgentQueueName;

    private String faceToCloudQueueName;

    private String carToAgentQueueName;

    private String carToCloudQueueName;

    private String faceToAgentroutingKey ;

    private String faceToCloudroutingKey ;

    private String carToAgentroutingKey ;

    private String carToCloudroutingKey ;

    private String ip ;

    private Boolean result = false;

    @Schema(description = "厂站id")
    private Long siteId;

    @Schema(description = "预约类型（数据字典）")
    private String appointmentType;

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


    @Schema(description = "排序字段")
    String order = "create_time";

    @Schema(description = "是否升序")
    boolean asc = false ;

}
