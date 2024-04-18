package com.hxls.datasection.controller;

import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.datasection.convert.TVehicleAccessLedgerConvert;
import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import com.hxls.datasection.service.TVehicleAccessLedgerService;
import com.hxls.datasection.query.TVehicleAccessLedgerQuery;
import com.hxls.datasection.vo.TVehicleAccessLedgerVO;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

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
public class TVehicleAccessLedgerController {
    private final TVehicleAccessLedgerService tVehicleAccessLedgerService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('datasection:ledger:page')")
    public Result<PageResult<TVehicleAccessLedgerVO>> page(@ParameterObject @Valid TVehicleAccessLedgerQuery query){
        PageResult<TVehicleAccessLedgerVO> page = tVehicleAccessLedgerService.page(query);

        return Result.ok(page);
    }

}
