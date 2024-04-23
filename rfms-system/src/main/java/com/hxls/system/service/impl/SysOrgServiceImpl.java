package com.hxls.system.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.TreeByCodeUtils;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.SysOrgConvert;
import com.hxls.system.dao.SysOrgDao;
import com.hxls.system.dao.SysUserDao;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.service.SysOrgService;
import com.hxls.system.vo.OrganizationVO;
import com.hxls.system.vo.SysOrgVO;
import com.squareup.okhttp.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机构管理
 *
 * @author
 *
 */
@Service
@AllArgsConstructor
public class SysOrgServiceImpl extends BaseServiceImpl<SysOrgDao, SysOrgEntity> implements SysOrgService {
    private final SysUserDao sysUserDao;

    @Override
    public PageResult<SysOrgVO> page(SysOrgQuery query) {
        IPage<SysOrgEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(SysOrgConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<SysOrgEntity> getWrapper(SysOrgQuery query){
        LambdaQueryWrapper<SysOrgEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysOrgEntity::getDeleted, 0);
        wrapper.eq(SysOrgEntity::getStatus, 1);
        wrapper.like(StringUtils.isNotEmpty(query.getCode()), SysOrgEntity::getCode, query.getCode());
        wrapper.like(StringUtils.isNotEmpty(query.getPcode()), SysOrgEntity::getPcode, query.getPcode());
        wrapper.like(StringUtils.isNotEmpty(query.getName()), SysOrgEntity::getName, query.getName());
        wrapper.eq(query.getProperty() != null, SysOrgEntity::getProperty, query.getProperty());
        wrapper.in(CollectionUtils.isNotEmpty(query.getOrgList()),SysOrgEntity::getId, query.getOrgList());

        return wrapper;
    }

    @Override
    public List<SysOrgVO> getList() {
        Map<String, Object> params = new HashMap<>();

        // 数据权限
        params.put(Constant.DATA_SCOPE, getDataScope("t1", "id"));

        // 机构列表
        List<SysOrgEntity> entityList = baseMapper.getList(params);

        return TreeByCodeUtils.build(SysOrgConvert.INSTANCE.convertList(entityList));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SysOrgVO vo) {
        SysOrgEntity entity = SysOrgConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);

        //添加组织编码(RFMS + 组织ID)
        entity.setCode("RFMS"+entity.getId());
        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysOrgVO vo) {
        SysOrgEntity entity = SysOrgConvert.INSTANCE.convert(vo);

        // 上级机构不能为自身
        if (entity.getCode().equals(entity.getPcode())) {
            throw new ServerException("上级机构不能为自身");
        }

        // 上级机构不能为下级
        List<String> subOrgList = getSubOrgCodeList(entity.getCode());
        if (subOrgList.contains(entity.getPcode())) {
            throw new ServerException("上级机构不能为下级");
        }

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 判断是否有子机构
        long orgCount = count(new QueryWrapper<SysOrgEntity>().eq("pid", id));
        if (orgCount > 0) {
            throw new ServerException("请先删除子机构");
        }

        // 判断机构下面是否有用户
        long userCount = sysUserDao.selectCount(new QueryWrapper<SysUserEntity>().eq("org_id", id));
        if (userCount > 0) {
            throw new ServerException("机构下面有用户，不能删除");
        }

        // 删除
        removeById(id);
    }


    public List<String> getSubOrgCodeList(String code) {
        // 所有机构的id、pid列表
        List<SysOrgEntity> orgList = baseMapper.getIdAndPidList();

        // 递归查询所有子机构ID列表
        List<String> subIdList = new ArrayList<>();
        getTreeByCode(code, orgList, subIdList);

        // 本机构也添加进去
        subIdList.add(code);

        return subIdList;
    }

    private void getTreeByCode(String code, List<SysOrgEntity> orgList, List<String> subIdList) {
        for (SysOrgEntity org : orgList) {
            if (ObjectUtil.equals(org.getPcode(), code)) {
                getTreeByCode(org.getCode(), orgList, subIdList);
                subIdList.add(org.getCode());
            }
        }
    }


