package com.hxls.datasection.controller.api;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import com.hxls.datasection.vo.AccessRecordsVO;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/PushNotification")
@Tag(name="icps推送通知")
@AllArgsConstructor
@Slf4j
public class IcpsPushNotification {

    @Value("$secret.key")
    private static String secretKey;

    private final TPersonAccessRecordsService tPersonAccessRecordsService;

    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;

    @PostMapping("/saveAccessRecords")
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    public Result<String> saveAccessRecords(@RequestBody AccessRecordsVO vo){
        try {
            System.out.println("获取到的数据 ： " + vo);
            if (StrUtil.isNotEmpty(vo.getSecretKey()) && secretKey.equals(vo.getSecretKey())){
                return Result.ok();
            }
            return Result.error();
        }catch (Exception e){
            e.printStackTrace();
            log.info("保存失败，进入catch:");
            return Result.error("添加失败！");
        }
    }



}
