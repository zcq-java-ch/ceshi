package com.hxls.system.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fhs.trans.service.impl.TransService;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.excel.ExcelFinishCallBack;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.DateUtils;
import com.hxls.framework.common.utils.ExcelUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.framework.security.cache.TokenStoreCache;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.framework.security.utils.TokenUtils;
import com.hxls.system.cache.MainPlatformCache;
import com.hxls.system.convert.SysUserConvert;
import com.hxls.system.dao.SysUserDao;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.enums.SuperAdminEnum;
import com.hxls.system.query.SysRoleUserQuery;
import com.hxls.system.query.SysUserQuery;
import com.hxls.system.service.*;
import com.hxls.system.vo.MainUserVO;
import com.hxls.system.vo.SysUserBaseVO;
import com.hxls.system.vo.SysUserExcelVO;
import com.hxls.system.vo.SysUserVO;
import com.squareup.okhttp.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理
 *
 * @author
 *
 */
@Service
@AllArgsConstructor
public class SysUserServiceImpl extends BaseServiceImpl<SysUserDao, SysUserEntity> implements SysUserService {
    private final SysUserRoleService sysUserRoleService;
    private final SysUserPostService sysUserPostService;
    private final SysUserTokenService sysUserTokenService;
    private final SysOrgService sysOrgService;
    private final TokenStoreCache tokenStoreCache;
    private final TransService transService;
    private final MainPlatformCache mainPlatformCache;

    @Override
    public PageResult<SysUserVO> page(SysUserQuery query) {
        // 查询参数
        Map<String, Object> params = getParams(query);

        // 分页查询
        IPage<SysUserEntity> page = getPage(query);
        params.put(Constant.PAGE, page);

        // 数据列表
        List<SysUserEntity> list = baseMapper.getList(params);

        return new PageResult<>(SysUserConvert.INSTANCE.convertList(list), page.getTotal());
    }

    private Map<String, Object> getParams(SysUserQuery query) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", query.getUsername());
        params.put("mobile", query.getMobile());
        params.put("gender", query.getGender());

        // 数据权限
        params.put(Constant.DATA_SCOPE, getDataScope("t1", null));

        // 机构过滤
        if (query.getOrgId() != null) {
            // 查询子机构ID列表，包含本机构
            List<Long> orgList = sysOrgService.getSubOrgIdList(query.getOrgId());
            params.put("orgList", orgList);
        }

