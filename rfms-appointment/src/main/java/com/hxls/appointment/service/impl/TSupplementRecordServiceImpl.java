package com.hxls.appointment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.api.dto.datasection.TPersonAccessRecordsDTO;
import com.hxls.api.feign.datasection.DatasectionFeign;
import com.hxls.appointment.convert.TAppointmentConvert;
import com.hxls.appointment.convert.TAppointmentPersonnelConvert;
import com.hxls.appointment.convert.TRecordSupplementConvert;
import com.hxls.appointment.dao.TAppointmentDao;
import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.entity.TAppointmentPersonnel;
import com.hxls.appointment.pojo.entity.TSupplementRecord;
import com.hxls.appointment.pojo.query.TSupplementRecordQuery;
import com.hxls.appointment.pojo.vo.TAppointmentPersonnelVO;
import com.hxls.appointment.pojo.vo.TSupplementRecordExcelVO;
import com.hxls.appointment.pojo.vo.TSupplementRecordVO;
import com.hxls.appointment.service.TAppointmentPersonnelService;
import com.hxls.appointment.service.TSupplementRecordService;
import com.hxls.appointment.dao.TSupplementRecordMapper;
import com.hxls.framework.common.excel.ExcelFinishCallBack;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.ExcelUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 针对表【t_supplement_record(预约补录表)】的数据库操作Service实现
 * @createDate 2024-03-26 14:47:54
 */
