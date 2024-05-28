package com.hxls.datasection.controller.api;

import com.hxls.api.dto.datasection.TPersonAccessRecordsDTO;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/tPersonAccess")
@Tag(name="人员通行记录外部调用")
@AllArgsConstructor
@Slf4j
public class TPersonAccessController {

    private final TPersonAccessRecordsService tPersonAccessRecordsService;

    @PostMapping("/saveTpersonAccessRecords")
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    public Result<String> saveTpersonAccessRecords(@RequestBody TPersonAccessRecordsVO vo){
        try {
            tPersonAccessRecordsService.save(vo);
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            log.info("保存失败，进入catch:");
            return Result.error("添加失败！");
        }
    }

     /**
      * @Description: 外部存储人员通行记录
      * @Autor: Mryang
      * @param
      * @return:
      */
    @PostMapping("/savePersonAccessRecords")
    @Operation(summary = "外部存储人员通行记录")
    @OperateLog(type = OperateTypeEnum.INSERT)
    public boolean savePersonAccessRecords(@RequestBody TPersonAccessRecordsDTO accessRecordsDTO){
        try {
            tPersonAccessRecordsService.saveFegin(accessRecordsDTO);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.info("外部存储人员通行记录失败，进入catch:");
            return false;
        }
    }

    /**
      * @author Mryang
      * @description 外部调用 删除某个补录单的人员进出记录
      * @date 11:08 2024/5/28
      * @param
      * @return
      */
    @PostMapping(value = "/deletePersonAccessRecords")
    @Operation(summary = "外部删除人员通行记录")
    @OperateLog(type = OperateTypeEnum.DELETE)
    public boolean deletePersonAccessRecords(@RequestParam("supplementId") Long supplement){
        try {
            tPersonAccessRecordsService.deletePersonAccessRecords(supplement);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.info("外部删除人员通行记录失败，进入catch:");
            return false;
        }
    }


}
