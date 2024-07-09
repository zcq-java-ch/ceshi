package com.hxls.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.excel.ExcelFinishCallBack;
import com.hxls.framework.common.exception.ErrorCode;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.ExcelUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.framework.security.user.SecurityUser;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.storage.properties.StorageProperties;
import com.hxls.system.config.BaseImageUtils;
import com.hxls.system.controller.ExcelController;
import com.hxls.system.convert.TVehicleConvert;
import com.hxls.system.dao.SysUserDao;
import com.hxls.system.dao.TVehicleDao;
import com.hxls.system.entity.SysAreacodeDeviceEntity;
import com.hxls.system.entity.SysSiteAreaEntity;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.entity.TVehicleEntity;
import com.hxls.system.query.TVehicleQuery;
import com.hxls.system.service.SysAreacodeDeviceService;
import com.hxls.system.service.SysSiteAreaService;
import com.hxls.system.service.TVehicleService;
import com.hxls.system.vo.SysUserVO;
import com.hxls.system.vo.TVehicleExcelVO;
import com.hxls.system.vo.TVehicleVO;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通用车辆管理表
 *
 * @author zhaohong
 * @since 1.0.0 2024-03-15
 */
@Service
@AllArgsConstructor
public class TVehicleServiceImpl extends BaseServiceImpl<TVehicleDao, TVehicleEntity> implements TVehicleService {

    private final AppointmentFeign appointmentFeign;
    private final StorageProperties properties;
    private final SysUserDao sysUserDao;
    private final SysSiteAreaService sysSiteAreaService;
    private final SysAreacodeDeviceService sysAreacodeDeviceService;

