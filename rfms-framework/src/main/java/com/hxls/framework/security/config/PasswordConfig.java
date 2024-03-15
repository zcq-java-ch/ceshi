package com.hxls.framework.security.config;

import com.hxls.framework.security.crypto.Sm3PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 加密配置
 *
 * @author
 *
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用国密SM3加密
        return new Sm3PasswordEncoder();

        // return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
