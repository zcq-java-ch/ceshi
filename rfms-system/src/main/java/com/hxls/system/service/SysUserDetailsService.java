package com.hxls.system.service;

import com.hxls.framework.security.user.UserDetail;
import org.springframework.security.core.userdetails.UserDetails;

public interface SysUserDetailsService {

    /**
     * 获取 UserDetails 对象，设置用户权限信息
     */
    UserDetails getUserDetails(UserDetail userDetail);
}
