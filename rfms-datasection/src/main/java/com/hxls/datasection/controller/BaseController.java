package com.hxls.datasection.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.system.SystemUtil;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

public class BaseController {
    @ModelAttribute("baseUser")
    protected UserDetail getBaseUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            throw new RuntimeException("服务端未获取到Token信息，请重新登录");

        } else {
            UserDetail user = SecurityUser.getUser();
            if (ObjectUtil.isEmpty(user)) {
                throw new RuntimeException("Token无法正常解析，请重新登录");
            } else {
                return user;
            }
        }
    }
}
