package com.hxls.framework.security.mobile;

/**
 * 手机短信登录，验证码效验
 *
 * @author
 *
 */
public interface MobileVerifyCodeService {

    boolean verifyCode(String mobile, String code);
}
