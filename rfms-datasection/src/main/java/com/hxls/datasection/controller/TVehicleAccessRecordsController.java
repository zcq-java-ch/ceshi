package com.hxls.datasection.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxls.api.feign.system.DeviceFeign;
import com.hxls.api.feign.system.VehicleFeign;
import com.hxls.datasection.util.BaseImageUtils;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import com.hxls.framework.common.utils.DateUtils;
import com.hxls.framework.common.utils.ExcelUtils;
import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.framework.security.user.UserDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.common.utils.Result;
import com.hxls.datasection.convert.TVehicleAccessRecordsConvert;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import com.hxls.datasection.query.TVehicleAccessRecordsQuery;
import com.hxls.datasection.vo.TVehicleAccessRecordsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.util.ImageUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import javax.print.DocFlavor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
* 车辆出入记录表
*
* @author zhaohong
* @since 1.0.0 2024-03-29
*/
@RestController
@RequestMapping("datasection/TVehicleAccessRecords")
@Tag(name="车辆出入记录表")
@AllArgsConstructor
@Slf4j
public class TVehicleAccessRecordsController extends BaseController {
    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;
    private final DeviceFeign deviceFeign;
    private final VehicleFeign vehicleFeign;

    private static final String NOTFIND_DEVICE = "设备未找到";

    @GetMapping("/pageTVehicleAccessRecords")
    @Operation(summary = "分页")
    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:page')")
    public Result<PageResult<TVehicleAccessRecordsVO>> page(@ParameterObject @Valid TVehicleAccessRecordsQuery query, @ModelAttribute("baseUser") UserDetail baseUser){
        PageResult<TVehicleAccessRecordsVO> page = tVehicleAccessRecordsService.page(query,baseUser);

        return Result.ok(page);
    }

    @GetMapping("/TVehicleAccessRecords/{id}")
    @Operation(summary = "信息")
//    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:info')")
    public Result<TVehicleAccessRecordsVO> get(@PathVariable("id") Long id){
        TVehicleAccessRecordsEntity entity = tVehicleAccessRecordsService.getById(id);

        return Result.ok(TVehicleAccessRecordsConvert.INSTANCE.convert(entity));
    }

    @PostMapping("/saveTVehicleAccessRecords")
    @Operation(summary = "保存")
    @OperateLog(type = OperateTypeEnum.INSERT)
    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:save')")
    public Result<String> save(@RequestBody TVehicleAccessRecordsVO vo){
        tVehicleAccessRecordsService.save(vo);

        return Result.ok();
    }

    @PutMapping("/updateTVehicleAccessRecords")
    @Operation(summary = "修改")
    @OperateLog(type = OperateTypeEnum.UPDATE)
//    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:update')")
    public Result<String> update(@RequestBody @Valid TVehicleAccessRecordsVO vo){
        tVehicleAccessRecordsService.update(vo);

        return Result.ok();
    }

    @DeleteMapping("/delTVehicleAccessRecords")
    @Operation(summary = "删除")
    @OperateLog(type = OperateTypeEnum.DELETE)
//    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:delete')")
    public Result<String> delete(@RequestBody List<Long> idList){
        tVehicleAccessRecordsService.delete(idList);

        return Result.ok();
    }

