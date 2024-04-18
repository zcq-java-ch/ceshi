package com.hxls.appointment.pojo.vo.leadingVO;

import lombok.Data;

@Data
public class PageParams {

    private String startTime;
    private String endTime;
    private Integer page;
    private Integer pageSize;
    private String carType;
    private String receiveStation;
}
