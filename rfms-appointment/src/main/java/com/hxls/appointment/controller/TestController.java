package com.hxls.appointment.controller;

import com.hxls.api.dto.StorageDTO;
import com.hxls.api.feign.system.StorageFeign;
import com.hxls.appointment.dao.TAppointmentDao;
import com.hxls.appointment.pojo.vo.RabbitInfoVO;
import com.hxls.appointment.pojo.vo.TVehicleVO;
import com.hxls.framework.common.utils.JsonUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.Result;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.repository.cdi.Eager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
* 新增模块演示
*
* @author
*/
@RestController
@Tag(name="企业检查演示")
@AllArgsConstructor
@RequestMapping("car")
public class TestController {

    private final TAppointmentDao dao;

    private final AmqpTemplate rabbitMQTemplate;

    @PostMapping("listByCarNumber")
    public Result<List<TVehicleVO>> listByCarNumber(@RequestBody List<String> data){
        System.out.println("开始进入方法");
       List<TVehicleVO> tVehicleVOS = dao.listByCarNumber(data);
        return Result.ok(tVehicleVOS);
    }

    @GetMapping("test")
    public void save(@RequestParam String router , @RequestParam String carNumber){
        RabbitInfoVO rabbitInfoVO = new RabbitInfoVO();
        rabbitInfoVO.setType("8");
        rabbitInfoVO.setCarPlateNumber(carNumber);
        rabbitInfoVO.setInstructionType(1);
        rabbitMQTemplate.convertAndSend(router ,router, JsonUtils.toJsonString(rabbitInfoVO));
    }

}
