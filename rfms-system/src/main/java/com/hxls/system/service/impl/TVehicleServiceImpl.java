package com.hxls.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.excel.ExcelFinishCallBack;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.ExcelUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.storage.properties.StorageProperties;
import com.hxls.system.convert.TVehicleConvert;
import com.hxls.system.dao.TVehicleDao;
import com.hxls.system.entity.TVehicleEntity;
import com.hxls.system.query.TVehicleQuery;
import com.hxls.system.service.TVehicleService;
import com.hxls.system.vo.SysUserVO;
import com.hxls.system.vo.TVehicleExcelVO;
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

    private final AppointmentFeign appointmentFeign;
    private final StorageProperties properties;

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
        //判断车牌号有没有，车牌号只能被创建一次
        long valusCount = baseMapper.selectCount(new QueryWrapper<TVehicleEntity>()
                .eq("license_plate", vo.getLicensePlate())
                .eq("deleted", 0));
        if (valusCount > 0) {
            throw new ServerException("当前车牌号已存在，不能重复添加");
        }

        TVehicleEntity entity = TVehicleConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);

        //通用车辆下发
        JSONObject vehicle = new JSONObject();
        vehicle.set("sendType","2");
        entity.setStationId(entity.getSiteId());
        vehicle.set("data" , JSONUtil.toJsonStr(entity));
        appointmentFeign.issuedPeople(vehicle);


    }

    @Override
    public void update(TVehicleVO vo) {
        // 判断车牌号是否存在
        TVehicleEntity byLicensePlate = baseMapper.getByLicensePlate(vo.getLicensePlate());
        if (byLicensePlate != null && !byLicensePlate.getId().equals(vo.getId())) {
            throw new ServerException("当前车牌号已存在，不能重复添加");
        }

        TVehicleEntity entity = TVehicleConvert.INSTANCE.convert(vo);

        //修改之前要判断是否更换了厂站
        Long id = vo.getId();
        TVehicleEntity byId = getById(id);
        if (ObjectUtil.isNull(byId)){
            throw new ServerException(ErrorCode.NOT_FOUND);
        }

        //原厂站的id
        Long siteId = byId.getSiteId();

        updateById(entity);

        if (!siteId.equals(vo.getSiteId())){
            //删除原厂站的信息
            JSONObject vehicle = new JSONObject();
            vehicle.set("sendType","2");
            entity.setStationId(entity.getSiteId());
            vehicle.set("data" , JSONUtil.toJsonStr(entity));
            vehicle.set("DELETE","DELETE");
            appointmentFeign.issuedPeople(vehicle);
        }


        JSONObject vehicle = new JSONObject();
        vehicle.set("sendType","2");
        entity.setStationId(entity.getSiteId());
        vehicle.set("data" , JSONUtil.toJsonStr(entity));
        appointmentFeign.issuedPeople(vehicle);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        List<TVehicleEntity> tVehicleEntities = listByIds(idList);

        for (TVehicleEntity entity : tVehicleEntities) {

            //删除原厂站的信息
            JSONObject vehicle = new JSONObject();
            vehicle.set("sendType","2");
            entity.setStationId(entity.getSiteId());
            vehicle.set("data" , JSONUtil.toJsonStr(entity));
            vehicle.set("DELETE","DELETE");
            appointmentFeign.issuedPeople(vehicle);
        }


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
            one.setDriverId(-1L);
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
            //删除车辆在设备上的信息
            JSONObject vehicle = new JSONObject();
            vehicle.set("sendType","2");
            entity.setStationId(entity.getSiteId());
            vehicle.set("data" , JSONUtil.toJsonStr(entity));
            if (vo.getStatus().equals(Constant.DISABLE)){
                vehicle.set("DELETE","DELETE");
            }
            appointmentFeign.issuedPeople(vehicle);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importByExcel(String file,Long siteId){
        try{
            //导入时候获取的地址是相对路径 需要拼接服务器路径
            String domain = properties.getConfig().getDomain();

            ExcelUtils.readAnalysis(ExcelUtils.convertToMultipartFile(domain + file), TVehicleExcelVO.class, new ExcelFinishCallBack<TVehicleExcelVO>() {
                @Override
                public void doAfterAllAnalysed(List<TVehicleExcelVO> result) {
                    saveTVehicle(result);
                }

                @Override
                public void doSaveBatch(List<TVehicleExcelVO> result) {
                    saveTVehicle(result);
                }

                private void saveTVehicle(List<TVehicleExcelVO> result) {
                    ExcelUtils.parseDict(result);
                    List<TVehicleEntity> tVehicleEntities = TVehicleConvert.INSTANCE.convertListEntity(result);
                    tVehicleEntities.forEach(tVehicle -> {
                        //判断车牌号有没有，车牌号只能被创建一次
                        long valusCount = baseMapper.selectCount(new QueryWrapper<TVehicleEntity>()
                                .eq("license_plate", tVehicle.getLicensePlate())
                                .eq("deleted", 0));
                        if (valusCount > 0) {
                            throw new ServerException("车辆"+tVehicle.getLicensePlate()+"已存在，不能重复添加");
                        }
                        tVehicle.setSiteId(siteId);
                        tVehicle.setStatus(1);

                        //下发车辆
                        JSONObject vehicle = new JSONObject();
                        vehicle.set("sendType","2");
                        tVehicle.setStationId(siteId);
                        vehicle.set("data" , JSONUtil.toJsonStr(tVehicle));
                        appointmentFeign.issuedPeople(vehicle);

                    });
                    saveBatch(tVehicleEntities);
                }
            });

        }catch (Exception e){
            throw new ServerException("导入数据不正确");
        }
    }

    @Override
    public void setLicensePlates(SysUserVO byMobile, String licensePlate) {

        TVehicleEntity one = getOne(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getLicensePlate ,licensePlate ));
        if (ObjectUtil.isNull(one)){
            throw new ServerException(ErrorCode.NOT_FOUND.getMsg());
        }
        //修改默认司机
        one.setDriverId(byMobile.getId());
        one.setDriverName(byMobile.getRealName());
        one.setDriverMobile(byMobile.getMobile());

        updateById(one);
    }
}
