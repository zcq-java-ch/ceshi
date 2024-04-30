package com.hxls.datasection.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import com.hxls.framework.security.user.UserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.datasection.service.TVehicleAccessLedgerService;
import com.hxls.datasection.query.TVehicleAccessLedgerQuery;
import com.hxls.datasection.vo.TVehicleAccessLedgerVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
* 车辆进出厂展示台账
*
* @author zhaohong 
* @since 1.0.0 2024-04-18
*/
@RestController
@RequestMapping("datasection/ledger")
@Tag(name="车辆进出厂展示台账")
@AllArgsConstructor
public class TVehicleAccessLedgerController extends BaseController {
    private final TVehicleAccessLedgerService tVehicleAccessLedgerService;

    /**
      * @author: Mryang
      * @Description: PC端-记录查询-车辆进出展示台账
      * @Date:
      * @Param:
      * @return:
      */
    @PostMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('datasection:ledger:page')")
    public Result<PageResult<TVehicleAccessLedgerVO>> page(@RequestBody TVehicleAccessLedgerQuery query, @ModelAttribute("baseUser") UserDetail baseUser){
        PageResult<TVehicleAccessLedgerVO> page = tVehicleAccessLedgerService.page(query, baseUser);

        return Result.ok(page);
    }

    /**
      * @author Mryang
      * @description 在调用查看台账数据前，发送该接口，返回所有车队列表
      * @date 11:17 2024/4/30
      * @param
      * @return
      */
    @GetMapping("getAlllistOfFleets")
    @Operation(summary = "查询台账车队列表")
    public Result<List<String>> getAlllistOfFleets(@RequestParam("siteId") String siteId){
        LambdaQueryWrapper<TVehicleAccessLedgerEntity> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        objectLambdaQueryWrapper.eq(TVehicleAccessLedgerEntity::getSiteId, siteId);
        List<String> list = tVehicleAccessLedgerService.list(objectLambdaQueryWrapper)
                .stream()
                .map(TVehicleAccessLedgerEntity::getFleetName)
                .collect(Collectors.toList());

        return Result.ok(list);
    }

}
