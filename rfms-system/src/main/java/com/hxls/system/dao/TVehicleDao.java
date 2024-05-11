package com.hxls.system.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxls.framework.mybatis.dao.BaseDao;
import com.hxls.system.entity.TVehicleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 通用车辆管理表
*
* @author zhaohong
* @since 1.0.0 2024-03-15
*/
@Mapper
public interface TVehicleDao extends BaseDao<TVehicleEntity> {
    default TVehicleEntity getByLicensePlate(String licensePlate){
        return this.selectOne(new QueryWrapper<TVehicleEntity>()
                .eq("license_plate", licensePlate)
                .eq("deleted", 0));
    }

}
