package com.hxls.appointment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.api.dto.message.MessageDTO;
import com.hxls.api.feign.datasection.DatasectionFeign;
import com.hxls.api.feign.message.SmsFeign;
import com.hxls.api.feign.system.StationFeign;
import com.hxls.api.feign.system.UserFeign;
import com.hxls.api.module.message.SmsApi;
import com.hxls.appointment.config.StorageImagesProperties;
import com.hxls.appointment.convert.TAppointmentConvert;
import com.hxls.appointment.convert.TAppointmentPersonnelConvert;
import com.hxls.appointment.convert.TAppointmentVehicleConvert;
import com.hxls.appointment.dao.TAppointmentDao;
import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.entity.TAppointmentPersonnel;
import com.hxls.appointment.pojo.entity.TAppointmentVehicle;
import com.hxls.appointment.pojo.query.TAppointmentQuery;
import com.hxls.appointment.pojo.vo.*;
import com.hxls.appointment.service.TAppointmentPersonnelService;
import com.hxls.appointment.service.TAppointmentService;
import com.hxls.appointment.service.TIssueEigenvalueService;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.ClassToMapUtils;
import com.hxls.framework.common.utils.DateUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hxls.framework.common.query.Query;


/**
 * 预约信息表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-15
 */
@Service
@AllArgsConstructor
@Slf4j
public class TAppointmentServiceImpl extends BaseServiceImpl<TAppointmentDao, TAppointmentEntity> implements TAppointmentService {

    /**
     * 注入预约人员数据层
     */
    private final TAppointmentPersonnelService tAppointmentPersonnelService;

    /**
     * 注入预约车辆数据层
     */
    private final TAppointmentVehicleServiceImpl tAppointmentVehicleService;

    /**
     * 自定义sql方法的mapper
     */
    private final TAppointmentDao appointmentDao;

    /**
     * 消息队列
     */
    private final AmqpTemplate rabbitMQTemplate;

    /*
     * 发送消息feign
     */
    private final UserFeign userFeign;

    /*
     * 系统服务 - 厂站
     */
    private final StationFeign stationFeign;
    /*
     * 配置文件
     */
    private final StorageImagesProperties properties;

    /*
     *数据记录feign
     */
    private final DatasectionFeign datasectionFeign;

    /*
     * 下发特质值表
     */
    private final TIssueEigenvalueService issueEigenvalueService;

    /*
     * 短息API
     */
    private final SmsFeign smsApi;

    /**
     * 注入线程池
     */
    private final ThreadPoolExecutor executor;


