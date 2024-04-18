package com.hxls.appointment.pojo.vo.leadingVO;

import lombok.Data;

import java.util.List;

@Data
public class PageInfo {


    private int code;
    private List<recordInfo> records;
    private long total;
    private long size;
    private long current;
    private boolean optimizeCountSql = true;
    private boolean isSearchCount= true;

}
