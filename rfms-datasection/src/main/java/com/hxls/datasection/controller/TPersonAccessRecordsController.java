package com.hxls.datasection.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fhs.trans.service.impl.TransService;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.api.feign.system.UserFeign;
import com.hxls.datasection.entity.DfWZCallBackDto;
import com.hxls.datasection.util.BaseImageUtils;
import com.hxls.framework.common.utils.DateUtils;
import com.hxls.framework.common.utils.ExcelUtils;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.security.user.UserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.datasection.convert.TPersonAccessRecordsConvert;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.query.TPersonAccessRecordsQuery;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
* 人员出入记录表
*
* @author zhaohong
* @since 1.0.0 2024-03-29
*/
@RestController
@RequestMapping("datasection/TPersonAccessRecords")
@Tag(name="人员出入记录表")
@AllArgsConstructor
@Slf4j
public class TPersonAccessRecordsController extends BaseController {

    private final TPersonAccessRecordsService tPersonAccessRecordsService;

    @GetMapping("/pageTpersonAccessRecords")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:page')")
    public Result<PageResult<TPersonAccessRecordsVO>> page(@ParameterObject @Valid TPersonAccessRecordsQuery query, @ModelAttribute("baseUser")  UserDetail baseUser){
        log.info("获取到token用户信息：{}",baseUser);
        List<Long> dataScopeList = baseUser.getDataScopeList();
        PageResult<TPersonAccessRecordsVO> page = tPersonAccessRecordsService.page(query, baseUser);
        return Result.ok(page);
    }

    @GetMapping("/tpersonAccessRecords/{id}")
    @Operation(summary = "信息")
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:info')")
    public Result<TPersonAccessRecordsVO> get(@PathVariable("id") Long id){
        TPersonAccessRecordsEntity entity = tPersonAccessRecordsService.getById(id);

        return Result.ok(TPersonAccessRecordsConvert.INSTANCE.convert(entity));
    }

    @PostMapping("/saveTpersonAccessRecords")
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:save')")
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

    @PutMapping("/updateTpersonAccessRecords")
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:update')")
    public Result<String> update(@RequestBody @Valid TPersonAccessRecordsVO vo){
        tPersonAccessRecordsService.update(vo);

        return Result.ok();
    }

    @DeleteMapping("/delTpersonAccessRecords")
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
//    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tPersonAccessRecordsService.delete(idList);

        return Result.ok();
    }

    /**
      * @author: Mryang
      * @Description: PC端-查询单行同行记录
      * @Date: 22:42 2024/4/20
      * @Param:
      * @return:
      */
    @GetMapping("/pageUnidirectionalTpersonAccessRecords")
    @Operation(summary = "查询单向通行记录")
    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:unidirectional')")
    public Result<PageResult<TPersonAccessRecordsVO>> pageUnidirectionalTVehicleAccessRecords(@ParameterObject @Valid TPersonAccessRecordsQuery query, @ModelAttribute("baseUser")  UserDetail baseUser){

        PageResult<TPersonAccessRecordsVO> page = tPersonAccessRecordsService.pageUnidirectionalTpersonAccessRecords(query,baseUser);
        return Result.ok(page);
    }

    /**
      * @author Mryang
      * @description 导出人员通行记录
      * @date 15:41 2024/4/25
      * @param
      * @return
      */
    @GetMapping("/exportUnidirectionalTpersonAccessRecords")
    @Operation(summary = "导出人员通行记录")
    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:export')")
    public void exportUnidirectionalTpersonAccessRecords(@ParameterObject @Valid TPersonAccessRecordsQuery query, @ModelAttribute("baseUser")  UserDetail baseUser){
        PageResult<TPersonAccessRecordsVO> page = tPersonAccessRecordsService.page(query, baseUser);
        List<TPersonAccessRecordsVO> list = page.getList();
        // 处理字典
        for (int i = 0; i < list.size(); i++) {
            TPersonAccessRecordsVO tPersonAccessRecordsVO = list.get(i);
            String accessType = tPersonAccessRecordsVO.getAccessType();
            if ("1".equals(accessType)){
                tPersonAccessRecordsVO.setAccessTypeLabel("进场");
            }else {
                tPersonAccessRecordsVO.setAccessTypeLabel("出场");
            }

            String busis = tPersonAccessRecordsVO.getBusis();
            if ("1".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("预拌");
            }else if ("2".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("预制");
            }else if ("3".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("管桩");
            }else if ("4".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("资源");
            }else if ("5".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("供应链");
            }else if ("6".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("工程");
            }else {
                tPersonAccessRecordsVO.setBusisLabel("错误");
            }

            String createType = tPersonAccessRecordsVO.getCreateType();
            if ("0".equals(createType)){
                tPersonAccessRecordsVO.setCreateTypeLabel("自动");
            }else {
                tPersonAccessRecordsVO.setCreateTypeLabel("手动");
            }
        }

//        transService.transBatch(list);
        // 写到浏览器打开
        ExcelUtils.excelExport(TPersonAccessRecordsVO.class, "人员通行记录" + DateUtils.format(new Date()), null, list);
    }

    /**
      * @author Mryang
      * @description 导出单行通行记录
      * @date 17:39 2024/4/25
      * @param
      * @return
      */
    @GetMapping("/exportTpersonAccessRecords")
    @Operation(summary = "导出单行通行记录")
    @PreAuthorize("hasAuthority('datasection:TPersonAccessRecords:unidirectionalexport')")
    public void exportTpersonAccessRecords(@ParameterObject @Valid TPersonAccessRecordsQuery query, @ModelAttribute("baseUser")  UserDetail baseUser){
        PageResult<TPersonAccessRecordsVO> page = tPersonAccessRecordsService.pageUnidirectionalTpersonAccessRecords(query,baseUser);
        List<TPersonAccessRecordsVO> list = page.getList();
        // 处理字典
        for (int i = 0; i < list.size(); i++) {
            TPersonAccessRecordsVO tPersonAccessRecordsVO = list.get(i);
            String accessType = tPersonAccessRecordsVO.getAccessType();
            if ("1".equals(accessType)){
                tPersonAccessRecordsVO.setAccessTypeLabel("进场");
            }else {
                tPersonAccessRecordsVO.setAccessTypeLabel("出场");
            }

            String busis = tPersonAccessRecordsVO.getBusis();
            if ("1".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("预拌");
            }else if ("2".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("预制");
            }else if ("3".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("管桩");
            }else if ("4".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("资源");
            }else if ("5".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("供应链");
            }else if ("6".equals(busis)){
                tPersonAccessRecordsVO.setBusisLabel("工程");
            }else {
                tPersonAccessRecordsVO.setBusisLabel("错误");
            }

            String createType = tPersonAccessRecordsVO.getCreateType();
            if ("0".equals(createType)){
                tPersonAccessRecordsVO.setCreateTypeLabel("自动");
            }else {
                tPersonAccessRecordsVO.setCreateTypeLabel("手动");
            }
        }

//        transService.transBatch(list);
        // 写到浏览器打开
        ExcelUtils.excelExport(TPersonAccessRecordsVO.class, "人员通行记录" + DateUtils.format(new Date()), null, list);
    }

}
