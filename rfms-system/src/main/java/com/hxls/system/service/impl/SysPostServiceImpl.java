package com.hxls.system.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.cache.MainPlatformCache;
import com.hxls.system.convert.SysPostConvert;
import com.hxls.system.dao.SysPostDao;
import com.hxls.system.entity.SysPostEntity;
import com.hxls.system.query.SysPostQuery;
import com.hxls.system.service.SysPostService;
import com.hxls.system.service.SysUserPostService;
import com.hxls.system.service.SysUserService;
import com.hxls.system.vo.MainPostVO;
import com.hxls.system.vo.MainUserVO;
import com.hxls.system.vo.SysPostVO;
import com.squareup.okhttp.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 岗位管理
 *
 * @author
 *
 */
@Service
@AllArgsConstructor
public class SysPostServiceImpl extends BaseServiceImpl<SysPostDao, SysPostEntity> implements SysPostService {
    private final SysUserPostService sysUserPostService;
    private final MainPlatformCache mainPlatformCache;
    private final SysUserService sysUserService;

    @Override
    public PageResult<SysPostVO> page(SysPostQuery query) {
        IPage<SysPostEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(SysPostConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    @Override
    public List<SysPostVO> getList() {
        SysPostQuery query = new SysPostQuery();
        //正常岗位列表
        query.setStatus(1);
        List<SysPostEntity> entityList = baseMapper.selectList(getWrapper(query));

        return SysPostConvert.INSTANCE.convertList(entityList);
    }

    @Override
    public List<String> getNameList(List<Long> idList) {
        if (idList.isEmpty()) {
            return null;
        }

        return baseMapper.selectBatchIds(idList).stream().map(SysPostEntity::getPostName).toList();
    }

    private Wrapper<SysPostEntity> getWrapper(SysPostQuery query) {
        LambdaQueryWrapper<SysPostEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getPostCode()), SysPostEntity::getPostCode, query.getPostCode());
        wrapper.like(StrUtil.isNotBlank(query.getPostName()), SysPostEntity::getPostName, query.getPostName());
        wrapper.eq(query.getStatus() != null, SysPostEntity::getStatus, query.getStatus());
        wrapper.orderByAsc(SysPostEntity::getSort);

        return wrapper;
    }

    @Override
    public void save(SysPostVO vo) {
        SysPostEntity entity = SysPostConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(SysPostVO vo) {
        SysPostEntity entity = SysPostConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        // 删除岗位
        removeByIds(idList);

        // 删除岗位用户关系
        sysUserPostService.deleteByPostIdList(idList);
    }

    @Override
    public List<MainPostVO> queryByMainPosts(SysPostQuery query) {
        OkHttpClient client = new OkHttpClient();
        //获取mainAccessToken
        String accessToken = mainPlatformCache.getAccessToken();
        if(!StringUtils.isNotEmpty(accessToken)){//如果mainAccessToken过期的话，就重新更新mainAccessToken
            sysUserService.cardLogin();
            //更新accessToken
            accessToken = mainPlatformCache.getAccessToken();
        }

        String secondRequestUrl = "http://182.150.57.78:9096/MainPlatform/travel/main_post/query_page";
        JSONObject params = new JSONObject();
        params.set("page",query.getPage());
        params.set("pageSize",query.getLimit());
        // 将参数封装到请求体中
        RequestBody secondRequestBody = RequestBody.create(MediaType.parse("application/json"), params.toString());
        // 将accessToken放入请求头
        Request secondRequest = new Request.Builder()
                .url(secondRequestUrl)
                .post(secondRequestBody)
                .addHeader("access-token", accessToken)
                .build();
        // 执行请求
        try {
            Response secondResponse = client.newCall(secondRequest).execute();
            if (secondResponse.isSuccessful()) {
                JSONObject rel1 = new JSONObject(secondResponse.body().string());
                System.out.println(rel1);
                JSONObject rel2 = new JSONObject(rel1.get("data").toString());
                List<MainPostVO> mainPostVOS = JSONUtil.toBean(rel2.get("data").toString(), new TypeReference<List<MainPostVO>>() {}, true);
                return mainPostVOS;
            }

        }catch (Exception e) {
            // 网络异常处理
            e.printStackTrace();
        }

        return null;
    }

}
