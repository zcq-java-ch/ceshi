package com.hxls.message.sms;

import cn.hutool.core.map.MapUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.hxls.message.sms.config.SmsConfig;
import lombok.extern.slf4j.Slf4j;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.JsonUtils;

import java.util.Map;

/**
 * 阿里云短信
 *
 * @author
 *
 */
@Slf4j
public class AliyunSmsStrategy implements SmsStrategy {
    private final Client client;
    private final SmsConfig smsConfig;

    public AliyunSmsStrategy(SmsConfig smsConfig) {
        this.smsConfig = smsConfig;

        try {
            Config config = new Config();
            config.setAccessKeyId(smsConfig.getAccessKey());
            config.setAccessKeySecret(smsConfig.getSecretKey());
            config.endpoint = "dysmsapi.aliyuncs.com";

            this.client = new Client(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(String mobile, Map<String, String> params) {
        SendSmsRequest request = new SendSmsRequest();
        request.setSignName(smsConfig.getSignName());
        request.setTemplateCode(smsConfig.getTemplateId());
        request.setPhoneNumbers(mobile);
//        request.setTemplateParam("{\"code\":\"1234\"}");
        if (MapUtil.isNotEmpty(params)) {
            request.setTemplateParam(JsonUtils.toJsonString(params));
        }

        try {
            // 发送短信
            SendSmsResponse response = client.sendSms(request);

            // 发送失败
            if (!Constant.OK.equalsIgnoreCase(response.getBody().getCode())) {
                throw new ServerException(response.getBody().getMessage());
            }
        } catch (Exception e) {
            throw new ServerException(e.getMessage());
        }
    }
}
