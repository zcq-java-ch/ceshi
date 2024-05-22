package com.hxls.datasection.controller;

import cn.hutool.core.util.ObjectUtil;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * @author admin
 */
public class BaseController {
    /**
     * @param request HttpServletRequest
     * @return UserDetail
     * */
    @ModelAttribute("baseUser")
    protected UserDetail getBaseUser(final HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            throw new ServerException("服务端未获取到Token信息，请重新登录", ErrorCode.REFRESH_TOKEN_INVALID.getCode());
        } else {
            UserDetail user = SecurityUser.getUser();
            if (ObjectUtil.isEmpty(user)) {
                throw new ServerException("Token无法正常解析，请重新登录", ErrorCode.REFRESH_TOKEN_INVALID.getCode());
            } else {
                return user;
            }
        }
    }
}
