package com.hxls.system.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.TVehicleEntity;
import com.hxls.system.query.TVehicleQuery;
import com.hxls.system.vo.TVehicleVO;

import java.util.List;

/**
 * 通用车辆管理表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-15
 */
public interface TVehicleService extends BaseService<TVehicleEntity> {

    PageResult<TVehicleVO> page(TVehicleQuery query);

    void save(TVehicleVO vo);

    void update(TVehicleVO vo);

    void delete(List<Long> idList);

    /**
     * 通过车牌号，查询车辆基本信息
     * @param data 入参车牌号
     * @return 返回车辆信息
     */
    List<TVehicleVO> getByLicensePlates(List<String> data);


    /**
     * 通过车牌号去设置绑定与解绑
     *
     * @param licensePlates 车牌号
     * @param userId        登陆人员id
     * @param type
     */
    void setByLicensePlates(String licensePlates, Long userId, Integer type);

    /**
     *获取车辆归属
     * @param licensePlates 车牌号
     * @return
     */
    String getVehicleByLicensePlates(String licensePlates, Long userId);

    void updateStatus(List<TVehicleVO> list);
}