        return params;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SysUserVO vo) {
        SysUserEntity entity = SysUserConvert.INSTANCE.convert(vo);
        entity.setSuperAdmin(SuperAdminEnum.NO.getValue());

        // 判断用户名是否存在
        SysUserEntity user = baseMapper.getByUsername(entity.getUsername());
        if (user != null) {
            throw new ServerException("用户名已经存在");
        }

        // 判断手机号是否存在
        user = baseMapper.getByMobile(entity.getMobile());
        if (user != null) {
            throw new ServerException("手机号已经存在");
        }

        // 保存用户
        baseMapper.insert(entity);

        // 保存用户角色关系
        sysUserRoleService.saveOrUpdate(entity.getId(), vo.getRoleIdList());

        // 更新用户岗位关系
        sysUserPostService.saveOrUpdate(entity.getId(), vo.getPostIdList());
    }

    @Override
    public void update(SysUserVO vo) {
        SysUserEntity entity = SysUserConvert.INSTANCE.convert(vo);

        // 判断用户名是否存在
        SysUserEntity user = baseMapper.getByUsername(entity.getUsername());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("用户名已经存在");
        }

        // 判断手机号是否存在
        user = baseMapper.getByMobile(entity.getMobile());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("手机号已经存在");
        }

        // 更新用户
        updateById(entity);

        // 更新用户角色关系
        sysUserRoleService.saveOrUpdate(entity.getId(), vo.getRoleIdList());

        // 更新用户岗位关系
        sysUserPostService.saveOrUpdate(entity.getId(), vo.getPostIdList());

        // 更新用户缓存权限
        sysUserTokenService.updateCacheAuthByUserId(entity.getId());
    }

    @Override
    public void updateLoginInfo(SysUserBaseVO vo, UserDetail LoginUser) {
        SysUserEntity entity = SysUserConvert.INSTANCE.convert(vo);
        // 设置登录用户ID
        entity.setId(LoginUser.getId());

        // 判断手机号是否存在
        SysUserEntity user = baseMapper.getByMobile(entity.getMobile());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("手机号已经存在");
        }
        // 更新用户
        updateById(entity);

        // 删除用户缓存
        tokenStoreCache.deleteUser(TokenUtils.getAccessToken());
    }

    @Override
    public void delete(List<Long> idList) {
        // 删除用户
        removeByIds(idList);

        // 删除用户角色关系
        sysUserRoleService.deleteByUserIdList(idList);

        // 删除用户岗位关系
        sysUserPostService.deleteByUserIdList(idList);
    }

    @Override
    public SysUserVO getByMobile(String mobile) {
        SysUserEntity user = baseMapper.getByMobile(mobile);

        return SysUserConvert.INSTANCE.convert(user);
    }

    @Override
    public void updatePassword(Long id, String newPassword) {
        // 修改密码
        SysUserEntity user = getById(id);
        user.setPassword(newPassword);

        updateById(user);
    }

    @Override
    public PageResult<SysUserVO> roleUserPage(SysRoleUserQuery query) {
        // 查询参数
        Map<String, Object> params = getParams(query);
        params.put("roleId", query.getRoleId());

        // 分页查询
        IPage<SysUserEntity> page = getPage(query);
        params.put(Constant.PAGE, page);

        // 数据列表
        List<SysUserEntity> list = baseMapper.getRoleUserList(params);

        return new PageResult<>(SysUserConvert.INSTANCE.convertList(list), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importByExcel(MultipartFile file, String password) {

        ExcelUtils.readAnalysis(file, SysUserExcelVO.class, new ExcelFinishCallBack<SysUserExcelVO>() {
            @Override
            public void doAfterAllAnalysed(List<SysUserExcelVO> result) {
                saveUser(result);
            }

            @Override
            public void doSaveBatch(List<SysUserExcelVO> result) {
                saveUser(result);
            }

            private void saveUser(List<SysUserExcelVO> result) {
                ExcelUtils.parseDict(result);
                List<SysUserEntity> sysUserEntities = SysUserConvert.INSTANCE.convertListEntity(result);
                sysUserEntities.forEach(user -> user.setPassword(password));
                saveBatch(sysUserEntities);
            }
        });

    }

    @Override
    @SneakyThrows
    public void export() {
        List<SysUserEntity> list = list(Wrappers.lambdaQuery(SysUserEntity.class).eq(SysUserEntity::getSuperAdmin, SuperAdminEnum.NO.getValue()));
        List<SysUserExcelVO> userExcelVOS = SysUserConvert.INSTANCE.convert2List(list);
        transService.transBatch(userExcelVOS);
        // 写到浏览器打开
        ExcelUtils.excelExport(SysUserExcelVO.class, "system_user_excel" + DateUtils.format(new Date()), null, userExcelVOS);
    }

    @Override
    public void cardLogin() {
        //请求小基础数据 1、请求登录接口，获取返回的token   2、根据token去请求小基础组织数据
        OkHttpClient client = new OkHttpClient();
        String jsonBody = "{\"idCard\":\"rfms\"}";
        // 创建RequestBody实例
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);

        // 构建请求
        Request request = new Request.Builder()
                .url("http://182.150.57.78:9096/MainPlatform/userLogin/cardLogin")
                .post(body) // 设置POST方法
                .addHeader("Content-Type", "application/json") // 通常OkHttp会自动设置，这里可以省略
                .build();

        // 发送请求并处理响应
        try {
            Response firstResponse = client.newCall(request).execute();

            if (firstResponse.isSuccessful()) {
                // 请求成功，处理响应数据
                String responseBody = firstResponse.body().string();
                // 解析响应体为Map，这里假设JSON结构是标准的
                JSONObject json1 = new JSONObject(responseBody);
                JSONObject json2 = new JSONObject(json1.get("data"));
                // 从响应中提取accessToken
                String accessToken = json2.get("accessToken").toString();
                //将accessToken存到缓存中，有效期一个半小时
                mainPlatformCache.saveAccessToken(accessToken);
            }

        }catch (Exception e) {
            // 网络异常处理
            e.printStackTrace();
        }

    }

    @Override
    public List<MainUserVO> queryByMainUsers() {
        OkHttpClient client = new OkHttpClient();
        //获取mainAccessToken
        String accessToken = mainPlatformCache.getAccessToken();
        if(!StringUtils.isNotEmpty(accessToken)){//如果mainAccessToken过期的话，就重新更新mainAccessToken
            cardLogin();
            //更新accessToken
            accessToken = mainPlatformCache.getAccessToken();
        }

        String secondRequestUrl = "http://182.150.57.78:9096/MainPlatform/travel/employee/queryList";
        RequestBody secondRequestBody = RequestBody.create(MediaType.parse("application/json"), "");
        Request secondRequest = new Request.Builder()
                .url(secondRequestUrl)
                .post(secondRequestBody)
                .addHeader("access-token", accessToken) // 将accessToken放入请求头
                .build();


        try {
            Response secondResponse = client.newCall(secondRequest).execute();
            if (secondResponse.isSuccessful()) {
                JSONObject rel1 = new JSONObject(secondResponse.body().string());
                List<MainUserVO> mainUserVOS = JSONUtil.toBean(rel1.get("data").toString(), new TypeReference<List<MainUserVO>>() {}, true);
                return mainUserVOS;
            }

        }catch (Exception e) {
            // 网络异常处理
            e.printStackTrace();
        }

        return null;
    }

}