    /**
      * @author Mryang
      * @description 导出车辆通行记录
      * @date 17:37 2024/4/25
      * @param
      * @return
      */
    @GetMapping("/exportTVehicleAccessRecords")
    @Operation(summary = "导出车辆通行记录")
    @PreAuthorize("hasAuthority('datasection:TVehicleAccessRecords:export')")
    public void exportTVehicleAccessRecords(@ParameterObject @Valid TVehicleAccessRecordsQuery query, @ModelAttribute("baseUser") UserDetail baseUser){
        PageResult<TVehicleAccessRecordsVO> page = tVehicleAccessRecordsService.page(query,baseUser);

        List<TVehicleAccessRecordsVO> list = page.getList();
        if (CollectionUtils.isNotEmpty(list)){
            for (int i = 0; i < list.size(); i++) {
                TVehicleAccessRecordsVO tVehicleAccessRecordsVO = list.get(i);
                String accessType = tVehicleAccessRecordsVO.getAccessType();
                if ("1".equals(accessType)){
                    tVehicleAccessRecordsVO.setAccessTypeLabel("进场");
                }else {
                    tVehicleAccessRecordsVO.setAccessTypeLabel("出场");
                }

                String createType = tVehicleAccessRecordsVO.getCreateType();
                if ("0".equals(createType)){
                    tVehicleAccessRecordsVO.setCreateTypeLabel("自动");
                }else {
                    tVehicleAccessRecordsVO.setCreateTypeLabel("手动");
                }

                String vehicleModel = tVehicleAccessRecordsVO.getVehicleModel();
                if ("1".equals(vehicleModel)){
                    tVehicleAccessRecordsVO.setVehicleModelLab("小客车");
                }else if ("2".equals(vehicleModel)){
                    tVehicleAccessRecordsVO.setVehicleModelLab("货车");
                }else if ("3".equals(vehicleModel)){
                    tVehicleAccessRecordsVO.setVehicleModelLab("罐车");
                }else {
                    tVehicleAccessRecordsVO.setVehicleModelLab("/");
                }

                String emissionStandard = tVehicleAccessRecordsVO.getEmissionStandard();
                if ("1".equals(vehicleModel)){
                    tVehicleAccessRecordsVO.setEmissionStandardLab("国Ⅰ");
                }else if ("2".equals(emissionStandard)){
                    tVehicleAccessRecordsVO.setEmissionStandardLab("国Ⅱ");
                }else if ("3".equals(emissionStandard)){
                    tVehicleAccessRecordsVO.setEmissionStandardLab("国Ⅲ");
                }else if ("4".equals(emissionStandard)){
                    tVehicleAccessRecordsVO.setEmissionStandardLab("国Ⅳ");
                }else if ("5".equals(emissionStandard)){
                    tVehicleAccessRecordsVO.setEmissionStandardLab("国Ⅴ");
                }else if ("6".equals(emissionStandard)){
                    tVehicleAccessRecordsVO.setEmissionStandardLab("国Ⅵ");
                }else {
                    tVehicleAccessRecordsVO.setEmissionStandardLab("/");
                }
            }
        }

        // 写到浏览器打开
        ExcelUtils.excelExport(TVehicleAccessRecordsVO.class, "车辆通行记录" + DateUtils.format(new Date()), null, list);
    }

    /**
      * @author Mryang
      * @description 精城临时接口，完善三个月数据，组合台账
      * @date 15:33 2024/4/26
      * @param
      * @return
      */
    @PostMapping("/jingchengMakeTaz")
    public Result<String> jingchengMakeTaz(@RequestParam("siteId") String siteId){
        tVehicleAccessRecordsService.jingchengMakeTaz(siteId);


        return Result.ok();
    }

