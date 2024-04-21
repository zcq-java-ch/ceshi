package com.hxls.datasection.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.datasection.dao.TVehicleAccessLedgerDao;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.datasection.convert.TVehicleAccessRecordsConvert;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.query.TVehicleAccessRecordsQuery;
import com.hxls.datasection.vo.TVehicleAccessRecordsVO;
import com.hxls.datasection.dao.TVehicleAccessRecordsDao;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 车辆出入记录表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-29
 */
@Service
@AllArgsConstructor
@Slf4j
public class TVehicleAccessRecordsServiceImpl extends BaseServiceImpl<TVehicleAccessRecordsDao, TVehicleAccessRecordsEntity> implements TVehicleAccessRecordsService {

    private final TVehicleAccessLedgerDao tVehicleAccessLedgerDao;

    private final DeviceFeign deviceFeign;
    @Override
    public PageResult<TVehicleAccessRecordsVO> page(TVehicleAccessRecordsQuery query) {
        IPage<TVehicleAccessRecordsEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(TVehicleAccessRecordsConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TVehicleAccessRecordsEntity> getWrapper(TVehicleAccessRecordsQuery query){
        LambdaQueryWrapper<TVehicleAccessRecordsEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ObjectUtils.isNotEmpty(query.getSiteId()), TVehicleAccessRecordsEntity::getSiteId, query.getSiteId());
        wrapper.eq(StringUtils.isNotEmpty(query.getAccessType()), TVehicleAccessRecordsEntity::getAccessType, query.getAccessType());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getChannelId()), TVehicleAccessRecordsEntity::getChannelId, query.getChannelId());
        wrapper.between(StringUtils.isNotEmpty(query.getStartRecordTime()) && StringUtils.isNotEmpty(query.getEndRecordTime()), TVehicleAccessRecordsEntity::getRecordTime, query.getStartRecordTime(), query.getEndRecordTime());
        wrapper.like(StringUtils.isNotEmpty(query.getPlateNumber()), TVehicleAccessRecordsEntity::getPlateNumber, query.getPlateNumber());
        wrapper.like(StringUtils.isNotEmpty(query.getDriverName()), TVehicleAccessRecordsEntity::getDriverName, query.getDriverName());
        return wrapper;
    }

    @Override
    public void save(TVehicleAccessRecordsVO vo) {
        TVehicleAccessRecordsEntity entity = TVehicleAccessRecordsConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(TVehicleAccessRecordsVO vo) {
        TVehicleAccessRecordsEntity entity = TVehicleAccessRecordsConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

    @Override
    public boolean whetherItExists(String recordsId) {
        LambdaQueryWrapper<TVehicleAccessRecordsEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TVehicleAccessRecordsEntity::getRecordsId, recordsId);
        List<TVehicleAccessRecordsEntity> tPersonAccessRecordsEntities = baseMapper.selectList(objectLambdaQueryWrapper);
        if (CollectionUtil.isNotEmpty(tPersonAccessRecordsEntities)){
            return true;
        }else {
            return false;
        }

    }

    @Override
    public void saveLedger(TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity) {
        log.info("当前的厂商是：{}",tVehicleAccessRecordsEntity.getManufacturerId());
        if (5 == tVehicleAccessRecordsEntity.getManufacturerId()){
            // 先只处理精诚元鸿的设备
            log.info("开始存储台账");
            if ("1".equals(tVehicleAccessRecordsEntity.getAccessType())){
                // 如果是入的记录 则直接插入一条
                // 先通过车牌找到对应的平台车辆信息
                String plateNumber = tVehicleAccessRecordsEntity.getPlateNumber();
                JSONObject jsonObject = deviceFeign.queryVehicleInformationByLicensePlateNumber(plateNumber);

                TVehicleAccessLedgerEntity tVehicleAccessLedgerEntity = new TVehicleAccessLedgerEntity();
                tVehicleAccessLedgerEntity.setVehicleModel(jsonObject.get("carType", String.class));
                tVehicleAccessLedgerEntity.setEmissionStandard(jsonObject.get("emissionStandard", String.class));
                tVehicleAccessLedgerEntity.setLicenseImage(jsonObject.get("licenseImage", String.class));
                tVehicleAccessLedgerEntity.setEnvirList(jsonObject.get("images", String.class));
                tVehicleAccessLedgerEntity.setFleetName(jsonObject.get("fleetName", String.class));
                tVehicleAccessLedgerEntity.setVinNumber(jsonObject.get("vinNumber", String.class));
                tVehicleAccessLedgerEntity.setEngineNumber(jsonObject.get("engineNumber", String.class));

                tVehicleAccessLedgerEntity.setSiteId(tVehicleAccessRecordsEntity.getSiteId());
                tVehicleAccessLedgerEntity.setSiteName(tVehicleAccessRecordsEntity.getSiteName());
                tVehicleAccessLedgerEntity.setPlateNumber(tVehicleAccessRecordsEntity.getPlateNumber());
                tVehicleAccessLedgerEntity.setInTime(tVehicleAccessRecordsEntity.getRecordTime());
                tVehicleAccessLedgerEntity.setInPic(tVehicleAccessRecordsEntity.getCarUrl());
                tVehicleAccessLedgerEntity.setIsOver(0);
                tVehicleAccessLedgerEntity.setCreateTime(new Date());
                tVehicleAccessLedgerDao.insert(tVehicleAccessLedgerEntity);
            }else {
                // 如果是出的，则需要找到对应的最近一条入的台账
                String plateNumber = tVehicleAccessRecordsEntity.getPlateNumber();
                LambdaQueryWrapper<TVehicleAccessLedgerEntity> tVehicleAccessLedgerEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
                tVehicleAccessLedgerEntityLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getPlateNumber, plateNumber);
                tVehicleAccessLedgerEntityLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getStatus, 1);
                tVehicleAccessLedgerEntityLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getDeleted, 0);
                tVehicleAccessLedgerEntityLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getIsOver, 0);

                tVehicleAccessLedgerEntityLambdaQueryWrapper.orderByDesc(TVehicleAccessLedgerEntity::getInTime);
                tVehicleAccessLedgerEntityLambdaQueryWrapper.last("LIMIT 1"); // 限制只查询一条数据
                List<TVehicleAccessLedgerEntity> tVehicleAccessLedgerEntities = tVehicleAccessLedgerDao.selectList(tVehicleAccessLedgerEntityLambdaQueryWrapper);

                if(CollectionUtil.isNotEmpty(tVehicleAccessLedgerEntities)){
                    TVehicleAccessLedgerEntity tVehicleAccessLedgerEntity = tVehicleAccessLedgerEntities.get(0);
                    tVehicleAccessLedgerEntity.setIsOver(1);
                    tVehicleAccessLedgerEntity.setOutPic(tVehicleAccessRecordsEntity.getCarUrl());
                    tVehicleAccessLedgerEntity.setOutTime(tVehicleAccessRecordsEntity.getRecordTime());
                    tVehicleAccessLedgerEntity.setUpdateTime(new Date());
                    tVehicleAccessLedgerDao.updateById(tVehicleAccessLedgerEntity);
                }
            }
        }
    }

    /**
     * @author: Mryang
     * @Description: 查询车辆通行记录表,并查询指定站点
     *                  查询最后一条通行记录为入的 所有车辆数据合计
     *                  再将查询到的车辆,区分车辆类型
     * @Date: 2024/4/21 23:00
     * @param
     * @return:
     */
    @Override
    public JSONObject QueryRealtimeTotalAndNumberVariousClasses(Long stationId) {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat timeformat = new SimpleDateFormat(format);

        LambdaQueryWrapper<TVehicleAccessRecordsEntity> objectLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper2.eq(TVehicleAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper2.eq(TVehicleAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper2.between(TVehicleAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
        List<TVehicleAccessRecordsEntity> tVehicleAccessRecordsEntities = baseMapper.selectList(objectLambdaQueryWrapper2);
        int inAllNumer = 0;
        int numberOfTrolleys = 0;
        int theNumberOfShipments = 0;

        // 按照车牌进行分组
        Map<String, List<TVehicleAccessRecordsEntity>> groupedByDevicePersonId2 = tVehicleAccessRecordsEntities.stream()
                .collect(Collectors.groupingBy(TVehicleAccessRecordsEntity::getPlateNumber));

        // 打印每个分组并更新inNumer变量
        for (Map.Entry<String, List<TVehicleAccessRecordsEntity>> entry : groupedByDevicePersonId2.entrySet()) {
            String devicePersonId = entry.getKey();
            List<TVehicleAccessRecordsEntity> recordsList = entry.getValue();

            System.out.println("车牌: " + devicePersonId);
            System.out.println("Records:");
            System.out.println("---------------------------------");

            // 找出每个分组中按照时间排序的最后一条数据
            TVehicleAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TVehicleAccessRecordsEntity::getRecordTime));
            if ("1".equals(lastRecord.getAccessType())) {
                // 最后一次为入厂
                inAllNumer += 1;
            } else {
                // 最后一次为出厂
            }
            if ("1".equals(lastRecord.getVehicleModel())){
                // 小客车
                numberOfTrolleys += 1;
            }else if("2".equals(lastRecord.getVehicleModel())){
                // 货车
                theNumberOfShipments += 1;
            }else if ("3".equals(lastRecord.getVehicleModel())){
                // 罐车
                theNumberOfShipments += 1;
            }else {

            }
        }
        JSONObject entries = new JSONObject();
        entries.putOnce("realTimeTotals", inAllNumer);
        entries.putOnce("numberOfTrolleys", numberOfTrolleys);
        entries.putOnce("theNumberOfShipments", theNumberOfShipments);
        return null;
    }

    private Date getTodayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getTodayEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
}
