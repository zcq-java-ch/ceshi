package com.hxls.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.setting.SettingUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxls.api.module.message.SmsApi;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.security.cache.TokenStoreCache;
import com.hxls.framework.security.crypto.Sm2Util;
import com.hxls.framework.security.mobile.MobileAuthenticationToken;
import com.hxls.framework.security.third.ThirdAuthenticationToken;
import com.hxls.framework.security.third.ThirdLogin;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.enums.LoginOperationEnum;
import com.hxls.system.service.*;
import com.hxls.system.vo.*;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import dm.jdbc.filter.stat.json.JSONException;
import lombok.AllArgsConstructor;
import com.hxls.framework.security.user.UserDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 权限认证服务
 *
 * @author
 *
 */
@Service
@AllArgsConstructor
public class SysAuthServiceImpl implements SysAuthService {
    private final SysCaptchaService sysCaptchaService;
    private final TokenStoreCache tokenStoreCache;
    private final AuthenticationManager authenticationManager;
    private final SysLogLoginService sysLogLoginService;
    private final SysUserService sysUserService;
    private final SysUserTokenService sysUserTokenService;
    private final SysOrgService orgService;
    private final SmsApi smsApi;

    @Override
    public SysUserTokenVO loginByAccount(SysAccountLoginVO login) {
        // 验证码效验
        boolean flag = sysCaptchaService.validate(login.getKey(), login.getCaptcha());
        if (!flag) {
            // 保存登录日志
            sysLogLoginService.save(login.getUsername(), Constant.FAIL, LoginOperationEnum.CAPTCHA_FAIL.getValue());
            throw new ServerException("验证码错误");
        }
        Authentication authentication;
        try {
            // 用户认证
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getUsername(), Sm2Util.decrypt(login.getPassword())));
        } catch (BadCredentialsException e) {
            //账户密码错误之后，判断是否是手机号进行登录
            SysUserVO byMobile = sysUserService.getByMobile(login.getUsername());
            if(byMobile == null){
                throw new ServerException("用户名/手机号码或密码错误");
            }

            try {
                // 用户认证
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(byMobile.getUsername(), Sm2Util.decrypt(login.getPassword())));
            }catch (BadCredentialsException be){
                throw new ServerException("用户名/手机号码或密码错误");
            }


        }
        // 用户信息
        UserDetail user = (UserDetail) authentication.getPrincipal();

        //查询管理场站
        Long id = user.getId();
        LambdaQueryWrapper<SysOrgEntity> wrapper = new LambdaQueryWrapper<>();
        String format = String.format(" JSON_CONTAINS( site_admin_ids, JSON_ARRAY(%s)) ", id);
        wrapper.apply(format);
        List<SysOrgEntity> list = orgService.list(wrapper);
        if (!CollectionUtils.isEmpty(list)){
            List<Long> ids = list.stream().map(SysOrgEntity::getId).toList();
            user.setManageStation(new HashSet<>(ids));
        }

        //需要补充一个管理场站的集合信息 -- 后期完善
        // 生成 accessToken
        SysUserTokenVO userTokenVO = sysUserTokenService.createToken(user.getId());
        // 保存用户信息到缓存
        tokenStoreCache.saveUser(userTokenVO.getAccessToken(), user);
        return userTokenVO;
    }

    @Override
    public SysUserTokenVO loginByMobile(SysMobileLoginVO login) {
        Authentication authentication;
        try {
            // 用户认证
            authentication = authenticationManager.authenticate(
                    new MobileAuthenticationToken(login.getMobile(), login.getCode()));
        } catch (BadCredentialsException e) {
            throw new ServerException("手机号或验证码错误");
        }

        // 用户信息
        UserDetail user = (UserDetail) authentication.getPrincipal();

        // 生成 accessToken
        SysUserTokenVO userTokenVO = sysUserTokenService.createToken(user.getId());

        // 保存用户信息到缓存
        tokenStoreCache.saveUser(userTokenVO.getAccessToken(), user);

        return userTokenVO;
    }

    @Override
    public SysUserTokenVO loginByThird(SysThirdCallbackVO login) {
        Authentication authentication;
        try {
            // 转换对象
            ThirdLogin thirdLogin = BeanUtil.copyProperties(login, ThirdLogin.class);

            // 用户认证
            authentication = authenticationManager.authenticate(new ThirdAuthenticationToken(thirdLogin));
        } catch (BadCredentialsException e) {
            throw new ServerException("第三方登录失败");
        }

        // 用户信息
        UserDetail user = (UserDetail) authentication.getPrincipal();

        // 生成 accessToken
        SysUserTokenVO userTokenVO = sysUserTokenService.createToken(user.getId());

        // 保存用户信息到缓存
        tokenStoreCache.saveUser(userTokenVO.getAccessToken(), user);

        return userTokenVO;
    }

    @Override
    public boolean sendCode(String mobile) {
        // 生成6位验证码
        String code = RandomUtil.randomNumbers(6);

        SysUserVO user = sysUserService.getByMobile(mobile);
        if (user == null) {
            throw new ServerException("手机号未注册");
        }

        // 发送短信
        return smsApi.sendCode(mobile, "code", code);
    }

    @Override
    public AccessTokenVO getAccessToken(String refreshToken) {
        SysUserTokenVO token = sysUserTokenService.refreshToken(refreshToken);

        // 封装 AccessToken
        AccessTokenVO accessToken = new AccessTokenVO();
        accessToken.setAccessToken(token.getAccessToken());
        accessToken.setAccessTokenExpire(token.getAccessTokenExpire());

        return accessToken;
    }

    @Override
    public void logout(String accessToken) {
        // 用户信息
        UserDetail user = tokenStoreCache.getUser(accessToken);

        // 删除用户信息
        tokenStoreCache.deleteUser(accessToken);

        // Token过期
        sysUserTokenService.expireToken(user.getId());

        // 保存登录日志
        sysLogLoginService.save(user.getUsername(), Constant.SUCCESS, LoginOperationEnum.LOGOUT_SUCCESS.getValue());
    }

    @Override
    public String getOpenIdByCode(String code) {
        // 微信小程序配置
        String appId = "wxf305c51a53760e43";
        String secret = "4505c07f3b820532117e2ec0192be088";

        OkHttpClient httpClient = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.weixin.qq.com/sns/jscode2session").newBuilder();
        urlBuilder.addQueryParameter("appid", appId);
        urlBuilder.addQueryParameter("secret", secret);
        urlBuilder.addQueryParameter("js_code", code);
        urlBuilder.addQueryParameter("grant_type", "authorization_code");
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();
        // 发送请求并处理响应
        try {
            Response firstResponse = httpClient.newCall(request).execute();
            // 检查响应码
            if (firstResponse.isSuccessful()) {
                // 请求成功，处理响应数据
                String openId = "获取失败";
                String responseBody = firstResponse.body().string();
                System.out.println("请求返回：" + responseBody);
                JSONObject json1 = new JSONObject(responseBody);
                if(json1.containsKey("openid")){
                    openId = json1.getStr("openid");
                }
                return openId;
            }
        } catch (Exception e) {
                // 网络异常处理
                e.printStackTrace();
        }

        return null;
    }
}
