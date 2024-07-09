package com.hxls.system.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysControlCar;

import java.util.List;


/**
* @author admin
* @description 针对表【sys_control_car】的数据库操作Service
* @createDate 2024-07-05 14:31:26
*/
public interface SysControlCarService extends BaseService<SysControlCar> {

    List<Long> getContro();

}
