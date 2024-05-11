package com.hxls.system.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.ContentType;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fhs.trans.service.impl.TransService;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.excel.ExcelFinishCallBack;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.DateUtils;
import com.hxls.framework.common.utils.ExcelUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.framework.security.cache.TokenStoreCache;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.framework.security.utils.TokenUtils;
import com.hxls.storage.properties.StorageProperties;
import com.hxls.system.cache.MainPlatformCache;
import com.hxls.system.convert.SysUserConvert;
import com.hxls.system.dao.SysUserDao;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.enums.SuperAdminEnum;
import com.hxls.system.query.SysRoleUserQuery;
import com.hxls.system.query.SysUserQuery;
import com.hxls.system.service.*;
import com.hxls.system.vo.*;
import com.squareup.okhttp.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Slf4j
public class SysUserServiceImpl extends BaseServiceImpl<SysUserDao, SysUserEntity> implements SysUserService {
    private final SysUserRoleService sysUserRoleService;
    private final SysUserPostService sysUserPostService;
    private final SysUserTokenService sysUserTokenService;
    private final SysOrgService sysOrgService;
    private final TokenStoreCache tokenStoreCache;
    private final TransService transService;
    private final MainPlatformCache mainPlatformCache;
    private final AppointmentFeign appointmentFeign;
    private final StorageProperties properties;
    private final PasswordEncoder passwordEncoder;

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


    @Override
    public PageResult<SysUserVO> pageByGys(SysUserQuery query) {
        // 查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("username", query.getUsername());
        params.put("mobile", query.getMobile());
        params.put("gender", query.getGender());
        params.put("userType", query.getUserType());
        params.put("supervisor", query.getSupervisor());
        params.put("licensePlate", query.getLicensePlate());
        params.put("code", query.getCode());
        params.put("status", query.getStatus());
        params.put("realName", query.getRealName());
        params.put("orgName", query.getOrgName());
        params.put("orgId", query.getOrgId());
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
        params.put("userType", query.getUserType());
        params.put("supervisor", query.getSupervisor());
        params.put("licensePlate", query.getLicensePlate());
        params.put("code", query.getCode());
        params.put("status", query.getStatus());
        params.put("realName", query.getRealName());
        params.put("orgName", query.getOrgName());


        if(query.getOrgId() != null){
            List<Long> subOrgIdList = sysOrgService.getSubOrgIdList(query.getOrgId());
            params.put("orgList", subOrgIdList);
        }

        // 数据权限
        params.put(Constant.DATA_SCOPE, getDataScope("t1", null));

        return params;
    }

    private Map<String, Object> getParamsByNoAuth(SysUserQuery query) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", query.getUsername());
        params.put("mobile", query.getMobile());
        params.put("gender", query.getGender());
        params.put("userType", query.getUserType());
        params.put("supervisor", query.getSupervisor());
        params.put("licensePlate", query.getLicensePlate());
        params.put("code", query.getCode());
        params.put("status", query.getStatus());
        params.put("realName", query.getRealName());
        params.put("orgName", query.getOrgName());
        params.put("orgId", query.getOrgId());

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

        // 判断用户名是否存在
        user = baseMapper.getByMobile(entity.getUsername());
        if (user != null) {
            throw new ServerException("用户名已经存在");
        }

        // 判断手机号是否存在
        user = baseMapper.getByUsername(entity.getMobile());
        if (user != null) {
            throw new ServerException("手机号已经存在");
        }

        // 判断手机号是否存在
        user = baseMapper.getByMobile(entity.getMobile());
        if (user != null) {
            throw new ServerException("手机号已经存在");
        }

        // 保存用户
        baseMapper.insert(entity);

