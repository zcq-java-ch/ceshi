package com.hxls.appointment.controller.api;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.entity.TAppointmentPersonnel;
import com.hxls.appointment.pojo.query.TAppointmentQuery;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
import com.hxls.appointment.pojo.vo.TIssueEigenvalueVO;
import com.hxls.appointment.server.RabbitMqManager;
import com.hxls.appointment.service.TAppointmentPersonnelService;
import com.hxls.appointment.service.TAppointmentService;
import com.hxls.framework.common.cache.RedisCache;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.All;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/appointment")
@Tag(name = "api接口(免登录)")
@AllArgsConstructor
@Slf4j
public class AppointmentApiController {


    private final RabbitMqManager rabbitMqManager;
    private final RedisCache redisCache;
    private final TAppointmentService tAppointmentService;
    private final TAppointmentPersonnelService tAppointmentPersonnelService;

    @PostMapping("establish")
    @Operation(summary = "建立站点队列")
    public AppointmentDTO establish(@RequestBody AppointmentDTO data) {
       log.info("建立站点队列接收到信息 : {}" , data);

        if (redisCache.get(data.getIp()) == null) {
            rabbitMqManager.declareExchangeAndQueue(data);
            data.setResult(true);
        }
        return data;
    }


    @PostMapping("establishAgentToCloud")
    @Operation(summary = "用户客户端到平台的 建立站点队列")
    public AppointmentDTO establishAgentToCloud(@RequestBody AppointmentDTO data) {
        log.info("用户客户端到平台的 建立站点队列 : {}" , data);
        rabbitMqManager.declareExchangeAndQueueToCloud(data);
        return data;
    }

    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    public Result<String> save(@RequestBody TAppointmentVO vo) {
        tAppointmentService.save(vo);
        return Result.ok();
    }

