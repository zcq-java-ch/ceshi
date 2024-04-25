package com.hxls.system.controller;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.RandomSnowUtils;
import com.hxls.framework.common.utils.Result;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.system.convert.SysAreacodeDeviceConvert;
import com.hxls.system.convert.SysSiteAreaConvert;
import com.hxls.system.entity.SysAreacodeDeviceEntity;
import com.hxls.system.entity.SysSiteAreaEntity;
import com.hxls.system.query.SysSiteAreaQuery;
import com.hxls.system.service.SysAreacodeDeviceService;
import com.hxls.system.service.SysSiteAreaService;
import com.hxls.system.vo.SysSiteAreaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
* 站点区域表
*
* @author zhaohong 
* @since 1.0.0 2024-04-01
*/
@RestController
@RequestMapping("sys/siteArea")
@Tag(name="站点区域表")
@AllArgsConstructor
public class SysSiteAreaController {
    private final SysSiteAreaService sysSiteAreaService;
    protected final SysAreacodeDeviceService sysAreacodeDeviceService;

    @GetMapping("page")
    @Operation(summary = "分页")
//    @PreAuthorize("hasAuthority('spd:area:page')")
    public Result<PageResult<SysSiteAreaVO>> page(@ParameterObject @Valid SysSiteAreaQuery query){
        PageResult<SysSiteAreaVO> page = sysSiteAreaService.page(query);

        return Result.ok(page);
    }

    @GetMapping("/queryAreaBySite/{id}")
    @Operation(summary = "信息")
//    @PreAuthorize("hasAuthority('spd:area:info')")
    public Result<List<SysSiteAreaVO>> get(@PathVariable("id") Long siteId){
        LambdaQueryWrapper<SysSiteAreaEntity> sysSiteAreaEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysSiteAreaEntityLambdaQueryWrapper.eq(SysSiteAreaEntity::getSiteId, siteId);
        sysSiteAreaEntityLambdaQueryWrapper.eq(SysSiteAreaEntity::getStatus, 1);
        sysSiteAreaEntityLambdaQueryWrapper.eq(SysSiteAreaEntity::getDeleted, 0);
        List<SysSiteAreaEntity> sysSiteAreaEntityList = sysSiteAreaService.list(sysSiteAreaEntityLambdaQueryWrapper);
        if (CollectionUtils.isNotEmpty(sysSiteAreaEntityList)){
            List<SysSiteAreaVO> returnList = new ArrayList<>();

            for (int i = 0; i < sysSiteAreaEntityList.size(); i++) {
                SysSiteAreaEntity entity = sysSiteAreaService.getById(sysSiteAreaEntityList.get(i).getId());
                SysSiteAreaVO convert = SysSiteAreaConvert.INSTANCE.convert(entity);
                String faceInCode = entity.getFaceInCode();
                if (StringUtils.isNotEmpty(faceInCode)){
                    JSONArray objects = sysAreacodeDeviceService.queryDeviceListByCode(faceInCode);
                    convert.setFaceInCodeAndDevices(objects);
                }
                String faceOutCode = entity.getFaceOutCode();
                if (StringUtils.isNotEmpty(faceOutCode)){
                    JSONArray objects = sysAreacodeDeviceService.queryDeviceListByCode(faceOutCode);
                    convert.setFaceOutCodeAndDevices(objects);
                }
                String carInCode = entity.getCarIntCode();
                if (StringUtils.isNotEmpty(carInCode)){
                    JSONArray objects = sysAreacodeDeviceService.queryDeviceListByCode(carInCode);
                    convert.setCarIntCodeAndDevices(objects);
                }
                String carOutCode = entity.getCarOutCode();
                if (StringUtils.isNotEmpty(carOutCode)){
                    JSONArray objects = sysAreacodeDeviceService.queryDeviceListByCode(carOutCode);
                    convert.setCarOutCodeAndDevices(objects);
                }
                returnList.add(convert);
            }
            return Result.ok(returnList);
        }else {
            return Result.ok();
        }
    }

    @PostMapping("/saveSiteArea")
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
//    @PreAuthorize("hasAuthority('spd:area:save')")
    public Result<String> save(@RequestBody SysSiteAreaVO vo){

        String faceInCode = RandomSnowUtils.getSnowRandom();
        String faceOutCode = RandomSnowUtils.getSnowRandom();
        String carIntCode = RandomSnowUtils.getSnowRandom();
        String carOutCode = RandomSnowUtils.getSnowRandom();
        sysSiteAreaService.addNewDevices(vo.getFaceInCodeAddDevices(), faceInCode);
        sysSiteAreaService.addNewDevices(vo.getFaceOutCodeAddDevices(), faceOutCode);
        sysSiteAreaService.addNewDevices(vo.getCarIntCodeAddDevices(), carIntCode);
        sysSiteAreaService.addNewDevices(vo.getCarOutCodeAddDevices(), carOutCode);


        vo.setFaceInCode(faceInCode);
        vo.setFaceOutCode(faceOutCode);
        vo.setCarIntCode(carIntCode);
        vo.setCarOutCode(carOutCode);
        sysSiteAreaService.save(vo);

        return Result.ok();
    }