    @Override
    public PageResult<TVehicleVO> page(TVehicleQuery query) {
        IPage<TVehicleEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));
        return new PageResult<>(TVehicleConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<TVehicleEntity> getWrapper(TVehicleQuery query) {
        LambdaQueryWrapper<TVehicleEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getSiteId() != null, TVehicleEntity::getSiteId, query.getSiteId());
        wrapper.like(StringUtils.isNotEmpty(query.getLicensePlate()), TVehicleEntity::getLicensePlate, query.getLicensePlate());
        wrapper.eq(query.getDriverId() != null, TVehicleEntity::getDriverId, query.getDriverId());
        wrapper.eq(query.getStatus() != null, TVehicleEntity::getStatus, query.getStatus());
        wrapper.eq(query.getCarClass() != null, TVehicleEntity::getCarClass, query.getCarClass());
        wrapper.eq(query.getSupplierId() != null, TVehicleEntity::getSupplierId, query.getSupplierId());
        wrapper.orderByAsc(TVehicleEntity::getId);
        //数据权限判断
        UserDetail user = SecurityUser.getUser();
        // 如果是超级管理员，则不进行数据过滤
        if( StringUtils.isNotEmpty(query.getCarClass())&&  query.getCarClass().equals("2")){
            if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)) {
                wrapper.in(TVehicleEntity::getSiteId, CollectionUtils.isEmpty(user.getDataScopeList()) ? List.of(Constant.EMPTY) : user.getDataScopeList());
            }
        }
        else if (query.getSupplierId() == null ){
            if (!user.getSuperAdmin().equals(Constant.SUPER_ADMIN)) {
                wrapper.in(TVehicleEntity::getSupplierId, CollectionUtils.isEmpty(user.getDataScopeList()) ? List.of(Constant.EMPTY) : user.getDataScopeList());
            }
        }


        return wrapper;
    }

    @Override
    public void save(TVehicleVO vo) {
        //判断车牌号有没有，车牌号只能被创建一次
        long valusCount = baseMapper.selectCount(new QueryWrapper<TVehicleEntity>()
                .eq("license_plate", vo.getLicensePlate())
                .eq("deleted", 0));
        if (valusCount > 0) {
            throw new ServerException("当前车牌号已存在，不能重复添加");
        }

        TVehicleEntity entity = TVehicleConvert.INSTANCE.convert(vo);
        entity.setUserId(entity.getDriverId());
        baseMapper.insert(entity);

        // 为空的情况是，外部人员私家车，和外部人员大车
        if (ObjectUtils.isNotEmpty(entity.getSiteId())){
            //通用车辆下发
            JSONObject vehicle = new JSONObject();
            vehicle.set("sendType","2");
            entity.setStationId(entity.getSiteId());
            vehicle.set("data" , JSONUtil.toJsonStr(entity));
            appointmentFeign.issuedPeople(vehicle);
        }

        // TODO 当单独保存的车辆数据为供应商车辆管理的时候，需要处理权限区域问题
        if ("4".equals(entity.getCarClass())) {
            if (StringUtils.isNotEmpty(vo.getAreaList())) {
                String areaList = vo.getAreaList();
                List<String> areas = JSONUtil.toList(areaList, String.class);
                List<Long> result = areas.stream().filter(item -> item.contains("A")).map(item -> {
                    return Long.parseLong(item.substring(1));
                }).toList();

                //下发
                List<SysSiteAreaEntity> sysSiteAreaEntities = sysSiteAreaService.listByIds(result);
                //获取到人脸设备
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(sysSiteAreaEntities)) {
                    //获取到设备编码
                    List<String> deviceIds = new ArrayList<>();
                    for (SysSiteAreaEntity sysSiteAreaEntity : sysSiteAreaEntities) {
                        deviceIds.add(sysSiteAreaEntity.getCarIntCode());
                        deviceIds.add(sysSiteAreaEntity.getCarOutCode());
                    }
                    List<SysAreacodeDeviceEntity> areacodeDeviceEntities = sysAreacodeDeviceService.list(new LambdaQueryWrapper<SysAreacodeDeviceEntity>().in(SysAreacodeDeviceEntity::getAreaDeviceCode, deviceIds));
                    if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(areacodeDeviceEntities)) {
                        List<Long> ids = areacodeDeviceEntities.stream().map(SysAreacodeDeviceEntity::getDeviceId).toList();
                        //下发设备
                        if (StringUtils.isNotEmpty(entity.getLicensePlate())) {
                            JSONObject vehicle = new JSONObject();
                            vehicle.set("sendType", "2");
                            vehicle.set("data", JSONUtil.toJsonStr(entity));
                            vehicle.set("ids", JSONUtil.toJsonStr(ids));
                            appointmentFeign.issuedPeople(vehicle);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void update(TVehicleVO vo) {
        // 判断车牌号是否存在
        TVehicleEntity byLicensePlate = baseMapper.getByLicensePlate(vo.getLicensePlate());
        if (byLicensePlate != null && !byLicensePlate.getId().equals(vo.getId())) {
            throw new ServerException("当前车牌号已存在，不能重复添加");
        }

        TVehicleEntity entity = TVehicleConvert.INSTANCE.convert(vo);

        //修改之前要判断是否更换了厂站
        Long id = vo.getId();
        TVehicleEntity byId = getById(id);
        if (ObjectUtil.isNull(byId)) {
            throw new ServerException(ErrorCode.NOT_FOUND);
        }
        updateById(entity);

        // 为空的情况是，外部人员私家车，和外部人员大车
//        if (ObjectUtils.isNotEmpty(entity.getSiteId())) {
//            //原厂站的id
//            Long siteId = byId.getSiteId();
//
//            if (!siteId.equals(vo.getSiteId())) {
//                //删除原厂站的信息
//                JSONObject vehicle = new JSONObject();
//                vehicle.set("sendType", "2");
//                entity.setStationId(entity.getSiteId());
//                vehicle.set("data", JSONUtil.toJsonStr(entity));
//                vehicle.set("DELETE", "DELETE");
//                appointmentFeign.issuedPeople(vehicle);
//            }
//
//
//            JSONObject vehicle = new JSONObject();
//            vehicle.set("sendType", "2");
//            entity.setStationId(entity.getSiteId());
//            vehicle.set("data", JSONUtil.toJsonStr(entity));
//            appointmentFeign.issuedPeople(vehicle);
//        }

        // TODO 当单独修改的车辆数据为供应商车辆管理的时候，需要处理权限区域问题
        if ("4".equals(entity.getCarClass())) {
            if (StringUtils.isNotEmpty(vo.getAreaList())) {
                String areaList = vo.getAreaList();
                List<String> areas = JSONUtil.toList(areaList, String.class);
                List<Long> result = areas.stream().filter(item -> item.contains("A")).map(item -> {
                    return Long.parseLong(item.substring(1));
                }).toList();
                //删除老站点ID
                if (StringUtils.isNotEmpty(byId.getLicensePlate())) {
                    JSONObject vehicle = new JSONObject();
                    vehicle.set("sendType", "2");
                    vehicle.set("data", JSONUtil.toJsonStr(byId));
                    vehicle.set("DELETE", "DELETE");
                    appointmentFeign.issuedPeople(vehicle);
                }

                if (CollectionUtils.isNotEmpty(result)){
                    //下发
                    List<SysSiteAreaEntity> sysSiteAreaEntities = sysSiteAreaService.listByIds(result);
                    //获取到人脸设备
                    if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(sysSiteAreaEntities)) {
                        //获取到设备编码
                        List<String> deviceIds = new ArrayList<>();
                        for (SysSiteAreaEntity sysSiteAreaEntity : sysSiteAreaEntities) {
                            deviceIds.add(sysSiteAreaEntity.getCarIntCode());
                            deviceIds.add(sysSiteAreaEntity.getCarOutCode());
                        }
                        List<SysAreacodeDeviceEntity> areacodeDeviceEntities = sysAreacodeDeviceService.list(new LambdaQueryWrapper<SysAreacodeDeviceEntity>().in(SysAreacodeDeviceEntity::getAreaDeviceCode, deviceIds));
                        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(areacodeDeviceEntities)) {
                            List<Long> ids = areacodeDeviceEntities.stream().map(SysAreacodeDeviceEntity::getDeviceId).toList();
                            //下发设备
                            if (StringUtils.isNotEmpty(entity.getLicensePlate())) {
                                JSONObject vehicle = new JSONObject();
                                vehicle.set("sendType", "2");
                                vehicle.set("data", JSONUtil.toJsonStr(entity));
                                vehicle.set("ids", JSONUtil.toJsonStr(ids));
                                appointmentFeign.issuedPeople(vehicle);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        List<TVehicleEntity> tVehicleEntities = listByIds(idList);

        for (TVehicleEntity entity : tVehicleEntities) {

            //删除原厂站的信息
            JSONObject vehicle = new JSONObject();
            vehicle.set("sendType", "2");
            entity.setStationId(entity.getSiteId());
            vehicle.set("data", JSONUtil.toJsonStr(entity));
            vehicle.set("DELETE", "DELETE");
            appointmentFeign.issuedPeople(vehicle);
        }


        removeByIds(idList);


    }

    /**
     * 通过车牌号，查询车辆基本信息
     *
     * @param data 入参车牌号
     * @return 返回车辆信息
     */
    @Override
    public List<TVehicleVO> getByLicensePlates(List<String> data) {
        List<TVehicleEntity> list = this.list(new LambdaQueryWrapper<TVehicleEntity>().in(TVehicleEntity::getLicensePlate, data));
        return TVehicleConvert.INSTANCE.convertList(list);
    }

    /**
     * 通过车牌号去设置绑定与解绑
     *
     * @param licensePlates 车牌号
     * @param userId        登陆人员id
     * @param type
     */
    @Override
    public void setByLicensePlates(String licensePlates, Long userId, Integer type) {

        List<TVehicleEntity> list = list(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getLicensePlate, licensePlates));
        if (CollectionUtils.isEmpty(list)) {
            throw new ServerException(ErrorCode.NOT_FOUND.getMsg());
        }

        if (type < 1) {
            //修改默认司机
            list.forEach(item -> {
                item.setDriverId(Constant.EMPTY);
                item.setUserId(Constant.EMPTY);
                item.setDriverMobile("未绑定");
                item.setDriverName("未绑定");
            });
        } else {
            //修改默认司机
            SysUserEntity byId = sysUserDao.getById(userId);
            list.forEach(item -> {
                item.setDriverId(userId);
                item.setUserId(userId);
                item.setDriverMobile(byId.getMobile());
                item.setDriverName(byId.getRealName());
            });
        }
        updateBatchById(list);
    }

    /**
     * 通过车牌号去设置绑定与解绑
     *
     * @param licensePlates 车牌号
     * @param userId        登陆人员id
     */
    @Override
    public String getVehicleByLicensePlates(String licensePlates, Long userId) {
        TVehicleEntity one = getOne(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getLicensePlate, licensePlates)
                .eq(TVehicleEntity::getDriverId, userId));
        if (ObjectUtil.isNull(one)) {
            return "绑定车辆";
        }
        return "解绑车辆";
    }

    @Override
    public void updateStatus(List<TVehicleVO> list) {
        for (TVehicleVO vo : list) {
            TVehicleEntity byId = getById(vo.getId());
            TVehicleEntity entity = new TVehicleEntity();
            entity.setId(vo.getId());
            if (vo.getStatus() != null) {
                entity.setStatus(vo.getStatus());
            }
            // 更新实体
            this.updateById(entity);
            //删除车辆在设备上的信息
            JSONObject vehicle = new JSONObject();
            vehicle.set("sendType", "2");
            entity.setLicensePlate(byId.getLicensePlate());
            entity.setStationId(byId.getSiteId());
            vehicle.set("data", JSONUtil.toJsonStr(entity));
            if (vo.getStatus().equals(Constant.DISABLE)) {
                vehicle.set("DELETE", "DELETE");
            }
            appointmentFeign.issuedPeople(vehicle);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importByExcel(String file, Long siteId) {
        try {
            //导入时候获取的地址是相对路径 需要拼接服务器路径
            String domain = properties.getConfig().getDomain();

            ExcelUtils.readAnalysis(ExcelUtils.convertToMultipartFile(domain + file), TVehicleExcelVO.class, new ExcelFinishCallBack<TVehicleExcelVO>() {
                @Override
                public void doAfterAllAnalysed(List<TVehicleExcelVO> result) {
                    saveTVehicle(result);
                }

                @Override
                public void doSaveBatch(List<TVehicleExcelVO> result) {
                    saveTVehicle(result);
                }

                private void saveTVehicle(List<TVehicleExcelVO> result) {
                    ExcelUtils.parseDict(result);
                    List<TVehicleEntity> tVehicleEntities = TVehicleConvert.INSTANCE.convertListEntity(result);
                    tVehicleEntities.forEach(tVehicle -> {
                        //判断车牌号有没有，车牌号只能被创建一次
                        long valusCount = baseMapper.selectCount(new QueryWrapper<TVehicleEntity>()
                                .eq("license_plate", tVehicle.getLicensePlate())
                                .eq("deleted", 0));
                        if (valusCount > 0) {
                            throw new ServerException("车辆" + tVehicle.getLicensePlate() + "已存在，不能重复添加");
                        }
                        tVehicle.setSiteId(siteId);
                        tVehicle.setStatus(1);
                        //下发车辆
                        JSONObject vehicle = new JSONObject();
                        vehicle.set("sendType", "2");
                        tVehicle.setStationId(siteId);
                        vehicle.set("data", JSONUtil.toJsonStr(tVehicle));
                        appointmentFeign.issuedPeople(vehicle);
                    });
                    saveBatch(tVehicleEntities);
                }
            });

        } catch (Exception e) {
            throw new ServerException("导入数据不正确");
        }
    }

    @Override
    public void setLicensePlates(SysUserVO byMobile, String licensePlate) {

        TVehicleEntity one = getOne(new LambdaQueryWrapper<TVehicleEntity>().eq(TVehicleEntity::getLicensePlate, licensePlate));
        if (ObjectUtil.isNull(one)) {
            throw new ServerException(ErrorCode.NOT_FOUND.getMsg());
        }
        //修改默认司机
        one.setDriverId(byMobile.getId());
        one.setDriverName(byMobile.getRealName());
        one.setDriverMobile(byMobile.getMobile());

        updateById(one);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importByExcelWithPictures(String excelUrl, Long siteId) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        //导入时候获取的地址是相对路径 需要拼接服务器路径
        String domain = properties.getConfig().getDomain();
        String allUrl = domain + excelUrl;

        // 初始化图片容器
        HashMap<ExcelController.PicturePosition, String> pictureMap = new HashMap<>();

        disableSslVerification();
        // 下载Excel文件到本地临时文件
        File tempFile = downloadFile(allUrl);

        try (InputStream inputStream = new FileInputStream(tempFile)) {
            Workbook workbook;
            String fileFormat = allUrl.substring(allUrl.lastIndexOf('.') + 1);
            try {
                if (ExcelController.ExcelFormatEnum.XLS.getValue().equalsIgnoreCase(fileFormat)) {
                    workbook = new HSSFWorkbook(inputStream);
                } else if (ExcelController.ExcelFormatEnum.XLSX.getValue().equalsIgnoreCase(fileFormat)) {
                    workbook = new XSSFWorkbook(inputStream);
                } else {
                    throw new ServerException("Unsupported file format.");
                }

                //读取excel所有图片
                if (ExcelController.ExcelFormatEnum.XLS.getValue().equals(fileFormat)) {
                    getPicturesXLS(workbook, pictureMap);
                } else {
                    getPicturesXLSX(workbook, pictureMap);
                }

                List<TVehicleExcelVO> transferList = new ArrayList<>();


                Sheet sheet = workbook.getSheetAt(0);
                int rows = sheet.getLastRowNum();
                for (int i = 1; i <= rows; i++) {
                    Row row = sheet.getRow(i);
                    TVehicleExcelVO tVehicleExcelVO = new TVehicleExcelVO();
                    if (row.getCell(0) != null) {
                        tVehicleExcelVO.setLicensePlate(this.getCellValue(row.getCell(0)));
                    }
                    if (row.getCell(1) != null) {
                        tVehicleExcelVO.setCarTypeName(this.getCellValue(row.getCell(1)));
                    }
                    if (row.getCell(2) != null) {
                        tVehicleExcelVO.setEmissionStandardName(this.getCellValue(row.getCell(2)));
                    }
                    if (row.getCell(3) != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String cellValue = this.getCellValue(row.getCell(3));
                        try {
                            tVehicleExcelVO.setRegistrationDate(dateFormat.parse(cellValue));
                        } catch (ParseException e) {
//                            throw new RuntimeException(e);
                            tVehicleExcelVO.setRegistrationDate(new Date());
                        }
                    }
                    if (row.getCell(4) != null) {
                        tVehicleExcelVO.setVinNumber(this.getCellValue(row.getCell(4)));
                    }
                    if (row.getCell(5) != null) {
                        tVehicleExcelVO.setEngineNumber(this.getCellValue(row.getCell(5)));
                    }
                    if (row.getCell(6) != null) {
                        tVehicleExcelVO.setFleetName(this.getCellValue(row.getCell(6)));
                    }
                    if (row.getCell(7) != null) {
                        tVehicleExcelVO.setMaxCapacity(this.getCellValue(row.getCell(7)));
                    }
                    if (row.getCell(8) != null) {
                        tVehicleExcelVO.setLicenseImage(String.valueOf(pictureMap.get(ExcelController.PicturePosition.newInstance(i, 8))));
                    }
                    if (row.getCell(9) != null) {
                        tVehicleExcelVO.setImageUrl(String.valueOf(pictureMap.get(ExcelController.PicturePosition.newInstance(i, 9))));
                    }
                    if (row.getCell(10) != null) {
                        tVehicleExcelVO.setImages(String.valueOf(pictureMap.get(ExcelController.PicturePosition.newInstance(i, 10))));
                    }
                    if (row.getCell(11) != null) {
                        tVehicleExcelVO.setDriverMobile(this.getCellValue(row.getCell(11)));
                    }

                    transferList.add(tVehicleExcelVO);
                }

                // 执行数据处理逻辑
                ExcelUtils.parseDict(transferList);
                List<TVehicleEntity> tVehicleEntities = TVehicleConvert.INSTANCE.convertListEntity(transferList);
                tVehicleEntities.forEach(tVehicle -> {
                    // 数据规则校验
                    checkData(tVehicle);

                    //判断车牌号有没有，车牌号只能被创建一次
                    long valusCount = baseMapper.selectCount(new QueryWrapper<TVehicleEntity>()
                            .eq("license_plate", tVehicle.getLicensePlate())
                            .eq("deleted", 0));
                    if (valusCount > 0) {
                        throw new ServerException("车辆" + tVehicle.getLicensePlate() + "已存在，不能重复添加");
                    }
                    tVehicle.setSiteId(siteId);
                    tVehicle.setStatus(1);
                    tVehicle.setCarClass("4");
                    //查询车辆驾驶员信息
                    String driverMobile = tVehicle.getDriverMobile();
                    if (StrUtil.isNotEmpty(driverMobile)) {
                        List<SysUserEntity> sysUserEntities = sysUserDao.selectList(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getMobile, driverMobile));
                        if (CollectionUtils.isNotEmpty(sysUserEntities)) {
                            tVehicle.setUserId(sysUserEntities.get(0).getId());
                            tVehicle.setDriverId(sysUserEntities.get(0).getId());
                            tVehicle.setDriverName(sysUserEntities.get(0).getRealName());
                        }
                    }
                    //下发车辆
                    JSONObject vehicle = new JSONObject();
                    vehicle.set("sendType", "2");
                    tVehicle.setStationId(siteId);
                    vehicle.set("data", JSONUtil.toJsonStr(tVehicle));
                    appointmentFeign.issuedPeople(vehicle);

                });

                saveBatch(tVehicleEntities);
//                return Result.ok();
            } finally {
                // 清理临时文件
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } catch (IOException e) {
            throw new ServerException("Error reading Excel from URL.");
        }
    }

    private void checkData(TVehicleEntity tVehicle) {
        String driverMobile1 = tVehicle.getDriverMobile();
        if (StringUtils.isEmpty(driverMobile1)) {
            throw new ServerException("司机手机号不能为空，请调整数据");
        }

        // 手机号11位判断
        int phoneminLength = 11; // 一般手机号长度
        if (phoneminLength != driverMobile1.length()) {
            throw new ServerException("手机号长度不对，请调整数据");
        }

        String licensePlate = tVehicle.getLicensePlate();
        // 车牌号不能为空
        if (StringUtils.isEmpty(licensePlate)) {
            throw new ServerException("车牌号不能为空，请调整数据");
        }

        // 车牌号去空格
        tVehicle.setLicensePlate(licensePlate.trim());

        int minLength = 6; // 一般车牌号的最小长度
        int maxLength = 10; // 一般车牌号的最大长度
        // 验证长度是否在指定范围内
        int length = licensePlate.length();
        if (length >= minLength && length <= maxLength) {
//                        System.out.println("车牌号长度符合要求。");
        } else {
            throw new ServerException("车辆" + tVehicle.getLicensePlate() + "车牌号不符合长度，不能添加");
        }

        // 车辆类型只能是 2 3 大车
        if (!"2".equals(tVehicle.getCarType()) && !"3".equals(tVehicle.getCarType())) {
            throw new ServerException("车辆" + tVehicle.getLicensePlate() + "车辆类型只能是[货车]或者[罐车]");
        }

        try {
            // 尝试将字符串转换为BigDecimal
            BigDecimal maxCapacity = new BigDecimal(tVehicle.getMaxCapacity());
            // 如果转换成功，则表示字符串是数字类型的
            // 在这里你可以继续使用maxCapacity进行其他操作
        } catch (NumberFormatException e) {
            // 如果转换失败，说明字符串不是数字类型的
            // 在这里你可以处理该情况，例如给出一个错误提示或采取其他适当的措施
            throw new ServerException("车辆" + tVehicle.getLicensePlate() + "最大容量不是一个有效的数字。");
        }

        // 如果运输量不为空的时候  需要校验他的输入类型
        if (StringUtils.isNotEmpty(tVehicle.getTransportVolume())) {
            try {
                BigDecimal transportVolume = new BigDecimal(tVehicle.getTransportVolume());
            } catch (NumberFormatException e) {
                throw new ServerException("车辆" + tVehicle.getLicensePlate() + "运输量不是一个有效的数字。");
            }
        }
    }


    // 在下载文件方法之前添加此代码
    public static void disableSslVerification() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    private File downloadFile(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // Always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String fileName = "";
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10, disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1, fileUrl.length());
            }

            InputStream inputStream = httpConn.getInputStream();
            File tempFile = File.createTempFile(fileName, ".tmp");
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } else {
            throw new IOException("No file to download. Server replied HTTP code: " + responseCode);
        }
    }

    /**
     * 获取Excel2003的图片
     *
     * @param workbook
     */
    private static void getPicturesXLS(Workbook workbook, HashMap<ExcelController.PicturePosition, String> pictureMap) {
        List<HSSFPictureData> pictures = (List<HSSFPictureData>) workbook.getAllPictures();
        HSSFSheet sheet = (HSSFSheet) workbook.getSheetAt(0);
        if (pictures.size() != 0) {
            for (HSSFShape shape : sheet.getDrawingPatriarch().getChildren()) {
                HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
                if (shape instanceof HSSFPicture) {
                    HSSFPicture pic = (HSSFPicture) shape;
                    int pictureIndex = pic.getPictureIndex() - 1;
                    HSSFPictureData picData = pictures.get(pictureIndex);
                    ExcelController.PicturePosition picturePosition = ExcelController.PicturePosition.newInstance(anchor.getRow1(), anchor.getCol1());
                    pictureMap.put(picturePosition, printImg(picData));
                }
            }
        }
    }

    /**
     * 获取Excel2007的图片
     *
     * @param workbook
     */
    private static void getPicturesXLSX(Workbook workbook, HashMap<ExcelController.PicturePosition, String> pictureMap) {
        XSSFSheet xssfSheet = (XSSFSheet) workbook.getSheetAt(0);
        for (POIXMLDocumentPart dr : xssfSheet.getRelations()) {
            if (dr instanceof XSSFDrawing) {
                XSSFDrawing drawing = (XSSFDrawing) dr;
                List<XSSFShape> shapes = drawing.getShapes()
                        .stream().filter(s -> s instanceof XSSFPicture).collect(Collectors.toList());
                for (XSSFShape shape : shapes) {
                    XSSFPicture pic = (XSSFPicture) shape;
                    try {
                        XSSFClientAnchor anchor = pic.getPreferredSize();
                        if (anchor != null) {
                            CTMarker ctMarker = anchor.getFrom();
                            ExcelController.PicturePosition picturePosition = ExcelController.PicturePosition.newInstance(ctMarker.getRow(), ctMarker.getCol());
                            pictureMap.put(picturePosition, printImg(pic.getPictureData()));
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        }
    }

    /**
     * 保存图片并返回存储地址
     *
     * @param pic
     * @return
     */
    public static String printImg(PictureData pic) {
        try {
            String filePath = UUID.randomUUID().toString() + "." + pic.suggestFileExtension();
            byte[] data = pic.getData();
            // 将二进制数据转换为 Base64 格式
            String base64String = Base64.getEncoder().encodeToString(data);
            String faceUrl = "";
            faceUrl = BaseImageUtils.base64ToUrl(base64String, "VEHICLE/IMAGES", "autoimport");
            return faceUrl;
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * cell数据格式转换
     *
     * @param cell
     * @return
     */
    private static String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case NUMERIC: // 数字
                //如果为时间格式的内容
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    //注：format格式 yyyy-MM-dd hh:mm:ss 中小时为12小时制，若要24小时制，则把小h变为H即可，yyyy-MM-dd HH:mm:ss
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    return sdf.format(HSSFDateUtil.getJavaDate(cell.
                            getNumericCellValue()));
                } else {
                    return new DecimalFormat("0").format(cell.getNumericCellValue());
                }
            case STRING: // 字符串
                return cell.getStringCellValue();
            case BOOLEAN: // Boolean
                return cell.getBooleanCellValue() + "";
            case FORMULA: // 公式
                return cell.getCellFormula() + "";
            case BLANK: // 空值
                return "";
            case ERROR: // 故障
                return null;
            default:
                return null;
        }
    }

    @Override
    public void importGysWithPictures(String excelUrl, Long supplierId, String supplierName) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        //导入时候获取的地址是相对路径 需要拼接服务器路径
        String domain = properties.getConfig().getDomain();
        String allUrl = domain + excelUrl;

        // 初始化图片容器
        HashMap<ExcelController.PicturePosition, String> pictureMap = new HashMap<>();

        disableSslVerification();
        // 下载Excel文件到本地临时文件
        File tempFile = downloadFile(allUrl);

        try (InputStream inputStream = new FileInputStream(tempFile)) {
            Workbook workbook;
            String fileFormat = allUrl.substring(allUrl.lastIndexOf('.') + 1);
            try {
                if (ExcelController.ExcelFormatEnum.XLS.getValue().equalsIgnoreCase(fileFormat)) {
                    workbook = new HSSFWorkbook(inputStream);
                } else if (ExcelController.ExcelFormatEnum.XLSX.getValue().equalsIgnoreCase(fileFormat)) {
                    workbook = new XSSFWorkbook(inputStream);
                } else {
                    throw new ServerException("Unsupported file format.");
                }

                //读取excel所有图片
                if (ExcelController.ExcelFormatEnum.XLS.getValue().equals(fileFormat)) {
                    getPicturesXLS(workbook, pictureMap);
                } else {
                    getPicturesXLSX(workbook, pictureMap);
                }

                List<TVehicleExcelVO> transferList = new ArrayList<>();


                Sheet sheet = workbook.getSheetAt(0);
                int rows = sheet.getLastRowNum();
                for (int i = 1; i <= rows; i++) {
                    Row row = sheet.getRow(i);
                    TVehicleExcelVO tVehicleExcelVO = new TVehicleExcelVO();
                    if (row.getCell(0) != null) {
                        tVehicleExcelVO.setLicensePlate(this.getCellValue(row.getCell(0)));
                    }
                    if (row.getCell(1) != null) {
                        tVehicleExcelVO.setCarTypeName(this.getCellValue(row.getCell(1)));
                    }
                    if (row.getCell(2) != null) {
                        tVehicleExcelVO.setEmissionStandardName(this.getCellValue(row.getCell(2)));
                    }
                    if (row.getCell(3) != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String cellValue = this.getCellValue(row.getCell(3));
                        try {
                            tVehicleExcelVO.setRegistrationDate(dateFormat.parse(cellValue));
                        } catch (ParseException e) {
//                            throw new RuntimeException(e);
                            tVehicleExcelVO.setRegistrationDate(new Date());
                        }
                    }
                    if (row.getCell(4) != null) {
                        tVehicleExcelVO.setVinNumber(this.getCellValue(row.getCell(4)));
                    }
                    if (row.getCell(5) != null) {
                        tVehicleExcelVO.setEngineNumber(this.getCellValue(row.getCell(5)));
                    }
                    if (row.getCell(6) != null) {
                        tVehicleExcelVO.setFleetName(this.getCellValue(row.getCell(6)));
                    }
                    if (row.getCell(7) != null) {
                        tVehicleExcelVO.setMaxCapacity(this.getCellValue(row.getCell(7)));
                    }
                    if (row.getCell(8) != null) {
                        tVehicleExcelVO.setLicenseImage(String.valueOf(pictureMap.get(ExcelController.PicturePosition.newInstance(i, 8))));
                    }
                    if (row.getCell(9) != null) {
                        tVehicleExcelVO.setImageUrl(String.valueOf(pictureMap.get(ExcelController.PicturePosition.newInstance(i, 9))));
                    }
                    if (row.getCell(10) != null) {
                        tVehicleExcelVO.setImages(String.valueOf(pictureMap.get(ExcelController.PicturePosition.newInstance(i, 10))));
                    }
                    if (row.getCell(11) != null) {
                        tVehicleExcelVO.setDriverMobile(this.getCellValue(row.getCell(11)));
                    }
                    if (row.getCell(12) != null) {
                        tVehicleExcelVO.setTransportVolume(this.getCellValue(row.getCell(12)));
                    }
                    if (row.getCell(13) != null) {
                        tVehicleExcelVO.setTransportGoods(this.getCellValue(row.getCell(13)));
                    }
                    transferList.add(tVehicleExcelVO);
                }

                // 执行数据处理逻辑
                ExcelUtils.parseDict(transferList);
                List<TVehicleEntity> tVehicleEntities = TVehicleConvert.INSTANCE.convertListEntity(transferList);
                tVehicleEntities.forEach(tVehicle -> {
                    // 数据规则校验
                    checkData(tVehicle);

                    //判断车牌号有没有，车牌号只能被创建一次
                    long valusCount = baseMapper.selectCount(new QueryWrapper<TVehicleEntity>()
                            .eq("license_plate", tVehicle.getLicensePlate())
                            .eq("deleted", 0));
                    if (valusCount > 0) {
                        throw new ServerException("车辆" + tVehicle.getLicensePlate() + "已存在，不能重复添加");
                    }
                    tVehicle.setSupplierId(supplierId);
                    tVehicle.setSupplierName(supplierName);
                    tVehicle.setStatus(1);
                    tVehicle.setCarClass("4");
                    //查询车辆驾驶员信息
                    String driverMobile = tVehicle.getDriverMobile();
                    if (StrUtil.isNotEmpty(driverMobile)) {
                        List<SysUserEntity> sysUserEntities = sysUserDao.selectList(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getMobile, driverMobile));
                        if (CollectionUtils.isNotEmpty(sysUserEntities)) {
                            tVehicle.setUserId(sysUserEntities.get(0).getId());
                            tVehicle.setDriverId(sysUserEntities.get(0).getId());
                            tVehicle.setDriverName(sysUserEntities.get(0).getRealName());
                        }
                    }
                });

                saveBatch(tVehicleEntities);
//                return Result.ok();
            } finally {
                // 清理临时文件
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } catch (IOException e) {
            throw new ServerException("Error reading Excel from URL.");
        }
    }

    @Override
    public void updateStationIdList(List<TVehicleVO> list) {

        for (TVehicleVO vo : list) {
//            Boolean issued = false;
            TVehicleEntity entity = TVehicleConvert.INSTANCE.convert(vo);
            //查询车辆  --
            Long id = vo.getId();
            //查询车辆详情
            TVehicleEntity byId = getById(id);
            //判断是否更换区域,删除之前得老区域，下发新站点和区域

            if (StringUtils.isNotEmpty(byId.getAreaList())&&StringUtils.isNotEmpty(vo.getAreaList()) && !byId.getAreaList().equals(vo.getAreaList())){
                String areaList = byId.getAreaList();
                List<String> areas = JSONUtil.toList(areaList, String.class);
                List<Long> result = areas.stream().filter(item -> item.contains("S")).map(item -> {
                    return Long.parseLong(item.substring(1));
                }).toList();
                if (CollectionUtils.isNotEmpty(result)){
                    for (Long stationId : result) {
                        //删除老站点ID
                        byId.setStationId(stationId);
                        JSONObject vehicle = new JSONObject();
                        vehicle.set("sendType", "2");
                        vehicle.set("data", JSONUtil.toJsonStr(byId));
                        vehicle.set("DELETE", "DELETE");
                        appointmentFeign.issuedPeople(vehicle);
                    }
                }
            }
            // 更新实体
            this.updateById(entity);

            if (StringUtils.isNotEmpty(vo.getAreaList())) {
                String areaList = vo.getAreaList();
                List<String> areas = JSONUtil.toList(areaList, String.class);
                List<Long> result = areas.stream().filter(item -> item.contains("A")).map(item -> {
                    return Long.parseLong(item.substring(1));
                }).collect(Collectors.toList());
                //下发
                if (CollectionUtils.isEmpty(result)){
                    throw new ServerException("所选厂站下没有划分区域");
                }
                List<SysSiteAreaEntity> sysSiteAreaEntities = sysSiteAreaService.listByIds(result);
                //获取到车辆设备
                if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(sysSiteAreaEntities)) {
                    //获取到设备编码
                    List<String> deviceIds = new ArrayList<>();
                    for (SysSiteAreaEntity sysSiteAreaEntity : sysSiteAreaEntities) {
                        deviceIds.add(sysSiteAreaEntity.getCarIntCode());
                        deviceIds.add(sysSiteAreaEntity.getCarOutCode());
                    }
                    List<SysAreacodeDeviceEntity> areacodeDeviceEntities = sysAreacodeDeviceService.list(new LambdaQueryWrapper<SysAreacodeDeviceEntity>().in(SysAreacodeDeviceEntity::getAreaDeviceCode, deviceIds));
                    if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(areacodeDeviceEntities)) {
                        List<Long> ids = areacodeDeviceEntities.stream().map(SysAreacodeDeviceEntity::getDeviceId).toList();
                        //下发设备
                        if (StringUtils.isNotEmpty(entity.getLicensePlate())) {
                            JSONObject vehicle = new JSONObject();
                            vehicle.set("sendType", "2");
                            vehicle.set("data", JSONUtil.toJsonStr(entity));
                            vehicle.set("ids", JSONUtil.toJsonStr(ids));
                            appointmentFeign.issuedPeople(vehicle);
                        }
                    }
                }
            }
        }
    }
}
