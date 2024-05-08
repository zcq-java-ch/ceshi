package com.hxls.appointment.pojo.vo.leadingVO;

import lombok.Data;

import java.util.List;

@Data
public class responseBodyList {
    private String code;

    private String codeMsg;

    private List<recordInfo> data;

    private List<recordInfo> recordInfoList;
}
