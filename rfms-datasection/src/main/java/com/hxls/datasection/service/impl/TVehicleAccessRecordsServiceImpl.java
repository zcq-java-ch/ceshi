package com.hxls.datasection.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.api.feign.system.UserFeign;
import com.hxls.api.feign.system.VehicleFeign;
import com.hxls.datasection.config.StorageImagesProperties;
import com.hxls.datasection.dao.TPersonAccessRecordsDao;
import com.hxls.datasection.dao.TVehicleAccessLedgerDao;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.utils.RandomStringUtils;
import com.hxls.framework.security.user.UserDetail;
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
import org.apache.commons.collections4.CollectionUtils;
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
    public StorageImagesProperties properties;
    private final DeviceFeign deviceFeign;
    private final VehicleFeign vehicleFeign;
    private final AppointmentFeign appointmentFeign;
    private final UserFeign userFeign;
    private final TPersonAccessRecordsDao tPersonAccessRecordsDao;
    private final static String CARTYPEXC = "1";
    private final static String CARTYPEHC = "2";
    private final static String CARTYPEGC = "3";
    @Override
    public PageResult<TVehicleAccessRecordsVO> page(TVehicleAccessRecordsQuery query, UserDetail baseUser) {
        if (!baseUser.getSuperAdmin().equals(Constant.SUPER_ADMIN) && !CollectionUtils.isNotEmpty(baseUser.getDataScopeList())){
            return new PageResult<>(new ArrayList<>(), 0);
        }
        IPage<TVehicleAccessRecordsEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query, baseUser));

        List<TVehicleAccessRecordsEntity> records = page.getRecords();
        String domain = properties.getConfig().getDomain();

        if (CollectionUtil.isNotEmpty(records)){
            for (int i = 0; i < records.size(); i++) {
                TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity = records.get(i);
                if (StringUtils.isNotEmpty(tVehicleAccessRecordsEntity.getCarUrl())){
                    String carUrl = tVehicleAccessRecordsEntity.getCarUrl();
                    boolean isHttpUrlcarUrl = carUrl.startsWith("http");
                    if (isHttpUrlcarUrl){
                        // 是http开头 不处理
                    }else {
                        String newCarUrl = domain + carUrl;
                        tVehicleAccessRecordsEntity.setCarUrl(newCarUrl);
                    }
                }
                if (StringUtils.isNotEmpty(tVehicleAccessRecordsEntity.getImageUrl())){
                    String imageUrl = tVehicleAccessRecordsEntity.getImageUrl();
                    boolean isHttpUrlimageUrl = imageUrl.startsWith("http");
                    if (isHttpUrlimageUrl){
                        // 是http开头 不处理
                    }else {
                        String newimageUrl = domain + imageUrl;
                        tVehicleAccessRecordsEntity.setImageUrl(newimageUrl);
                    }
                }
                if (StringUtils.isNotEmpty(tVehicleAccessRecordsEntity.getLicenseImage())){
                    String licenseImage = tVehicleAccessRecordsEntity.getLicenseImage();
                    boolean isHttpUrllicenseImage = licenseImage.startsWith("http");
                    if (isHttpUrllicenseImage){
                        // 是http开头 不处理
                    }else {
                        String newlicenseImage = domain + licenseImage;
                        tVehicleAccessRecordsEntity.setLicenseImage(newlicenseImage);
                    }

                }
            }
        }

        return new PageResult<>(TVehicleAccessRecordsConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TVehicleAccessRecordsEntity> getWrapper(TVehicleAccessRecordsQuery query, UserDetail baseUser){
        LambdaQueryWrapper<TVehicleAccessRecordsEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ObjectUtils.isNotEmpty(query.getSiteId()), TVehicleAccessRecordsEntity::getSiteId, query.getSiteId());
        wrapper.eq(StringUtils.isNotEmpty(query.getAccessType()), TVehicleAccessRecordsEntity::getAccessType, query.getAccessType());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getChannelId()), TVehicleAccessRecordsEntity::getChannelId, query.getChannelId());
        wrapper.between(StringUtils.isNotEmpty(query.getStartRecordTime()) && StringUtils.isNotEmpty(query.getEndRecordTime()), TVehicleAccessRecordsEntity::getRecordTime, query.getStartRecordTime(), query.getEndRecordTime());
        wrapper.like(StringUtils.isNotEmpty(query.getPlateNumber()), TVehicleAccessRecordsEntity::getPlateNumber, query.getPlateNumber());
        wrapper.like(StringUtils.isNotEmpty(query.getDriverName()), TVehicleAccessRecordsEntity::getDriverName, query.getDriverName());
        if (baseUser.getSuperAdmin().equals(Constant.SUPER_ADMIN)){

        }else {
            List<Long> dataScopeList = baseUser.getDataScopeList();
            wrapper.in(TVehicleAccessRecordsEntity::getSiteId, dataScopeList);
        }
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

            // 先通过车牌查询平台通用车辆管理数据，如果有才执行存储操作，如果没有则不进行操作
            String plateNumber = tVehicleAccessRecordsEntity.getPlateNumber();
            JSONObject jsonObject = vehicleFeign.queryVehicleInformationByLicensePlateNumber(plateNumber);

            // 先判断车辆类型如果是小车则不录入
            String carType = jsonObject.getString("carType");
            if(StringUtils.isNotEmpty(carType) && !"1".equals(carType)){
                if (ObjectUtils.isNotEmpty(jsonObject)){
                    if ("1".equals(tVehicleAccessRecordsEntity.getAccessType())){
                        /**
                         * 如果是入场记录，则先找上一条记录，判断是出场还是入场
                         * 如果是入场记录，将原来的记录替换掉
                         * 如果是出场记录，则插入一条
                         * 如果没有，则插入一条
                         * */
                        TVehicleAccessLedgerEntity recordLedger = queryLastData(plateNumber);

                        if (ObjectUtils.isNotEmpty(recordLedger) && recordLedger.getIsOver() == 0){
                            // 如果上一条数据有数据，并且是未完成的数据，那么覆盖上条数据
                            recordLedger.setStatus(0);
                            recordLedger.setDeleted(1);
                            tVehicleAccessLedgerDao.updateById(recordLedger);
                        }
                        TVehicleAccessLedgerEntity tVehicleAccessLedgerEntity = new TVehicleAccessLedgerEntity();
                        tVehicleAccessLedgerEntity.setVehicleModel(jsonObject.getString("carType"));
                        tVehicleAccessLedgerEntity.setEmissionStandard(jsonObject.getString("emissionStandard"));
                        tVehicleAccessLedgerEntity.setLicenseImage(jsonObject.getString("licenseImage"));
                        tVehicleAccessLedgerEntity.setEnvirList(jsonObject.getString("images"));
                        tVehicleAccessLedgerEntity.setFleetName(jsonObject.getString("fleetName"));
                        tVehicleAccessLedgerEntity.setVinNumber(jsonObject.getString("vinNumber"));
                        tVehicleAccessLedgerEntity.setEngineNumber(jsonObject.getString("engineNumber"));

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
                }else {
                    log.info("当前车牌{}，在平台通用车辆管理中不存在，不纳入台账！",plateNumber);
                }
            }else {
                log.info("车辆类型是小客车，不录入");
            }
        }
    }

    /**
     * @author: Mryang
     * @Description: 查询指定车牌最后一条台账记录
     * @Date: 2024/5/9 22:37
     * @param
     * @return:
     */
    private TVehicleAccessLedgerEntity queryLastData(String plateNumber) {
        LambdaQueryWrapper<TVehicleAccessLedgerEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getPlateNumber, plateNumber);
        objectLambdaQueryWrapper.orderByDesc(TVehicleAccessLedgerEntity::getInTime);
        List<TVehicleAccessLedgerEntity> tVehicleAccessLedgerEntities = tVehicleAccessLedgerDao.selectList(objectLambdaQueryWrapper);
        TVehicleAccessLedgerEntity tVehicleAccessLedgerEntity = null;

        if (CollectionUtils.isNotEmpty(tVehicleAccessLedgerEntities)){
            tVehicleAccessLedgerEntity = tVehicleAccessLedgerEntities.get(0);
        }
        return tVehicleAccessLedgerEntity;
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
        objectLambdaQueryWrapper2.eq(TVehicleAccessRecordsEntity::getSiteId, stationId);
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

