package com.hxls.api.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@Tag(name="预约模块对象")
@Accessors(chain = true)
public class AppointmentDTO implements Serializable {

    private String exchangeName ;

    private String queueName;

    private String ip ;

    private Boolean result = false;

    @Schema(description = "厂站id")
    private Long siteId;

    @Schema(description = "预约类型（数据字典）")
    private String appointmentType;

    @Schema(description = "提交时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date[] creatTime;


}