    /**
      * @author: Mryang
      * @Description: 注释，临时接口，处理精城大范围时间内返回的数据，插入至数据库
      * @Date: 21:47 2024/4/27
      * @Param:
      * @return:
      */
    @PostMapping("/directlyInsterData")
    public Result<String> directlyInsterData(@RequestBody JSONObject jsonObjectBody) {
        JSONArray jsonArray = jsonObjectBody.getJSONArray("carRecords");
        if (CollectionUtils.isNotEmpty(jsonArray)) {
            int insterCount = 0;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObjectRecords = jsonArray.getJSONObject(i);
                // 判断数据  数据库中已经存在
                String recordsId = jsonObjectRecords.getString("records_id");
                boolean whetherItExists = tVehicleAccessRecordsService.whetherItExists(recordsId);
                if (whetherItExists){
                    // 存在
                }else {
                    log.info("开始插入车辆记录");
                    TVehicleAccessRecordsEntity tVehicleAccessRecordsEntity = new TVehicleAccessRecordsEntity();
                    tVehicleAccessRecordsEntity.setChannelId(jsonObjectRecords.getLong("channel_id"));
                    tVehicleAccessRecordsEntity.setChannelName(jsonObjectRecords.getString("channel_name"));
                    tVehicleAccessRecordsEntity.setDeviceId(jsonObjectRecords.getLong("device_id"));
                    tVehicleAccessRecordsEntity.setDeviceName(jsonObjectRecords.getString("deviceName"));

                    tVehicleAccessRecordsEntity.setManufacturerId(jsonObjectRecords.getLong("manufacturer_id"));
                    tVehicleAccessRecordsEntity.setManufacturerName(jsonObjectRecords.getString("manufacturer_name"));
                    tVehicleAccessRecordsEntity.setPlateNumber(jsonObjectRecords.getString("plateNumber"));
                    tVehicleAccessRecordsEntity.setRecordsId(jsonObjectRecords.getString("records_id"));

                    String passChannelType = jsonObjectRecords.getString("passChannelType");
                    String accessType = jsonObjectRecords.getString("access_type");
                    tVehicleAccessRecordsEntity.setAccessType(accessType);
                    tVehicleAccessRecordsEntity.setCarUrl(jsonObjectRecords.getString("car_url"));
                    tVehicleAccessRecordsEntity.setRecordTime(jsonObjectRecords.getDate("record_time"));
                    tVehicleAccessRecordsEntity.setSiteId(jsonObjectRecords.getLong("siteId"));
                    tVehicleAccessRecordsEntity.setSiteName(jsonObjectRecords.getString("siteName"));

                    /**
                     * 需要通过车牌绑定平台车辆信息数据
                     * */
                    if (ObjectUtils.isNotEmpty(jsonObjectRecords.getString("plateNumber"))){
                        JSONObject jsonObject = vehicleFeign.queryVehicleInformationByLicensePlateNumber(jsonObjectRecords.getString("plateNumber"));
                        if(ObjectUtils.isNotEmpty(jsonObject)){
                            tVehicleAccessRecordsEntity.setVehicleModel(jsonObject.getString("carType"));
                            tVehicleAccessRecordsEntity.setEmissionStandard(jsonObject.getString("emissionStandard"));
                            tVehicleAccessRecordsEntity.setDriverId(jsonObject.getLong("driverId"));
                            tVehicleAccessRecordsEntity.setDriverName(jsonObject.getString("driverName"));
                            tVehicleAccessRecordsEntity.setDriverPhone(jsonObject.getString("driverMobile"));
                            tVehicleAccessRecordsEntity.setImageUrl(jsonObject.getString("imageUrl"));
                            tVehicleAccessRecordsEntity.setLicenseImage(jsonObject.getString("licenseImage"));
                        }
                    }

                    tVehicleAccessRecordsService.save(tVehicleAccessRecordsEntity);
                    insterCount++;
                    // 存储车辆进出场展示台账
//                    log.info("通信记录存储完成，开始记录台账");
//                    tVehicleAccessRecordsService.saveLedger(tVehicleAccessRecordsEntity);

                }
            }
            return Result.ok("插入完成总条数为："+insterCount);
        }
        return Result.error("数据传入为空");
    }


    /**
      * @author Mryang
      * @description 管理员接口自动补充某个站点的 车辆记录中，没有数据的字段，比如 记录车辆通行的时候，没有车辆信息，后续补录的车辆信息，用该方法进行补录信息
      * @date 14:29 2024/5/23
      * @param
      * @return
      */
    @PostMapping("/supplementAndRecordVehicleInformation")
    public Result<String> supplementAndRecordVehicleInformation(@RequestParam("siteId") String siteId,
                                                                @RequestParam("startDateTime") String startDateTime,
                                                                @RequestParam("endDateTime") String endDateTime){
        tVehicleAccessRecordsService.supplementAndRecordVehicleInformation(siteId, startDateTime, endDateTime);


        return Result.ok();
    }

}