    @GetMapping("page")
    @Operation(summary = "分页")
    public Result<PageResult<TAppointmentVO>> page(@ParameterObject @Valid TAppointmentQuery query) {
        //查询外部预约
        query.setOther(true);
        PageResult<TAppointmentVO> page = tAppointmentService.page(query);
        return Result.ok(page);

    }

    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    public Result<String> update(@RequestBody @Valid TAppointmentVO vo) {
        tAppointmentService.update(vo);
        return Result.ok();
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    public Result<TAppointmentVO> get(@PathVariable("id") Long id) {
        TAppointmentVO vo = tAppointmentService.getDetailById(id,2L);
        return Result.ok(vo);
    }

    @PostMapping("board")
    @Operation(summary = "获取安防看板")
    public PageResult<TAppointmentVO> board(@RequestBody AppointmentDTO data) {
       log.info("开始访问获取安防看板");
        PageResult<TAppointmentVO> result = tAppointmentService.pageBoard(data);
        return result;
    }

    @GetMapping(value = "del")
    public void delAppointment(@RequestParam Long id) {
        tAppointmentService.delAppointment(id);
    }

    @GetMapping(value = "/sum/{id}/{type}")
    public JSONObject appointmentSum(@PathVariable Long id, @PathVariable Long type) {
        return tAppointmentService.appointmentSum(id, type);
    }

    @PostMapping("issuedPeople")
    @Operation(summary = "下发信息")
    public Boolean issuedPeople(@RequestBody JSONObject data) {
       log.info("下发信息");
        try{
            tAppointmentService.issuedPeople(data);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    @PostMapping("queryTheNumberOfResidencies")
    @Operation(summary = "查询派驻人员数量和所有id")
    public JSONObject queryTheNumberOfResidencies(@RequestParam Long siteId) {

        Integer pzNum = 0;
        LambdaQueryWrapper<TAppointmentEntity> tAppointmentEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        tAppointmentEntityLambdaQueryWrapper.eq(TAppointmentEntity::getStatus, 1);
        tAppointmentEntityLambdaQueryWrapper.eq(TAppointmentEntity::getDeleted, 0);
        tAppointmentEntityLambdaQueryWrapper.eq(TAppointmentEntity::getSiteId, siteId);
        // 预约类型人员派驻
        tAppointmentEntityLambdaQueryWrapper.eq(TAppointmentEntity::getAppointmentType, "1");
        // 审核通过
        tAppointmentEntityLambdaQueryWrapper.eq(TAppointmentEntity::getReviewStatus, "1");
        tAppointmentEntityLambdaQueryWrapper.le(TAppointmentEntity::getStartTime, LocalDateTime.now());
        tAppointmentEntityLambdaQueryWrapper.ge(TAppointmentEntity::getEndTime, LocalDateTime.now());
        List<TAppointmentEntity> tAppointmentEntities = tAppointmentService.list(tAppointmentEntityLambdaQueryWrapper);
        List<Long> wpPersonnel = new ArrayList<>();
        List<TAppointmentPersonnel> AllpersonnelList = new ArrayList<>();
        JSONObject postobjects = new JSONObject();
        if (CollectionUtil.isNotEmpty(tAppointmentEntities)){
            for (int i = 0; i < tAppointmentEntities.size(); i++) {
                TAppointmentEntity tAppointmentEntity = tAppointmentEntities.get(i);
                List<TAppointmentPersonnel> personnelList = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, tAppointmentEntity.getId()));
                pzNum += personnelList.size();
                List<Long> collect = personnelList.stream().map(TAppointmentPersonnel::getUserId).collect(Collectors.toList());
                wpPersonnel.addAll(collect);

                AllpersonnelList.addAll(personnelList);
            }

        }
//        // 按照 busis 字段进行分组
//        Map<String, Long> postCounts = AllpersonnelList.stream()
//                .filter(tAppointmentPersonnel -> ObjectUtils.isNotEmpty(tAppointmentPersonnel.getPostCode()))
//                .collect(Collectors.groupingBy(TAppointmentPersonnel::getPostCode, Collectors.counting()));
//        // 打印每个类型及其数量
//        for (Map.Entry<String, Long> entry2 : postCounts.entrySet()) {
//           log.info("岗位：" + entry2.getKey() + "，数量：" + entry2.getValue());
//            postobjects.putOnce(entry2.getKey(), entry2.getValue());
//        }


        JSONObject jsonObject = new JSONObject();
        jsonObject.putOnce("pzNum", pzNum);
        jsonObject.putOnce("pzAllIds", wpPersonnel);
//        jsonObject.putOnce("postAll", postobjects);
        return jsonObject;
    }

    @GetMapping("auditPage")
    @Operation(summary = "主页查询")
    public Result<PageResult<TAppointmentVO>> auditPage(@ParameterObject @Valid TAppointmentQuery query) {
        PageResult<TAppointmentVO> page = tAppointmentService.pageByAuthority(query);
        return Result.ok(page);
    }

    @PostMapping("checkTheDetailsOfExternalAppointments")
    @Operation(summary = "查询外部预约人员明细")
    public JSONArray checkTheDetailsOfExternalAppointments(@RequestParam Long siteId,@RequestParam Integer page,@RequestParam Integer limit) {
        JSONArray objects = tAppointmentService.querOtherAppointmentService(siteId, page, limit);
        return objects;
    }

    /**
      * @author Mryang
      * @description PC端-公司看板-查询派驻人数和外部预约人数
      * @date 15:40 2024/4/23
      * @param
      * @return
      */
    @PostMapping("queryStatisticsallPeopleReservation")
    @Operation(summary = "查询派驻和外部预约数量")
    public com.alibaba.fastjson.JSONObject queryStatisticsallPeopleReservation() {
        com.alibaba.fastjson.JSONObject objects = tAppointmentService.queryStatisticsallPeopleReservation();
        return objects;
    }

    /**
      * @author Mryang
      * @description PC端-厂站看板-查询指定站点的外部预约的所有人总数
      * @date 15:21 2024/5/6
      * @param
      * @return
      */
    @GetMapping("queryTotalAppointments")
    @Operation(summary = "查询外部预约总数")
    public com.alibaba.fastjson.JSONObject queryTotalAppointments(@RequestParam Long siteId) {
        com.alibaba.fastjson.JSONObject objects = tAppointmentService.queryTotalAppointments(siteId);
        return objects;
    }

    /**
      * @author Mryang
      * @description 通过车牌，与记录时间，查询符合条件的预约单，将随行人员返回
      * @date 9:32 2024/5/11
      * @param
      * @return
      */
    @GetMapping("queryappointmentFormspecifyLicensePlatesAndEntourage")
    @Operation(summary = "查询指定车牌预约单随行人员")
    public com.alibaba.fastjson.JSONObject queryappointmentFormspecifyLicensePlatesAndEntourage(@RequestParam String plateNumber, @RequestParam String recordTime) {
        com.alibaba.fastjson.JSONObject objects = tAppointmentService.queryappointmentFormspecifyLicensePlatesAndEntourage(plateNumber, recordTime);
        return objects;
    }

    /**
     * @author Mryang
     * @description 通过用户ID或者用户名字，查询预约单所对应的站点
     * @date 10:07 2024/6/4
     */
    @GetMapping("queryStationIdFromAppointmentByUserInfo")
    @Operation(summary = "查询预约单站点")
    public com.alibaba.fastjson.JSONObject queryStationIdFromAppointmentByUserInfo(@RequestParam String personId, @RequestParam String personName) {
        com.alibaba.fastjson.JSONObject objects = tAppointmentService.queryStationIdFromAppointmentByUserInfo(personId, personName);
        return objects;
    }

    /**
     * @author Mryang
     * @description 通过车牌找该时间内有没有预约单，如果有预约单，则返沪这个预约单对应的站点ID
     * @date 15:51 2024/6/4
     */
    @GetMapping("queryStationIdFromAppointmentByPlatenumber")
    @Operation(summary = "查询预约单站点")
    public com.alibaba.fastjson.JSONObject queryStationIdFromAppointmentByPlatenumber(@RequestParam String palteNumber) {
        com.alibaba.fastjson.JSONObject objects = tAppointmentService.queryStationIdFromAppointmentByPlatenumber(palteNumber);
        return objects;
    }


    @PostMapping("/addTIssueEigenvalue")
    @Operation(summary = "回调操作接口")
    public void updateTIssueEigenvalue(@RequestBody TIssueEigenvalueVO data){

        tAppointmentService.updateTIssueEigenvalue(data);

    }
}
