package com.hxls.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.entity.SysControlCar;
import com.hxls.system.service.SysControlCarService;
import com.hxls.system.dao.SysControlCarMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 针对表【sys_control_car】的数据库操作Service实现
 * @createDate 2024-07-05 14:31:26
 */
@Service
public class SysControlCarServiceImpl extends BaseServiceImpl<SysControlCarMapper, SysControlCar>
        implements SysControlCarService {

    /**
     * 获取管控厂站
     *
     * @return
     */
    @Override
    public List<Long> getContro() {

        List<SysControlCar> list = list(new LambdaQueryWrapper<SysControlCar>());

        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        return list.stream().map(SysControlCar::getSiteId).distinct().collect(Collectors.toList());


    }
}




