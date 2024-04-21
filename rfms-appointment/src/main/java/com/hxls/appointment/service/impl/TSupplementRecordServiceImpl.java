package com.hxls.appointment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
        wrapper.between(ArrayUtils.isNotEmpty(query.getSupplementTime()), TSupplementRecord::getSupplementTime, ArrayUtils.isNotEmpty(query.getSupplementTime()) ? query.getSupplementTime()[0] : null, ArrayUtils.isNotEmpty(query.getSupplementTime()) ? query.getSupplementTime()[1] : null);
        wrapper.eq(StringUtils.isNotEmpty(query.getAccessType()), TSupplementRecord::getAccessType, query.getAccessType());
        wrapper.eq(StringUtils.isNotEmpty(query.getSupplementType()), TSupplementRecord::getSupplementType, query.getSupplementType());
        return wrapper;
    }


    @Override
    public TSupplementRecordVO getDetailById(Long id) {

        TSupplementRecord byId = getById(id);
        TSupplementRecordVO convert = TRecordSupplementConvert.INSTANCE.convert(byId);
        if (ObjectUtil.isNull(byId)) {
            throw new ServerException("查找的数据已删除，或不存在");
        }
        List<TAppointmentPersonnel> list = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getSupplementaryId, id));
        convert.setRemark1(TAppointmentPersonnelConvert.INSTANCE.convertList(list));
        return convert;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void export(MultipartFile file) {
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

//                List<TSupplementRecord> collect = result.stream().map(item -> {
//
//
//
//                    siteMap.get(item.getSiteName()) == null?
//
//                    return ;
//
//                }).collect(Collectors.toList());
//                saveBatch(collect);
            }
        });



    }


}




