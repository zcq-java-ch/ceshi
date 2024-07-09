package com.hxls.appointment.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxls.api.dto.appointment.TIssueEigenvalueDTO;
import com.hxls.appointment.convert.TAppointmentConvert;
import com.hxls.appointment.convert.TIssueEigenvalueConvert;
import com.hxls.appointment.dao.TAppointmentDao;
import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.entity.TIssueEigenvalue;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
import com.hxls.appointment.pojo.vo.TIssueEigenvalueVO;
import com.hxls.appointment.pojo.vo.TIssueVO;
import com.hxls.appointment.service.TIssueEigenvalueService;
import com.hxls.appointment.dao.TIssueEigenvalueMapper;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author admin
* @description 针对表【t_issue_eigenvalue(下发特征值表)】的数据库操作Service实现
* @createDate 2024-06-12 16:57:41
*/
@Service
@AllArgsConstructor
@Slf4j
public class TIssueEigenvalueServiceImpl extends BaseServiceImpl<TIssueEigenvalueMapper, TIssueEigenvalue>
    implements TIssueEigenvalueService{


    private final TAppointmentDao tAppointmentDao;

    /**
     * 消息队列
     */
    private final AmqpTemplate rabbitMQTemplate;

    @Override
    public Long save(TIssueEigenvalueVO data) {

        //主表转换
        TIssueEigenvalue entity = TIssueEigenvalueConvert.INSTANCE.convert(data);
        int insert = this.baseMapper.insert(entity);

        return entity.getId();

    }

    @Override
    public PageResult<TIssueEigenvalueVO> pageList(TIssueEigenvalueDTO data) {

        LambdaQueryWrapper<TIssueEigenvalue> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(data.getSiteId() != null, TIssueEigenvalue::getStationId, data.getSiteId());
        wrapper.eq(data.getStatus() != null, TIssueEigenvalue::getStatus, data.getStatus());
        wrapper.eq(data.getAreaId() !=null , TIssueEigenvalue::getAreaId, data.getAreaId());
        wrapper.between(ArrayUtils.isNotEmpty(data.getCreatTime()), TIssueEigenvalue::getCreateTime, ArrayUtils.isNotEmpty(data.getCreatTime()) ? data.getCreatTime()[0] : null, ArrayUtils.isNotEmpty(data.getCreatTime()) ? data.getCreatTime()[1] : null);

        wrapper.eq(data.getType() != null , TIssueEigenvalue::getType,data.getType());
        wrapper.orderByDesc(TIssueEigenvalue::getCreateTime);
        Page<TIssueEigenvalue> page = new Page<>(data.getPage(), data.getLimit());
        IPage<TIssueEigenvalue> result = page(page, wrapper);

        List<TIssueEigenvalueVO> tIssueEigenvalueVOList = TIssueEigenvalueConvert.INSTANCE.convertList(result.getRecords());

        return new PageResult<>(tIssueEigenvalueVOList, page.getTotal());


    }

    @Override
    public void issue(Long id) {


        TIssueEigenvalue byId = getById(id);

        if (byId ==null){
            throw new ServerException(ErrorCode.NOT_FOUND);
        }

        Long stationId = byId.getStationId();
        String sendType = byId.getType().toString();

        JSONObject entries = JSONUtil.parseObj(byId.getData());
        entries.set("ID" , id);

        String siteCode = tAppointmentDao.selectSiteCodeById(stationId);
      switch (sendType){
          case "1" ->{
              rabbitMQTemplate.convertAndSend(siteCode + Constant.EXCHANGE, siteCode + Constant.SITE_ROUTING_FACE_TOAGENT, entries);
          }
          case "2" ->{
              rabbitMQTemplate.convertAndSend(siteCode + Constant.EXCHANGE, siteCode + Constant.SITE_ROUTING_CAR_TOAGENT, entries);
          }
      }
    }

    @Override
    @Transactional
    public String updateTIssueEigenvalue(TIssueVO data) {
        String data1 = data.getData();
        String type = data.getType();
        String ipFail = data.getIpFail();
        Map<String, List<String>> idFail = null;
        if (StringUtils.isNotEmpty(ipFail)){
            List<String> ipList = JSONUtil.toList(ipFail, String.class);
            idFail = ipList.stream().collect(Collectors.groupingBy(item -> item.split("_")[0]));
        }

        StringBuilder result = new StringBuilder();

        if (StringUtils.isNotEmpty(data1)){
            List<String> list = JSONUtil.toList(data1, String.class);
            List<TIssueEigenvalue> upList = new ArrayList<>();
            for (String id : list) {
                result.append(",").append(id);
                TIssueEigenvalue tIssueEigenvalue = new TIssueEigenvalue();
                tIssueEigenvalue.setId(Long.parseLong(id));
                tIssueEigenvalue.setStatus(type.equals("succeed")? 2 : 0 );
                tIssueEigenvalue.setDeviceName("");
                if (idFail !=null){
                    List<String> strings = idFail.get(id);
                    tIssueEigenvalue.setDeviceName(JSONUtil.toJsonStr(strings));
                }
                upList.add(tIssueEigenvalue);
            }
            updateBatchById(upList);
        }

        return result.toString();
    }
}