//            System.out.println("车牌: " + devicePersonId);
//            System.out.println("Records:");
//            System.out.println("---------------------------------");

            // 找出每个分组中按照时间排序的最后一条数据
            TVehicleAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TVehicleAccessRecordsEntity::getRecordTime));
            if ("1".equals(lastRecord.getAccessType())) {
                // 最后一次为入厂
                inAllNumer += 1;

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
            } else {
                // 最后一次为出厂
            }

        }
        JSONObject entries = new JSONObject();
        entries.put("realTimeTotals", inAllNumer);
        entries.put("numberOfTrolleys", numberOfTrolleys);
        entries.put("theNumberOfShipments", theNumberOfShipments);
        return entries;
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

    @Override
    public JSONArray queryTheDetailsOfSiteCar(Long stationId) {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat timeformat = new SimpleDateFormat(format);

        LambdaQueryWrapper<TVehicleAccessRecordsEntity> objectLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper2.eq(TVehicleAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper2.eq(TVehicleAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper2.eq(TVehicleAccessRecordsEntity::getSiteId, stationId);
        objectLambdaQueryWrapper2.between(TVehicleAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
        List<TVehicleAccessRecordsEntity> tVehicleAccessRecordsEntities = baseMapper.selectList(objectLambdaQueryWrapper2);

        JSONArray objects = new JSONArray();
        for (int i = 0; i < tVehicleAccessRecordsEntities.size(); i++) {
            TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity = tVehicleAccessRecordsEntities.get(0);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("licensePlateNumber", tVehicleAccessRecordsEntity.getPlateNumber());
            jsonObject.put("driver", tVehicleAccessRecordsEntity.getDriverName());
            jsonObject.put("models", tVehicleAccessRecordsEntity.getVehicleModel());
            jsonObject.put("emissionStandards", tVehicleAccessRecordsEntity.getEmissionStandard());
            jsonObject.put("time", tVehicleAccessRecordsEntity.getRecordTime());
            jsonObject.put("typeOfEntryAndExit", tVehicleAccessRecordsEntity.getAccessType());
            objects.add(jsonObject);
        }

//        // 按照车牌进行分组
//        Map<String, List<TVehicleAccessRecordsEntity>> groupedByDevicePersonId2 = tVehicleAccessRecordsEntities.stream()
//                .collect(Collectors.groupingBy(TVehicleAccessRecordsEntity::getPlateNumber));
//
//        // 打印每个分组并更新inNumer变量
//        for (Map.Entry<String, List<TVehicleAccessRecordsEntity>> entry : groupedByDevicePersonId2.entrySet()) {
//            String devicePersonId = entry.getKey();
//            List<TVehicleAccessRecordsEntity> recordsList = entry.getValue();
//
////            System.out.println("车牌: " + devicePersonId);
////            System.out.println("Records:");
////            System.out.println("---------------------------------");
//
//            // 找出每个分组中按照时间排序的最后一条数据
//            TVehicleAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TVehicleAccessRecordsEntity::getRecordTime));
//            if ("1".equals(lastRecord.getAccessType())) {
//                // 最后一次为入厂
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("licensePlateNumber", lastRecord.getPlateNumber());
//                jsonObject.put("driver", lastRecord.getDriverName());
//                jsonObject.put("models", lastRecord.getVehicleModel());
//                jsonObject.put("emissionStandards", lastRecord.getEmissionStandard());
//                jsonObject.put("time", lastRecord.getRecordTime());
//                jsonObject.put("typeOfEntryAndExit", lastRecord.getAccessType());
//                objects.add(jsonObject);
//            } else {
//                // 最后一次为出厂
//            }
//        }
        return objects;
    }

    @Override
    public void jingchengMakeTaz(String siteId) {
        // 定义一个日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LambdaQueryWrapper<TVehicleAccessRecordsEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TVehicleAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(TVehicleAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(TVehicleAccessRecordsEntity::getSiteId, siteId);
//        objectLambdaQueryWrapper.isNotNull(TVehicleAccessRecordsEntity::getDriverId);
        List<TVehicleAccessRecordsEntity> tVehicleAccessRecordsEntities = baseMapper.selectList(objectLambdaQueryWrapper);
        Map<String, List<TVehicleAccessRecordsEntity>> groupedByManufacturerId = tVehicleAccessRecordsEntities.stream()
                .filter(tVehicleAccessRecordsEntity -> ObjectUtils.isNotEmpty(tVehicleAccessRecordsEntity.getPlateNumber()))
                .collect(Collectors.groupingBy(TVehicleAccessRecordsEntity::getPlateNumber));
        groupedByManufacturerId.forEach((plateNumber, recordsList) -> {

            // 在通用车辆管理里面查询是否有这个车牌
            JSONObject jsonObject = vehicleFeign.queryVehicleInformationByLicensePlateNumber(plateNumber);
            if (ObjectUtils.isNotEmpty(jsonObject)){
                // 还要判断这个车型是不是大车，字典类型为2.3
                String carType = jsonObject.getString("carType");
                if (StringUtils.isNotEmpty(carType)){
//                    if (CARTYPEHC.equals(carType) || CARTYPEGC.equals(carType)){
                    if (!CARTYPEXC.equals(carType)){
                        // 或者使用Java 8的Stream API进行排序
                        recordsList.sort(Comparator.comparing(TVehicleAccessRecordsEntity::getRecordTime));

                        TVehicleAccessRecordsEntity inData = null;
                        TVehicleAccessRecordsEntity outData;
                        for (int i = 0; i < recordsList.size(); i++) {
                            TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity = recordsList.get(i);
                            if ("1".equals(tVehicleAccessRecordsEntity.getAccessType())){
                                // 入场
                                inData = tVehicleAccessRecordsEntity;
                            }else if("2".equals(tVehicleAccessRecordsEntity.getAccessType())) {
                                // 出场
                                outData = tVehicleAccessRecordsEntity;
                                // 存储记录
                                if (ObjectUtils.isNotEmpty(inData)){
                                    saveData(inData, outData, jsonObject);
                                }
                                inData = null;
                                outData = null;
                            }else {

                            }
                        }

                        // 当最后循环后，发现入库记录还不为空，那么说明有车辆在内还没出来，也要生成记录
                        if (ObjectUtils.isNotEmpty(inData)){
                            TVehicleAccessLedgerEntity tVehicleAccessLedgerEntity = new TVehicleAccessLedgerEntity();
                            tVehicleAccessLedgerEntity.setVehicleModel(inData.getVehicleModel());
                            tVehicleAccessLedgerEntity.setEmissionStandard(inData.getEmissionStandard());
                            tVehicleAccessLedgerEntity.setLicenseImage(inData.getLicenseImage());
                            tVehicleAccessLedgerEntity.setEnvirList(jsonObject.getString("images"));
                            tVehicleAccessLedgerEntity.setFleetName(jsonObject.getString("fleetName"));
                            tVehicleAccessLedgerEntity.setVinNumber(jsonObject.getString("vinNumber"));
                            tVehicleAccessLedgerEntity.setEngineNumber(jsonObject.getString("engineNumber"));

                            tVehicleAccessLedgerEntity.setSiteId(inData.getSiteId());
                            tVehicleAccessLedgerEntity.setSiteName(inData.getSiteName());
                            tVehicleAccessLedgerEntity.setPlateNumber(inData.getPlateNumber());
                            tVehicleAccessLedgerEntity.setInTime(inData.getRecordTime());
                            tVehicleAccessLedgerEntity.setInPic(inData.getCarUrl());
                            tVehicleAccessLedgerEntity.setIsOver(0);
                            tVehicleAccessLedgerEntity.setCreateTime(new Date());

                            tVehicleAccessLedgerDao.insert(tVehicleAccessLedgerEntity);
                        }
                    }
                }
            }
        });
    }

    private void saveData(TVehicleAccessRecordsEntity inData, TVehicleAccessRecordsEntity outData, JSONObject carInfo) {
        TVehicleAccessLedgerEntity tVehicleAccessLedgerEntity = new TVehicleAccessLedgerEntity();
        tVehicleAccessLedgerEntity.setVehicleModel(inData.getVehicleModel());
        tVehicleAccessLedgerEntity.setEmissionStandard(inData.getEmissionStandard());
        tVehicleAccessLedgerEntity.setLicenseImage(inData.getLicenseImage());
        tVehicleAccessLedgerEntity.setEnvirList(carInfo.getString("images"));
        tVehicleAccessLedgerEntity.setFleetName(carInfo.getString("fleetName"));
        tVehicleAccessLedgerEntity.setVinNumber(carInfo.getString("vinNumber"));
        tVehicleAccessLedgerEntity.setEngineNumber(carInfo.getString("engineNumber"));

        tVehicleAccessLedgerEntity.setSiteId(inData.getSiteId());
        tVehicleAccessLedgerEntity.setSiteName(inData.getSiteName());
        tVehicleAccessLedgerEntity.setPlateNumber(inData.getPlateNumber());
        tVehicleAccessLedgerEntity.setInTime(inData.getRecordTime());
        tVehicleAccessLedgerEntity.setInPic(inData.getCarUrl());
        tVehicleAccessLedgerEntity.setIsOver(1);
        tVehicleAccessLedgerEntity.setCreateTime(new Date());

        tVehicleAccessLedgerEntity.setOutPic(outData.getCarUrl());
        tVehicleAccessLedgerEntity.setOutTime(outData.getRecordTime());
        tVehicleAccessLedgerEntity.setUpdateTime(new Date());
        tVehicleAccessLedgerDao.insert(tVehicleAccessLedgerEntity);
    }

    @Override
    public void retinuegenerateRecords(TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity) {
        String plateNumber = tVehicleAccessRecordsEntity.getPlateNumber();
        Date recordTime = tVehicleAccessRecordsEntity.getRecordTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 使用 SimpleDateFormat 的 format 方法将 Date 对象转换为 String 对象
        String dateString = dateFormat.format(recordTime);

        JSONObject jsonObject = appointmentFeign.queryappointmentFormspecifyLicensePlatesAndEntourage(plateNumber, dateString);

        JSONArray jsonArray = jsonObject.getJSONArray("dataArray");

        if (CollectionUtils.isNotEmpty(jsonArray)){
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                Long aLong = jsonObject1.getLong("userId");
                Long positionId = jsonObject1.getLong("positionId");
                String positionName = jsonObject1.getString("positionName");
                String userName = jsonObject1.getString("userName");

                String aString = aLong.toString();
                JSONObject userDetail = userFeign.queryUserInformationUserId(aString);
                if (ObjectUtils.isNotEmpty(userDetail)){
                    savePersonRecord(aLong, positionId, positionName, tVehicleAccessRecordsEntity);
                }else {
                    savePersonRecord2(userName, positionId, positionName, tVehicleAccessRecordsEntity);
                }
            }
        }else {
            // 预约单没有数据，则需要去找user表，user表没有去找车辆管理表
            JSONObject userInformationLicensePlate = userFeign.queryUserInformationLicensePlate(plateNumber);
            if (ObjectUtils.isNotEmpty(userInformationLicensePlate)){
                savePersonRecordByUser(userInformationLicensePlate, tVehicleAccessRecordsEntity);
            }else {
                JSONObject vehicleInformationByLicensePlateNumber = vehicleFeign.queryVehicleInformationByLicensePlateNumber(plateNumber);
                Long aLong = vehicleInformationByLicensePlateNumber.getLong("driverId");
                if (ObjectUtils.isNotEmpty(aLong)){
                    JSONObject userInformationLicenseUser= userFeign.queryUserInformationUserId(aLong.toString());
                    savePersonRecordByUser(userInformationLicenseUser, tVehicleAccessRecordsEntity);
                }
            }
        }
    }

    private void savePersonRecord2(String userName, Long positionId, String positionName, TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity) {
        log.info("供应商车辆入场申请中的司机 存储记录开始");
        TPersonAccessRecordsEntity tPersonAccessRecordsEntity = new TPersonAccessRecordsEntity();
        tPersonAccessRecordsEntity.setAccessType(tVehicleAccessRecordsEntity.getAccessType());
        tPersonAccessRecordsEntity.setHeadUrl(tVehicleAccessRecordsEntity.getCarUrl());
        tPersonAccessRecordsEntity.setRecordTime(tVehicleAccessRecordsEntity.getRecordTime());
        tPersonAccessRecordsEntity.setSiteId(tVehicleAccessRecordsEntity.getSiteId());
        tPersonAccessRecordsEntity.setSiteName(tVehicleAccessRecordsEntity.getSiteName());
        tPersonAccessRecordsEntity.setRecordsId(RandomStringUtils.generateRandomString(32));
        tPersonAccessRecordsEntity.setPersonName(userName);
        tPersonAccessRecordsEntity.setPositionId(positionId);
        tPersonAccessRecordsEntity.setPositionName(positionName);
        tPersonAccessRecordsEntity.setCreateType("4");

        tPersonAccessRecordsDao.insert(tPersonAccessRecordsEntity);
        log.info("供应商车辆入场申请中的司机 存储记录结束");
    }

    private void savePersonRecordByUser(JSONObject userInformationLicensePlate, TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity) {
        log.info("用户车辆，记录用户通行记录开始");
        TPersonAccessRecordsEntity tPersonAccessRecordsEntity = new TPersonAccessRecordsEntity();
        tPersonAccessRecordsEntity.setAccessType(tVehicleAccessRecordsEntity.getAccessType());
        tPersonAccessRecordsEntity.setHeadUrl(tVehicleAccessRecordsEntity.getCarUrl());
        tPersonAccessRecordsEntity.setPersonId(userInformationLicensePlate.getLong("userId"));
        tPersonAccessRecordsEntity.setRecordTime(tVehicleAccessRecordsEntity.getRecordTime());
        tPersonAccessRecordsEntity.setSiteId(tVehicleAccessRecordsEntity.getSiteId());
        tPersonAccessRecordsEntity.setSiteName(tVehicleAccessRecordsEntity.getSiteName());
        tPersonAccessRecordsEntity.setRecordsId(RandomStringUtils.generateRandomString(32));
        tPersonAccessRecordsEntity.setPositionId(userInformationLicensePlate.getLong("postId"));
        tPersonAccessRecordsEntity.setPositionName(userInformationLicensePlate.getString("postName"));
        tPersonAccessRecordsEntity.setCompanyId(userInformationLicensePlate.getLong("orgId"));
        tPersonAccessRecordsEntity.setCompanyName(userInformationLicensePlate.getString("orgName"));
        tPersonAccessRecordsEntity.setSupervisorName(userInformationLicensePlate.getString("supervisor"));
        tPersonAccessRecordsEntity.setIdCardNumber(userInformationLicensePlate.getString("idCard"));
        tPersonAccessRecordsEntity.setPhone(userInformationLicensePlate.getString("mobile"));

        tPersonAccessRecordsEntity.setBusis(userInformationLicensePlate.getString("busis"));
        tPersonAccessRecordsEntity.setPersonName(userInformationLicensePlate.getString("personName"));
        tPersonAccessRecordsEntity.setCreateType("4");
        // 通过用户唯一编码查询用户，然后将客户端的识别数据与平台的用户数据进行绑定
        tPersonAccessRecordsDao.insert(tPersonAccessRecordsEntity);
        log.info("用户车辆，记录用户通行记录结束");
    }

    private void savePersonRecord(Long aLong, Long positionId, String positionName, TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity) {
        log.info("随行人间存储记录开始");
        TPersonAccessRecordsEntity tPersonAccessRecordsEntity = new TPersonAccessRecordsEntity();
        tPersonAccessRecordsEntity.setAccessType(tVehicleAccessRecordsEntity.getAccessType());
        tPersonAccessRecordsEntity.setHeadUrl(tVehicleAccessRecordsEntity.getCarUrl());
        tPersonAccessRecordsEntity.setPersonId(aLong);
        tPersonAccessRecordsEntity.setRecordTime(tVehicleAccessRecordsEntity.getRecordTime());
        tPersonAccessRecordsEntity.setSiteId(tVehicleAccessRecordsEntity.getSiteId());
        tPersonAccessRecordsEntity.setSiteName(tVehicleAccessRecordsEntity.getSiteName());
        tPersonAccessRecordsEntity.setRecordsId(RandomStringUtils.generateRandomString(32));
        tPersonAccessRecordsEntity.setPositionId(positionId);
        tPersonAccessRecordsEntity.setPositionName(positionName);

        // 通过用户唯一编码查询用户，然后将客户端的识别数据与平台的用户数据进行绑定
        String aString = aLong.toString();
        if (StringUtils.isNotEmpty(aString)){
            JSONObject userDetail = userFeign.queryUserInformationUserId(aString);
            if (ObjectUtils.isNotEmpty(userDetail)) {
                tPersonAccessRecordsEntity.setCompanyId(userDetail.getLong("orgId"));
                tPersonAccessRecordsEntity.setCompanyName(userDetail.getString("orgName"));
                tPersonAccessRecordsEntity.setSupervisorName(userDetail.getString("supervisor"));
                tPersonAccessRecordsEntity.setIdCardNumber(userDetail.getString("idCard"));
                tPersonAccessRecordsEntity.setPhone(userDetail.getString("mobile"));

                tPersonAccessRecordsEntity.setBusis(userDetail.getString("busis"));
                tPersonAccessRecordsEntity.setPersonName(userDetail.getString("personName"));
            }
        }
        tPersonAccessRecordsEntity.setCreateType("4");
        tPersonAccessRecordsDao.insert(tPersonAccessRecordsEntity);
        log.info("随行人员存储记录结束");

    }

    @Override
    public void supplementAndRecordVehicleInformation(String siteId, String startDateTime, String endDateTime) {
        /**
         * 车辆补录信息为，车辆类型、排放标准、行驶证、随车清单、驾驶员id、驾驶员姓名、驾驶员手机号
         * */
        LambdaQueryWrapper<TVehicleAccessRecordsEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TVehicleAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(TVehicleAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(TVehicleAccessRecordsEntity::getSiteId, siteId);
        objectLambdaQueryWrapper.between(TVehicleAccessRecordsEntity::getRecordTime, startDateTime, endDateTime);
        List<TVehicleAccessRecordsEntity> tVehicleAccessRecordsEntities = baseMapper.selectList(objectLambdaQueryWrapper);
        List<TVehicleAccessRecordsEntity> filteredRecords = tVehicleAccessRecordsEntities.stream()
                .filter(record -> record.getVehicleModel() == null ||
                        record.getEmissionStandard() == null ||
                        record.getImageUrl() == null ||
                        record.getLicenseImage() == null)
                .collect(Collectors.toList());

        for (int i = 0; i < filteredRecords.size(); i++) {
            TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity = filteredRecords.get(i);
            String plateNumber = tVehicleAccessRecordsEntity.getPlateNumber();
            JSONObject jsonObject = vehicleFeign.queryVehicleInformationByLicensePlateNumber(plateNumber);
            String carType = jsonObject.getString("carType");
            String emissionStandard = jsonObject.getString("emissionStandard");
            String licenseImage = jsonObject.getString("licenseImage");
            String images = jsonObject.getString("images"); // 随车清单
            Long driverId = jsonObject.getLong("driverId");
            String driverName = jsonObject.getString("driverName");
            String driverMobile = jsonObject.getString("driverMobile");

            tVehicleAccessRecordsEntity.setVehicleModel(carType);
            tVehicleAccessRecordsEntity.setEmissionStandard(emissionStandard);
            tVehicleAccessRecordsEntity.setLicenseImage(licenseImage);
            tVehicleAccessRecordsEntity.setDriverId(driverId);
            tVehicleAccessRecordsEntity.setDriverName(driverName);
            tVehicleAccessRecordsEntity.setDriverPhone(driverMobile);
            baseMapper.updateById(tVehicleAccessRecordsEntity);
        }
    }
}