    @PutMapping("/updateSiteArea")
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
//    @PreAuthorize("hasAuthority('spd:area:update')")
    public Result<String> update(@RequestBody @Valid SysSiteAreaVO vo){

        // 清空中间表原有数据并重新生成随机码
        String faceInCode = sysSiteAreaService.resetCodeAndDeleteData(vo.getFaceInCode());
        String faceOutCode = sysSiteAreaService.resetCodeAndDeleteData(vo.getFaceOutCode());
        String carIntCode = sysSiteAreaService.resetCodeAndDeleteData(vo.getCarIntCode());
        String carOutCode = sysSiteAreaService.resetCodeAndDeleteData(vo.getCarOutCode());

        // 添加新的设备数据
        sysSiteAreaService.addNewDevices(vo.getFaceInCodeAddDevices(), faceInCode);
        sysSiteAreaService.addNewDevices(vo.getFaceOutCodeAddDevices(), faceOutCode);
        sysSiteAreaService.addNewDevices(vo.getCarIntCodeAddDevices(), carIntCode);
        sysSiteAreaService.addNewDevices(vo.getCarOutCodeAddDevices(), carOutCode);

        vo.setFaceInCode(faceInCode);
        vo.setFaceOutCode(faceOutCode);
        vo.setCarIntCode(carIntCode);
        vo.setCarOutCode(carOutCode);
        sysSiteAreaService.update(vo);

        return Result.ok();
    }

    @PostMapping("/bindCheckDeviceSiteArea")
    @Operation(summary = "绑定检查")
    @OperateLog(type = OperateTypeEnum.INSERT)
//    @PreAuthorize("hasAuthority('spd:area:save')")
    public Result<String> bindCheckDeviceSiteArea(@RequestBody JSONObject params) {
        JSONArray checkBind = params.getJSONArray("checkBind");
        JSONArray areaCodes = params.getJSONArray("areaCodes");

        boolean hasDuplicates = checkDuplicates(checkBind);
        if (hasDuplicates){
            return Result.error("不能存在重复的设备！");
        }
        // 检查设备是否被绑定过
        for (int i = 0; i < checkBind.size(); i++) {
            // 检查设备是否被绑定过
            LambdaQueryWrapper<SysAreacodeDeviceEntity> sysAreacodeDeviceEntity = new LambdaQueryWrapper<SysAreacodeDeviceEntity>();
            sysAreacodeDeviceEntity.eq(SysAreacodeDeviceEntity::getDeviceId, checkBind.get(i));
            sysAreacodeDeviceEntity.eq(SysAreacodeDeviceEntity::getStatus, 1);
            sysAreacodeDeviceEntity.eq(SysAreacodeDeviceEntity::getDeleted, 0);
            sysAreacodeDeviceEntity.notIn(CollectionUtils.isNotEmpty(areaCodes), SysAreacodeDeviceEntity::getAreaDeviceCode, areaCodes.toArray());
            long count = sysAreacodeDeviceService.count(sysAreacodeDeviceEntity);
            if (count > 0){
                return Result.error(checkBind.get(i)+":该设备已被绑定！");
            }
        }
        return Result.ok();
    }
    public static boolean checkDuplicates(JSONArray list) {
        HashSet<Long> set = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            Long element = list.getLong(i);
            if (!set.add(element)) {
                // 如果元素已经存在于集合中，则说明存在重复元素
                return true;
            }
        }
        // 如果集合中没有重复元素，则返回false
        return false;
    }


    @PostMapping("/delSiteArea")
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
//    @PreAuthorize("hasAuthority('spd:area:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        try{
            // 删除中间表
            for (int i = 0; i < idList.size(); i++) {
                SysSiteAreaEntity byId = sysSiteAreaService.getById(idList.get(i));
                String faceInCode = byId.getFaceInCode();
                String faceOutCode = byId.getFaceOutCode();
                String carIntCode = byId.getCarIntCode();
                String carOutCode = byId.getCarOutCode();
                sysAreacodeDeviceService.deleteDataByCode(faceInCode);
                sysAreacodeDeviceService.deleteDataByCode(faceOutCode);
                sysAreacodeDeviceService.deleteDataByCode(carIntCode);
                sysAreacodeDeviceService.deleteDataByCode(carOutCode);
            }
            sysSiteAreaService.delete(idList);
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("删除失败!");
        }
    }
}
