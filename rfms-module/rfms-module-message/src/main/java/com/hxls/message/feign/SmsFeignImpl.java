package com.hxls.message.feign;

import lombok.AllArgsConstructor;
import com.hxls.api.feign.message.SmsFeign;
import com.hxls.message.cache.SmsSendCache;
import com.hxls.message.sms.service.SmsService;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短信服务
 *
 *  @author
 */
@RestController
@AllArgsConstructor
public class SmsFeignImpl implements SmsFeign {
    private final SmsService smsService;
    private final SmsSendCache smsSendCache;

    @Override
    public Boolean send(String mobile, Map<String, String> params) {
        return smsService.send(mobile, params);
    }

    @Override
    public Boolean sendById(List<String> mobile, Map<String, String> params, Long id) {
        return smsService.sendById(mobile , params , id);
    }

    @Override
    public Boolean sendCode(String mobile, String key, String value) {
        // 短信参数
        Map<String, String> params = new HashMap<>();
        params.put(key, value);

        // 发送短信
        boolean flag = smsService.send(mobile, params);
        if (flag) {
            smsSendCache.saveCode(mobile, value);
        }
        return flag;
    }
}
