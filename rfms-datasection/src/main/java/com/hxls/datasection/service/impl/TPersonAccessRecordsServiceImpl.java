package com.hxls.datasection.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hxls.api.dto.datasection.TPersonAccessRecordsDTO;
import com.hxls.api.feign.system.UserFeign;
import com.hxls.datasection.config.StorageImagesProperties;
import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.service.TVehicleAccessLedgerService;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.utils.RandomSnowUtils;
import com.hxls.framework.security.user.UserDetail;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.datasection.convert.TPersonAccessRecordsConvert;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.query.TPersonAccessRecordsQuery;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import com.hxls.datasection.dao.TPersonAccessRecordsDao;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 人员出入记录表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-29
 */
@Service
@AllArgsConstructor
public class TPersonAccessRecordsServiceImpl extends BaseServiceImpl<TPersonAccessRecordsDao, TPersonAccessRecordsEntity> implements TPersonAccessRecordsService {

    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;
    public StorageImagesProperties properties;
    private final UserFeign userFeign;
    @Override
    public PageResult<TPersonAccessRecordsVO> page(TPersonAccessRecordsQuery query, UserDetail baseUser) {
        if (!baseUser.getSuperAdmin().equals(Constant.SUPER_ADMIN) && !org.apache.commons.collections4.CollectionUtils.isNotEmpty(baseUser.getDataScopeList())){
            return new PageResult<>(new ArrayList<>(), 0);
        }
        IPage<TPersonAccessRecordsEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query, baseUser));

        List<TPersonAccessRecordsEntity> records = page.getRecords();
        String domain = properties.getConfig().getDomain();

        if (CollectionUtil.isNotEmpty(records)) {
            for (int i = 0; i < records.size(); i++) {
                TPersonAccessRecordsEntity tPersonAccessRecordsEntity = records.get(i);
                if (StringUtils.isNotEmpty(tPersonAccessRecordsEntity.getHeadUrl())) {
                    String headUrl = tPersonAccessRecordsEntity.getHeadUrl();
                    boolean isHttpheadUrl = headUrl.startsWith("http");
                    if (isHttpheadUrl) {
                        // 是http开头 不处理
                    } else {
                        String newheadUrl = domain + headUrl;
                        tPersonAccessRecordsEntity.setHeadUrl(newheadUrl);
                    }
                }
            }
        }

        return new PageResult<>(TPersonAccessRecordsConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TPersonAccessRecordsEntity> getWrapper(TPersonAccessRecordsQuery query, UserDetail baseUser){
        LambdaQueryWrapper<TPersonAccessRecordsEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.isNotBlank(query.getPersonName()),TPersonAccessRecordsEntity::getPersonName, query.getPersonName());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getSiteId()), TPersonAccessRecordsEntity::getSiteId, query.getSiteId());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getAccessType()), TPersonAccessRecordsEntity::getAccessType, query.getAccessType());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getChannelId()), TPersonAccessRecordsEntity::getChannelId, query.getChannelId());
        wrapper.between(ObjectUtils.isNotEmpty(query.getStartRecordTime()) && ObjectUtils.isNotEmpty(query.getEndRecordTime()), TPersonAccessRecordsEntity::getRecordTime, query.getStartRecordTime(), query.getEndRecordTime());
        wrapper.eq(TPersonAccessRecordsEntity::getStatus, 1);
        wrapper.eq(TPersonAccessRecordsEntity::getDeleted, 0);
        if (baseUser.getSuperAdmin().equals(Constant.SUPER_ADMIN)){

        }else {
            List<Long> dataScopeList = baseUser.getDataScopeList();
            wrapper.in(TPersonAccessRecordsEntity::getSiteId, dataScopeList);
        }
        return wrapper;
    }

    @Override
    public void save(TPersonAccessRecordsVO vo) {
        TPersonAccessRecordsEntity entity = TPersonAccessRecordsConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(TPersonAccessRecordsVO vo) {
        TPersonAccessRecordsEntity entity = TPersonAccessRecordsConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

    @Override
    public PageResult<TPersonAccessRecordsVO> pageUnidirectionalTpersonAccessRecords(TPersonAccessRecordsQuery query, UserDetail baseUser) {
        if (!baseUser.getSuperAdmin().equals(Constant.SUPER_ADMIN) && !org.apache.commons.collections4.CollectionUtils.isNotEmpty(baseUser.getDataScopeList())){
            return new PageResult<>(new ArrayList<>(), 0);
        }

        LocalDate today = LocalDate.now();
        // 获取今天的开始时间（00:00:00）
        LocalDateTime startOfDay = today.atStartOfDay();
        // 获取今天的结束时间（23:59:59.999999999）
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 将开始时间和结束时间转换为字符串
        String startOfDayString = startOfDay.format(formatter);
        String endOfDayString = endOfDay.format(formatter);

        if (StringUtils.isEmpty(query.getStartRecordTime())){
            query.setStartRecordTime(startOfDayString);
        }
        if (StringUtils.isEmpty(query.getEndRecordTime())){
            query.setEndRecordTime(endOfDayString);
        }

        LambdaQueryWrapper<TPersonAccessRecordsEntity> wrapper = getWrapper(query, baseUser);
        List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities = baseMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(tPersonAccessRecordsEntities)){

            List<TPersonAccessRecordsVO> personAccessRecordsEntityArrayList = new ArrayList<>();

            // 按照 站点 进行分组
            Map<Long, List<TPersonAccessRecordsEntity>> groupedByManufacturerId = tPersonAccessRecordsEntities.stream()
                    .filter(tPersonAccessRecordsEntity -> ObjectUtils.isNotEmpty(tPersonAccessRecordsEntity.getSiteId()))
                    .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getSiteId));
            // 打印每个分组
            groupedByManufacturerId.forEach((siteId, recordsList) -> {

                // 按照 姓名 进行分组
//                Map<String, List<TPersonAccessRecordsEntity>> groupedByPersonId = recordsList.stream()
//                        .filter(tPersonAccessRecordsEntity -> StringUtils.isNotBlank(tPersonAccessRecordsEntity.getDevicePersonId()))
//                        .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getDevicePersonId));

                // 首先过滤掉 userId 为空的数据
                List<TPersonAccessRecordsEntity> filteredList = recordsList.stream()
                        .filter(entity -> entity.getPersonId() != null)
                        .collect(Collectors.toList());

                // 按照 userId 进行分组
                Map<Long, List<TPersonAccessRecordsEntity>> groupedByPersonId = filteredList.stream()
                        .collect(Collectors.groupingBy(entity -> entity.getPersonId()));


                // 打印每个分组
                groupedByPersonId.forEach((personId, records2List) -> {

                    // 判断这个人在当前站点是不是【住宿】如果是注册，则忽略records2List  如果不是住宿人员，则需要统计未出场，未入场
                    boolean isStay = userFeign.queryIsStayByUser(personId, siteId);

                    if (!isStay){
                        // 按照 进出类型 进行分组
                        Map<String, List<TPersonAccessRecordsEntity>> groupedByAccessType = records2List.stream()
                                .filter(tPersonAccessRecordsEntity -> StringUtils.isNotBlank(tPersonAccessRecordsEntity.getAccessType()))
                                .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getAccessType));
                        int size = groupedByAccessType.size();
                        if (size == 1){
                            groupedByAccessType.forEach((accessType, records3List) -> {
                                TPersonAccessRecordsEntity tPersonAccessRecordsEntity = records3List.get(0);
                                TPersonAccessRecordsVO convert = TPersonAccessRecordsConvert.INSTANCE.convert(tPersonAccessRecordsEntity);
                                convert.setDirectionType("1".equals(convert.getAccessType()) ? "未出场" : "未入场");
                                convert.setTodayDetails(records3List);
                                personAccessRecordsEntityArrayList.add(convert);
                            });
                        }
                    }
                });
            });

            // 每页的大小
            int pageSize = query.getLimit();

            // 要获取的页数
            int pageNumber = query.getPage();

            // 计算起始索引
            int startIndex = (pageNumber - 1) * pageSize;

            // 计算结束索引
            int endIndex = Math.min(startIndex + pageSize, personAccessRecordsEntityArrayList.size());

            // 获取分页数据
            List<TPersonAccessRecordsVO> pageData = personAccessRecordsEntityArrayList.subList(startIndex, endIndex);

            return  new PageResult<>(pageData, personAccessRecordsEntityArrayList.size());

        }else {
            return new PageResult<>(null, 0);
        }
    }

    @Override
    public boolean whetherItExists(String recordsId) {
        LambdaQueryWrapper<TPersonAccessRecordsEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TPersonAccessRecordsEntity::getRecordsId, recordsId);
        List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities = baseMapper.selectList(objectLambdaQueryWrapper);
        if (CollectionUtil.isNotEmpty(tPersonAccessRecordsEntities)){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public JSONObject queryInformationOnkanbanPersonnelStation(Long stationId, List<Long> nbNumids, List<Long> pzNumIds, Integer numberOfPeopleRegistered) {

        /**
         * 在册场内 （内部+派驻）
         * */
        List<Long> allnbNumids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(pzNumIds)){
            allnbNumids.addAll(pzNumIds);
        }
        if (CollectionUtils.isNotEmpty(nbNumids)){
            allnbNumids.addAll(nbNumids);
        }
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat timeformat = new SimpleDateFormat(format);
        LambdaQueryWrapper<TPersonAccessRecordsEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TPersonAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(TPersonAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(TPersonAccessRecordsEntity::getSiteId, stationId);
        objectLambdaQueryWrapper.between(TPersonAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
        int inNumer = 0;
        if(CollectionUtil.isNotEmpty(allnbNumids)){
            objectLambdaQueryWrapper.in(TPersonAccessRecordsEntity::getPersonId, allnbNumids);
            List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities = baseMapper.selectList(objectLambdaQueryWrapper);
            // 按照姓名id进行分组
//            Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId = tPersonAccessRecordsEntities.stream()
//                    .filter(tPersonAccessRecordsEntity -> StringUtils.isNotEmpty(tPersonAccessRecordsEntity.getDevicePersonId()))
//                    .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getDevicePersonId));
            // 首先过滤掉 devicePersonId 为空且 personName 也为空的数据
            List<TPersonAccessRecordsEntity> filteredList = tPersonAccessRecordsEntities.stream()
                    .filter(entity -> {
                        String devicePersonId = entity.getDevicePersonId();
                        String personName = entity.getPersonName();
                        // 保留 devicePersonId 和 personName 至少有一个不为空的实体
                        return (devicePersonId != null && !devicePersonId.isEmpty()) ||
                                (personName != null && !personName.isEmpty());
                    })
                    .collect(Collectors.toList());

            // 按照姓名进行分组，当devicePersonId为空时
            Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId = filteredList.stream()
                    .collect(Collectors.groupingBy(
                            entity -> {
                                String devicePersonId = entity.getDevicePersonId();
                                if (devicePersonId != null && !devicePersonId.isEmpty()) {
                                    return devicePersonId;
                                } else {
                                    return entity.getPersonName();
                                }
                            }
                    ));

            // 打印每个分组并更新inNumer变量
            for (Map.Entry<String, List<TPersonAccessRecordsEntity>> entry : groupedByDevicePersonId.entrySet()) {
                String devicePersonId = entry.getKey();
                List<TPersonAccessRecordsEntity> recordsList = entry.getValue();

                // 找出每个分组中按照时间排序的最后一条数据
                TPersonAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TPersonAccessRecordsEntity::getRecordTime));
                if ("1".equals(lastRecord.getAccessType())) {
                    // 最后一次为入厂
                    inNumer += 1;
                } else {
                    // 最后一次为出厂
                }
            }
        }

        /**
         * 实时总人数
         * */
        LambdaQueryWrapper<TPersonAccessRecordsEntity> objectLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper2.eq(TPersonAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper2.eq(TPersonAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper2.eq(TPersonAccessRecordsEntity::getSiteId, stationId);
        objectLambdaQueryWrapper2.between(TPersonAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
        List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities2 = baseMapper.selectList(objectLambdaQueryWrapper2);
        int inAllNumer = 0;

        // 按照姓名id进行分组
//        Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId2 = tPersonAccessRecordsEntities2
//                .stream()
//                .filter(tPersonAccessRecordsEntity -> StringUtils.isNotEmpty(tPersonAccessRecordsEntity.getDevicePersonId()))
//                .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getDevicePersonId));
        // 首先过滤掉 devicePersonId 为空且 personName 也为空的数据
        List<TPersonAccessRecordsEntity> filteredList = tPersonAccessRecordsEntities2.stream()
                .filter(entity -> {
                    String devicePersonId = entity.getDevicePersonId();
                    String personName = entity.getPersonName();
                    // 保留 devicePersonId 和 personName 至少有一个不为空的实体
                    return (devicePersonId != null && !devicePersonId.isEmpty()) ||
                            (personName != null && !personName.isEmpty());
                })
                .collect(Collectors.toList());

        // 按照姓名进行分组，当devicePersonId为空时
        Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId2 = filteredList.stream()
                .collect(Collectors.groupingBy(
                        entity -> {
                            String devicePersonId = entity.getDevicePersonId();
                            if (devicePersonId != null && !devicePersonId.isEmpty()) {
                                return devicePersonId;
                            } else {
                                return entity.getPersonName();
                            }
                        }
                ));


        // 打印每个分组并更新inNumer变量
        for (Map.Entry<String, List<TPersonAccessRecordsEntity>> entry : groupedByDevicePersonId2.entrySet()) {
            String devicePersonId = entry.getKey();
            List<TPersonAccessRecordsEntity> recordsList = entry.getValue();

            // 找出每个分组中按照时间排序的最后一条数据
            TPersonAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TPersonAccessRecordsEntity::getRecordTime));
            if ("1".equals(lastRecord.getAccessType())) {
                // 最后一次为入厂
                inAllNumer += 1;
            } else {
                // 最后一次为出厂
            }
        }

        /**
         * 内部员工在厂总数
         * */
        LambdaQueryWrapper<TPersonAccessRecordsEntity> objectLambdaQueryWrapper3 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper3.eq(TPersonAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper3.eq(TPersonAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper3.eq(TPersonAccessRecordsEntity::getSiteId, stationId);
        objectLambdaQueryWrapper3.between(TPersonAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));

        int innbNumer = 0;
        List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities3 = baseMapper.selectList(objectLambdaQueryWrapper3);
        // 获取当前站点，今天，所有出入厂记录的人员id
        List<Long> collect = tPersonAccessRecordsEntities3.stream()
                .map(TPersonAccessRecordsEntity::getPersonId)
                .distinct()
                .toList();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(collect)){
            // 筛选出这些id中哪些人 是内部人员
            JSONObject nbUserIds = userFeign.queryNbUserIdByUserIdS(collect);
            List<Long> userIds = nbUserIds.getObject("userIds", ArrayList.class);
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(userIds)){
                LambdaQueryWrapper<TPersonAccessRecordsEntity> objectLambdaQueryWrapper9 = new LambdaQueryWrapper<>();
                objectLambdaQueryWrapper9.eq(TPersonAccessRecordsEntity::getStatus, 1);
                objectLambdaQueryWrapper9.eq(TPersonAccessRecordsEntity::getDeleted, 0);
                objectLambdaQueryWrapper9.eq(TPersonAccessRecordsEntity::getSiteId, stationId);
                objectLambdaQueryWrapper9.in(TPersonAccessRecordsEntity::getPersonId, userIds);
                objectLambdaQueryWrapper9.between(TPersonAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
                List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities9 = baseMapper.selectList(objectLambdaQueryWrapper9);

                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tPersonAccessRecordsEntities9)){
                    // 首先过滤掉 devicePersonId 为空且 personName 也为空的数据
                    List<TPersonAccessRecordsEntity> filteredList1 = tPersonAccessRecordsEntities9.stream()
                            .filter(entity -> {
                                String devicePersonId = entity.getDevicePersonId();
                                String personName = entity.getPersonName();
                                // 保留 devicePersonId 和 personName 至少有一个不为空的实体
                                return (devicePersonId != null && !devicePersonId.isEmpty()) ||
                                        (personName != null && !personName.isEmpty());
                            })
                            .collect(Collectors.toList());

                    // 按照姓名进行分组，当devicePersonId为空时
                    Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId3 = filteredList1.stream()
                            .collect(Collectors.groupingBy(
                                    entity -> {
                                        String devicePersonId = entity.getDevicePersonId();
                                        if (devicePersonId != null && !devicePersonId.isEmpty()) {
                                            return devicePersonId;
                                        } else {
                                            return entity.getPersonName();
                                        }
                                    }
                            ));

                    // 打印每个分组并更新inNumer变量
                    for (Map.Entry<String, List<TPersonAccessRecordsEntity>> entry : groupedByDevicePersonId3.entrySet()) {
                        String devicePersonId = entry.getKey();
                        List<TPersonAccessRecordsEntity> recordsList = entry.getValue();


                        // 找出每个分组中按照时间排序的最后一条数据
                        TPersonAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TPersonAccessRecordsEntity::getRecordTime));
                        if ("1".equals(lastRecord.getAccessType())) {
                            // 最后一次为入厂
                            innbNumer += 1;
                        } else {
                            // 最后一次为出厂
                        }
                    }
                }
            }
        }


        /**
         * 驻场员工在厂总数
         * */
        LambdaQueryWrapper<TPersonAccessRecordsEntity> objectLambdaQueryWrapper4 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper4.eq(TPersonAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper4.eq(TPersonAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper4.eq(TPersonAccessRecordsEntity::getSiteId, stationId);
        objectLambdaQueryWrapper4.between(TPersonAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
        int inzpNumer = 0;
        if (ObjectUtils.isNotEmpty(pzNumIds)){
            objectLambdaQueryWrapper4.in(TPersonAccessRecordsEntity::getPersonId, pzNumIds);
            List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities4 = baseMapper.selectList(objectLambdaQueryWrapper4);

            // 按照姓名id进行分组
//            Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId4 = tPersonAccessRecordsEntities4.stream()
//                    .filter(tPersonAccessRecordsEntity -> StringUtils.isNotEmpty(tPersonAccessRecordsEntity.getDevicePersonId()))
//                    .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getDevicePersonId));
            // 首先过滤掉 devicePersonId 为空且 personName 也为空的数据
            List<TPersonAccessRecordsEntity> filteredList1 = tPersonAccessRecordsEntities4.stream()
                    .filter(entity -> {
                        String devicePersonId = entity.getDevicePersonId();
                        String personName = entity.getPersonName();
                        // 保留 devicePersonId 和 personName 至少有一个不为空的实体
                        return (devicePersonId != null && !devicePersonId.isEmpty()) ||
                                (personName != null && !personName.isEmpty());
                    })
                    .collect(Collectors.toList());

            // 按照姓名进行分组，当devicePersonId为空时
            Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId4 = filteredList1.stream()
                    .collect(Collectors.groupingBy(
                            entity -> {
                                String devicePersonId = entity.getDevicePersonId();
                                if (devicePersonId != null && !devicePersonId.isEmpty()) {
                                    return devicePersonId;
                                } else {
                                    return entity.getPersonName();
                                }
                            }
                    ));

            // 打印每个分组并更新inNumer变量
            for (Map.Entry<String, List<TPersonAccessRecordsEntity>> entry : groupedByDevicePersonId4.entrySet()) {
                String devicePersonId = entry.getKey();
                List<TPersonAccessRecordsEntity> recordsList = entry.getValue();


                // 找出每个分组中按照时间排序的最后一条数据
                TPersonAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TPersonAccessRecordsEntity::getRecordTime));
                if ("1".equals(lastRecord.getAccessType())) {
                    // 最后一次为入厂
                    inzpNumer += 1;
                } else {
                    // 最后一次为出厂
                }
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("inTheRegisteredFactory", inNumer);
        jsonObject.put("realTimeTotalNumberOfPeople", inAllNumer);
        jsonObject.put("nbzc", innbNumer);
        jsonObject.put("wbzp", inzpNumer);
        return jsonObject;
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
    public JSONArray queryTheDetailsOfSitePersonnel(Long stationId) {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat timeformat = new SimpleDateFormat(format);
        LambdaQueryWrapper<TPersonAccessRecordsEntity> objectLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper2.eq(TPersonAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper2.eq(TPersonAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper2.eq(TPersonAccessRecordsEntity::getSiteId, stationId);
        objectLambdaQueryWrapper2.between(TPersonAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
        List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities2 = baseMapper.selectList(objectLambdaQueryWrapper2);

        JSONArray objects = new JSONArray();
        // 按照姓名id进行分组
//        Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId2 = tPersonAccessRecordsEntities2
//                .stream()
//                .filter(tPersonAccessRecordsEntity -> StringUtils.isNotEmpty(tPersonAccessRecordsEntity.getDevicePersonId()))
//                .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getDevicePersonId));

        // 首先过滤掉 devicePersonId 为空且 personName 也为空的数据
        List<TPersonAccessRecordsEntity> filteredList1 = tPersonAccessRecordsEntities2.stream()
                .filter(entity -> {
                    String devicePersonId = entity.getDevicePersonId();
                    String personName = entity.getPersonName();
                    // 保留 devicePersonId 和 personName 至少有一个不为空的实体
                    return (devicePersonId != null && !devicePersonId.isEmpty()) ||
                            (personName != null && !personName.isEmpty());
                })
                .collect(Collectors.toList());

        // 按照姓名进行分组，当devicePersonId为空时
        Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId2 = filteredList1.stream()
                .collect(Collectors.groupingBy(
                        entity -> {
                            String devicePersonId = entity.getDevicePersonId();
                            if (devicePersonId != null && !devicePersonId.isEmpty()) {
                                return devicePersonId;
                            } else {
                                return entity.getPersonName();
                            }
                        }
                ));


        for (Map.Entry<String, List<TPersonAccessRecordsEntity>> entry : groupedByDevicePersonId2.entrySet()) {
            String devicePersonId = entry.getKey();
            List<TPersonAccessRecordsEntity> recordsList = entry.getValue();


            // 找出每个分组中按照时间排序的最后一条数据
            TPersonAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TPersonAccessRecordsEntity::getRecordTime));
            if ("1".equals(lastRecord.getAccessType())) {
                // 最后一次为入厂
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", lastRecord.getPersonName());
                jsonObject.put("fire", lastRecord.getCompanyName());
                jsonObject.put("post", lastRecord.getPositionName());
                jsonObject.put("region", lastRecord.getChannelName());
                objects.add(jsonObject);
            } else {
                // 最后一次为出厂
            }
        }
        return objects;
    }

    @Override
    public JSONObject queryAllVehicleAndPersonStatistics() {
        /**
         * 查询人员统计
         * */
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat timeformat = new SimpleDateFormat(format);
        LambdaQueryWrapper<TPersonAccessRecordsEntity> objectLambdaQueryWrapper3 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper3.eq(TPersonAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper3.eq(TPersonAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper3.between(TPersonAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
        List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities3 = baseMapper.selectList(objectLambdaQueryWrapper3);

        int numberOfFactoryStation = 0;
        // 按照姓名id进行分组
//        Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId3 = tPersonAccessRecordsEntities3.stream()
//                .filter(tPersonAccessRecordsEntity -> StringUtils.isNotEmpty(tPersonAccessRecordsEntity.getDevicePersonId()))
//                .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getDevicePersonId));

        // 首先过滤掉 devicePersonId 为空且 personName 也为空的数据
        List<TPersonAccessRecordsEntity> filteredList1 = tPersonAccessRecordsEntities3.stream()
                .filter(entity -> {
                    String devicePersonId = entity.getDevicePersonId();
                    String personName = entity.getPersonName();
                    // 保留 devicePersonId 和 personName 至少有一个不为空的实体
                    return (devicePersonId != null && !devicePersonId.isEmpty()) ||
                            (personName != null && !personName.isEmpty());
                })
                .collect(Collectors.toList());

        // 按照姓名进行分组，当devicePersonId为空时
        Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId3 = filteredList1.stream()
                .collect(Collectors.groupingBy(
                        entity -> {
                            String devicePersonId = entity.getDevicePersonId();
                            if (devicePersonId != null && !devicePersonId.isEmpty()) {
                                return devicePersonId;
                            } else {
                                return entity.getPersonName();
                            }
                        }
                ));


        List<TPersonAccessRecordsEntity> personAccessRecordsEntityArrayList = new ArrayList<>();
        // 打印每个分组并更新inNumer变量
        for (Map.Entry<String, List<TPersonAccessRecordsEntity>> entry : groupedByDevicePersonId3.entrySet()) {
            List<TPersonAccessRecordsEntity> recordsList = entry.getValue();
            // 找出每个分组中按照时间排序的最后一条数据
            TPersonAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TPersonAccessRecordsEntity::getRecordTime));
            if ("1".equals(lastRecord.getAccessType())) {
                // 最后一次为入厂
                numberOfFactoryStation += 1;
                personAccessRecordsEntityArrayList.add(lastRecord);
            } else {
                // 最后一次为出厂
            }
        }
        // 按照 busis 字段进行分组
        Map<String, Long> typeCounts = personAccessRecordsEntityArrayList.stream()
                .filter(tPersonAccessRecordsEntity -> StringUtils.isNotEmpty(tPersonAccessRecordsEntity.getBusis()))
                .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getBusis, Collectors.counting()));
        // 打印每个类型及其数量
        JSONObject busisStatistics = new JSONObject();
        busisStatistics.putAll(typeCounts);


        /**
         * 查询车辆统计
         * */
        LambdaQueryWrapper<TVehicleAccessRecordsEntity> objectLambdaQueryWrapper4 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper4.eq(TVehicleAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper4.eq(TVehicleAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper4.between(TVehicleAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
        List<TVehicleAccessRecordsEntity> tVehicleAccessLedgerEntities = tVehicleAccessRecordsService.list(objectLambdaQueryWrapper4);
        int numberOfCarStation = 0;
        // 按照车牌进行分组
        Map<String, List<TVehicleAccessRecordsEntity>> groupPlateNumber = tVehicleAccessLedgerEntities.stream()
                .filter(tVehicleAccessRecordsEntity -> StringUtils.isNotEmpty(tVehicleAccessRecordsEntity.getPlateNumber()))
                .collect(Collectors.groupingBy(TVehicleAccessRecordsEntity::getPlateNumber));

        List<TVehicleAccessRecordsEntity> tVehicleAccessRecordsEntities = new ArrayList<>();
        // 打印每个分组并更新inNumer变量
        for (Map.Entry<String, List<TVehicleAccessRecordsEntity>> entry : groupPlateNumber.entrySet()) {
            List<TVehicleAccessRecordsEntity> recordsList = entry.getValue();
            // 找出每个分组中按照时间排序的最后一条数据
            TVehicleAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TVehicleAccessRecordsEntity::getRecordTime));
            if ("1".equals(lastRecord.getAccessType())) {
                // 最后一次为入厂
                numberOfCarStation += 1;
                tVehicleAccessRecordsEntities.add(lastRecord);
            } else {
                // 最后一次为出厂
            }
        }
        // 按照 busis 字段进行分组
        Map<String, Long> modelCounts = tVehicleAccessRecordsEntities.stream()
                .filter(tVehicleAccessRecordsEntity -> StringUtils.isNotEmpty(tVehicleAccessRecordsEntity.getVehicleModel()))
                .collect(Collectors.groupingBy(TVehicleAccessRecordsEntity::getVehicleModel, Collectors.counting()));
        // 打印每个类型及其数量
        JSONObject catTypeStatistics = new JSONObject();
        catTypeStatistics.putAll(modelCounts);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("numberOfFactoryStation", numberOfFactoryStation);
        jsonObject.put("busisStatistics", busisStatistics);
        jsonObject.put("numberOfCarStation", numberOfCarStation);
        jsonObject.put("catTypeStatistics", catTypeStatistics);
        return jsonObject;
    }


    @Override
    public JSONArray numberOfAssemblersAndVehicles(JSONArray siteCoor) {
        JSONArray siteCoorAfter = siteCoor;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(siteCoor)){
            for (int i = 0; i < siteCoor.size(); i++) {
                JSONObject jsonObject = siteCoor.getJSONObject(i);
                Long siteId = jsonObject.getLong("siteId");
                Integer personnelOnlineTotal = querSitePersonnelOnlineTotal(siteId);
                Integer vehicleOnlineTotal = querSiteVehicleOnlineTotal(siteId);
                jsonObject.put("numberOfPeopleAtTheSite", personnelOnlineTotal);
                jsonObject.put("numberOfStops", vehicleOnlineTotal);
            }
        }
        return siteCoorAfter;
    }

    /**
    * @Aouthor: Mryang
    * @Date: 2024/4/23 16:23
    * @describe: 查询指点站点的在厂车辆数量
    *
    */
    private Integer querSiteVehicleOnlineTotal(Long siteId) {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat timeformat = new SimpleDateFormat(format);
        LambdaQueryWrapper<TVehicleAccessRecordsEntity> objectLambdaQueryWrapper4 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper4.eq(TVehicleAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper4.eq(TVehicleAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper4.eq(TVehicleAccessRecordsEntity::getSiteId, siteId);
        objectLambdaQueryWrapper4.between(TVehicleAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
        List<TVehicleAccessRecordsEntity> tVehicleAccessLedgerEntities = tVehicleAccessRecordsService.list(objectLambdaQueryWrapper4);
        int numberOfCarStation = 0;
        // 按照车牌进行分组
        Map<String, List<TVehicleAccessRecordsEntity>> groupPlateNumber = tVehicleAccessLedgerEntities.stream()
                .filter(tVehicleAccessRecordsEntity -> StringUtils.isNotBlank(tVehicleAccessRecordsEntity.getPlateNumber()))
                .collect(Collectors.groupingBy(TVehicleAccessRecordsEntity::getPlateNumber));

        // 打印每个分组并更新inNumer变量
        for (Map.Entry<String, List<TVehicleAccessRecordsEntity>> entry : groupPlateNumber.entrySet()) {
            List<TVehicleAccessRecordsEntity> recordsList = entry.getValue();
            // 找出每个分组中按照时间排序的最后一条数据
            TVehicleAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TVehicleAccessRecordsEntity::getRecordTime));
            if ("1".equals(lastRecord.getAccessType())) {
                // 最后一次为入厂
                numberOfCarStation += 1;
            } else {
                // 最后一次为出厂
            }
        }
        return numberOfCarStation;
    }

    /**
    * @Aouthor: Mryang
    * @Date: 2024/4/23 16:22
    * @describe: 查询指定站点的在厂人员数量
    *
    */
    private Integer querSitePersonnelOnlineTotal(Long siteId) {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat timeformat = new SimpleDateFormat(format);
        LambdaQueryWrapper<TPersonAccessRecordsEntity> objectLambdaQueryWrapper3 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper3.eq(TPersonAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper3.eq(TPersonAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper3.eq(TPersonAccessRecordsEntity::getSiteId, siteId);
        objectLambdaQueryWrapper3.between(TPersonAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
        List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities3 = baseMapper.selectList(objectLambdaQueryWrapper3);

        int numberOfFactoryStation = 0;
        // 按照姓名id进行分组
//        Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId3 = tPersonAccessRecordsEntities3.stream()
//                .filter(tPersonAccessRecordsEntity -> StringUtils.isNotBlank(tPersonAccessRecordsEntity.getDevicePersonId()))
//                .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getDevicePersonId));

        // 首先过滤掉 devicePersonId 为空且 personName 也为空的数据
        List<TPersonAccessRecordsEntity> filteredList1 = tPersonAccessRecordsEntities3.stream()
                .filter(entity -> {
                    String devicePersonId = entity.getDevicePersonId();
                    String personName = entity.getPersonName();
                    // 保留 devicePersonId 和 personName 至少有一个不为空的实体
                    return (devicePersonId != null && !devicePersonId.isEmpty()) ||
                            (personName != null && !personName.isEmpty());
                })
                .collect(Collectors.toList());

        // 按照姓名进行分组，当devicePersonId为空时
        Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId3 = filteredList1.stream()
                .collect(Collectors.groupingBy(
                        entity -> {
                            String devicePersonId = entity.getDevicePersonId();
                            if (devicePersonId != null && !devicePersonId.isEmpty()) {
                                return devicePersonId;
                            } else {
                                return entity.getPersonName();
                            }
                        }
                ));

        // 打印每个分组并更新inNumer变量
        for (Map.Entry<String, List<TPersonAccessRecordsEntity>> entry : groupedByDevicePersonId3.entrySet()) {
            List<TPersonAccessRecordsEntity> recordsList = entry.getValue();
            // 找出每个分组中按照时间排序的最后一条数据
            TPersonAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TPersonAccessRecordsEntity::getRecordTime));
            if ("1".equals(lastRecord.getAccessType())) {
                // 最后一次为入厂
                numberOfFactoryStation += 1;
            } else {
                // 最后一次为出厂
            }
        }
        return numberOfFactoryStation;
    }

    @Override
    public JSONObject queryTheStatisticsOfTheTypeOfWorkBySiteId(Long stationId) {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat timeformat = new SimpleDateFormat(format);
        LambdaQueryWrapper<TPersonAccessRecordsEntity> objectLambdaQueryWrapper3 = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper3.eq(TPersonAccessRecordsEntity::getStatus, 1);
        objectLambdaQueryWrapper3.eq(TPersonAccessRecordsEntity::getDeleted, 0);
        objectLambdaQueryWrapper3.eq(TPersonAccessRecordsEntity::getSiteId, stationId);
        objectLambdaQueryWrapper3.between(TPersonAccessRecordsEntity::getRecordTime,  timeformat.format(getTodayStart()), timeformat.format(getTodayEnd()));
        List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities3 = baseMapper.selectList(objectLambdaQueryWrapper3);

        int numberOfFactoryStation = 0;
        List<TPersonAccessRecordsEntity> objects = new ArrayList<>();
        // 首先过滤掉 devicePersonId 为空且 personName 也为空的数据
        List<TPersonAccessRecordsEntity> filteredList1 = tPersonAccessRecordsEntities3.stream()
                .filter(entity -> {
                    String devicePersonId = entity.getDevicePersonId();
                    String personName = entity.getPersonName();
                    // 保留 devicePersonId 和 personName 至少有一个不为空的实体
                    return (devicePersonId != null && !devicePersonId.isEmpty()) ||
                            (personName != null && !personName.isEmpty());
                })
                .collect(Collectors.toList());

        // 按照姓名进行分组，当devicePersonId为空时
        Map<String, List<TPersonAccessRecordsEntity>> groupedByDevicePersonId3 = filteredList1.stream()
                .collect(Collectors.groupingBy(
                        entity -> {
                            String devicePersonId = entity.getDevicePersonId();
                            if (devicePersonId != null && !devicePersonId.isEmpty()) {
                                return devicePersonId;
                            } else {
                                return entity.getPersonName();
                            }
                        }
                ));

        // 打印每个分组并更新inNumer变量
        for (Map.Entry<String, List<TPersonAccessRecordsEntity>> entry : groupedByDevicePersonId3.entrySet()) {
            List<TPersonAccessRecordsEntity> recordsList = entry.getValue();
            // 找出每个分组中按照时间排序的最后一条数据
            TPersonAccessRecordsEntity lastRecord = Collections.max(recordsList, Comparator.comparing(TPersonAccessRecordsEntity::getRecordTime));
            if ("1".equals(lastRecord.getAccessType())) {
                // 最后一次为入厂
                numberOfFactoryStation += 1;
                objects.add(lastRecord);
            } else {
                // 最后一次为出厂
            }
        }
        // 过滤掉positionName为空的实体
        List<TPersonAccessRecordsEntity> filteredList = objects.stream()
                .filter(entity -> entity.getPositionName() != null && !entity.getPositionName().isEmpty())
                .collect(Collectors.toList());

        // 按positionName进行分组并计算每个组中的人数
        Map<String, Long> groupCountMap = filteredList.stream()
                .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getPositionName, Collectors.counting()));

        // 过滤掉busis为空的实体
        List<TPersonAccessRecordsEntity> filteredBusisList = objects.stream()
                .filter(entity -> entity.getBusis() != null && !entity.getBusis().isEmpty())
                .collect(Collectors.toList());
        // 按busis进行分组并计算每个组中的人数
        Map<String, Long> busisGroupCountMap = filteredBusisList.stream()
                .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getBusis, Collectors.counting()));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("postAll", groupCountMap);
        jsonObject.put("busisAll", busisGroupCountMap);
        return jsonObject;
    }

    @Override
    public void saveFegin(TPersonAccessRecordsDTO accessRecordsDTO) {
        TPersonAccessRecordsEntity entity = TPersonAccessRecordsConvert.INSTANCE.convertDto(accessRecordsDTO);
        entity.setRecordsId(RandomSnowUtils.getSnowRandom());

        baseMapper.insert(entity);
    }

    @Override
    public void deletePersonAccessRecords(Long supplement) {
        if (supplement != null){
            QueryWrapper<TPersonAccessRecordsEntity> objectQueryWrapper = new QueryWrapper<>();
            objectQueryWrapper.eq("status", 1);
            objectQueryWrapper.eq("deleted", 0);
            objectQueryWrapper.eq("supplement_id", supplement);
            baseMapper.delete(objectQueryWrapper);
        }
    }
}