@Service
@AllArgsConstructor
public class TSupplementRecordServiceImpl extends BaseServiceImpl<TSupplementRecordMapper, TSupplementRecord>
        implements TSupplementRecordService {

    private final TAppointmentPersonnelService tAppointmentPersonnelService;
    private final TAppointmentDao appointmentDao;
    private final DatasectionFeign datasectionFeign;

    @Override
    public PageResult<TSupplementRecordVO> page(TSupplementRecordQuery query) {
        IPage<TSupplementRecord> page = baseMapper.selectPage(getPage(query), getWrapper(query));
        List<TSupplementRecordVO> tSupplementRecordVOS = TRecordSupplementConvert.INSTANCE.convertList(page.getRecords());
        for (TSupplementRecordVO tSupplementRecordVO : tSupplementRecordVOS) {
            Long id = tSupplementRecordVO.getId();
            List<TAppointmentPersonnel> list = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getSupplementaryId, id));
            List<TAppointmentPersonnelVO> tAppointmentPersonnelVOS = TAppointmentPersonnelConvert.INSTANCE.convertList(list);
            tSupplementRecordVO.setRemark1(tAppointmentPersonnelVOS);
            Long submitter = tSupplementRecordVO.getCreator();

            String name = appointmentDao.getNameById(submitter);
            tSupplementRecordVO.setCreatorName(name);

            //区域翻译
            if (StringUtils.isNotEmpty(tSupplementRecordVO.getChannel())){
                String areaName = appointmentDao.selectAreaNameById(Long.parseLong(tSupplementRecordVO.getChannel()));
                tSupplementRecordVO.setChannel(areaName);
            }
            //场站翻译
            if (tSupplementRecordVO.getSiteId()!=null ){
                String siteName = appointmentDao.selectSiteNameById(tSupplementRecordVO.getSiteId());
                tSupplementRecordVO.setSiteName( siteName );
            }

            //创建人翻译
            if (tSupplementRecordVO.getCreator() != null ) {
                com.alibaba.fastjson.JSONObject jsonObject = appointmentDao.selectRealNameById(tSupplementRecordVO.getCreator());
                if (jsonObject != null ){
                    String realName = jsonObject.getString("real_name");
                    String postName = jsonObject.getString("name");
                    tSupplementRecordVO.setCreatorName(realName);
                    tSupplementRecordVO.setSubmitterOrgName(postName);
                }
            }
        }
        return new PageResult<>(tSupplementRecordVOS, page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(TSupplementRecordVO vo) {
        //主表转换
        TSupplementRecord entity = TRecordSupplementConvert.INSTANCE.convert(vo);
        //插入
        int insert = baseMapper.insert(entity);
        if (insert > 0) {
            List<TAppointmentPersonnelVO> remark = vo.getRemark1();
            if (CollectionUtils.isNotEmpty(remark)){
                List<TAppointmentPersonnel> appointmentPersonnels = remark.stream().map(item -> {
                    TAppointmentPersonnel tAppointmentPersonnel = new TAppointmentPersonnel();
                    BeanUtil.copyProperties(item, tAppointmentPersonnel);
                    tAppointmentPersonnel.setSupplementaryId(entity.getId());
                    return tAppointmentPersonnel;
                }).toList();
                tAppointmentPersonnelService.saveBatch(appointmentPersonnels);

                // 生成对应进出记录
                for (int i = 0; i < appointmentPersonnels.size(); i++) {
                    TAppointmentPersonnel tAppointmentPersonnel = appointmentPersonnels.get(i);

                    TPersonAccessRecordsDTO tPersonAccessRecordsDTO = new TPersonAccessRecordsDTO();
                    tPersonAccessRecordsDTO.setAccessType(entity.getAccessType());
                    tPersonAccessRecordsDTO.setBusis("");
                    tPersonAccessRecordsDTO.setChannelId(StringUtils.isNotEmpty(vo.getChannel()) ? Long.valueOf(vo.getChannel()) : null);
                    tPersonAccessRecordsDTO.setChannelName(StringUtils.isNotEmpty(vo.getChannelName()) ? vo.getChannelName() : null);
                    tPersonAccessRecordsDTO.setCreateType("2");
                    tPersonAccessRecordsDTO.setDeviceId(null);
                    tPersonAccessRecordsDTO.setDeviceName(null);
                    tPersonAccessRecordsDTO.setHeadUrl(null);
                    tPersonAccessRecordsDTO.setIdCardNumber(null);
                    tPersonAccessRecordsDTO.setPersonName(tAppointmentPersonnel.getExternalPersonnel());
                    tPersonAccessRecordsDTO.setPhone(tAppointmentPersonnel.getPhone());
                    tPersonAccessRecordsDTO.setPositionId(null);
                    tPersonAccessRecordsDTO.setPositionName(null);
                    tPersonAccessRecordsDTO.setRecordTime(vo.getSupplementTime());
                    tPersonAccessRecordsDTO.setSiteId(vo.getSiteId());
                    tPersonAccessRecordsDTO.setSiteName(vo.getSiteName());
                    tPersonAccessRecordsDTO.setSupervisorName(null);
                    // 关联补录单ID
                    tPersonAccessRecordsDTO.setSupplementId(entity.getId());

                    datasectionFeign.savePersonAccessRecords(tPersonAccessRecordsDTO);

                }
            }
        }
    }

    @Override
    public void update(TSupplementRecordVO vo) {
        //修改主表的信息
        TSupplementRecord entity = TRecordSupplementConvert.INSTANCE.convert(vo);
        updateById(entity);

        if (vo.getPerson()) {
            Long id = vo.getId();
            tAppointmentPersonnelService.remove(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getSupplementaryId, id));
            List<TAppointmentPersonnelVO> remark = vo.getRemark1();
            if (CollectionUtils.isNotEmpty(remark)) {
                List<TAppointmentPersonnel> appointmentPersonnels = remark.stream().map(item -> {
                    TAppointmentPersonnel tAppointmentPersonnel = new TAppointmentPersonnel();
                    BeanUtil.copyProperties(item, tAppointmentPersonnel);
                    tAppointmentPersonnel.setSupplementaryId(vo.getId());
                    return tAppointmentPersonnel;
                }).toList();
                tAppointmentPersonnelService.saveBatch(appointmentPersonnels);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }


    private LambdaQueryWrapper<TSupplementRecord> getWrapper(TSupplementRecordQuery query) {

        LambdaQueryWrapper<TSupplementRecord> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getSiteId() != null, TSupplementRecord::getSiteId, query.getSiteId());
        wrapper.in(CollectionUtils.isNotEmpty(query.getSiteIds()) , TSupplementRecord::getSiteId,query.getSiteIds());
        wrapper.between(ArrayUtils.isNotEmpty(query.getSupplementTime()), TSupplementRecord::getSupplementTime, ArrayUtils.isNotEmpty(query.getSupplementTime()) ? query.getSupplementTime()[0] : null, ArrayUtils.isNotEmpty(query.getSupplementTime()) ? query.getSupplementTime()[1] : null);
        wrapper.eq(StringUtils.isNotEmpty(query.getAccessType()), TSupplementRecord::getAccessType, query.getAccessType());
        wrapper.eq(StringUtils.isNotEmpty(query.getSupplementType()), TSupplementRecord::getSupplementType, query.getSupplementType());

        return query.getCreator() ==null? wrapper  : wrapper.or().eq(TSupplementRecord::getCreator ,query.getCreator());
    }


    @Override
    public TSupplementRecordVO getDetailById(Long id) {

        TSupplementRecord byId = getById(id);
        TSupplementRecordVO convert = TRecordSupplementConvert.INSTANCE.convert(byId);
        if (ObjectUtil.isNull(byId)) {
            throw new ServerException("查找的数据已删除，或不存在");
        }

        //做翻译
        //区域
        String channel = convert.getChannel();
        convert.setChannelName(appointmentDao.selectAreaNameById(Long.parseLong(channel)));
        //站点
        Long siteId = convert.getSiteId();
        convert.setSiteName(appointmentDao.selectSiteNameById(siteId));

        List<TAppointmentPersonnel> list = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getSupplementaryId, id));
        convert.setRemark1(TAppointmentPersonnelConvert.INSTANCE.convertList(list));
        return convert;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long export(MultipartFile file) {

        final Long[] resultId = new Long[1];
        ExcelUtils.readAnalysis(file, TSupplementRecordExcelVO.class, new ExcelFinishCallBack<TSupplementRecordExcelVO>() {
            @Override
            public void doAfterAllAnalysed(List<TSupplementRecordExcelVO> result) {
                saveUser(result);
            }

            @Override
            public void doSaveBatch(List<TSupplementRecordExcelVO> result) {
                saveUser(result);
            }

            private void saveUser(List<TSupplementRecordExcelVO> result) {

                if (CollectionUtils.isEmpty(result)){
                    return;
                }
                TSupplementRecord tSupplementRecord = new TSupplementRecord();
                //解析传入的参数是否正确
                //获取所有的厂站
                List<JSONObject> allSite = appointmentDao.getAllSite();

                Map<String, String> siteMap = allSite.stream()
                        .collect(Collectors.toMap(
                                jsonObject -> jsonObject.getString("name"), // 使用"name"作为key
                                jsonObject -> jsonObject.getString("id") // 使用"id"作为value
                        ));
                //获取所有的区域 area_name
                List<JSONObject> allChannel = appointmentDao.getAllChannel();
                Map<String, String> channelMap = allChannel.stream()
                        .collect(Collectors.toMap(
                                jsonObject -> jsonObject.getString("area_name"), // 使用"area_name"作为key
                                jsonObject -> jsonObject.getString("id") // 使用"id"作为value
                        ));
                //获取所有的出入类型
                List<JSONObject> allDictByType = appointmentDao.getAllDictByType(19);
                Map<String, String> map1 = allDictByType.stream()
                        .collect(Collectors.toMap(
                                jsonObject -> jsonObject.getString("dict_label"), // 使用"dict_label"作为key
                                jsonObject -> jsonObject.getString("dict_value") // 使用"dict_value"作为value
                        ));
                //获取所有的预约类型
                List<JSONObject> allDictByType1 = appointmentDao.getAllDictByType(18);
                Map<String, String> map2 = allDictByType1.stream()
                        .collect(Collectors.toMap(
                                jsonObject -> jsonObject.getString("dict_label"), // 使用"dict_label"作为key
                                jsonObject -> jsonObject.getString("dict_value") // 使用"dict_value"作为value
                        ));

                List<TAppointmentPersonnel> personnelList = new ArrayList<>();

                result.forEach(item ->{

                    if (siteMap.get( item.getSiteName()) ==null) {
                        throw new ServerException("填写错误 ：" +item.getSiteName() );
                    }


                    if (map1.get( item.getAccessType()) ==null) {
                        throw new ServerException("填写错误 ：" +item.getAccessType() );
                    }
                    if (channelMap.get( item.getChannel()) ==null) {
                        throw new ServerException("填写错误 ：" +item.getChannel() );
                    }
                    if (map2.get( item.getSupplementType()) ==null) {
                        throw new ServerException("填写错误 ：" +item.getSupplementType() );
                    }
                    tSupplementRecord.setSiteId(Long.parseLong(siteMap.get( item.getSiteName())));
                    tSupplementRecord.setAccessType(map1.get( item.getAccessType()));
                    tSupplementRecord.setChannel(channelMap.get( item.getChannel()));
                    tSupplementRecord.setSupplementType(map2.get( item.getSupplementType()));
                    tSupplementRecord.setSupplementTime(item.getSupplementTime());

                    TAppointmentPersonnel tAppointmentPersonnel = new TAppointmentPersonnel();

                    //内部预约查询人员的基础信息
                    if (tSupplementRecord.getSupplementType().equals("1") || tSupplementRecord.getSupplementType().equals("2")){
                        if ( StringUtils.isEmpty(item.getPhone())) {
                            throw new ServerException("填写错误 ：" +item.getPhone() );
                        }
                        JSONObject jsonObject = appointmentDao.selectByPhone(item.getPhone());
                        if (jsonObject ==null)throw new ServerException("填写错误 ：" +item.getPhone() );
                        tAppointmentPersonnel.setIdCardNumber(jsonObject.getString("id_card"));
                        tAppointmentPersonnel.setUserId(jsonObject.getLong("id"));
                        tAppointmentPersonnel.setSupervisorName(jsonObject.getString("supervisor"));
                        tAppointmentPersonnel.setOrgCode(jsonObject.getString("org_id"));
                        String orgName = appointmentDao.selectOrgName(Long.parseLong(tAppointmentPersonnel.getOrgCode()));
                        tAppointmentPersonnel.setOrgName(orgName);
                        tAppointmentPersonnel.setPostCode(jsonObject.getString("post_id"));
                        tAppointmentPersonnel.setExternalPersonnel(item.getUserName());
                        tAppointmentPersonnel.setPhone(item.getPhone());
                        tAppointmentPersonnel.setIdCardNumber(item.getIdCardNumber());
                        tAppointmentPersonnel.setPositionName(item.getPositionName());
                    }else {
                        tAppointmentPersonnel.setExternalPersonnel(item.getUserName());
                        tAppointmentPersonnel.setPhone(item.getPhone());
                        tAppointmentPersonnel.setIdCardNumber(item.getIdCardNumber());
                        tAppointmentPersonnel.setPositionName(item.getPositionName());
                    }
                    personnelList.add(tAppointmentPersonnel);
                });

                boolean save = save(tSupplementRecord);
                if (save){
                    Long id = tSupplementRecord.getId();
                    personnelList.forEach(item-> item.setSupplementaryId(id));
                    tAppointmentPersonnelService.saveBatch(personnelList);
                    resultId[0] = id;
                }
            }
        });
        return resultId[0];
    }



}




