package com.hxls.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.SysOrgConvert;
import com.hxls.system.dao.SysRoleDataScopeDao;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.entity.SysRoleDataScopeEntity;
import com.hxls.system.service.SysOrgService;
import com.hxls.system.service.SysRoleDataScopeService;
import com.hxls.system.vo.SysOrgVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色数据权限
 *
 * @author
 *
 */
@Service
public class SysRoleDataScopeServiceImpl extends BaseServiceImpl<SysRoleDataScopeDao, SysRoleDataScopeEntity>
        implements SysRoleDataScopeService {

    private final SysOrgService sysOrgService;

    public SysRoleDataScopeServiceImpl(SysOrgService sysOrgService) {
        this.sysOrgService = sysOrgService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(Long roleId, List<Long> orgIdList) {
        // 数据库机构ID列表
        List<Long> dbOrgIdList = getOrgIdList(roleId);

        // 需要新增的机构ID
        Collection<Long> insertOrgIdList = CollUtil.subtract(orgIdList, dbOrgIdList);
        if (CollUtil.isNotEmpty(insertOrgIdList)){
            List<SysRoleDataScopeEntity> orgList = insertOrgIdList.stream().map(orgId -> {
                SysRoleDataScopeEntity entity = new SysRoleDataScopeEntity();
                entity.setOrgId(orgId);
                entity.setRoleId(roleId);
                return entity;
            }).collect(Collectors.toList());

            // 批量新增
            saveBatch(orgList);
        }

        // 需要删除的机构ID
        Collection<Long> deleteOrgIdList = CollUtil.subtract(dbOrgIdList, orgIdList);
        if (CollUtil.isNotEmpty(deleteOrgIdList)){
            LambdaQueryWrapper<SysRoleDataScopeEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysRoleDataScopeEntity::getRoleId, roleId);
            queryWrapper.in(SysRoleDataScopeEntity::getOrgId, deleteOrgIdList);

            remove(queryWrapper);
        }
    }

    @Override
    public List<Long> getOrgIdList(Long roleId) {
        return baseMapper.getOrgIdList(roleId);
    }

    @Override
    public void deleteByRoleIdList(List<Long> roleIdList) {
        remove(new LambdaQueryWrapper<SysRoleDataScopeEntity>().in(SysRoleDataScopeEntity::getRoleId, roleIdList));
    }

    @Override
    public List<SysOrgVO> getOrgList(Long userId) {
        List<Long> dataScopeList = baseMapper.getDataScopeList(userId);
        List<SysOrgVO> orgVOS = new ArrayList<>();
        for(Long orgId : dataScopeList){
            SysOrgEntity byId = sysOrgService.getById(orgId);
            orgVOS.add(SysOrgConvert.INSTANCE.convert(byId));
        }
        return orgVOS;
    }
}
