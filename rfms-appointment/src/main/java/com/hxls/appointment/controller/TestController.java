package com.hxls.appointment.controller;

import com.hxls.api.dto.StorageDTO;
import com.hxls.api.feign.system.StorageFeign;
import com.hxls.appointment.dao.TAppointmentDao;
import com.hxls.appointment.pojo.vo.TVehicleVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.Result;
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

    private final StorageFeign feign;

    @PostMapping("listByCarNumber")
    public Result<List<TVehicleVO>> listByCarNumber(@RequestBody List<String> data){
        System.out.println("开始进入方法");
       List<TVehicleVO> tVehicleVOS = dao.listByCarNumber(data);
        return Result.ok(tVehicleVOS);
    }


    @PostMapping("test")
    public Result<StorageDTO> save(@RequestBody MultipartFile file){

        try {
            return Result.ok(  feign.upload(file) );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
