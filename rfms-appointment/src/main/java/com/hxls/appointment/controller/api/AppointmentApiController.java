package com.hxls.appointment.controller.api;


import com.hxls.api.dto.appointment.AppointmentDTO;
import com.hxls.appointment.pojo.query.TAppointmentQuery;
import com.hxls.appointment.pojo.vo.TAppointmentVO;
import com.hxls.appointment.server.RabbitMqManager;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/appointment")
@Tag(name = "api接口")
@AllArgsConstructor
public class AppointmentApiController {

    private final RabbitMqManager rabbitMqManager;
    private final RedisCache redisCache;
    private final TAppointmentService tAppointmentService;

    @PostMapping("establish")
    @Operation(summary = "建立站点队列")
    public AppointmentDTO establish(@RequestBody AppointmentDTO data){
        System.out.println("接收到信息");
        System.out.println(data);
        if ( redisCache.get(data.getIp()) == null){
            rabbitMqManager.declareExchangeAndQueue(data);
            data.setResult(true);
        }

        return data;
    }

    @PostMapping("establishAgentToCloud")
    @Operation(summary = "用户客户端到平台的 建立站点队列")
    public AppointmentDTO establishAgentToCloud(@RequestBody AppointmentDTO data){
        System.out.println("接收到信息");
        System.out.println(data);
        rabbitMqManager.declareExchangeAndQueueToCloud(data);

        return data;
    }


    @PostMapping
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    public Result<String> save(@RequestBody TAppointmentVO vo){
        tAppointmentService.save(vo);
        return Result.ok();
    }
    @GetMapping("page")
    @Operation(summary = "分页")
    public Result<PageResult<TAppointmentVO>> page(@ParameterObject @Valid TAppointmentQuery query){
        //查询外部预约
        query.setOther(true);
        PageResult<TAppointmentVO> page = tAppointmentService.page(query);
        return Result.ok(page);
    }


    @PutMapping
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
    public Result<String> update(@RequestBody @Valid TAppointmentVO vo){
        tAppointmentService.update(vo);
        return Result.ok();
    }


    @GetMapping("{id}")
    @Operation(summary = "信息")
    public Result<TAppointmentVO> get(@PathVariable("id") Long id){
        TAppointmentVO vo = tAppointmentService.getDetailById(id);
        return Result.ok(vo);
    }


    @PostMapping("board")
    @Operation(summary = "获取安防看板")
    public PageResult<TAppointmentVO> board(@RequestBody AppointmentDTO data ){
        System.out.println("开始访问获取安防看板");
        PageResult<TAppointmentVO>  result = tAppointmentService.pageBoard(data);
        System.out.println(result);
        return result;
    }

    @GetMapping(value = "del")
    public void delAppointment(@RequestParam  Long id){
        tAppointmentService.delAppointment(id);
    }



}
