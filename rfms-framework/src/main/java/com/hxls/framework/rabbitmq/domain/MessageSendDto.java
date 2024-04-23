package com.hxls.framework.rabbitmq.domain;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageSendDto implements Serializable {

    private String messageId;

    private JSONObject messageData;

    private String createTime;
}
