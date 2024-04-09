package com.hxls.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.TVehicleConvert;
import com.hxls.system.dao.TVehicleDao;
import com.hxls.system.entity.TVehicleEntity;
import com.hxls.system.query.TVehicleQuery;
import com.hxls.system.service.TVehicleService;
import com.hxls.system.vo.TVehicleVO;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通用车辆管理表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-15
 */
@Service
@AllArgsConstructor
public class TVehicleServiceImpl extends BaseServiceImpl<TVehicleDao, TVehicleEntity> implements TVehicleService {

    @Override
    public PageResult<TVehicleVO> page(TVehicleQuery query) {
        IPage<TVehicleEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(TVehicleConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TVehicleEntity> getWrapper(TVehicleQuery query){
        LambdaQueryWrapper<TVehicleEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getSiteId() != null, TVehicleEntity::getSiteId, query.getSiteId());
        wrapper.like(StringUtils.isNotEmpty(query.getLicensePlate()), TVehicleEntity::getLicensePlate, query.getLicensePlate());
        wrapper.eq(query.getDriverId() != null, TVehicleEntity::getDriverId, query.getDriverId());
        wrapper.eq(query.getStatus() != null, TVehicleEntity::getStatus, query.getStatus());
        return wrapper;
    }

    @Override
    public void save(TVehicleVO vo) {
        TVehicleEntity entity = TVehicleConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(TVehicleVO vo) {
        TVehicleEntity entity = TVehicleConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

    /**
     * 通过车牌号，查询车辆基本信息
     * @param data 入参车牌号
     * @return 返回车辆信息
     */
    @Override
    public List<TVehicleVO> getByLicensePlates(List<String> data) {
        List<TVehicleEntity> list = this.list(new LambdaQueryWrapper<TVehicleEntity>().in(TVehicleEntity::getLicensePlate , data));
        return  TVehicleConvert.INSTANCE.convertList(list);
    }

    /**
     * 通过车牌号去设置绑定与解绑
     *
     * @param licensePlates 车牌号
     * @param userId        登陆人员id
     * @param type
     */
    @Override
    public void setByLicensePlates(String licensePlates, Long userId, Integer type) {

        TVehicleEntity one = getOne(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getLicensePlate ,licensePlates ));
        if (ObjectUtil.isNull(one)){
            throw new ServerException(ErrorCode.NOT_FOUND.getMsg());
        }
        //修改默认司机
        one.setDriverId(userId);
        if( type < 1){
            //修改默认司机
            one.setDriverId(null);
        }
        updateById(one);
    }

    /**
     * 通过车牌号去设置绑定与解绑
     *
     * @param licensePlates 车牌号
     * @param userId 登陆人员id
     */
    @Override
    public String getVehicleByLicensePlates(String licensePlates, Long userId) {
        TVehicleEntity one = getOne(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getLicensePlate,licensePlates)
                .eq(TVehicleEntity::getDriverId,userId));
        if (ObjectUtil.isNull(one)){
            return  "绑定车辆";
        }
        return "解绑车辆";
    }

    @Override
    public void updateStatus(List<TVehicleVO> list) {
        for (TVehicleVO vo : list) {
            TVehicleEntity entity = new TVehicleEntity();
            entity.setId(vo.getId());
            if(vo.getStatus() != null ){
                entity.setStatus(vo.getStatus());
            }
            // 更新实体
            this.updateById(entity);
        }
    }
}
