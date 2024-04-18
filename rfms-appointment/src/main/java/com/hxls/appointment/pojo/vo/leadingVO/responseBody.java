package com.hxls.appointment.pojo.vo.leadingVO;

import cn.hutool.json.JSONObject;
import lombok.Data;

@Data
public class responseBody {


    private String code;

    private String codeMsg;

    private JSONObject data;


}
