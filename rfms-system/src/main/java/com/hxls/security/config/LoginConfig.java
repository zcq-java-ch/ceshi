package com.hxls.security.config;

import com.hxls.api.module.message.SmsApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 登录配置信息
 *
 * @author
 *
 */
@Configuration
public class LoginConfig {

    @Bean
    @ConditionalOnMissingBean
    SmsApi smsApi() {
        return new SmsApi() {
            @Override
            public boolean send(String mobile, Map<String, String> params) {
                return false;
            }

            @Override
            public boolean sendById(List<String> mobile, Map<String, String> params, Long id) {
                return false;
            }

            @Override
            public boolean sendCode(String mobile, String key, String value) {
                return false;
            }

            @Override
            public boolean verifyCode(String mobile, String code) {
                return false;
            }
        };
    }
}
