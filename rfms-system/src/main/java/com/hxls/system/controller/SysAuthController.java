package com.hxls.system.controller;

import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.service.SysAuthService;
import com.hxls.system.service.SysCaptchaService;
import com.hxls.system.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import com.hxls.framework.security.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 认证管理
 *
 * @author
 *
 */
@RestController
@RequestMapping("sys/auth")
@Tag(name = "认证管理")
@AllArgsConstructor
public class SysAuthController {
    private final SysCaptchaService sysCaptchaService;
    private final SysAuthService sysAuthService;
    private final AppointmentFeign feign;

    @GetMapping("captcha")
    @Operation(summary = "验证码")
    public Result<SysCaptchaVO> captcha() {
        SysCaptchaVO captchaVO = sysCaptchaService.generate();
        return Result.ok(captchaVO);
    }

    @GetMapping("captcha/enabled")
    @Operation(summary = "是否开启验证码")
    public Result<Boolean> captchaEnabled() {
        boolean enabled = sysCaptchaService.isCaptchaEnabled();
        return Result.ok(enabled);
    }

    @PostMapping("login")
    @Operation(summary = "账号密码登录")
    public Result<SysUserTokenVO> login(@RequestBody SysAccountLoginVO login) {
        SysUserTokenVO token = sysAuthService.loginByAccount(login);
        return Result.ok(token);
    }

    @PostMapping("send/code")
    @Operation(summary = "发送短信验证码")
    public Result<String> sendCode(String mobile) {
        boolean flag = sysAuthService.sendCode(mobile);
        if (!flag) {
            return Result.error("短信发送失败！");
        }
        return Result.ok();
    }

    @PostMapping("mobile")
    @Operation(summary = "手机号登录")
    public Result<SysUserTokenVO> mobile(@RequestBody SysMobileLoginVO login) {
        SysUserTokenVO token = sysAuthService.loginByMobile(login);
        return Result.ok(token);
    }

    @PostMapping("third")
    @Operation(summary = "第三方登录")
    public Result<SysUserTokenVO> third(@RequestBody SysThirdCallbackVO login) {
        SysUserTokenVO token = sysAuthService.loginByThird(login);

        return Result.ok(token);
    }

    @PostMapping("token")
    @Operation(summary = "获取 accessToken")
    public Result<AccessTokenVO> token(String refreshToken) {
        AccessTokenVO token = sysAuthService.getAccessToken(refreshToken);

        return Result.ok(token);
    }


    @PostMapping("logout")
    @Operation(summary = "退出")
    public Result<String> logout(HttpServletRequest request) {
        sysAuthService.logout(TokenUtils.getAccessToken(request));

        return Result.ok();
    }

    @PostMapping("getOpenIdByCode/{code}")
    @Operation(summary = "通过code获取openId")
    public Result<String> getOpenIdByCode(@PathVariable("code") String code) {
        String openId = sysAuthService.getOpenIdByCode(code);
        return Result.ok(openId);
    }

}