    @Override
    public PageResult<TAppointmentVO> page(TAppointmentQuery query) {

        LambdaQueryWrapper<TAppointmentEntity> wrapper = query.getCreator() == null ? getWrapper(query) : getWrapperAll(query);

        IPage<TAppointmentEntity> page = baseMapper.selectPage(getPage(query), wrapper);
        List<TAppointmentVO> tAppointmentVOS = TAppointmentConvert.INSTANCE.convertList(page.getRecords());
        //这里需要做一个处理，回显提交人
        //需要翻译回显
        for (TAppointmentVO tAppointmentVO : tAppointmentVOS) {
            Long id = tAppointmentVO.getId();
            Long submitter = tAppointmentVO.getSubmitter();

            String name = appointmentDao.getNameById(submitter);
            tAppointmentVO.setCreatorName(name);

            TAppointmentPersonnel one = tAppointmentPersonnelService.getOne(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, id)
                    .eq(TAppointmentPersonnel::getUserId, submitter));
            if (ObjectUtil.isNotNull(one)) {
                tAppointmentVO.setSubmitPeople(TAppointmentPersonnelConvert.INSTANCE.convert(one));
                tAppointmentVO.setSubmitterName(one.getExternalPersonnel());
            }

            //场站名称
            if (tAppointmentVO.getSiteId() != null) {
                String siteName = appointmentDao.selectSiteNameById(tAppointmentVO.getSiteId());
                tAppointmentVO.setSiteName(siteName);
            }
            //供应商名称
            if (StringUtils.isNotEmpty(tAppointmentVO.getSupplierName())) {
                String siteName = appointmentDao.selectSupplierNameById(Long.parseLong(tAppointmentVO.getSupplierName()));
                tAppointmentVO.setSupplierName(siteName);
            }
            //创建者名称
            if (tAppointmentVO.getCreator() != null) {
                com.alibaba.fastjson.JSONObject jsonObject = appointmentDao.selectRealNameById(tAppointmentVO.getCreator());
                if (jsonObject != null) {
                    String realName = jsonObject.getString("real_name");
                    String postName = jsonObject.getString("name");
                    tAppointmentVO.setCreatorName(realName);
                    tAppointmentVO.setSubmitterOrgName(postName);
                }
            }
        }
        return new PageResult<>(tAppointmentVOS, page.getTotal());
    }

    private LambdaQueryWrapper<TAppointmentEntity> getWrapper(TAppointmentQuery query) {
        LambdaQueryWrapper<TAppointmentEntity> wrapper = Wrappers.lambdaQuery();
        List<String> list = Stream.of("3", "4", "5").toList();
        wrapper.in(query.getOther(), TAppointmentEntity::getAppointmentType, list);
        if (query.getIsFinish() != null) {
            if (query.getIsFinish()) {
                wrapper.eq(TAppointmentEntity::getReviewStatus, 0);
            } else {
                wrapper.in(TAppointmentEntity::getReviewStatus, List.of(1, -1));
            }
        }
        wrapper.in(CollectionUtils.isNotEmpty(query.getSiteIds()), TAppointmentEntity::getSiteId, query.getSiteIds());
        wrapper.eq(StringUtils.isNotEmpty(query.getAppointmentType()), TAppointmentEntity::getAppointmentType, query.getAppointmentType());
        wrapper.eq(StringUtils.isNotEmpty(query.getSupplierName()), TAppointmentEntity::getSupplierName, query.getSupplierName());
        wrapper.eq(query.getSubmitter() != null, TAppointmentEntity::getSubmitter, query.getSubmitter());
        wrapper.eq(query.getSiteId() != null, TAppointmentEntity::getSiteId, query.getSiteId());
        wrapper.like(StringUtils.isNotEmpty(query.getSiteName()), TAppointmentEntity::getSiteName, query.getSiteName());
        wrapper.ge(StringUtils.isNotEmpty(query.getStartTime()), TAppointmentEntity::getStartTime, query.getStartTime());
        wrapper.le(StringUtils.isNotEmpty(query.getEndTime()), TAppointmentEntity::getEndTime, query.getEndTime());
        wrapper.ge(ArrayUtils.isNotEmpty(query.getAppointmentTime()), TAppointmentEntity::getStartTime, ArrayUtils.isNotEmpty(query.getAppointmentTime()) ? query.getAppointmentTime()[0] : null);
        wrapper.le(ArrayUtils.isNotEmpty(query.getAppointmentTime()), TAppointmentEntity::getEndTime, ArrayUtils.isNotEmpty(query.getAppointmentTime()) ? query.getAppointmentTime()[1] : null);

        wrapper.between(ArrayUtils.isNotEmpty(query.getReviewTime()), TAppointmentEntity::getReviewTime, ArrayUtils.isNotEmpty(query.getReviewTime()) ? query.getReviewTime()[0] : null, ArrayUtils.isNotEmpty(query.getReviewTime()) ? query.getReviewTime()[1] : null);
        wrapper.between(ArrayUtils.isNotEmpty(query.getCreatTime()), TAppointmentEntity::getCreateTime, ArrayUtils.isNotEmpty(query.getCreatTime()) ? query.getCreatTime()[0] : null, ArrayUtils.isNotEmpty(query.getCreatTime()) ? query.getCreatTime()[1] : null);
        wrapper.eq(StringUtils.isNotEmpty(query.getReviewResult()), TAppointmentEntity::getReviewResult, query.getReviewResult());
        wrapper.eq(StringUtils.isNotEmpty(query.getReviewStatus()), TAppointmentEntity::getReviewStatus, query.getReviewStatus());
        if (query.getIsPerson()) {
            //  wrapper.isNull(TAppointmentEntity::getSupplierSubclass).or().eq(TAppointmentEntity::getSupplierSubclass, 0);
            wrapper.ne(TAppointmentEntity::getSupplierSubclass, 1);
        }
        wrapper.eq(query.getSupplierSubclass() != null, TAppointmentEntity::getSupplierSubclass, query.getSupplierSubclass());
        wrapper.eq(query.getId() != null, TAppointmentEntity::getCreator, query.getId());
        wrapper.eq(StringUtils.isNotEmpty(query.getOpenId()), TAppointmentEntity::getOpenId, query.getOpenId());
        if (StringUtils.isNotEmpty(query.getSubmitterName())) {
            List<TAppointmentPersonnel> tAppointmentPersonnels = tAppointmentPersonnelService
                    .list(new LambdaQueryWrapper<TAppointmentPersonnel>().like(TAppointmentPersonnel::getExternalPersonnel, query.getSubmitterName())
                    );
            if (CollectionUtils.isNotEmpty(tAppointmentPersonnels)) {
                List<Long> ids = new ArrayList<>();
                for (TAppointmentPersonnel tAppointmentPersonnel : tAppointmentPersonnels) {
                    Long appointmentId = tAppointmentPersonnel.getAppointmentId();
                    List<TAppointmentPersonnel> appointmentPersonnels = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, appointmentId));
                    if (CollectionUtils.isNotEmpty(appointmentPersonnels)) {
                        if (appointmentPersonnels.get(0).getExternalPersonnel().equals(query.getSubmitterName())) {
                            ids.add(appointmentId);
                        }
                    }
                }
                wrapper.in(CollectionUtils.isNotEmpty(ids), TAppointmentEntity::getId, ids);
            }
        }
        return wrapper;
    }

    private LambdaQueryWrapper<TAppointmentEntity> getWrapperAll(TAppointmentQuery query) {
        List<String> list = Stream.of("3", "4", "5").toList();
        LambdaQueryWrapper<TAppointmentEntity> wrapper = getWrapper(query);

        wrapper.or().eq(TAppointmentEntity::getCreator, query.getCreator())
                .in(query.getOther(), TAppointmentEntity::getAppointmentType, list)

                .eq(StringUtils.isNotEmpty(query.getAppointmentType()), TAppointmentEntity::getAppointmentType, query.getAppointmentType())
                .eq(StringUtils.isNotEmpty(query.getSupplierName()), TAppointmentEntity::getSupplierName, query.getSupplierName())
                .eq(query.getSubmitter() != null, TAppointmentEntity::getSubmitter, query.getSubmitter())
                .eq(query.getSiteId() != null, TAppointmentEntity::getSiteId, query.getSiteId())
                .like(StringUtils.isNotEmpty(query.getSiteName()), TAppointmentEntity::getSiteName, query.getSiteName())
                .ge(StringUtils.isNotEmpty(query.getStartTime()), TAppointmentEntity::getStartTime, query.getStartTime())
                .le(StringUtils.isNotEmpty(query.getEndTime()), TAppointmentEntity::getEndTime, query.getEndTime())
                .between(ArrayUtils.isNotEmpty(query.getReviewTime()), TAppointmentEntity::getReviewTime, ArrayUtils.isNotEmpty(query.getReviewTime()) ? query.getReviewTime()[0] : null, ArrayUtils.isNotEmpty(query.getReviewTime()) ? query.getReviewTime()[1] : null)
                .between(ArrayUtils.isNotEmpty(query.getCreatTime()), TAppointmentEntity::getCreateTime, ArrayUtils.isNotEmpty(query.getCreatTime()) ? query.getCreatTime()[0] : null, ArrayUtils.isNotEmpty(query.getCreatTime()) ? query.getCreatTime()[1] : null)
                .eq(StringUtils.isNotEmpty(query.getReviewResult()), TAppointmentEntity::getReviewResult, query.getReviewResult())
                .eq(StringUtils.isNotEmpty(query.getReviewStatus()), TAppointmentEntity::getReviewStatus, query.getReviewStatus())
                .eq(query.getSupplierSubclass() != null, TAppointmentEntity::getSupplierSubclass, query.getSupplierSubclass())
                .eq(query.getId() != null, TAppointmentEntity::getCreator, query.getId())
                .eq(StringUtils.isNotEmpty(query.getOpenId()), TAppointmentEntity::getOpenId, query.getOpenId());

        if (query.getIsFinish() != null) {
            if (query.getIsFinish()) {
                wrapper.eq(TAppointmentEntity::getReviewStatus, 0);
            } else {
                wrapper.in(TAppointmentEntity::getReviewStatus, List.of(1, -1));
            }
        }
        if (query.getIsPerson() && StringUtils.isEmpty(query.getReviewStatus())) {
            //  wrapper.isNull(TAppointmentEntity::getSupplierSubclass).or().eq(TAppointmentEntity::getSupplierSubclass, 0);
            wrapper.ne(TAppointmentEntity::getSupplierSubclass, 1);
        }
        if (StringUtils.isNotEmpty(query.getSubmitterName())) {
            List<TAppointmentPersonnel> tAppointmentPersonnels = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().like(TAppointmentPersonnel::getExternalPersonnel, query.getSubmitterName()));
            if (CollectionUtils.isNotEmpty(tAppointmentPersonnels)) {
                List<Long> ids = tAppointmentPersonnels.stream().map(TAppointmentPersonnel::getAppointmentId).toList();
                wrapper.in(TAppointmentEntity::getId, ids);
            }
        }
        return wrapper;
    }

    /**
     * 插入主表单以及附属信息
     *
     * @param vo 主单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(TAppointmentVO vo) {
        //主表转换
        TAppointmentEntity entity = TAppointmentConvert.INSTANCE.convert(vo);
        //检查预约单是否有未完成得预约信息
        List<TAppointmentEntity> tAppointmentEntityList = list(new LambdaQueryWrapper<TAppointmentEntity>().eq(TAppointmentEntity::getSiteId, vo.getSiteId()).le(TAppointmentEntity::getStartTime, vo.getStartTime()).ge(TAppointmentEntity::getEndTime, vo.getEndTime()));
        if (CollectionUtils.isNotEmpty(tAppointmentEntityList)) {
            List<Long> list = tAppointmentEntityList.stream().map(TAppointmentEntity::getId).collect(Collectors.toList());
            List<TAppointmentPersonnel> personnelList = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().in(TAppointmentPersonnel::getAppointmentId, list));
            if (CollectionUtils.isNotEmpty(personnelList)) {
                List<Long> userIds = personnelList.stream().map(TAppointmentPersonnel::getUserId).collect(Collectors.toList());
                List<String> names = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(vo.getPersonnelList())) {
                    vo.getPersonnelList().forEach(item -> {
                        Long userId = item.getUserId();
                        if (userIds.contains(userId)) {
                            names.add(item.getExternalPersonnel());
                        }
                    });
                }
                if (CollectionUtils.isNotEmpty(names)) {
                    String join = StrUtil.join(",", names);
                    // 如果连接后的字符串长度大于 0
                    throw new ServerException(join + "在预约时间段内有未完成的预约单");
                }
            }
        }

        //默认启用
        entity.setStatus(Constant.ENABLE);
        //插入主预约信息单
        int insert = baseMapper.insert(entity);
        //判断是否插入成功
        if (insert > Constant.ZERO) {
            Long id = entity.getId();
            //避免没有id报错
            if (ObjectUtil.isNotNull(id)) {
                //判断是否有人员清单
                List<TAppointmentPersonnelVO> personnelList = vo.getPersonnelList();
                if (CollectionUtils.isNotEmpty(personnelList)) {
                    List<TAppointmentPersonnel> tAppointmentPersonnels = personnelList.stream().map(item -> {
                        TAppointmentPersonnel tAppointmentPersonnel = new TAppointmentPersonnel();
                        BeanUtil.copyProperties(item, tAppointmentPersonnel);
                        tAppointmentPersonnel.setAppointmentId(id);
                        return tAppointmentPersonnel;
                    }).toList();
                    tAppointmentPersonnelService.saveBatch(tAppointmentPersonnels);
                }
                //判断是否有车辆清单
                List<TAppointmentVehicleVO> vehicleList = vo.getVehicleList();
                if (CollectionUtils.isNotEmpty(vehicleList)) {
                    List<TAppointmentVehicle> tAppointmentVehicles = vehicleList.stream().map(item -> {
                        TAppointmentVehicle tAppointmentVehicle = new TAppointmentVehicle();
                        BeanUtil.copyProperties(item, tAppointmentVehicle);
                        tAppointmentVehicle.setAppointmentId(id);
                        return tAppointmentVehicle;
                    }).toList();
                    tAppointmentVehicleService.saveBatch(tAppointmentVehicles);
                }
            }
            //发送消息
            userFeign.sendSystemMessage(entity.getAppointmentType(), entity.getSiteId());


            if (vo.getReviewStatus().equals(Constant.PASS)) {

                TAppointmentEntity byId = getById(id);
                List<TAppointmentVehicle> list = tAppointmentVehicleService.list(new LambdaQueryWrapper<TAppointmentVehicle>().eq(
                        TAppointmentVehicle::getAppointmentId, id
                ));
                List<TAppointmentPersonnel> personnelList = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>()
                        .eq(TAppointmentPersonnel::getAppointmentId, id));
                if (CollectionUtils.isNotEmpty(personnelList)) {
                    String domain = properties.getConfig().getDomain();
                    String siteCode = appointmentDao.selectSiteCodeById(byId.getSiteId());
                    List<String> strings = appointmentDao.selectManuFacturerIdById(byId.getSiteId(), "1");
//
//                    List<String> dictValue = null;
//
//                    List<com.alibaba.fastjson.JSONObject> allDictByType = appointmentDao.getAllDictByType(37);
//                    if (CollectionUtils.isNotEmpty(allDictByType)){
//                        dictValue = allDictByType.stream().map(item -> {
//                            return item.getString("dict_value");
//                        }).toList();
//                    }


                    for (String device : strings) {
                        List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDeviceList(device, byId.getSiteId());
                        List<String> masterIpById = appointmentDao.selectMasterIpById(device, "1", byId.getSiteId());
                        for (String masterIp : masterIpById) {
                            for (TAppointmentPersonnel personnel : personnelList) {
                                JSONObject entries = new JSONObject();
                                entries.set("type", device);
                                entries.set("startTime", DateUtils.format(byId.getStartTime(), DateUtils.DATE_TIME_PATTERN));
                                entries.set("deadline", DateUtils.format(byId.getEndTime(), DateUtils.DATE_TIME_PATTERN));
                                entries.set("peopleName", personnel.getExternalPersonnel());
                                entries.set("peopleCode", personnel.getUserId());
                                entries.set("faceUrl", domain + personnel.getHeadUrl());
                                entries.set("masterIp", masterIp);
                                entries.set("deviceInfos", JSONUtil.toJsonStr(jsonObjects));
                                entries.set("password", jsonObjects.get(0).get("password"));

//                                //判断 是否是需要修改下发人员id的站点
//                                if (CollectionUtils.isNotEmpty(dictValue) && dictValue.contains(siteCode)){
//                                    if (StringUtils.isNotEmpty(personnel.getPhone())){
//                                        entries.set("peopleCode", personnel.getPhone());
//                                    }
//                                }

                                TIssueEigenvalueVO tIssueEigenvalueVO = new TIssueEigenvalueVO();
                                tIssueEigenvalueVO.setStationId(byId.getSiteId());
                                tIssueEigenvalueVO.setType(1);
                                tIssueEigenvalueVO.setStatus(1);
                                tIssueEigenvalueVO.setData(JSONUtil.toJsonStr(entries));
                                tIssueEigenvalueVO.setAreaId(Long.parseLong(byId.getAppointmentType()));
                                tIssueEigenvalueVO.setAreaName(entries.getStr("DELETE") == null ? "新增" : "删除");
                                Long saveId = issueEigenvalueService.save(tIssueEigenvalueVO);
                                entries.set("id", saveId);

                                rabbitMQTemplate.convertAndSend(siteCode + Constant.EXCHANGE, siteCode + Constant.SITE_ROUTING_FACE_TOAGENT, entries);

                                //查看人员表中是否带有车辆信息
                                String plateNumber = personnel.getPlateNumber();
                                if (StrUtil.isNotEmpty(plateNumber)) {
                                    TAppointmentVehicle tAppointmentVehicle = new TAppointmentVehicle();
                                    tAppointmentVehicle.setPlateNumber(plateNumber);
                                    list.add(tAppointmentVehicle);
                                }
                            }
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(list)) {
                    String siteCode = appointmentDao.selectSiteCodeById(byId.getSiteId());
                    List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDevices(byId.getSiteId(), "2");
                    //主机分组
                    Set<String> name = new HashSet<>();
                    for (com.alibaba.fastjson.JSONObject jsonObject : jsonObjects) {
                        for (TAppointmentVehicle tAppointmentVehicle : list) {
                            if (name.add(jsonObject.getString("master_ip"))) {
                                JSONObject entries = new JSONObject();
                                entries.set("type", jsonObject.getString("type"));
                                if (byId.getSupplierSubclass().equals(1)) {
                                    String deliveryDate = tAppointmentVehicle.getDeliveryDate();
                                    entries.set("startTime", deliveryDate.split(",")[0]);
                                    entries.set("deadline", deliveryDate.split(",")[1]);
                                } else {
                                    entries.set("startTime", DateUtils.format(byId.getStartTime(), DateUtils.DATE_TIME_PATTERN));
                                    entries.set("deadline", DateUtils.format(byId.getEndTime(), DateUtils.DATE_TIME_PATTERN));
                                }
                                entries.set("carNumber", tAppointmentVehicle.getPlateNumber());
                                entries.set("status", "add");
                                entries.set("masterIp", jsonObject.get("master_ip"));
                                entries.set("databaseName", jsonObject.get("master_sn"));
                                entries.set("username", jsonObject.get("master_account"));
                                entries.set("password", jsonObject.get("master_password"));

                                TIssueEigenvalueVO tIssueEigenvalueVO = new TIssueEigenvalueVO();
                                tIssueEigenvalueVO.setStationId(byId.getSiteId());
                                tIssueEigenvalueVO.setType(2);
                                tIssueEigenvalueVO.setStatus(1);
                                tIssueEigenvalueVO.setData(JSONUtil.toJsonStr(entries));
                                tIssueEigenvalueVO.setAreaId(Long.parseLong(byId.getAppointmentType()));
                                tIssueEigenvalueVO.setAreaName(entries.getStr("DELETE") == null ? "新增" : "删除");
                                Long saveId = issueEigenvalueService.save(tIssueEigenvalueVO);
                                entries.set("id", saveId);

                                rabbitMQTemplate.convertAndSend(siteCode + Constant.EXCHANGE, siteCode + Constant.SITE_ROUTING_CAR_TOAGENT, entries);
                            }
                        }
                    }
                }
            } else {

                log.info("发送短信");
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    sendMessage(vo.getSiteId(), vo);
                }, executor);

            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TAppointmentVO vo) {
        //修改主表的信息
        TAppointmentEntity entity = TAppointmentConvert.INSTANCE.convert(vo);
        updateById(entity);
        //判断是否修改随行人员表
        List<TAppointmentPersonnelVO> personnelList = vo.getPersonnelList();
        if (CollectionUtils.isNotEmpty(personnelList)) {
            if (vo.getPerson()) {
                //删除之前的人员子单据，再新增
                tAppointmentPersonnelService.remove(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, vo.getId()));
                List<TAppointmentPersonnel> tAppointmentPersonnels = BeanUtil.copyToList(personnelList, TAppointmentPersonnel.class);
                tAppointmentPersonnelService.saveBatch(tAppointmentPersonnels);
            }
        }
        //判断是否有随行车辆
        List<TAppointmentVehicleVO> vehicleList = vo.getVehicleList();
        if (vo.getVehicle()) {
            tAppointmentVehicleService.remove(new LambdaQueryWrapper<TAppointmentVehicle>().eq(TAppointmentVehicle::getAppointmentId, vo.getId()));
            if (CollectionUtils.isNotEmpty(vehicleList)) {
                List<TAppointmentVehicle> vehicles = BeanUtil.copyToList(vehicleList, TAppointmentVehicle.class);
                tAppointmentVehicleService.saveBatch(vehicles);
            }
        }

        log.info("发送短信");
        //修改先不发送短信
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            sendMessage(vo.getSiteId(), vo);
//        }, executor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
        //关联的子表单也需要删除
        tAppointmentPersonnelService.remove(new LambdaQueryWrapper<TAppointmentPersonnel>().in(TAppointmentPersonnel::getAppointmentId, idList));
        tAppointmentVehicleService.remove(new LambdaQueryWrapper<TAppointmentVehicle>().in(TAppointmentVehicle::getAppointmentId, idList));
    }


    /**
     * 根据id获取详细表单信息
     *
     * @param id id
     * @return 主单表
     */
    @Override
    public TAppointmentVO getDetailById(Long id, Long type) {
        //查询基础表单
        TAppointmentEntity byId = this.getById(id);
        if (ObjectUtil.isNull(byId)) {
            throw new ServerException("查询得记录不存在或者已被删除");
        }
        TAppointmentVO vo = TAppointmentConvert.INSTANCE.convert(byId);

        //获取预约信息表单下面人员以及车辆的集合
        //1 人员
        List<TAppointmentPersonnel> personnelList = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, id));
        vo.setPersonnelList(TAppointmentPersonnelConvert.INSTANCE.convertList(personnelList));

        List<TAppointmentPersonnelVO> personnelList1 = vo.getPersonnelList();
        if (CollectionUtils.isNotEmpty(personnelList1)) {
            Iterator<TAppointmentPersonnelVO> iterator = personnelList1.iterator();
            while (iterator.hasNext()) {
                TAppointmentPersonnelVO tAppointmentPersonnelVO = iterator.next();
                if (tAppointmentPersonnelVO.getUserId().equals(vo.getSubmitter())) {
                    vo.setSubmitPeople(tAppointmentPersonnelVO);
                    iterator.remove(); // 使用迭代器安全地移除元素
                    break;
                }
            }
        }

        //2 车辆
        List<TAppointmentVehicle> vehicleList = tAppointmentVehicleService.list(new LambdaQueryWrapper<TAppointmentVehicle>().eq(TAppointmentVehicle::getAppointmentId, id));
        List<TAppointmentVehicleVO> tAppointmentVehicleVOS = TAppointmentVehicleConvert.INSTANCE.convertList(vehicleList);
        vo.setVehicleList(tAppointmentVehicleVOS);


        return vo;
    }


    @Override
    public PageResult<TAppointmentVO> pageByAuthority(TAppointmentQuery query) {

        LambdaQueryWrapper<TAppointmentEntity> wrapper = getWrapperByAuth(query);
        IPage<TAppointmentEntity> page = baseMapper.selectPage(getPage(query), wrapper);
        List<TAppointmentVO> tAppointmentVOS = TAppointmentConvert.INSTANCE.convertList(page.getRecords());
        for (TAppointmentVO tAppointmentVO : tAppointmentVOS) {
            List<TAppointmentPersonnel> list = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>()
                    .eq(TAppointmentPersonnel::getAppointmentId, tAppointmentVO.getId()));
            if (CollectionUtils.isNotEmpty(list)) {
                for (TAppointmentPersonnel tAppointmentPersonnel : list) {
                    if (tAppointmentVO.getSubmitter().equals(tAppointmentPersonnel.getUserId())) {
                        tAppointmentVO.setSubmitterName(tAppointmentPersonnel.getExternalPersonnel());
                        break;
                    }
                }
                tAppointmentVO.setPersonnelList(TAppointmentPersonnelConvert.INSTANCE.convertList(list));
            }

            Long submitter = tAppointmentVO.getSubmitter();

            String name = appointmentDao.getNameById(submitter);
            tAppointmentVO.setCreatorName(name);

            //场站名称
            if (tAppointmentVO.getSiteId() != null) {
                String siteName = appointmentDao.selectSiteNameById(tAppointmentVO.getSiteId());
                tAppointmentVO.setSiteName(siteName);
            }
            //供应商名称
            if (StringUtils.isNotEmpty(tAppointmentVO.getSupplierName())) {
                String siteName = appointmentDao.selectSupplierNameById(Long.parseLong(tAppointmentVO.getSupplierName()));
                tAppointmentVO.setSupplierName(siteName);
            }
            //创建者名称
            if (tAppointmentVO.getCreator() != null) {
                com.alibaba.fastjson.JSONObject jsonObject = appointmentDao.selectRealNameById(tAppointmentVO.getCreator());
                if (jsonObject != null) {
                    String realName = jsonObject.getString("real_name");
                    String postName = jsonObject.getString("name");
                    tAppointmentVO.setCreatorName(realName);
                    tAppointmentVO.setSubmitterOrgName(postName);
                }
            }

            //修改者名字
            if (tAppointmentVO.getUpdater() != null) {
                com.alibaba.fastjson.JSONObject jsonObject = appointmentDao.selectRealNameById(tAppointmentVO.getUpdater());
                if (jsonObject != null) {
                    String realName = jsonObject.getString("real_name");
                    tAppointmentVO.setUpdaterName(realName);
                }
            }


        }
        return new PageResult<>(tAppointmentVOS, page.getTotal());

    }

    private LambdaQueryWrapper<TAppointmentEntity> getWrapperByAuth(TAppointmentQuery query) {

        LambdaQueryWrapper<TAppointmentEntity> wrapper = Wrappers.lambdaQuery();
        List<String> list = Stream.of("3", "4", "5").toList();
        wrapper.in(query.getOther(), TAppointmentEntity::getAppointmentType, list);
        if (query.getIsFinish() != null) {
            if (query.getIsFinish()) {
                wrapper.eq(TAppointmentEntity::getReviewStatus, 0);
            } else {
                wrapper.in(TAppointmentEntity::getReviewStatus, List.of(1, -1));
            }
        }
        wrapper.in(CollectionUtils.isNotEmpty(query.getSiteIds()), TAppointmentEntity::getSiteId, query.getSiteIds());
        wrapper.eq(StringUtils.isNotEmpty(query.getAppointmentType()), TAppointmentEntity::getAppointmentType, query.getAppointmentType());
        wrapper.eq(StringUtils.isNotEmpty(query.getSupplierName()), TAppointmentEntity::getSupplierName, query.getSupplierName());
        wrapper.eq(query.getSubmitter() != null, TAppointmentEntity::getSubmitter, query.getSubmitter());
        wrapper.eq(query.getSiteId() != null, TAppointmentEntity::getSiteId, query.getSiteId());
        wrapper.like(StringUtils.isNotEmpty(query.getSiteName()), TAppointmentEntity::getSiteName, query.getSiteName());
        wrapper.ge(StringUtils.isNotEmpty(query.getStartTime()), TAppointmentEntity::getStartTime, query.getStartTime());
        wrapper.le(StringUtils.isNotEmpty(query.getEndTime()), TAppointmentEntity::getEndTime, query.getEndTime());
        wrapper.ge(ArrayUtils.isNotEmpty(query.getAppointmentTime()), TAppointmentEntity::getStartTime, ArrayUtils.isNotEmpty(query.getAppointmentTime()) ? query.getAppointmentTime()[0] : null);
        wrapper.le(ArrayUtils.isNotEmpty(query.getAppointmentTime()), TAppointmentEntity::getEndTime, ArrayUtils.isNotEmpty(query.getAppointmentTime()) ? query.getAppointmentTime()[1] : null);

        wrapper.between(ArrayUtils.isNotEmpty(query.getReviewTime()), TAppointmentEntity::getUpdateTime, ArrayUtils.isNotEmpty(query.getReviewTime()) ? query.getReviewTime()[0] : null, ArrayUtils.isNotEmpty(query.getReviewTime()) ? query.getReviewTime()[1] : null);
        wrapper.between(ArrayUtils.isNotEmpty(query.getCreatTime()), TAppointmentEntity::getReviewTime, ArrayUtils.isNotEmpty(query.getCreatTime()) ? query.getCreatTime()[0] : null, ArrayUtils.isNotEmpty(query.getCreatTime()) ? query.getCreatTime()[1] : null);
        wrapper.eq(StringUtils.isNotEmpty(query.getReviewResult()), TAppointmentEntity::getReviewResult, query.getReviewResult());
        wrapper.eq(StringUtils.isNotEmpty(query.getReviewStatus()), TAppointmentEntity::getReviewStatus, query.getReviewStatus());
        if (query.getIsPerson()) {
            //  wrapper.isNull(TAppointmentEntity::getSupplierSubclass).or().eq(TAppointmentEntity::getSupplierSubclass, 0);
            wrapper.ne(TAppointmentEntity::getSupplierSubclass, 1);
        }
        wrapper.eq(query.getSupplierSubclass() != null, TAppointmentEntity::getSupplierSubclass, query.getSupplierSubclass());
        wrapper.eq(query.getId() != null, TAppointmentEntity::getCreator, query.getId());
        wrapper.eq(StringUtils.isNotEmpty(query.getOpenId()), TAppointmentEntity::getOpenId, query.getOpenId());
        if (StringUtils.isNotEmpty(query.getSubmitterName())) {
            List<TAppointmentPersonnel> tAppointmentPersonnels = tAppointmentPersonnelService
                    .list(new LambdaQueryWrapper<TAppointmentPersonnel>().like(TAppointmentPersonnel::getExternalPersonnel, query.getSubmitterName())
                    );
            if (CollectionUtils.isNotEmpty(tAppointmentPersonnels)) {
                List<Long> ids = new ArrayList<>();
                for (TAppointmentPersonnel tAppointmentPersonnel : tAppointmentPersonnels) {
                    Long appointmentId = tAppointmentPersonnel.getAppointmentId();
                    List<TAppointmentPersonnel> appointmentPersonnels = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, appointmentId));
                    if (CollectionUtils.isNotEmpty(appointmentPersonnels)) {
                        if (appointmentPersonnels.get(0).getExternalPersonnel().contains(query.getSubmitterName())) {
                            ids.add(appointmentId);
                        }
                    }
                }
                List<Long> idList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(ids)) {
                    List<TAppointmentEntity> allAppointmentList = listByIds(ids);
                    //排除掉 人员派驻
                    List<Long> result = allAppointmentList.stream().filter(item -> !item.getAppointmentType().equals("1")).map(TAppointmentEntity::getId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(result)) {
                        idList.addAll(result);
                    }
                }
                //人员派驻查询提交人的方式不同
                List<Long> longs = appointmentDao.selectByName(query.getSubmitterName());

                if (CollectionUtils.isNotEmpty(longs)) {
                    idList.addAll(longs);
                }
                wrapper.in(CollectionUtils.isNotEmpty(idList), TAppointmentEntity::getId, idList);

                if (CollectionUtils.isEmpty(idList)) {
                    wrapper.eq(TAppointmentEntity::getId, Constant.EMPTY);
                }
            } else {
                wrapper.eq(TAppointmentEntity::getId, Constant.EMPTY);
            }
        }
        return wrapper;
    }

    @Override
    public void updateByAudit(TAppointmentVO vo) {
        //修改主表的信息
        if (CollectionUtils.isEmpty(vo.getIds())) {
            throw new ServerException(ErrorCode.NOT_FOUND);
        }
        for (Long updateId : vo.getIds()) {
            TAppointmentEntity entity = new TAppointmentEntity();
            entity.setId(updateId);
            entity.setReviewStatus(vo.getReviewStatus());

            updateById(entity);
            //审核通过后会执行下发对应指定的厂站
            if (vo.getReviewStatus().equals(Constant.PASS)) {

                //根据指定通道下发数据
                Long id = entity.getId();
                TAppointmentEntity byId = getById(id);
                if (byId.getAppointmentType().equals("1") || byId.getAppointmentType().equals("2") || byId.getAppointmentType().equals("6")) {

                    List<TAppointmentVehicle> list = tAppointmentVehicleService.list(new LambdaQueryWrapper<TAppointmentVehicle>().eq(
                            TAppointmentVehicle::getAppointmentId, id
                    ));
                    List<TAppointmentPersonnel> personnelList = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>()
                            .eq(TAppointmentPersonnel::getAppointmentId, id));
                    //审核通过之后,如果有修改，需要先删除之前的
                    deleteInfo(list, personnelList, byId.getSiteId());

                    if (CollectionUtils.isNotEmpty(personnelList)) {
                        String domain = properties.getConfig().getDomain();
                        //判断是否 透传厂站 -- 传递到设备保存的厂站。
                        List<com.alibaba.fastjson.JSONObject> allDictByType = appointmentDao.getAllDictByType(35);
                        for (com.alibaba.fastjson.JSONObject jsonObject : allDictByType) {
                            String string = jsonObject.getString("dict_value");
                            if (string.contains(byId.getSiteId().toString()) && string.split("_")[0].equals(byId.getSiteId().toString())) {
                                String[] split = string.split("_");
                                byId.setSiteId(Long.parseLong(split[1]));
                            }
                        }


//                        //判断是否是特殊站点
//                        List<String> dictValue = null;
//
//                        List<com.alibaba.fastjson.JSONObject> allDictByType1 = appointmentDao.getAllDictByType(37);
//                        if (CollectionUtils.isNotEmpty(allDictByType1)){
//                            dictValue = allDictByType1.stream().map(item -> {
//                                return item.getString("dict_value");
//                            }).toList();
//                        }


                        String siteCode = appointmentDao.selectSiteCodeById(byId.getSiteId());
                        List<String> strings = appointmentDao.selectManuFacturerIdById(byId.getSiteId(), "1");
                        for (String device : strings) {
                            List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDeviceList(device, byId.getSiteId());
                            List<String> masterIpById = appointmentDao.selectMasterIpById(device, "1", byId.getSiteId());
                            for (String masterIp : masterIpById) {
                                for (TAppointmentPersonnel personnel : personnelList) {
                                    JSONObject entries = new JSONObject();
                                    entries.set("type", device);
                                    entries.set("startTime", DateUtils.format(byId.getStartTime(), DateUtils.DATE_TIME_PATTERN));
                                    entries.set("deadline", DateUtils.format(byId.getEndTime(), DateUtils.DATE_TIME_PATTERN));
                                    entries.set("peopleName", personnel.getExternalPersonnel());
                                    entries.set("peopleCode", personnel.getUserId());
                                    entries.set("faceUrl", domain + personnel.getHeadUrl());
                                    entries.set("masterIp", masterIp);
                                    entries.set("deviceInfos", JSONUtil.toJsonStr(jsonObjects));
                                    entries.set("password", jsonObjects.get(0).get("password"));

                                    TIssueEigenvalueVO tIssueEigenvalueVO = new TIssueEigenvalueVO();
                                    tIssueEigenvalueVO.setStationId(byId.getSiteId());
                                    tIssueEigenvalueVO.setType(1);
                                    tIssueEigenvalueVO.setStatus(1);
                                    tIssueEigenvalueVO.setData(JSONUtil.toJsonStr(entries));
                                    tIssueEigenvalueVO.setAreaId(Long.parseLong(byId.getAppointmentType()));
                                    tIssueEigenvalueVO.setAreaName(entries.getStr("DELETE") == null ? "新增" : "删除");
                                    Long saveId = issueEigenvalueService.save(tIssueEigenvalueVO);
                                    entries.set("id", saveId);

                                    rabbitMQTemplate.convertAndSend(siteCode + Constant.EXCHANGE, siteCode + Constant.SITE_ROUTING_FACE_TOAGENT, entries);

                                    log.info("审核后人脸发送交换机：{} , 和消息:{}", siteCode, entries);
                                    //查看人员表中是否带有车辆信息
                                    String plateNumber = personnel.getPlateNumber();
                                    if (StrUtil.isNotEmpty(plateNumber)) {
                                        TAppointmentVehicle tAppointmentVehicle = new TAppointmentVehicle();
                                        tAppointmentVehicle.setPlateNumber(plateNumber);
                                        list.add(tAppointmentVehicle);
                                    }
                                }
                            }
                        }
                    }


                    if (CollectionUtils.isNotEmpty(list)) {

                        //判断是否 透传厂站 -- 传递到设备保存的厂站。
                        List<com.alibaba.fastjson.JSONObject> allDictByType = appointmentDao.getAllDictByType(35);
                        for (com.alibaba.fastjson.JSONObject jsonObject : allDictByType) {
                            String string = jsonObject.getString("dict_value");
                            if (string.contains(byId.getSiteId().toString()) && string.split("_")[0].equals(byId.getSiteId().toString())) {
                                String[] split = string.split("_");
                                byId.setSiteId(Long.parseLong(split[1]));
                            }
                        }

                        String siteCode = appointmentDao.selectSiteCodeById(byId.getSiteId());
                        List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDevices(byId.getSiteId(), "2");
                        //主机分组

                        //主机分组
                        Set<String> name = new HashSet<>();
                        for (com.alibaba.fastjson.JSONObject jsonObject : jsonObjects) {
                            for (TAppointmentVehicle tAppointmentVehicle : list) {
                                if (name.add(jsonObject.getString("master"))) {
                                    JSONObject entries = new JSONObject();
                                    entries.set("type", jsonObject.getString("type"));
                                    if (byId.getSupplierSubclass().equals(1)) {
                                        String deliveryDate = tAppointmentVehicle.getDeliveryDate();
                                        entries.set("startTime", deliveryDate.split(",")[0]);
                                        entries.set("deadline", deliveryDate.split(",")[1]);
                                    } else {
                                        entries.set("startTime", DateUtils.format(byId.getStartTime(), DateUtils.DATE_TIME_PATTERN));
                                        entries.set("deadline", DateUtils.format(byId.getEndTime(), DateUtils.DATE_TIME_PATTERN));
                                    }
                                    entries.set("carNumber", tAppointmentVehicle.getPlateNumber());
                                    entries.set("status", "add");
                                    entries.set("masterIp", jsonObject.get("master"));
                                    entries.set("databaseName", jsonObject.get("master_sn"));
                                    entries.set("username", jsonObject.get("master_account"));
                                    entries.set("password", jsonObject.get("master_password"));


                                    TIssueEigenvalueVO tIssueEigenvalueVO = new TIssueEigenvalueVO();
                                    tIssueEigenvalueVO.setStationId(byId.getSiteId());
                                    tIssueEigenvalueVO.setType(2);
                                    tIssueEigenvalueVO.setData(JSONUtil.toJsonStr(entries));
                                    tIssueEigenvalueVO.setStatus(1);
                                    tIssueEigenvalueVO.setAreaName(entries.getStr("DELETE") == null ? "新增" : "删除");
                                    tIssueEigenvalueVO.setAreaId(Long.parseLong(byId.getAppointmentType()));
                                    Long saveId = issueEigenvalueService.save(tIssueEigenvalueVO);
                                    entries.set("id", saveId);

                                    rabbitMQTemplate.convertAndSend(siteCode + Constant.EXCHANGE, siteCode + Constant.SITE_ROUTING_CAR_TOAGENT, entries);
                                    log.info("审核后车辆发送交换机：{} , 和消息:{}", siteCode, entries);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private void deleteInfo(List<TAppointmentVehicle> list, List<TAppointmentPersonnel> personnelList, Long siteId) {
        log.info("审核的时候优先删除");
        if (CollectionUtils.isNotEmpty(list)) {
            for (TAppointmentVehicle tAppointmentVehicle : list) {
                JSONObject person = new JSONObject();
                tAppointmentVehicle.setStationId(siteId.toString());
                person.set("sendType", "2");
                person.set("data", JSONUtil.toJsonStr(tAppointmentVehicle));
                person.set("DELETE", "DELETE");
                issuedPeople(person);
            }
        }

        if (CollectionUtils.isNotEmpty(personnelList)) {
            for (TAppointmentPersonnel tAppointmentPersonnel : personnelList) {
                JSONObject person = new JSONObject();
                tAppointmentPersonnel.setStationId(siteId.toString());
                person.set("sendType", "1");
                person.set("data", JSONUtil.toJsonStr(tAppointmentPersonnel));
                person.set("DELETE", "DELETE");
                issuedPeople(person);
            }
        }
    }


    @Override
    public List<TAppointmentPersonnelVO> getListById(Long id) {
        List<TAppointmentPersonnel> list = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, id));
        return TAppointmentPersonnelConvert.INSTANCE.convertList(list);

    }

    @Override
    public List<TAppointmentVehicleVO> getVehicleListById(Long id) {
        List<TAppointmentVehicle> list = tAppointmentVehicleService.list(new LambdaQueryWrapper<TAppointmentVehicle>().eq(TAppointmentVehicle::getAppointmentId, id));
        return TAppointmentVehicleConvert.INSTANCE.convertList(list);
    }


    @Override
    public PageResult<TAppointmentVO> pageBoard(AppointmentDTO data) {

        LambdaQueryWrapper<TAppointmentEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(StringUtils.isNotEmpty(data.getAppointmentType()), TAppointmentEntity::getAppointmentType, data.getAppointmentType());
        wrapper.eq(data.getSiteId() != null, TAppointmentEntity::getSiteId, data.getSiteId());
        wrapper.eq(TAppointmentEntity::getStatus, Constant.ENABLE);
        wrapper.in(CollectionUtils.isNotEmpty(data.getDataScopeList()) , TAppointmentEntity::getSiteId , data.getDataScopeList());
        wrapper.in(CollectionUtils.isNotEmpty(data.getAppointmentTypeList()), TAppointmentEntity::getAppointmentType, data.getAppointmentTypeList());
        wrapper.between(ArrayUtils.isNotEmpty(data.getCreatTime()), TAppointmentEntity::getCreateTime, ArrayUtils.isNotEmpty(data.getCreatTime()) ? data.getCreatTime()[0] : null, ArrayUtils.isNotEmpty(data.getCreatTime()) ? data.getCreatTime()[1] : null);
        wrapper.ge(ArrayUtils.isNotEmpty(data.getAppointmentTime()), TAppointmentEntity::getStartTime, ArrayUtils.isNotEmpty(data.getAppointmentTime()) ? data.getAppointmentTime()[0] : null);
        wrapper.le(ArrayUtils.isNotEmpty(data.getAppointmentTime()), TAppointmentEntity::getEndTime, ArrayUtils.isNotEmpty(data.getAppointmentTime()) ? data.getAppointmentTime()[1] : null);
        wrapper.orderByDesc(TAppointmentEntity::getReviewTime);
        Page<TAppointmentEntity> page = new Page<>(data.getPage(), data.getLimit());

        IPage<TAppointmentEntity> tAppointmentEntityPage = page(page, wrapper);

        List<TAppointmentVO> tAppointmentVOS = TAppointmentConvert.INSTANCE.convertList(tAppointmentEntityPage.getRecords());
        //这里需要做一个处理，回显提交人
        for (TAppointmentVO tAppointmentVO : tAppointmentVOS) {
            Long id = tAppointmentVO.getId();
            Long submitter = tAppointmentVO.getSubmitter();
            TAppointmentPersonnel one = tAppointmentPersonnelService.getOne(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, id)
                    .eq(TAppointmentPersonnel::getUserId, submitter));
            if (ObjectUtil.isNotNull(one)) {
                tAppointmentVO.setSubmitPeople(TAppointmentPersonnelConvert.INSTANCE.convert(one));
                tAppointmentVO.setSubmitterName(one.getExternalPersonnel());
            }
            //场站名称
            if (tAppointmentVO.getSiteId() != null) {
                String siteName = appointmentDao.selectSiteNameById(tAppointmentVO.getSiteId());
                tAppointmentVO.setSiteName(siteName);
            }
            //供应商名称
            if (StringUtils.isNotEmpty(tAppointmentVO.getSupplierName())) {
                String siteName = appointmentDao.selectSupplierNameById(Long.parseLong(tAppointmentVO.getSupplierName()));
                tAppointmentVO.setSupplierName(siteName);
            }
            //创建者名称
            if (tAppointmentVO.getCreator() != null) {
                com.alibaba.fastjson.JSONObject jsonObject = appointmentDao.selectRealNameById(tAppointmentVO.getCreator());
                if (jsonObject != null) {
                    String realName = jsonObject.getString("real_name");
                    String postName = jsonObject.getString("name");
                    tAppointmentVO.setCreatorName(realName);
                    tAppointmentVO.setSubmitterOrgName(postName);
                }
            }
        }
        return new PageResult<>(tAppointmentVOS, tAppointmentEntityPage.getTotal());
    }

    @Override
    public void delAppointment(Long id) {
        TAppointmentEntity byId = getById(id);
        if (ObjectUtil.isNotNull(byId)) {
            byId.setStatus(Constant.ZERO);
            updateById(byId);
        }
    }

    /**
     * @param siteId   场站id
     * @param type 类型
     * @return
     */
    @Override
    public JSONObject appointmentSum(String siteId, Long type) {

        List<Long> siteIds = new ArrayList<>();
        if (!siteId.equals("1")){
            String[] strArray = siteId.split(",");
            for (String s : strArray) {
                siteIds.add(Long.parseLong(s));
            }
        }

        String[] startAndEndTime = DateUtils.getStartAndEndTime();

        JSONObject entries = new JSONObject();
        entries.set("vehicleCount", Constant.ZERO);
        entries.set("personCount", Constant.ZERO);
        if (type > 1) {
            List<TAppointmentEntity> list = list(new LambdaQueryWrapper<TAppointmentEntity>()
                    .in(CollectionUtils.isNotEmpty(siteIds) ,TAppointmentEntity::getSiteId, siteIds).le(TAppointmentEntity::getEndTime, startAndEndTime[1]).ge(TAppointmentEntity::getStartTime, startAndEndTime[0]));
            List<Long> longList = list.stream().map(TAppointmentEntity::getId).toList();
            if (CollectionUtils.isNotEmpty(list)) {
                long vehicleCount = tAppointmentVehicleService.count(new LambdaQueryWrapper<TAppointmentVehicle>().in(TAppointmentVehicle::getAppointmentId, longList));
                long personCount = tAppointmentPersonnelService.count(new LambdaQueryWrapper<TAppointmentPersonnel>().in(TAppointmentPersonnel::getAppointmentId, longList));
                entries.set("vehicleCount", vehicleCount);
                entries.set("personCount", personCount);
            }
            return entries;
        }
        List<String> typeList = Stream.of("3", "4", "5","6").toList();
        List<TAppointmentEntity> list = list(new LambdaQueryWrapper<TAppointmentEntity>().in(TAppointmentEntity::getAppointmentType, typeList)
                .in(CollectionUtils.isNotEmpty(siteIds) , TAppointmentEntity::getSiteId, siteIds).le(TAppointmentEntity::getEndTime, startAndEndTime[1]).ge(TAppointmentEntity::getStartTime, startAndEndTime[0]));
        if (CollectionUtils.isNotEmpty(list)) {
            List<Long> longList = list.stream().map(TAppointmentEntity::getId).toList();
            long vehicleCount = tAppointmentVehicleService.count(new LambdaQueryWrapper<TAppointmentVehicle>().in(TAppointmentVehicle::getAppointmentId, longList));
            long personCount = tAppointmentPersonnelService.count(new LambdaQueryWrapper<TAppointmentPersonnel>().in(TAppointmentPersonnel::getAppointmentId, longList));
            entries.set("vehicleCount", vehicleCount);
            entries.set("personCount", personCount);
        }
        return entries;
    }





    @Override
    public void issuedPeople(JSONObject data) {
        log.info("下发的消息：{}", data);
        String sendType = data.getStr("sendType");
        Long personId = data.getLong("personId");
        String isControl = data.getStr("isControl");
        String domain = properties.getConfig().getDomain();

        switch (sendType) {
            case "1" -> {
                //人脸进入
                JSONObject entries = JSONUtil.parseObj(data.get("data"));
                //所属站点
                String stationId = entries.getStr("stationId");
                //realName  真实姓名
                String peopleName = entries.getStr("realName");
                //人脸地址  avatar
                String faceUrl = entries.getStr("avatar");
                //编码 code
                String code = entries.getStr("id");
                //编码 mobile
                String mobile = entries.getStr("mobile");

//                //场站关联编码
//                String siteCode = appointmentDao.selectSiteCodeById(Long.parseLong(stationId));
                List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDevices(Long.parseLong(stationId), sendType);
                if (StringUtils.isNotEmpty(data.getStr("ids"))) {
                    String ids = data.getStr("ids");
                    List<Long> list = JSONUtil.toList(ids, Long.class);
                    if (CollectionUtils.isNotEmpty(list)) {
                        jsonObjects = appointmentDao.selectNewByIds(list, sendType);
                    }
                }
                if (CollectionUtils.isNotEmpty(jsonObjects)) {
                    //按照厂站分组
                    Map<String, List<com.alibaba.fastjson.JSONObject>> listMap = jsonObjects.stream().collect(Collectors.groupingBy(item -> item.getString("master")));
                    for (String master : listMap.keySet()) {
                        List<com.alibaba.fastjson.JSONObject> devices = listMap.get(master);
                        //按照设备类型分组
                        Map<String, List<com.alibaba.fastjson.JSONObject>> types = devices.stream().collect(Collectors.groupingBy(item -> item.getString("type")));
                        for (String type : types.keySet()) {
                            List<com.alibaba.fastjson.JSONObject> typeList = types.get(type);
                            JSONObject sendData = new JSONObject();
                            sendData.set("type", type);
                            sendData.set("startTime", "2024-04-01 00:00:00");
                            sendData.set("deadline", "2034-04-01 00:00:00");
                            sendData.set("peopleName", peopleName);
                            sendData.set("peopleCode", code);
                            sendData.set("masterIp", typeList.get(0).getString("master"));
                            sendData.set("faceUrl", domain + faceUrl);
                            sendData.set("deviceInfos", JSONUtil.toJsonStr(typeList));
                            sendData.set("password", typeList.get(0).getString("password"));
                            sendData.set("DELETE", data.getStr("DELETE"));

                            TIssueEigenvalueVO tIssueEigenvalueVO = new TIssueEigenvalueVO();
                            tIssueEigenvalueVO.setStationId(typeList.get(0).getLong("site_id"));

//                            //判断 是否是需要修改下发人员id的站点
//                            String siteCode = devices.get(0).getString("siteCode");
//                            if (CollectionUtils.isNotEmpty(dictValue) && dictValue.contains(siteCode)){
//                                sendData.set("peopleCode", mobile);
//                            }

                            tIssueEigenvalueVO.setType(1);
                            tIssueEigenvalueVO.setStatus(1);
                            tIssueEigenvalueVO.setData(JSONUtil.toJsonStr(sendData));
                            tIssueEigenvalueVO.setAreaId(0L);
                            tIssueEigenvalueVO.setAreaName(data.getStr("DELETE") == null ? "新增" : "删除");
                            tIssueEigenvalueVO.setCreator(personId);
                            Long saveId = issueEigenvalueService.save(tIssueEigenvalueVO);
                            sendData.set("id", saveId);
                            rabbitMQTemplate.convertAndSend(devices.get(0).getString("siteCode") + Constant.EXCHANGE, devices.get(0).getString("siteCode") + Constant.SITE_ROUTING_FACE_TOAGENT, sendData);
                            log.info("发送交换机：{} , 和消息:{}", devices.get(0).getString("siteCode"), sendData);
                        }
                    }
                }
            }

            case "2" -> {
                //车辆进入
                JSONObject entries = JSONUtil.parseObj(data.get("data"));
                //所属站点
                String stationId = entries.getStr("stationId");
                //车牌号
                String licensePlate = entries.getStr("licensePlate");
                if (StrUtil.isEmpty(licensePlate)) {
                    licensePlate = entries.getStr("plateNumber");
                }
                //场站关联编码
                List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDevices(stationId == null ? -1 : Long.parseLong(stationId), sendType);

                if (StringUtils.isNotEmpty(data.getStr("ids"))) {
                    String ids = data.getStr("ids");
                    List<Long> list = JSONUtil.toList(ids, Long.class);
                    if (CollectionUtils.isNotEmpty(list)) {
                        jsonObjects = appointmentDao.selectNewByIds(list, sendType);
                    }
                }

                if (CollectionUtils.isNotEmpty(jsonObjects)) {
                    //主机分组
                    Set<String> name = new HashSet<>();
                    for (com.alibaba.fastjson.JSONObject jsonObject : jsonObjects) {
                        if (name.add(jsonObject.getString("master"))) {
                            JSONObject sendData = new JSONObject();
                            sendData.set("type", jsonObject.getString("type"));
                            sendData.set("startTime", StringUtils.isNotEmpty(data.getStr("startTime"))? data.getStr("startTime") :"2024-04-01 00:00:00");
                            sendData.set("deadline",  StringUtils.isNotEmpty(data.getStr("deadline"))? data.getStr("deadline") :"2034-04-01 00:00:00");
                            sendData.set("carNumber", licensePlate);
                            sendData.set("status", data.getStr("DELETE") == null ? "add" : "delete");
                            sendData.set("masterIp", jsonObject.get("master"));
                            sendData.set("databaseName", jsonObject.get("master_sn"));
                            sendData.set("username", jsonObject.get("master_account"));
                            sendData.set("password", jsonObject.get("master_password"));
                            sendData.set("DELETE", data.getStr("DELETE"));
                            // issueEigenvalueService.save(new TIssueEigenvalue().setType(1).setData(entries.toString()));
                            TIssueEigenvalueVO tIssueEigenvalueVO = new TIssueEigenvalueVO();
                            tIssueEigenvalueVO.setStationId(jsonObject.getLong("site_id"));
                            tIssueEigenvalueVO.setType(2);
                            tIssueEigenvalueVO.setStatus(1);
                            tIssueEigenvalueVO.setData(JSONUtil.toJsonStr(sendData));
                            tIssueEigenvalueVO.setAreaId(StringUtils.isNotEmpty(isControl) ? 7L :0L);
                            tIssueEigenvalueVO.setAreaName(data.getStr("DELETE") == null ? "新增" : "删除");
                            tIssueEigenvalueVO.setCreator(personId);
                            Long saveId = issueEigenvalueService.save(tIssueEigenvalueVO);
                            sendData.set("id", saveId);
                            rabbitMQTemplate.convertAndSend(jsonObject.getString("siteCode") + Constant.EXCHANGE, jsonObject.getString("siteCode") + Constant.SITE_ROUTING_CAR_TOAGENT, sendData);
                            log.info("发送交换机：{} , 和消息:{}", jsonObject.getString("siteCode"), sendData);
                        }
                    }
                }
            }
        }
    }

    @Override
    public JSONArray querOtherAppointmentService(Long siteId, Integer page, Integer limit) {

        JSONArray objects = new JSONArray();
        Query query = new Query();
        query.setPage(page);
        query.setLimit(limit);
        IPage<TAppointmentEntity> pageList = baseMapper.selectPage(getPage(query), getWrapperByHttp(siteId));

        List<TAppointmentVO> tAppointmentVOS = TAppointmentConvert.INSTANCE.convertList(pageList.getRecords());
        for (TAppointmentVO tAppointmentVO : tAppointmentVOS) {
            JSONObject jsonObject = new JSONObject();
            Long id = tAppointmentVO.getId();
            Long submitter = tAppointmentVO.getSubmitter();

            String name = appointmentDao.getNameById(submitter);
            tAppointmentVO.setCreatorName(name);

            TAppointmentPersonnel one = tAppointmentPersonnelService.getOne(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, id)
                    .eq(TAppointmentPersonnel::getUserId, submitter));

            LambdaQueryWrapper<TAppointmentPersonnel> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
            objectLambdaQueryWrapper.eq(TAppointmentPersonnel::getAppointmentId, id);
            List<TAppointmentPersonnel> list = tAppointmentPersonnelService.list(objectLambdaQueryWrapper);
            StringBuilder thePersonWhoMadeTheReservation = new StringBuilder();
            if (ObjectUtil.isNotEmpty(one)) {
                thePersonWhoMadeTheReservation.append(one.getExternalPersonnel());
            }
            jsonObject.putOnce("thePersonWhoMadeTheReservation", thePersonWhoMadeTheReservation);
            jsonObject.putOnce("totalNumberOfPeople", list.size());
            jsonObject.putOnce("firm", tAppointmentVO.getCompanyName());
            jsonObject.putOnce("reasonForEnteringTheFactory", tAppointmentVO.getPurpose());
            objects.add(jsonObject);

        }
        return objects;

    }

    @Override
    public List<TAppointmentVehicle> importData(MultipartFile file) {


        List<TAppointmentVehicle> list = new ArrayList<>();
        try {
            List<TAppointmentVehicleExcelVO> readAllSync = EasyExcel.read(file.getInputStream()).head(TAppointmentVehicleExcelVO.class).doReadAllSync();

            if (CollectionUtils.isEmpty(readAllSync)) {
                return list;
            }
            //车型
            List<com.alibaba.fastjson.JSONObject> allDictByType = appointmentDao.getAllDictByType(25);
            Map<String, String> map1 = allDictByType.stream()
                    .collect(Collectors.toMap(
                            jsonObject -> jsonObject.getString("dict_label"), // 使用"dict_label"作为key
                            jsonObject -> jsonObject.getString("dict_value") // 使用"dict_value"作为value
                    ));

            //排放标准
            List<com.alibaba.fastjson.JSONObject> allDictByType1 = appointmentDao.getAllDictByType(15);
            Map<String, String> map2 = allDictByType1.stream()
                    .collect(Collectors.toMap(
                            jsonObject -> jsonObject.getString("dict_label"), // 使用"dict_label"作为key
                            jsonObject -> jsonObject.getString("dict_value") // 使用"dict_value"作为value
                    ));
            readAllSync.forEach(item -> {
                TAppointmentVehicle tAppointmentVehicle = new TAppointmentVehicle();
                tAppointmentVehicle.setDeliveryDate(item.getStartTime() + "," + item.getEndTime());
                tAppointmentVehicle.setPlateNumber(item.getPlateNumber());
                tAppointmentVehicle.setVehicleModel(map1.get(item.getVehicleModel()));
                tAppointmentVehicle.setEmissionStandard(map2.get(item.getEmissionStandard()));
                tAppointmentVehicle.setPassenger(item.getPassenger());
                list.add(tAppointmentVehicle);
            });
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Wrapper<TAppointmentEntity> getWrapperByHttp(Long siteId) {
        LambdaQueryWrapper<TAppointmentEntity> wrapper = Wrappers.lambdaQuery();
        List<String> list = Stream.of("3", "4", "5").toList();
        wrapper.eq(TAppointmentEntity::getStatus, 1);
        wrapper.eq(TAppointmentEntity::getDeleted, 0);
        wrapper.in(TAppointmentEntity::getAppointmentType, list);

        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat timeformat = new SimpleDateFormat(format);
        // 获取当前时间
        Date now = new Date();
        String nowFormatted = timeformat.format(now);

        // 设置startTime小于等于当前时间
        wrapper.ge(TAppointmentEntity::getStartTime, nowFormatted);
        // 设置endTime大于等于当前时间
        wrapper.le(TAppointmentEntity::getEndTime, nowFormatted);

        wrapper.eq(TAppointmentEntity::getSiteId, siteId);
        wrapper.eq(TAppointmentEntity::getSiteId, siteId);
        return wrapper;
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
    public com.alibaba.fastjson.JSONObject queryStatisticsallPeopleReservation() {
        LambdaQueryWrapper<TAppointmentEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getDeleted, 0);
        // 查询已审核的预约单
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getReviewStatus, 1);
        List<TAppointmentEntity> tAppointmentEntities = baseMapper.selectList(objectLambdaQueryWrapper);

        // 筛选出预约类型为派驻类型的
        List<Long> list = tAppointmentEntities.stream()
                .filter(tAppointmentEntity -> "1".equals(tAppointmentEntity.getAppointmentType()))
                .map(TAppointmentEntity::getId)
                .toList();

        // 筛选出预约类型是外部预约的
        List<Long> list1 = tAppointmentEntities.stream()
                .filter(entity -> List.of("3", "4", "5").contains(entity.getAppointmentType()))
                .map(TAppointmentEntity::getId)
                .toList();

        LambdaQueryWrapper<TAppointmentPersonnel> personnelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        personnelLambdaQueryWrapper.in(TAppointmentPersonnel::getAppointmentId, list);
        List<TAppointmentPersonnel> tAppointmentPersonnelList = tAppointmentPersonnelService.list(personnelLambdaQueryWrapper);
        int numberOfResidents = 0;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentPersonnelList)) {
            numberOfResidents = tAppointmentPersonnelList.size();
        }

        LambdaQueryWrapper<TAppointmentPersonnel> personnelLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        personnelLambdaQueryWrapper2.in(TAppointmentPersonnel::getAppointmentId, list1);
        List<TAppointmentPersonnel> tAppointmentPersonnelList2 = tAppointmentPersonnelService.list(personnelLambdaQueryWrapper2);
        int numberOfExternalAppointments = 0;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentPersonnelList2)) {
            numberOfExternalAppointments = tAppointmentPersonnelList2.size();
        }
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("numberOfResidents", numberOfResidents);
        jsonObject.put("numberOfExternalAppointments", numberOfExternalAppointments);
        return jsonObject;
    }

    @Override
    public com.alibaba.fastjson.JSONObject queryTotalAppointments(Long siteId) {
        LambdaQueryWrapper<TAppointmentEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getReviewStatus, 1);
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getSiteId, siteId);
        objectLambdaQueryWrapper.ge(TAppointmentEntity::getStartTime, LocalDateTime.now());
        objectLambdaQueryWrapper.le(TAppointmentEntity::getEndTime, LocalDateTime.now());

        List<TAppointmentEntity> tAppointmentEntities = baseMapper.selectList(objectLambdaQueryWrapper);
        int numberOfExternalAppointments = 0;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentEntities)) {
            for (int i = 0; i < tAppointmentEntities.size(); i++) {
                TAppointmentEntity tAppointmentEntity = tAppointmentEntities.get(i);
                String appointmentType = tAppointmentEntity.getAppointmentType();
                if ("3".equals(appointmentType) || "4".equals(appointmentType) || "4".equals(appointmentType)) {
                    LambdaQueryWrapper<TAppointmentPersonnel> personnelLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
                    personnelLambdaQueryWrapper2.eq(TAppointmentPersonnel::getAppointmentId, tAppointmentEntity.getId());
                    List<TAppointmentPersonnel> tAppointmentPersonnelList2 = tAppointmentPersonnelService.list(personnelLambdaQueryWrapper2);
                    if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentPersonnelList2)) {
                        // 检查每人当前是否在场内
                        for (int i1 = 0; i1 < tAppointmentPersonnelList2.size(); i1++) {
                            TAppointmentPersonnel tAppointmentPersonnel = tAppointmentPersonnelList2.get(i1);
                            String externalPersonnel = tAppointmentPersonnel.getExternalPersonnel();
                            // 查询该人员是否在场内
                            boolean b = datasectionFeign.whetherItIsInTheFieldOrNot(externalPersonnel, siteId);
                            if (b) {
                                numberOfExternalAppointments++;
                            }
                        }
                    }
                }
            }
            com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
            jsonObject.put("numberOfExternalAppointments", numberOfExternalAppointments);
            return jsonObject;
        } else {
            com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
            jsonObject.put("numberOfExternalAppointments", numberOfExternalAppointments);
            return jsonObject;
        }
    }

    @Override
    public com.alibaba.fastjson.JSONObject queryappointmentFormspecifyLicensePlatesAndEntourage(String plateNumber, String recordTime) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LambdaQueryWrapper<TAppointmentEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getReviewStatus, 1);
        // 开始时间小于等于 recordTime
        objectLambdaQueryWrapper.le(TAppointmentEntity::getStartTime, recordTime);
        // 结束时间大于等于 recordTime
        objectLambdaQueryWrapper.ge(TAppointmentEntity::getEndTime, recordTime);
        List<TAppointmentEntity> tAppointmentEntities = baseMapper.selectList(objectLambdaQueryWrapper);

        List<Long> list1 = tAppointmentEntities.stream()
                .map(TAppointmentEntity::getId)
                .toList();

        LambdaQueryWrapper<TAppointmentVehicle> appointmentVehicleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        appointmentVehicleLambdaQueryWrapper.eq(TAppointmentVehicle::getStatus, 1);
        appointmentVehicleLambdaQueryWrapper.eq(TAppointmentVehicle::getDeleted, 0);
        appointmentVehicleLambdaQueryWrapper.eq(TAppointmentVehicle::getPlateNumber, plateNumber);
        appointmentVehicleLambdaQueryWrapper.in(TAppointmentVehicle::getAppointmentId, list1);
        List<TAppointmentVehicle> tAppointmentVehicles = tAppointmentVehicleService.list(appointmentVehicleLambdaQueryWrapper);
        com.alibaba.fastjson.JSONArray objects = new com.alibaba.fastjson.JSONArray();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentVehicles)) {

            // 提取 passenger 字段并拼接在一起
            String concatenatedPassengers = tAppointmentVehicles.stream()
                    .map(TAppointmentVehicle::getPassenger) // 获取 passenger 字段
                    .collect(Collectors.joining(",")); // 使用逗号拼接
            String[] passengerArray = concatenatedPassengers.split(",");

            List<Long> appointmentIdList = tAppointmentVehicles.stream()
                    .map(TAppointmentVehicle::getAppointmentId)
                    .toList();
            LambdaQueryWrapper<TAppointmentPersonnel> personnelLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
            personnelLambdaQueryWrapper2.in(TAppointmentPersonnel::getAppointmentId, appointmentIdList);
            personnelLambdaQueryWrapper2.in(TAppointmentPersonnel::getExternalPersonnel, passengerArray);
            List<TAppointmentPersonnel> tAppointmentPersonnelList2 = tAppointmentPersonnelService.list(personnelLambdaQueryWrapper2);
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentPersonnelList2)) {
                for (int i = 0; i < tAppointmentPersonnelList2.size(); i++) {
                    TAppointmentPersonnel tAppointmentPersonnel = tAppointmentPersonnelList2.get(i);

                    com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
                    jsonObject.put("userId", tAppointmentPersonnel.getUserId());
                    jsonObject.put("positionId", tAppointmentPersonnel.getPositionId());
                    jsonObject.put("positionName", tAppointmentPersonnel.getPositionName());
                    jsonObject.put("userName", tAppointmentPersonnel.getExternalPersonnel());
                    objects.add(jsonObject);
                }
            } else {
                // 如果person里面为空，说明是供应商车辆入场申请，也要生成记录，只不过只有一个名字而已
                TAppointmentVehicle tAppointmentVehicle = tAppointmentVehicles.get(0);
                com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
                jsonObject.put("userId", null);
                jsonObject.put("positionId", null);
                jsonObject.put("positionName", null);
                jsonObject.put("userName", tAppointmentVehicle.getPassenger());
                objects.add(jsonObject);

            }
        }
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("dataArray", objects);
        return jsonObject;
    }

    @Override
    public com.alibaba.fastjson.JSONObject queryStationIdFromAppointmentByUserInfo(String personId, String personName) {
        LambdaQueryWrapper<TAppointmentEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getReviewStatus, 1);
        objectLambdaQueryWrapper.ge(TAppointmentEntity::getStartTime, LocalDateTime.now());
        objectLambdaQueryWrapper.le(TAppointmentEntity::getEndTime, LocalDateTime.now());
        List<TAppointmentEntity> tAppointmentEntities = baseMapper.selectList(objectLambdaQueryWrapper);

        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("stationId", null);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentEntities)) {
            for (int i = 0; i < tAppointmentEntities.size(); i++) {
                TAppointmentEntity tAppointmentEntity = tAppointmentEntities.get(i);
                LambdaQueryWrapper<TAppointmentPersonnel> personnelLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
                personnelLambdaQueryWrapper2.eq(TAppointmentPersonnel::getAppointmentId, tAppointmentEntity.getId());
                personnelLambdaQueryWrapper2.eq(TAppointmentPersonnel::getExternalPersonnel, personName);
                List<TAppointmentPersonnel> tAppointmentPersonnelList2 = tAppointmentPersonnelService.list(personnelLambdaQueryWrapper2);
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentPersonnelList2)) {
                    TAppointmentPersonnel tAppointmentPersonnel = tAppointmentPersonnelList2.get(0);
                    jsonObject.put("stationId", tAppointmentPersonnel.getStationId());

                }
            }
        }
        return jsonObject;
    }

    @Override
    public com.alibaba.fastjson.JSONObject queryStationIdFromAppointmentByPlatenumber(String palteNumber) {
        LambdaQueryWrapper<TAppointmentEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getDeleted, 0);
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getReviewStatus, 1);
        objectLambdaQueryWrapper.ge(TAppointmentEntity::getStartTime, LocalDateTime.now());
        objectLambdaQueryWrapper.le(TAppointmentEntity::getEndTime, LocalDateTime.now());
        List<TAppointmentEntity> tAppointmentEntities = baseMapper.selectList(objectLambdaQueryWrapper);

        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("stationId", null);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentEntities)) {
            for (int i = 0; i < tAppointmentEntities.size(); i++) {
                TAppointmentEntity tAppointmentEntity = tAppointmentEntities.get(i);
                LambdaQueryWrapper<TAppointmentVehicle> tvehicleLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
                tvehicleLambdaQueryWrapper2.eq(TAppointmentVehicle::getAppointmentId, tAppointmentEntity.getId());
                tvehicleLambdaQueryWrapper2.eq(TAppointmentVehicle::getPlateNumber, palteNumber);
                List<TAppointmentVehicle> tAppointmentTvehicleLists = tAppointmentVehicleService.list(tvehicleLambdaQueryWrapper2);
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentTvehicleLists)) {
                    TAppointmentVehicle tAppointmentVehicle = tAppointmentTvehicleLists.get(0);
                    jsonObject.put("stationId", tAppointmentVehicle.getStationId());
                }
            }
        }
        return jsonObject;
    }


    @Override
    public void commit(Long id, String name, String type) {

        //获取主表
        TAppointmentEntity mainOne = getById(id);

        //获取到厂站id
        Long stationId = mainOne.getSiteId();

        //修改状态值为已确认
        mainOne.setReviewStatus("2");
        updateById(mainOne);

        List<com.alibaba.fastjson.JSONObject> allDictByType = appointmentDao.getAllDictByType(35);
        for (com.alibaba.fastjson.JSONObject jsonObject : allDictByType) {
            String string = jsonObject.getString("dict_value");
            if (string.contains(mainOne.getSiteId().toString()) && string.split("_")[0].equals(mainOne.getSiteId().toString())) {
                String[] split = string.split("_");
                mainOne.setSiteId(Long.parseLong(split[1]));
            }
        }

        switch (type) {
            case "1" -> {
                //进入人员
                List<TAppointmentPersonnel> list = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, id).eq(TAppointmentPersonnel::getExternalPersonnel, name));
                if (CollectionUtils.isNotEmpty(list)) {
                    for (TAppointmentPersonnel personnel : list) {
                        if (personnel.getIsCommit() == 0) {
                            if (StringUtils.isNotEmpty(personnel.getHeadUrl())) {
                                String domain = properties.getConfig().getDomain();
                                String siteCode = appointmentDao.selectSiteCodeById(stationId);
                                List<String> strings = appointmentDao.selectManuFacturerIdById(stationId, "1");
                                for (String device : strings) {
                                    List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDeviceList(device, stationId);
                                    List<String> masterIpById = appointmentDao.selectMasterIpById(device, "1", stationId);
                                    for (String masterIp : masterIpById) {
                                        //表示没有下发过
                                        JSONObject entries = new JSONObject();
                                        entries.set("type", device);
                                        entries.set("startTime", DateUtils.format(mainOne.getStartTime(), DateUtils.DATE_TIME_PATTERN));
                                        entries.set("deadline", DateUtils.format(mainOne.getEndTime(), DateUtils.DATE_TIME_PATTERN));
                                        entries.set("peopleName", personnel.getExternalPersonnel());
                                        entries.set("peopleCode", personnel.getUserId());
                                        entries.set("faceUrl", domain + personnel.getHeadUrl());
                                        entries.set("masterIp", masterIp);
                                        entries.set("deviceInfos", JSONUtil.toJsonStr(jsonObjects));
                                        entries.set("password", jsonObjects.get(0).get("password"));
                                        TIssueEigenvalueVO tIssueEigenvalueVO = new TIssueEigenvalueVO();
                                        tIssueEigenvalueVO.setStationId(stationId);
                                        tIssueEigenvalueVO.setType(1);
                                        tIssueEigenvalueVO.setStatus(1);
                                        tIssueEigenvalueVO.setAreaId(Long.parseLong(mainOne.getAppointmentType()));
                                        tIssueEigenvalueVO.setData(JSONUtil.toJsonStr(entries));
                                        tIssueEigenvalueVO.setAreaName(entries.getStr("DELETE") == null ? "新增" : "删除");
                                        Long saveId = issueEigenvalueService.save(tIssueEigenvalueVO);
                                        entries.set("id", saveId);
                                        rabbitMQTemplate.convertAndSend(siteCode + Constant.EXCHANGE, siteCode + Constant.SITE_ROUTING_FACE_TOAGENT, entries);
                                        log.info("发送交换机：{} , 和消息:{}", siteCode, entries);
                                    }
                                }
                            }
                            //修改下发状态
                            personnel.setIsCommit(1);
                            tAppointmentPersonnelService.updateById(personnel);
                        }
                    }
                }
            }

            case "2" -> {
                //进入车辆
                List<TAppointmentVehicle> list = tAppointmentVehicleService.list(new LambdaQueryWrapper<TAppointmentVehicle>().eq(TAppointmentVehicle::getAppointmentId, id).eq(TAppointmentVehicle::getPlateNumber, name));
                if (CollectionUtils.isNotEmpty(list)) {
                    Set<String> masterIp = new HashSet<>();
                    List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDevices(stationId, "2");
                    for (com.alibaba.fastjson.JSONObject jsonObject : jsonObjects) {
                        if (masterIp.add(jsonObject.getString("master"))) {
                            JSONObject sendData = new JSONObject();
                            sendData.set("type", jsonObject.getString("type"));
                            sendData.set("startTime", "2024-04-01 00:00:00");
                            sendData.set("deadline", "2034-04-01 00:00:00");
                            sendData.set("carNumber", name);
                            sendData.set("status", "add");
                            sendData.set("masterIp", jsonObject.get("master"));
                            sendData.set("databaseName", jsonObject.get("master_sn"));
                            sendData.set("username", jsonObject.get("master_account"));
                            sendData.set("password", jsonObject.get("master_password"));
                            // issueEigenvalueService.save(new TIssueEigenvalue().setType(1).setData(entries.toString()));
                            TIssueEigenvalueVO tIssueEigenvalueVO = new TIssueEigenvalueVO();
                            tIssueEigenvalueVO.setStationId(stationId);
                            tIssueEigenvalueVO.setType(2);
                            tIssueEigenvalueVO.setStatus(1);
                            tIssueEigenvalueVO.setAreaId(Long.parseLong(mainOne.getAppointmentType()));
                            tIssueEigenvalueVO.setData(JSONUtil.toJsonStr(sendData));
                            tIssueEigenvalueVO.setAreaName(sendData.getStr("DELETE") == null ? "新增" : "删除");
                            Long saveId = issueEigenvalueService.save(tIssueEigenvalueVO);
                            sendData.set("id", saveId);

                            rabbitMQTemplate.convertAndSend(jsonObject.getString("siteCode") + Constant.EXCHANGE, jsonObject.getString("siteCode") + Constant.SITE_ROUTING_CAR_TOAGENT, sendData);
                            log.info("发送交换机：{} , 和消息:{}", jsonObject.getString("siteCode"), sendData);

                            //车辆发送成功后 需要下发车辆对应有人脸的人员
                            TAppointmentVehicle tAppointmentVehicle = list.get(0);

                            //获取随行人员
                            String passenger = tAppointmentVehicle.getPassenger();
                            String[] nameList = passenger.split(",");
                            for (String people : nameList) {
                                commit(id, people, "1");
                            }

                            tAppointmentVehicle.setIsCommit(1);
                            tAppointmentVehicleService.updateById(tAppointmentVehicle);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<TAppointmentVehicleVO> getAppointmentCar(Long siteId) {

        LocalDateTime now = LocalDateTime.now();

        List<TAppointmentEntity> appointmentList = list(new LambdaQueryWrapper<TAppointmentEntity>()
                .eq(TAppointmentEntity::getSiteId ,siteId)
                .ge(TAppointmentEntity::getEndTime , now)
        );

        if (CollectionUtils.isNotEmpty(appointmentList)){
            List<Long> list = appointmentList.stream().map(TAppointmentEntity::getId).toList();
            //拿取字典里面的管控类型
            List<com.alibaba.fastjson.JSONObject> allDictByType = appointmentDao.getAllDictByType(38);
            List<String> dictValue = allDictByType.stream().map(item -> item.getString("dict_value")).toList();

            List<TAppointmentVehicle> vehicleList = tAppointmentVehicleService
                    .list(new LambdaQueryWrapper<TAppointmentVehicle>()
                            .in(TAppointmentVehicle::getAppointmentId ,list)
                            .ne(TAppointmentVehicle::getVehicleModel , "1")
                            .in(TAppointmentVehicle::getEmissionStandard , dictValue));

            if (CollectionUtils.isNotEmpty(vehicleList)){
                return TAppointmentVehicleConvert.INSTANCE.convertList(vehicleList);
            }
        }
        return new ArrayList<>();
    }


    /**
     * 发送短信
     *
     * @param siteId
     * @param vo
     */
    private void sendMessage(Long siteId, TAppointmentVO vo) {

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        }, executor);


        List<String> phoneBySiteId = stationFeign.getPhoneBySiteId(siteId);

        if (CollectionUtils.isNotEmpty(phoneBySiteId)) {
            MessageDTO.Appointment appointment = new MessageDTO.Appointment();

            //提交人id
            Long submitter = vo.getSubmitter();
            String companyName = vo.getCompanyName();

            String realName = "";

            //获取预约类型名称
            String label = appointmentDao.getAllDictByTypeAndCode(18, vo.getAppointmentType());

            if (vo.getAppointmentType().equals("1") || vo.getAppointmentType().equals("2")) {
                //获取人员加公司
                com.alibaba.fastjson.JSONObject jsonObject = appointmentDao.selectRealNameById(submitter);
                realName = jsonObject.getString("real_name");
                companyName = jsonObject.getString("name");
            } else {
                TAppointmentPersonnelVO tAppointmentPersonnelVO = vo.getPersonnelList().get(0);
                realName = tAppointmentPersonnelVO.getExternalPersonnel();
            }

            String siteName = appointmentDao.selectSiteNameById(siteId);
            appointment.setAppointmentType(label);
            appointment.setStationName(siteName);
            appointment.setName(realName);
            appointment.setCompanyName(companyName);

            Map<String, String> map = ClassToMapUtils.objectToStringMap(appointment);
            if (map.isEmpty()) {
                return;
            }
            smsApi.sendById(phoneBySiteId, map, 1L);
        }


    }


}
