package com.hxls.datasection.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.datasection.convert.TPersonAccessRecordsConvert;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.query.TPersonAccessRecordsQuery;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import com.hxls.datasection.dao.TPersonAccessRecordsDao;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Override
    public PageResult<TPersonAccessRecordsVO> page(TPersonAccessRecordsQuery query) {
        IPage<TPersonAccessRecordsEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(TPersonAccessRecordsConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TPersonAccessRecordsEntity> getWrapper(TPersonAccessRecordsQuery query){
        LambdaQueryWrapper<TPersonAccessRecordsEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.isNotBlank(query.getPersonName()),TPersonAccessRecordsEntity::getPersonName, query.getPersonName());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getManufacturerId()), TPersonAccessRecordsEntity::getManufacturerId, query.getManufacturerId());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getAccessType()), TPersonAccessRecordsEntity::getAccessType, query.getAccessType());
        wrapper.eq(ObjectUtils.isNotEmpty(query.getChannelId()), TPersonAccessRecordsEntity::getChannelId, query.getChannelId());
        wrapper.between(ObjectUtils.isNotEmpty(query.getStartRecordTime()) && ObjectUtils.isNotEmpty(query.getEndRecordTime()), TPersonAccessRecordsEntity::getRecordTime, query.getStartRecordTime(), query.getEndRecordTime());
        wrapper.eq(TPersonAccessRecordsEntity::getStatus, 1);
        wrapper.eq(TPersonAccessRecordsEntity::getDeleted, 0);
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
    public PageResult<TPersonAccessRecordsVO> pageUnidirectionalTpersonAccessRecords(TPersonAccessRecordsQuery query) {
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

        query.setStartRecordTime(startOfDayString);
        query.setEndRecordTime(endOfDayString);
        LambdaQueryWrapper<TPersonAccessRecordsEntity> wrapper = getWrapper(query);
        List<TPersonAccessRecordsEntity> tPersonAccessRecordsEntities = baseMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(tPersonAccessRecordsEntities)){

            List<TPersonAccessRecordsVO> personAccessRecordsEntityArrayList = new ArrayList<>();

            // 按照 manufacturerId 进行分组
            Map<Long, List<TPersonAccessRecordsEntity>> groupedByManufacturerId = tPersonAccessRecordsEntities.stream()
                    .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getManufacturerId));
            // 打印每个分组
            groupedByManufacturerId.forEach((manufacturerId, recordsList) -> {
                System.out.println("Manufacturer ID: " + manufacturerId);
                System.out.println("Records:");
                recordsList.forEach(System.out::println);
                System.out.println("---------------------------------");

                // 按照 姓名 进行分组
                Map<Long, List<TPersonAccessRecordsEntity>> groupedByPersonId = recordsList.stream()
                        .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getPersonId));

                // 打印每个分组
                groupedByPersonId.forEach((personId, records2List) -> {
                    System.out.println("Person ID: " + personId);
                    System.out.println("Records:");
                    records2List.forEach(System.out::println);
                    System.out.println("---------------------------------");

                    // 按照 进出类型 进行分组
                    Map<String, List<TPersonAccessRecordsEntity>> groupedByAccessType = records2List.stream()
                            .collect(Collectors.groupingBy(TPersonAccessRecordsEntity::getAccessType));
                    int size = groupedByAccessType.size();
                    if (size == 1){
                        groupedByPersonId.forEach((accessType, records3List) -> {
                            TPersonAccessRecordsEntity tPersonAccessRecordsEntity = records3List.get(0);
                            TPersonAccessRecordsVO convert = TPersonAccessRecordsConvert.INSTANCE.convert(tPersonAccessRecordsEntity);
                            convert.setDirectionType(convert.getAccessType().equals("1") ? "未出场" : "未入场");
                            convert.setTodayDetails(records3List);
                            personAccessRecordsEntityArrayList.add(convert);
                        });
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
}