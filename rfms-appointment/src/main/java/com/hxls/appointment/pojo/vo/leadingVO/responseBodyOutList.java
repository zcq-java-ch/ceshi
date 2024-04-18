package com.hxls.appointment.pojo.vo.leadingVO;

import lombok.Data;

import java.util.List;

@Data
public class responseBodyOutList {

    private String code;

    private String codeMsg;

    private List<recordOutInfo> data;


}