        //TODO  添加用户的时候人脸下发  还需要判断是否有场站
        if (entity.getStationId() != null && entity.getStationId().equals( Constant.EMPTY )) {
            JSONObject person = new JSONObject();
            person.set("sendType","1");
            person.set("data" , JSONUtil.toJsonStr(entity));
            appointmentFeign.issuedPeople(person);
            //TODO 添加用户的时候车辆下发 ---判断是否有值
            if (StringUtils.isNotEmpty(entity.getLicensePlate())){
                JSONObject vehicle = new JSONObject();
                vehicle.set("sendType","2");
                vehicle.set("data" , JSONUtil.toJsonStr(entity));
                appointmentFeign.issuedPeople(vehicle);
            }
        }

        // 保存用户角色关系
        sysUserRoleService.saveOrUpdate(entity.getId(), vo.getRoleIdList());

        // 更新用户岗位关系
        sysUserPostService.saveOrUpdate(entity.getId(), vo.getPostIdList());
    }

    @Override
    public void update(SysUserVO vo) {
        SysUserEntity entity = SysUserConvert.INSTANCE.convert(vo);
        if ( entity.getStationId() == null ){
            entity.setStationId(Constant.EMPTY);
        }
        //判断是否需要删除原有厂站的数据
        Long id = vo.getId();
        SysUserEntity byId = baseMapper.getById(id);
        if (byId == null ){
            throw new ServerException(ErrorCode.NOT_FOUND);
        }
        if (byId.getStationId() != null &&  !byId.getStationId().equals(Constant.EMPTY) && !byId.getStationId().equals(entity.getStationId())){
            System.out.println("开始删除之前的");
            JSONObject person = new JSONObject();
            person.set("sendType","1");
            person.set("data" , JSONUtil.toJsonStr(byId));
            person.set("DELETE" , "DELETE");
            appointmentFeign.issuedPeople(person);
            if (StringUtils.isNotEmpty(entity.getLicensePlate())){
                JSONObject vehicle = new JSONObject();
                vehicle.set("sendType","2");
                vehicle.set("data" , JSONUtil.toJsonStr(byId));
                vehicle.set("DELETE" , "DELETE");
                appointmentFeign.issuedPeople(vehicle);
            }
        }

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

        // 判断用户名是否存在
        user = baseMapper.getByMobile(entity.getUsername());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("用户名已经存在");
        }

        // 判断手机号是否存在
        user = baseMapper.getByUsername(entity.getMobile());
        if (user != null && !user.getId().equals(entity.getId())) {
            throw new ServerException("手机号已经存在");
        }

        // 更新用户
        updateById(entity);

        if (entity.getStationId() !=null && !entity.getStationId().equals(Constant.EMPTY)) {
            JSONObject person = new JSONObject();
            person.set("sendType","1");
            person.set("data" , JSONUtil.toJsonStr(entity));
            appointmentFeign.issuedPeople(person);

            if (StringUtils.isNotEmpty(entity.getLicensePlate())){
                JSONObject vehicle = new JSONObject();
                vehicle.set("sendType","2");
                vehicle.set("data" , JSONUtil.toJsonStr(entity));
                appointmentFeign.issuedPeople(vehicle);
            }
        }


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
    public void importByExcel(String file, String password,Long orgId) {
        try{
            SysOrgEntity byId = sysOrgService.getById(orgId);

            //导入时候获取的地址是相对路径 需要拼接服务器路径
            String domain = properties.getConfig().getDomain();
            ExcelUtils.readAnalysis(ExcelUtils.convertToMultipartFile(domain+file), SysUserGysExcelVO.class, new ExcelFinishCallBack<SysUserGysExcelVO>() {
                @Override
                public void doAfterAllAnalysed(List<SysUserGysExcelVO> result) {
                    saveUser(result);
                }

                @Override
                public void doSaveBatch(List<SysUserGysExcelVO> result) {
                    saveUser(result);
                }

                private void saveUser(List<SysUserGysExcelVO> result) {
                    ExcelUtils.parseDict(result);
                    List<SysUserEntity> sysUserEntities = SysUserConvert.INSTANCE.convertListEntity(result);
                    sysUserEntities.forEach(user -> {
                        // 判断手机号是否存在
                        SysUserEntity  olduser = baseMapper.getByUsername(user.getMobile());
                        if (olduser != null) {
                            throw new ServerException("手机号已经存在");
                        }
                        // 判断手机号是否存在
                        olduser = baseMapper.getByMobile(user.getMobile());
                        if (olduser != null) {
                            throw new ServerException("手机号已经存在");
                        }
                        user.setUserType("2");
                        user.setPassword(password);
                        user.setOrgId(orgId);
                        user.setUsername(user.getMobile());
                        user.setOrgName(byId.getName());
                        user.setStatus(1);
                        user.setSuperAdmin(0);
                    });
                    saveBatch(sysUserEntities);
                }
            });
        }catch (Exception e){
            throw new ServerException("导入模板不正确");
        }


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

//    @Override
//    public void cardLogin() {
//        //请求小基础数据 1、请求登录接口，获取返回的token   2、根据token去请求小基础组织数据
//        OkHttpClient client = new OkHttpClient();
//        String jsonBody = "{\"idCard\":\"rfms\"}";
//        // 创建RequestBody实例
//        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);
//
//        // 构建请求
//        Request request = new Request.Builder()
//                .url("https://jcmdm.huashijc.com/MainPlatform/userLogin/cardLogin")
//                .post(body) // 设置POST方法
//                .addHeader("Content-Type", "application/json") // 通常OkHttp会自动设置，这里可以省略
//                .build();
//
//        // 发送请求并处理响应
//        try {
//            Response firstResponse = client.newCall(request).execute();
//
//            if (firstResponse.isSuccessful()) {
//                // 请求成功，处理响应数据
//                String responseBody = firstResponse.body().string();
//                // 解析响应体为Map，这里假设JSON结构是标准的
//                JSONObject json1 = new JSONObject(responseBody);
//                JSONObject json2 = new JSONObject(json1.get("data"));
//                // 从响应中提取accessToken
//                String accessToken = json2.get("accessToken").toString();
//                //将accessToken存到缓存中，有效期一个半小时
//                mainPlatformCache.saveAccessToken(accessToken);
//            }
//
//        }catch (Exception e) {
//            // 网络异常处理
//            e.printStackTrace();
//        }
//
//    }

    @Override
    public void cardLogin() {
        String url = "https://jcmdm.huashijc.com/MainPlatform/userLogin/cardLogin";
        String jsonBody = "{\"idCard\":\"rfms\"}";

        // 发送POST请求
        HttpResponse response = HttpUtil.createPost(url)
                .body(jsonBody, ContentType.JSON.toString())
                .execute();

        // 处理响应
        if (response.isOk()) {
            // 请求成功，处理响应数据
            String responseBody = response.body();
            JSONObject json1 = new JSONObject(responseBody);
            JSONObject json2 = json1.getJSONObject("data");
            String accessToken = json2.get("accessToken").toString();
            //将accessToken存到缓存中，有效期一个半小时
            mainPlatformCache.saveAccessToken(accessToken);
        } else {
            // 请求失败，处理错误信息
            System.out.println("Request failed: " + response.getStatus() + ", " + response.body());
        }
    }


    @Override
    public List<MainUserVO> queryByMainUsers() {
        //获取mainAccessToken
        String accessToken = mainPlatformCache.getAccessToken();
        if(!StringUtils.isNotEmpty(accessToken)){//如果mainAccessToken过期的话，就重新更新mainAccessToken
            cardLogin();
            //更新accessToken
            accessToken = mainPlatformCache.getAccessToken();
        }

        String secondRequestUrl = "https://jcmdm.huashijc.com/MainPlatform/travel/employee/queryList";
        HttpResponse httpResponse = HttpUtil.createPost(secondRequestUrl)
                .header("access-token", accessToken)  // 设置请求头
                .execute();

        // 处理响应
        if (httpResponse.isOk()) {
            // 获取响应体
            String body = httpResponse.body();
            JSONObject rel1 = new JSONObject(body);
            List<MainUserVO> mainUserVOS = JSONUtil.toBean(rel1.get("data").toString(), new TypeReference<List<MainUserVO>>() {}, true);
            return mainUserVOS;
        }else {
            throw new ServerException("请求主数据人员异常");
        }
    }

    @Override
    public void updateStatus(List<SysUserVO> list) {
        for (SysUserVO vo : list) {
            SysUserEntity entity = new SysUserEntity();
            entity.setId(vo.getId());

            //查询人员详情
            SysUserEntity byId = getById(vo.getId());

            if(vo.getStatus() != null ){
                entity.setStatus(vo.getStatus());
            }
            // 更新实体
            this.updateById(entity);
            //更新成功后 - 下发设备指令
            JSONObject person = new JSONObject();
            person.set("sendType","1");
            person.set("data" , JSONUtil.toJsonStr(byId));
            if (!vo.getStatus().equals(Constant.ENABLE)){
                log.info("此时发起禁用删除");
                person.set("DELETE" , "DELETE");
            }
            appointmentFeign.issuedPeople(person);
            if (StringUtils.isNotEmpty(entity.getLicensePlate())){
                JSONObject vehicle = new JSONObject();
                vehicle.set("sendType","2");
                vehicle.set("data" , JSONUtil.toJsonStr(byId));
                if (!vo.getStatus().equals(Constant.ENABLE)){
                    log.info("此时发起禁用删除");
                    vehicle.set("DELETE" , "DELETE");
                }
                appointmentFeign.issuedPeople(vehicle);
            }
        }
    }

    @Override
    public PageResult<SysUserVO> pageByNoAuth(SysUserQuery query) {
        // 查询参数
        Map<String, Object> params = getParamsByNoAuth(query);

        // 分页查询
        IPage<SysUserEntity> page = getPage(query);
        params.put(Constant.PAGE, page);

        // 数据列表
        List<SysUserEntity> list = baseMapper.getList(params);

        return new PageResult<>(SysUserConvert.INSTANCE.convertList(list), page.getTotal());
    }

    @Override
    public void updateByUser(SysUserVO vo) {
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

        if (entity.getStationId() !=null) {
            //TODO 修改用户的时候人脸下发
            JSONObject person = new JSONObject();
            person.set("sendType","1");
            person.set("data" , JSONUtil.toJsonStr(entity));
            appointmentFeign.issuedPeople(person);

            //TODO 修改用户的时候车辆下发 ---判断是否有值

            if (StringUtils.isNotEmpty(entity.getLicensePlate())){
                JSONObject vehicle = new JSONObject();
                vehicle.set("sendType","2");
                vehicle.set("data" , JSONUtil.toJsonStr(entity));
                appointmentFeign.issuedPeople(vehicle);
            }
        }
    }

    @Override
    public void synOrg() {
        //获取mainAccessToken
        String accessToken = mainPlatformCache.getAccessToken();
        if(!com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotEmpty(accessToken)){//如果mainAccessToken过期的话，就重新更新mainAccessToken
            cardLogin();
            //更新accessToken
            accessToken = mainPlatformCache.getAccessToken();
        }

        String secondRequestUrl = "https://jcmdm.huashijc.com/MainPlatform/travel/administrative_organization/query_page";
        JSONObject params = new JSONObject();
        params.set("page",1);
        params.set("pageSize",1000000);
        params.set("status",1);

        // 发送POST请求
        HttpResponse response = HttpUtil.createPost(secondRequestUrl)
                .header("access-token", accessToken)
                .body(params.toString())
                .execute();

        // 处理响应
        if (response.isOk()) {
            JSONObject rel1 = JSONUtil.parseObj(response.body());
            JSONObject rel2 = JSONUtil.parseObj(rel1.get("data"));
            List<OrganizationVO> organizationList = JSONUtil.toBean(rel2.get("data").toString(), new TypeReference<List<OrganizationVO>>() {}, true);
            // 处理解析后的数据
            for (OrganizationVO organization : organizationList) {
                SysOrgVO sysOrgEntity = new SysOrgVO();
                sysOrgEntity.setCode(organization.getCode());
                sysOrgEntity.setName(organization.getName());
                sysOrgEntity.setPcode(organization.getPcode());
                sysOrgEntity.setPname(organization.getPname());
                sysOrgEntity.setSort(1);
                sysOrgEntity.setStatus(1);
                sysOrgEntity.setProperty(Integer.parseInt(organization.getProperty()+""));
                sysOrgEntity.setVirtualFlag(0);
                sysOrgService.save(sysOrgEntity);
            }
        } else {
            throw new ServerException("请求主数据岗位异常");
        }
    }

    @Override
    public void synUser() {
        //获取mainAccessToken
        String accessToken = mainPlatformCache.getAccessToken();
        //如果mainAccessToken过期的话，就重新更新mainAccessToken
        if(!StringUtils.isNotEmpty(accessToken)){
            cardLogin();
            //更新accessToken
            accessToken = mainPlatformCache.getAccessToken();
        }

        String secondRequestUrl = "https://jcmdm.huashijc.com/MainPlatform/travel/employee/query_page";
        JSONObject params = new JSONObject();
        params.set("page",1);
        params.set("pageSize",1000000);
        params.set("status",1);

        HttpResponse httpResponse = HttpUtil.createPost(secondRequestUrl)
                .header("access-token", accessToken)
                .body(params.toString())
                .execute();

        // 处理响应
        if (httpResponse.isOk()) {
            // 获取响应体
            String body = httpResponse.body();
            JSONObject rel1 = new JSONObject(body);
            JSONObject rel2 = JSONUtil.parseObj(rel1.get("data"));
            List<MainUserVO> mainUserVOS = JSONUtil.toBean(rel2.get("data").toString(), new TypeReference<List<MainUserVO>>() {}, true);
            for(MainUserVO mainUserVO:mainUserVOS){
                //判断员工是在职状态
                if("001".equals(mainUserVO.getRelationStatusCode())){
                    SysUserEntity one = baseMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUsername, mainUserVO.getPhone()));
                    if(one == null){
                        one = baseMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getMobile, mainUserVO.getPhone()));
                    }
                    List<SysOrgEntity> orgs = sysOrgService.list(new LambdaQueryWrapper<SysOrgEntity>().eq(SysOrgEntity::getCode, mainUserVO.getSysRefs().get(0).get("deptCode")));
                    //如果已经存在的用户，更新组织
                    if (one!=null){
                        if (CollectionUtils.isNotEmpty(orgs)){
                            one.setOrgId(orgs.get(0).getId());
                            one.setOrgName(orgs.get(0).getName());
                            one.setPostName(mainUserVO.getSysRefs().get(0).get("positionName"));
                            baseMapper.updateById(one);
                        }
                    }else{
                        SysUserEntity user = new SysUserEntity();
                        user.setCode(mainUserVO.getCode());
                        user.setUserType("1");
                        user.setUsername(mainUserVO.getPhone());
                        user.setRealName(mainUserVO.getName());
                        user.setPassword(passwordEncoder.encode("hxls123456"));
                        user.setMobile(mainUserVO.getPhone());
                        user.setStatus(1);
                        user.setSuperAdmin(0);

                        if (CollectionUtils.isNotEmpty(orgs)){
                            user.setOrgId(orgs.get(0).getId());
                            user.setOrgName(orgs.get(0).getName());
                            user.setPostName(mainUserVO.getSysRefs().get(0).get("positionName"));
                        }else{
                            user.setOrgName("无组织");
                        }
                        baseMapper.insert(user);
                    }
                }

            }
        }else {
            throw new ServerException("同步主数据人员异常");
        }
    }


}