    @Override
    public List<Long> getSubOrgIdList(Long id) {

        //根据id获取机构code
        SysOrgEntity sysOrgEntity = baseMapper.selectById(id);

        // 根据code获取所有下级code
        List<String> subOrgCodeList = getSubOrgCodeList(sysOrgEntity.getCode());

        //遍历所有的code获取所有的id
        List<Long> subIdList = baseMapper.getIdsByCodes(subOrgCodeList);

        return subIdList;
    }

    private void getTree(Long id, List<SysOrgEntity> orgList, List<Long> subIdList) {
        for (SysOrgEntity org : orgList) {
            if (ObjectUtil.equals(org.getPcode(), id)) {
                getTree(org.getId(), orgList, subIdList);

                subIdList.add(org.getId());
            }
        }
    }


    @Override
    public void synOrg() {
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

            // 检查响应码
            if (firstResponse.isSuccessful()) {
                // 请求成功，处理响应数据
                String responseBody = firstResponse.body().string();
                // 解析响应体为Map，这里假设JSON结构是标准的
                JSONObject json1 = new JSONObject(responseBody);
                JSONObject json2 = new JSONObject(json1.get("data"));
                // 从响应中提取accessToken
                String accessToken =json2.get("accessToken").toString();
                if (accessToken != null) {
                    // 第二次请求的URL和请求头
                    String secondRequestUrl = "http://182.150.57.78:9096/MainPlatform/travel/administrative_organization/query_page"; // 替换为实际的第二个接口URL
                    JSONObject params = new JSONObject();
                    params.set("page",1);
                    params.set("pageSize",1000000);
                    params.set("status",1);
                    RequestBody secondRequestBody = RequestBody.create(MediaType.parse("application/json"), params.toString()); // 第二个请求的JSON体
                    Request secondRequest = new Request.Builder()
                            .url(secondRequestUrl)
                            .post(secondRequestBody)
                            .addHeader("access-token", accessToken) // 将accessToken放入请求头
                            .build();

                    // 发送第二个请求
                    Response secondResponse = client.newCall(secondRequest).execute();
                    if (secondResponse.isSuccessful()) {
                        JSONObject rel1 = new JSONObject(secondResponse.body().string());
                        JSONObject rel2 = new JSONObject(rel1.get("data"));
                        List<OrganizationVO> organizationList = JSONUtil.toBean(rel2.get("data").toString(), new TypeReference<List<OrganizationVO>>() {}, true);
                        // 处理解析后的数据
                        for (OrganizationVO organization : organizationList) {
                            // 例如，打印每个OrganizationVO的名称
                            System.out.println(organization.getName());

                            SysOrgEntity sysOrgEntity = new SysOrgEntity();

                            sysOrgEntity.setCode(organization.getCode());
                            sysOrgEntity.setName(organization.getName());
                            sysOrgEntity.setPcode(organization.getPcode());
                            sysOrgEntity.setPname(organization.getPname());
                            sysOrgEntity.setSort(1);
                            sysOrgEntity.setStatus(1);
                            sysOrgEntity.setProperty(Integer.parseInt(organization.getProperty()+""));
                            sysOrgEntity.setVirtualFlag(0);
                            baseMapper.insert(sysOrgEntity);
                        }
                    }

                }
            }
        } catch (Exception e) {
            // 网络异常处理
            e.printStackTrace();
        }
        //根据小基础数据添加未同步的组织数据

    }

    @Override
    public SysOrgEntity getByCode(String pcode) {
        LambdaQueryWrapper<SysOrgEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(SysOrgEntity::getCode, pcode);
        objectLambdaQueryWrapper.eq(SysOrgEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(SysOrgEntity::getDeleted, 0);
        List<SysOrgEntity> sysOrgEntities = baseMapper.selectList(objectLambdaQueryWrapper);
        if(CollectionUtils.isNotEmpty(sysOrgEntities)){
            return sysOrgEntities.get(0);
        }else {
            return null;
        }
    }

    @Override
    public void updateStatus(List<SysOrgVO> list) {
        for (SysOrgVO vo : list) {
            SysOrgEntity entity = new SysOrgEntity();
            entity.setId(vo.getId());
            if(vo.getStatus() != null ){
                entity.setStatus(vo.getStatus());
            }
            if(vo.getDeleted() != null ){
                entity.setDeleted(vo.getDeleted());
            }
            // 更新实体
            this.updateById(entity);
        }
    }
}
