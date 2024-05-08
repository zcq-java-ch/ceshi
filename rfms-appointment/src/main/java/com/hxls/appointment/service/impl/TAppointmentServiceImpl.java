package com.hxls.appointment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.hxls.api.feign.system.UserFeign;
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
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ServerException;
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
import java.time.LocalDateTime;
import java.util.*;
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

    private final StorageImagesProperties properties;

    @Override
    public PageResult<TAppointmentVO> page(TAppointmentQuery query) {

        LambdaQueryWrapper<TAppointmentEntity> wrapper =query.getCreator() == null? getWrapper(query) : getWrapperAll(query);

        IPage<TAppointmentEntity> page = baseMapper.selectPage(getPage(query),wrapper );
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
        if (query.getIsFinish() != null ){
           if (query.getIsFinish()){
               wrapper.eq(TAppointmentEntity::getReviewStatus , 0);
           } else {
               wrapper.in(TAppointmentEntity::getReviewStatus ,List.of(1,-1));
           }
        }
        wrapper.in(CollectionUtils.isNotEmpty(query.getSiteIds()),TAppointmentEntity::getSiteId, query.getSiteIds());
        wrapper.eq(StringUtils.isNotEmpty(query.getAppointmentType()), TAppointmentEntity::getAppointmentType, query.getAppointmentType());
        wrapper.eq(StringUtils.isNotEmpty(query.getSupplierName()), TAppointmentEntity::getSupplierName, query.getSupplierName());
        wrapper.eq(query.getSubmitter() != null, TAppointmentEntity::getSubmitter, query.getSubmitter());
        wrapper.eq(query.getSiteId() != null, TAppointmentEntity::getSiteId, query.getSiteId());
        wrapper.like(StringUtils.isNotEmpty(query.getSiteName()), TAppointmentEntity::getSiteName, query.getSiteName());
        wrapper.ge(StringUtils.isNotEmpty(query.getStartTime()), TAppointmentEntity::getStartTime, query.getStartTime());
        wrapper.le(StringUtils.isNotEmpty(query.getEndTime()), TAppointmentEntity::getEndTime, query.getEndTime());
        wrapper.ge(ArrayUtils.isNotEmpty(query.getAppointmentTime()), TAppointmentEntity::getStartTime, ArrayUtils.isNotEmpty(query.getAppointmentTime()) ? query.getAppointmentTime()[0] : null );
        wrapper.le(ArrayUtils.isNotEmpty(query.getAppointmentTime()), TAppointmentEntity::getEndTime, ArrayUtils.isNotEmpty(query.getAppointmentTime()) ? query.getAppointmentTime()[1] : null);

        wrapper.between(ArrayUtils.isNotEmpty(query.getReviewTime()), TAppointmentEntity::getReviewTime, ArrayUtils.isNotEmpty(query.getReviewTime()) ? query.getReviewTime()[0] : null, ArrayUtils.isNotEmpty(query.getReviewTime()) ? query.getReviewTime()[1] : null);
        wrapper.between(ArrayUtils.isNotEmpty(query.getCreatTime()), TAppointmentEntity::getCreateTime, ArrayUtils.isNotEmpty(query.getCreatTime()) ? query.getCreatTime()[0] : null, ArrayUtils.isNotEmpty(query.getCreatTime()) ? query.getCreatTime()[1] : null);
        wrapper.eq(StringUtils.isNotEmpty(query.getReviewResult()), TAppointmentEntity::getReviewResult, query.getReviewResult());
        wrapper.eq(StringUtils.isNotEmpty(query.getReviewStatus()), TAppointmentEntity::getReviewStatus, query.getReviewStatus());
        if (query.getIsPerson() ) {
          //  wrapper.isNull(TAppointmentEntity::getSupplierSubclass).or().eq(TAppointmentEntity::getSupplierSubclass, 0);
            wrapper.ne(TAppointmentEntity::getSupplierSubclass , 1);
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
                    if (CollectionUtils.isNotEmpty(appointmentPersonnels)){
                        if (appointmentPersonnels.get(0).getExternalPersonnel().equals(query.getSubmitterName())){
                            ids.add(appointmentId);
                        }
                    }
                }
                wrapper.in(CollectionUtils.isNotEmpty(ids) , TAppointmentEntity::getId, ids);
            }
        }
        return wrapper;
    }

    private LambdaQueryWrapper<TAppointmentEntity> getWrapperAll(TAppointmentQuery query) {
        List<String> list = Stream.of("3", "4", "5").toList();
        LambdaQueryWrapper<TAppointmentEntity> wrapper = getWrapper(query);

        wrapper.or().eq(TAppointmentEntity::getCreator , query.getCreator())
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

        if (query.getIsFinish() != null ){
            if (query.getIsFinish()){
                wrapper.eq(TAppointmentEntity::getReviewStatus , 0);
            } else {
                wrapper.in(TAppointmentEntity::getReviewStatus ,List.of(1,-1));
            }
        }
        if (query.getIsPerson() && StringUtils.isEmpty(query.getReviewStatus())) {
            //  wrapper.isNull(TAppointmentEntity::getSupplierSubclass).or().eq(TAppointmentEntity::getSupplierSubclass, 0);
            wrapper.ne(TAppointmentEntity::getSupplierSubclass , 1);
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
            userFeign.sendSystemMessage(entity.getAppointmentType() , entity.getSiteId());
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
        if (CollectionUtils.isNotEmpty(vehicleList)) {
            if (vo.getVehicle()) {
                tAppointmentVehicleService.remove(new LambdaQueryWrapper<TAppointmentVehicle>().eq(TAppointmentVehicle::getAppointmentId, vo.getId()));
                List<TAppointmentVehicle> vehicles = BeanUtil.copyToList(vehicleList, TAppointmentVehicle.class);
                tAppointmentVehicleService.saveBatch(vehicles);
            }
        }
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
    public TAppointmentVO getDetailById(Long id) {
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
        vo.setVehicleList(TAppointmentVehicleConvert.INSTANCE.convertList(vehicleList));

        return vo;
    }


    @Override
    public PageResult<TAppointmentVO> pageByAuthority(TAppointmentQuery query) {

        LambdaQueryWrapper<TAppointmentEntity> wrapper = getWrapper(query );
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
        }
        return new PageResult<>(tAppointmentVOS, page.getTotal());

    }


    @Override
    public void updateByAudit(TAppointmentVO vo) {
        //修改主表的信息
        TAppointmentEntity entity = TAppointmentConvert.INSTANCE.convert(vo);
        updateById(entity);
        //审核通过后会执行下发对应指定的厂站
        if (vo.getReviewStatus().equals(Constant.PASS)) {
            //根据指定通道下发数据
            Long id = entity.getId();
            TAppointmentEntity byId = getById(id);
            List<TAppointmentVehicle> list = tAppointmentVehicleService.list(new LambdaQueryWrapper<TAppointmentVehicle>().eq(
                    TAppointmentVehicle::getAppointmentId, id
            ));

            List<TAppointmentPersonnel> personnelList = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>()
                    .eq(TAppointmentPersonnel::getAppointmentId , id));

            if (CollectionUtils.isNotEmpty(personnelList)) {
                String domain = properties.getConfig().getDomain();
                String siteCode = appointmentDao.selectSiteCodeById(byId.getSiteId());
                List<String> strings = appointmentDao.selectManuFacturerIdById(byId.getSiteId(), "1");
                for (String device : strings) {
                    List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDeviceList(device ,byId.getSiteId());
                    List<String> masterIpById = appointmentDao.selectMasterIpById(device, "1" ,byId.getSiteId());
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
                            rabbitMQTemplate.convertAndSend(siteCode + Constant.EXCHANGE, siteCode + Constant.SITE_ROUTING_FACE_TOAGENT, entries);
                        }
                    }
                }
            }


            if (CollectionUtils.isNotEmpty(list)) {
                String siteCode = appointmentDao.selectSiteCodeById(byId.getSiteId());
                List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDevices(byId.getSiteId(), "2");
                //主机分组
                Map<String, List<com.alibaba.fastjson.JSONObject>> master = jsonObjects.stream().collect(Collectors.groupingBy(item -> item.getString("master")));
                //遍历
                for (String key : master.keySet()) {
                    //主机下面带的设备
                    for (com.alibaba.fastjson.JSONObject jsonObject : master.get(key)) {
                        //遍历预约车辆
                        for (TAppointmentVehicle tAppointmentVehicle : list) {
                            JSONObject entries = new JSONObject();
                            entries.set("type", jsonObject.getString("type"));
                            entries.set("startTime", DateUtils.format(byId.getStartTime(), DateUtils.DATE_TIME_PATTERN));
                            entries.set("deadline", DateUtils.format(byId.getEndTime(), DateUtils.DATE_TIME_PATTERN));
                            entries.set("carNumber", tAppointmentVehicle.getPlateNumber());
                            entries.set("status", "add");
                            entries.set("masterIp", jsonObject.get("master_ip"));
                            entries.set("databaseName", jsonObject.get("master_sn"));
                            entries.set("username", jsonObject.get("master_account"));
                            entries.set("password", jsonObject.get("master_password"));
                            rabbitMQTemplate.convertAndSend(siteCode + Constant.EXCHANGE, siteCode + Constant.SITE_ROUTING_CAR_TOAGENT, entries);
                        }
                        //如果是科飞达智设备,就不需要循环
                        if (jsonObject.getString("type").equals(Constant.KFDZ)){
                            return;
                        }
                    }
                }
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
        wrapper.between(ArrayUtils.isNotEmpty(data.getCreatTime()), TAppointmentEntity::getCreateTime, ArrayUtils.isNotEmpty(data.getCreatTime()) ? data.getCreatTime()[0] : null, ArrayUtils.isNotEmpty(data.getCreatTime()) ? data.getCreatTime()[1] : null);
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
     * @param id   场站id
     * @param type 类型
     * @return
     */
    @Override
    public JSONObject appointmentSum(Long id, Long type) {

        LocalDateTime now = LocalDateTime.now();
        JSONObject entries = new JSONObject();
        entries.set("vehicleCount", Constant.ZERO);
        entries.set("personCount", Constant.ZERO);
        if (type > 1) {
            List<TAppointmentEntity> list = list(new LambdaQueryWrapper<TAppointmentEntity>()
                    .eq(TAppointmentEntity::getSiteId, id).le(TAppointmentEntity::getEndTime, now).ge(TAppointmentEntity::getStartTime, now));
            List<Long> longList = list.stream().map(TAppointmentEntity::getId).toList();
            if (CollectionUtils.isNotEmpty(list)) {
                long vehicleCount = tAppointmentVehicleService.count(new LambdaQueryWrapper<TAppointmentVehicle>().in(TAppointmentVehicle::getAppointmentId, longList));
                long personCount = tAppointmentPersonnelService.count(new LambdaQueryWrapper<TAppointmentPersonnel>().in(TAppointmentPersonnel::getAppointmentId, longList));
                entries.set("vehicleCount", vehicleCount);
                entries.set("personCount", personCount);
            }
            return entries;
        }
        List<String> typeList = Stream.of("3", "4", "5").toList();
        List<TAppointmentEntity> list = list(new LambdaQueryWrapper<TAppointmentEntity>().in(TAppointmentEntity::getAppointmentType, typeList)
                .eq(TAppointmentEntity::getSiteId, id).le(TAppointmentEntity::getEndTime, now).ge(TAppointmentEntity::getStartTime, now));
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
        log.info("下发的消息：{}",data);
        String sendType = data.getStr("sendType");
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
                //场站关联编码
                String siteCode = appointmentDao.selectSiteCodeById(Long.parseLong(stationId));
                List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDevices(Long.parseLong(stationId) , sendType);
                //主机分组
                Map<String, List<com.alibaba.fastjson.JSONObject>> master = jsonObjects.stream().collect(Collectors.groupingBy(item -> item.getString("master")));
                //遍历
                for (String key : master.keySet()) {
                    //主机下面带的设备
                    for (com.alibaba.fastjson.JSONObject jsonObject : master.get(key)) {
                        JSONObject sendData = new JSONObject();
                        sendData.set("type", jsonObject.getString("type"));
                        sendData.set("startTime", "2024-04-01 00:00:00");
                        sendData.set("deadline", "2034-04-01 00:00:00");
                        sendData.set("peopleName", peopleName);
                        sendData.set("peopleCode", code);
                        sendData.set("faceUrl", faceUrl);
                        sendData.set("masterIp", key);
                        sendData.set("faceUrl", domain+faceUrl);
                        sendData.set("masterIp", key);
                        sendData.set("deviceInfos", JSONUtil.toJsonStr(jsonObjects));
                        sendData.set("password" , jsonObjects.get(0).getString("password"));
                        sendData.set("DELETE" , data.getStr("DELETE"));
                        log.info("发送的消息："+sendData);
                        rabbitMQTemplate.convertAndSend(siteCode + Constant.EXCHANGE, siteCode + Constant.SITE_ROUTING_FACE_TOAGENT, sendData);
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

                String siteCode = appointmentDao.selectSiteCodeById(Long.parseLong(stationId));
                List<com.alibaba.fastjson.JSONObject> jsonObjects = appointmentDao.selectDevices(Long.parseLong(stationId) , sendType);
                //主机分组
                Map<String, List<com.alibaba.fastjson.JSONObject>> master = jsonObjects.stream().collect(Collectors.groupingBy(item -> item.getString("master")));
                //遍历
                for (String key : master.keySet()) {
                    //主机下面带的设备
                    for (com.alibaba.fastjson.JSONObject jsonObject : master.get(key)) {
                        JSONObject sendData = new JSONObject();
                        sendData.set("type", jsonObject.getString("type"));
                        sendData.set("startTime", "2024-04-01 00:00:00");
                        sendData.set("deadline", "2034-04-01 00:00:00");
                        sendData.set("carNumber", licensePlate);
                        sendData.set("status", data.getStr("DELETE")==null ? "add" : "delete" );
                        sendData.set("masterIp", jsonObject.get("master"));
                        sendData.set("databaseName",jsonObject.get("master_sn"));
                        sendData.set("username",jsonObject.get("master_account"));
                        sendData.set("password",jsonObject.get("master_password"));
                        sendData.set("DELETE" , data.getStr("DELETE"));
                        log.info("发送的消息："+sendData);
                        rabbitMQTemplate.convertAndSend(siteCode + Constant.EXCHANGE, siteCode + Constant.SITE_ROUTING_CAR_TOAGENT, sendData);
                        //如果是科飞达智设备,就不需要循环
                        if (jsonObject.getString("type").equals(Constant.KFDZ)){
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public JSONArray querOtherAppointmentService(Long siteId, Integer page, Integer limit) {

        JSONArray objects = new JSONArray();

//        UserDetail user = SecurityUser.getUser();
//        if (ObjectUtil.isNull(user)) {
//            throw new ServerException(ErrorCode.FORBIDDEN);
//        }
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
            if (ObjectUtil.isNotEmpty(one)){
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

            if (CollectionUtils.isEmpty(readAllSync)){
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
            readAllSync.forEach( item->{
                TAppointmentVehicle tAppointmentVehicle = new TAppointmentVehicle();
                tAppointmentVehicle.setDeliveryDate(item.getStartTime()+","+item.getEndTime());
                tAppointmentVehicle.setPlateNumber(item.getPlateNumber());
                tAppointmentVehicle.setVehicleModel(map1.get(item.getVehicleModel()));
                tAppointmentVehicle.setEmissionStandard(map2.get(item.getEmissionStandard()));
                tAppointmentVehicle.setPassenger(item.getPassenger());
                list.add(tAppointmentVehicle);
            } );
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
        wrapper.eq(TAppointmentEntity::getSiteId, siteId);
        return wrapper;
    }

    @Override
    public com.alibaba.fastjson.JSONObject queryStatisticsallPeopleReservation() {
        LambdaQueryWrapper<TAppointmentEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getStatus, 1);
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getDeleted, 0);
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
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentPersonnelList)){
            numberOfResidents = tAppointmentPersonnelList.size();
        }

        LambdaQueryWrapper<TAppointmentPersonnel> personnelLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        personnelLambdaQueryWrapper2.in(TAppointmentPersonnel::getAppointmentId, list1);
        List<TAppointmentPersonnel> tAppointmentPersonnelList2 = tAppointmentPersonnelService.list(personnelLambdaQueryWrapper2);
        int numberOfExternalAppointments = 0;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentPersonnelList2)){
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
        objectLambdaQueryWrapper.eq(TAppointmentEntity::getSiteId, siteId);
        List<TAppointmentEntity> tAppointmentEntities = baseMapper.selectList(objectLambdaQueryWrapper);

        // 筛选出预约类型是外部预约的
        List<Long> list1 = tAppointmentEntities.stream()
                .filter(entity -> List.of("3", "4", "5").contains(entity.getAppointmentType()))
                .map(TAppointmentEntity::getId)
                .toList();


        LambdaQueryWrapper<TAppointmentPersonnel> personnelLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        personnelLambdaQueryWrapper2.in(TAppointmentPersonnel::getAppointmentId, list1);
        List<TAppointmentPersonnel> tAppointmentPersonnelList2 = tAppointmentPersonnelService.list(personnelLambdaQueryWrapper2);
        int numberOfExternalAppointments = 0;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tAppointmentPersonnelList2)){
            numberOfExternalAppointments = tAppointmentPersonnelList2.size();
        }
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("numberOfExternalAppointments", numberOfExternalAppointments);
        return jsonObject;
    }
}
