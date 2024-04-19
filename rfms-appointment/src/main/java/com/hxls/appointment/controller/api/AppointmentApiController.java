package com.hxls.appointment.controller.api;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.appointment.pojo.entity.TAppointmentEntity;
import com.hxls.appointment.pojo.entity.TAppointmentPersonnel;
import com.hxls.appointment.pojo.query.TAppointmentQuery;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
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
import org.apache.xmlbeans.impl.xb.xsdschema.All;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/appointment")
@Tag(name = "api接口(免登录)")
@AllArgsConstructor
public class AppointmentApiController {


    private final RabbitMqManager rabbitMqManager;
    private final RedisCache redisCache;
    private final TAppointmentService tAppointmentService;
    private final TAppointmentPersonnelService tAppointmentPersonnelService;

    @PostMapping("establish")
    @Operation(summary = "建立站点队列")
    public AppointmentDTO establish(@RequestBody AppointmentDTO data) {
        System.out.println("接收到信息");
        System.out.println(data);
        if (redisCache.get(data.getIp()) == null) {
            rabbitMqManager.declareExchangeAndQueue(data);
            data.setResult(true);
        }
        return data;
    }


    @PostMapping("establishAgentToCloud")
    @Operation(summary = "用户客户端到平台的 建立站点队列")
    public AppointmentDTO establishAgentToCloud(@RequestBody AppointmentDTO data) {
        System.out.println("接收到信息");
        System.out.println(data);
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
        TAppointmentVO vo = tAppointmentService.getDetailById(id);
        return Result.ok(vo);
    }

    @PostMapping("board")
    @Operation(summary = "获取安防看板")
    public PageResult<TAppointmentVO> board(@RequestBody AppointmentDTO data) {
        System.out.println("开始访问获取安防看板");
        PageResult<TAppointmentVO> result = tAppointmentService.pageBoard(data);
        System.out.println(result);
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
        System.out.println("下发信息");
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
        tAppointmentEntityLambdaQueryWrapper.eq(TAppointmentEntity::getReviewStatus, 1); // 审核通过
        List<TAppointmentEntity> tAppointmentEntities = tAppointmentService.list(tAppointmentEntityLambdaQueryWrapper);
        List<Long> wpPersonnel = new ArrayList<>();
        List<TAppointmentPersonnel> AllpersonnelList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(tAppointmentEntities)){
            for (int i = 0; i < tAppointmentEntities.size(); i++) {
                TAppointmentEntity tAppointmentEntity = tAppointmentEntities.get(i);
                List<TAppointmentPersonnel> personnelList = tAppointmentPersonnelService.list(new LambdaQueryWrapper<TAppointmentPersonnel>().eq(TAppointmentPersonnel::getAppointmentId, tAppointmentEntity.getId()));
                pzNum += personnelList.size();
                List<Long> collect = personnelList.stream().map(TAppointmentPersonnel::getId).collect(Collectors.toList());
                wpPersonnel.addAll(collect);

                AllpersonnelList.addAll(personnelList);
            }
        }

        // 按照 busis 字段进行分组
        Map<String, Long> postCounts = AllpersonnelList.stream()
                .collect(Collectors.groupingBy(TAppointmentPersonnel::getPostCode, Collectors.counting()));
        // 打印每个类型及其数量
        JSONObject postobjects = new JSONObject();
        for (Map.Entry<String, Long> entry2 : postCounts.entrySet()) {
            System.out.println("岗位：" + entry2.getKey() + "，数量：" + entry2.getValue());
            postobjects.putOnce(entry2.getKey(), entry2.getValue());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOnce("pzNum", pzNum);
        jsonObject.putOnce("pzAllIds", wpPersonnel);
        jsonObject.putOnce("postAll", postobjects);
        return jsonObject;
    }


    @GetMapping("auditPage")
    @Operation(summary = "主页查询")
    public Result<PageResult<TAppointmentVO>> auditPage(@ParameterObject @Valid TAppointmentQuery query) {
        PageResult<TAppointmentVO> page = tAppointmentService.pageByAuthority(query);
        return Result.ok(page);
    }


}
