package com.hxls.appointment.controller;

import com.hxls.appointment.dao.TAppointmentDao;
import com.hxls.appointment.pojo.vo.TVehicleVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.Result;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("listByCarNumber")
    public Result<List<TVehicleVO>> listByCarNumber(@RequestBody List<String> data){
        System.out.println("开始进入方法");
       List<TVehicleVO> tVehicleVOS = dao.listByCarNumber(data);
        return Result.ok(tVehicleVOS);
    }

}
