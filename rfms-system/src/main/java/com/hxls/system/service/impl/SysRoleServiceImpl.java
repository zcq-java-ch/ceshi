package com.hxls.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.system.convert.SysRoleConvert;
import com.hxls.system.dao.SysRoleDao;
import com.hxls.system.dao.SysUserRoleDao;
import com.hxls.system.entity.SysRoleEntity;
import com.hxls.system.entity.SysUserRoleEntity;
import com.hxls.system.enums.DataScopeEnum;
import com.hxls.system.query.SysRoleQuery;
import com.hxls.system.service.*;
import com.hxls.system.vo.SysRoleDataScopeVO;
import com.hxls.system.vo.SysRoleVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 角色
 *
 * @author
 */
@Service
@AllArgsConstructor
public class SysRoleServiceImpl extends BaseServiceImpl<SysRoleDao, SysRoleEntity> implements SysRoleService {
    private final SysRoleMenuService sysRoleMenuService;
    private final SysRoleDataScopeService sysRoleDataScopeService;
    private final SysUserRoleService sysUserRoleService;
    private final SysUserRoleDao sysUserRoleDao;
    private final SysUserTokenService sysUserTokenService;

    @Override
    public PageResult<SysRoleVO> page(SysRoleQuery query) {

        IPage<SysRoleEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));
        return new PageResult<>(SysRoleConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    @Override
    public List<SysRoleVO> getList(SysRoleQuery query) {
        List<SysRoleEntity> entityList = baseMapper.selectList(getWrapper(query));

        return SysRoleConvert.INSTANCE.convertList(entityList);
    }

    private Wrapper<SysRoleEntity> getWrapper(SysRoleQuery query) {
        LambdaQueryWrapper<SysRoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getName()), SysRoleEntity::getName, query.getName());
        wrapper.eq(query.getStatus() != null, SysRoleEntity::getStatus, query.getStatus());
        // 数据权限
        dataScopeWrapperToRole(wrapper);

        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SysRoleVO vo) {
        SysRoleEntity entity = SysRoleConvert.INSTANCE.convert(vo);

        // 保存角色
        if (vo.getDataScope() ==null ){
            entity.setDataScope(DataScopeEnum.SELF.getValue());
        }

        baseMapper.insert(entity);
        // 保存角色菜单关系(pc端)
        sysRoleMenuService.saveOrUpdate(entity.getId(), vo.getMenuIdList(), 1);

        // 保存角色菜单关系（移动端）
        sysRoleMenuService.saveOrUpdate(entity.getId(), vo.getAppMenuIdList(), 2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysRoleVO vo) {
        SysRoleEntity entity = SysRoleConvert.INSTANCE.convert(vo);

        // 更新角色
        updateById(entity);
        Long id = vo.getId();
        List<SysUserRoleEntity> sysUserRoleEntityList = sysUserRoleDao.selectByStatus(id ,Objects.equals(vo.getStatus(), Constant.ENABLE) ? Constant.ENABLE : Constant.DISABLE );

        if (CollectionUtils.isNotEmpty(sysUserRoleEntityList)) {
            for (SysUserRoleEntity sysUserRoleEntity : sysUserRoleEntityList) {
                sysUserRoleEntity.setDeleted(Objects.equals(vo.getStatus(), Constant.ENABLE) ? Constant.DISABLE : Constant.ENABLE);
                sysUserRoleDao.updateByStatue(sysUserRoleEntity.getId() ,sysUserRoleEntity.getDeleted() );
            }
        }

        // 更新角色菜单关系
        sysRoleMenuService.saveOrUpdate(entity.getId(), vo.getMenuIdList(), 1);

        // 更新角色菜单关系
        sysRoleMenuService.saveOrUpdate(entity.getId(), vo.getAppMenuIdList(), 2);

        // 更新角色对应用户的缓存权限
        sysUserTokenService.updateCacheAuthByRoleId(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dataScope(SysRoleDataScopeVO vo) {
        SysRoleEntity entity = getById(vo.getId());
        entity.setDataScope(vo.getDataScope());
        // 更新角色
        updateById(entity);

        // 更新角色数据权限关系
        if (vo.getDataScope().equals(DataScopeEnum.CUSTOM.getValue())) {
            sysRoleDataScopeService.saveOrUpdate(entity.getId(), vo.getOrgIdList());
        } else {
            sysRoleDataScopeService.deleteByRoleIdList(Collections.singletonList(vo.getId()));
        }

        // 更新角色对应用户的缓存权限
        sysUserTokenService.updateCacheAuthByRoleId(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        // 删除角色
        removeByIds(idList);

        // 删除用户角色关系
        sysUserRoleService.deleteByRoleIdList(idList);

        // 删除角色菜单关系
        sysRoleMenuService.deleteByRoleIdList(idList);

        // 删除角色数据权限关系
        sysRoleDataScopeService.deleteByRoleIdList(idList);

        // 更新角色对应用户的缓存权限
        idList.forEach(sysUserTokenService::updateCacheAuthByRoleId);
    }


    @Override
    public void updateStatus(List<SysRoleVO> list) {
        for (SysRoleVO vo : list) {
            SysRoleEntity entity = new SysRoleEntity();
            entity.setId(vo.getId());
            if (vo.getStatus() != null) {
                entity.setStatus(vo.getStatus());
            }
            // 更新实体
            this.updateById(entity);
            //修改用户角色关联表的状态
            Long id = vo.getId();
            List<SysUserRoleEntity> sysUserRoleEntityList = sysUserRoleDao.selectByStatus(id ,Objects.equals(vo.getStatus(), Constant.ENABLE) ? Constant.ENABLE : Constant.DISABLE );

            if (CollectionUtils.isNotEmpty(sysUserRoleEntityList)) {
                for (SysUserRoleEntity sysUserRoleEntity : sysUserRoleEntityList) {
                    sysUserRoleEntity.setDeleted(Objects.equals(vo.getStatus(), Constant.ENABLE) ? Constant.DISABLE : Constant.ENABLE);
                    sysUserRoleDao.updateByStatue(sysUserRoleEntity.getId() ,sysUserRoleEntity.getDeleted() );
                }
            }
        }
    }

}
