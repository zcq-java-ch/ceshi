package com.hxls.system.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
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
import com.hxls.system.cache.MainPlatformCache;
import com.hxls.system.convert.SysOrgConvert;
import com.hxls.system.dao.SysOrgDao;
import com.hxls.system.dao.SysUserDao;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.service.SysOrgService;
import com.hxls.system.service.SysUserService;
import com.hxls.system.vo.MainPostVO;
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

    private LambdaQueryWrapper<SysOrgEntity> getWrapper(SysOrgQuery query) {
        LambdaQueryWrapper<SysOrgEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getId() != null, SysOrgEntity::getId, query.getId());
        wrapper.eq(SysOrgEntity::getDeleted, 0);
        wrapper.eq(query.getStatus() != null, SysOrgEntity::getStatus, query.getStatus());
        wrapper.like(StringUtils.isNotEmpty(query.getCode()), SysOrgEntity::getCode, query.getCode());

        List<String> codeList = new ArrayList<>();
        if (StringUtils.isNotEmpty(query.getPcode())) {
            List<SysOrgEntity> list = list();
            for (SysOrgEntity sysOrgEntity : list) {
                if (checkData(sysOrgEntity , list,query.getPcode())) {
                    codeList.add(sysOrgEntity.getCode());
                }
            }
        }

        wrapper.in(CollectionUtils.isNotEmpty(codeList), SysOrgEntity::getCode, codeList);
        wrapper.like(StringUtils.isNotEmpty(query.getName()), SysOrgEntity::getName, query.getName());
        wrapper.eq(query.getProperty() != null, SysOrgEntity::getProperty, query.getProperty());
        wrapper.in(CollectionUtils.isNotEmpty(query.getOrgList()), SysOrgEntity::getId, query.getOrgList());

        if (query.getCreator()!=null){
            wrapper.or().eq(SysOrgEntity::getCreator,query.getCreator());
        }
        return wrapper;
    }

    private Boolean checkData(SysOrgEntity sysOrgEntity, List<SysOrgEntity> list,String pcode) {

        if (sysOrgEntity.getPcode() == null){
            return false;
        }
        if ( sysOrgEntity.getPcode().equals(pcode)){
            return true;
        }
        for (SysOrgEntity item : list) {
            if (item.getCode().equals(sysOrgEntity.getPcode())) {
                return checkData(item, list,pcode);
            }
        }
        return false;
    }

    @Override
    public List<SysOrgVO> getList() {
        Map<String, Object> params = new HashMap<>();

        // 数据权限
        params.put(Constant.DATA_SCOPE, getDataScopeByCreator("t1", "id"));


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
        if (StringUtils.isEmpty(entity.getCode())) {
            entity.setCode("RFMS" + entity.getId());
            updateById(entity);
        }

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

        //修改成功后需要更换人员表中的名称
        List<SysUserEntity> sysUserEntities = sysUserDao.selectList(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getOrgId, entity.getId()));
        if (CollectionUtils.isNotEmpty(sysUserEntities)) {
            for (SysUserEntity item : sysUserEntities) {
                item.setOrgName(entity.getName());
                sysUserDao.updateById(item);
            }
        }

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
    public SysOrgEntity getByCode(String pcode) {
        LambdaQueryWrapper<SysOrgEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(SysOrgEntity::getCode, pcode);
        objectLambdaQueryWrapper.eq(SysOrgEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(SysOrgEntity::getDeleted, 0);
        List<SysOrgEntity> sysOrgEntities = baseMapper.selectList(objectLambdaQueryWrapper);
        if (CollectionUtils.isNotEmpty(sysOrgEntities)) {
            return sysOrgEntities.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void updateStatus(List<SysOrgVO> list) {
        for (SysOrgVO vo : list) {
            SysOrgEntity entity = new SysOrgEntity();
            entity.setId(vo.getId());
            if (vo.getStatus() != null) {
                entity.setStatus(vo.getStatus());
                // 更新实体
                baseMapper.updateById(entity);
            }
            if (vo.getDeleted() != null) {
                // 更新实体
                baseMapper.deleteById(entity);
            }

        }
    }

    @Override
    public SysOrgEntity getByCodeNoStatus(String code) {
        LambdaQueryWrapper<SysOrgEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrgEntity::getCode, code);
        List<SysOrgEntity> sysOrgEntities = baseMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(sysOrgEntities)) {
            return sysOrgEntities.get(0);
        } else {
            return null;
        }
    }
}
