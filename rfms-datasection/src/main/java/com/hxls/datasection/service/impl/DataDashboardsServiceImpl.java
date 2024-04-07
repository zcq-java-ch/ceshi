package com.hxls.datasection.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.datasection.convert.TPersonAccessRecordsConvert;
import com.hxls.datasection.dao.TPersonAccessRecordsDao;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.query.TPersonAccessRecordsQuery;
import com.hxls.datasection.service.DataDashboardsService;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.vo.TPersonAccessRecordsVO;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DataDashboardsServiceImpl implements DataDashboardsService {

    /**
     * 人员信息部分
     * */
    @Override
    public JSONObject personnelInformationSection(Long stationId) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("numberOfPeopleRegistered", "10"); // 在册人数
        jsonObject.put("inTheRegisteredFactory", "10"); // 在册厂内
        jsonObject.put("outsideTheRegisteredFactory", "10"); // 在册厂外
        jsonObject.put("numberOfPeopleReadyToMix", "10"); // 预拌人数
        jsonObject.put("preMadeNumberOfPeople", "10"); // 预制人数
        jsonObject.put("realTimeTotalNumberOfPeople", "10"); // 实时总人数
        jsonObject.put("companyPersonnel", "10"); // 公司人员
        jsonObject.put("residency", "10"); // 派驻人员
        jsonObject.put("externalAppointments", "10"); // 外部预约

        JSONObject jsonObjectType = new JSONObject();
        jsonObjectType.put("钢筋工", "10"); // 钢筋工
        jsonObjectType.put("xx工", "10"); // xx工

        jsonObject.put("jobs", jsonObjectType); // 工种人员数量集合

        return jsonObject;
    }
    /**
     * 车辆信息部分
     * */
    @Override
    public JSONObject vehicleInformationSection(Long stationId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("totalNumberOfRegisteredVehicles", "10"); // 在册车辆总数
        jsonObject.put("realTimeTotals", "10"); // 实时总数
        jsonObject.put("numberOfTrolleys", "10"); // 小车数量
        jsonObject.put("theNumberOfShipments", "10"); // 货运数量
        return jsonObject;
    }
    /**
     * 站点人员明细部分
     * */
    @Override
    public JSONObject sitePersonnelBreakdownSection(Long stationId) {
        JSONArray objects = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("serialNumber", "1"); // 序号
        jsonObject1.put("name", "1"); // 姓名
        jsonObject1.put("firm", "1"); // 公司
        jsonObject1.put("post", "1"); // 岗位
        jsonObject1.put("region", "1"); // 所在区域

        objects.add(jsonObject1);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sitePersonnelDetails", objects); // 站点人员明细
        return jsonObject;
    }
    /**
     * 车辆出入明细部分
     * */
    @Override
    public JSONObject vehicleAccessDetails(Long stationId) {
        JSONArray objects = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("serialNumber", "1"); // 序号
        jsonObject1.put("licensePlateNumber", "1"); // 车牌号
        jsonObject1.put("driver", "1"); // 司机
        jsonObject1.put("models", "1"); // 车型
        jsonObject1.put("emissionStandards", "1"); // 排放标准
        jsonObject1.put("time", "1"); // 时间
        jsonObject1.put("typeOfEntryAndExit", "1"); // 进出类型

        objects.add(jsonObject1);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("vehicleEntryAndExitDetails", objects); // 车辆出入明细
        return jsonObject;
    }
    /**
     * 外部预约人员明细部分
     * */
    @Override
    public JSONObject breakdownOfExternalAppointments(Long stationId) {
        JSONArray objects = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("serialNumber", "1"); // 序号
        jsonObject1.put("thePersonWhoMadeTheReservation", "1"); // 预约人
        jsonObject1.put("totalNumberOfPeople", "1"); // 总人数
        jsonObject1.put("firm", "1"); // 公司
        jsonObject1.put("reasonForEnteringTheFactory", "1"); // 入厂事由

        objects.add(jsonObject1);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("detailsOfExternalReservationStaff", objects); // 外部预约员明细
        return jsonObject;
    }

    /**
     * 基本信息部分
     * */
    @Override
    public JSONObject basicInformationSection() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("numberOfSites", "10"); // 站点数量
        jsonObject.put("vehicularAccess", "10"); // 车辆通道
        jsonObject.put("personnelAccess", "10"); // 人员通道
        jsonObject.put("licensePlateRecognitionOnline", "10"); // 车牌识别（在线）
        jsonObject.put("licensePlateRecognitionOffline", "10"); // 车牌识别（离线）
        jsonObject.put("faceRecognitionOnline", "10"); // 人脸识别（在线）
        jsonObject.put("faceRecognitionOffline", "10"); // 人脸识别（离线）

        return jsonObject;
    }

    /**
     * 实名制信息部分
     * */
    @Override
    public JSONObject realNameInformationSection() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("numberOfFactoryStation", "10"); // 厂站实时总人数
        jsonObject.put("numberOfPeopleReadyToMix", "10"); // 预拌人数
        jsonObject.put("preMadeNumberOfPeople", "10"); // 预制人数
        jsonObject.put("numberOfPipePiles", "10"); // 管桩人数
        jsonObject.put("numberOfResources", "10"); // 资源人数
        jsonObject.put("numberOfPeopleInTheSupplyChain", "10"); // 供应链人数
        jsonObject.put("numberOfEngineers", "10"); // 工程人数
        jsonObject.put("numberOfCarStation", "10"); // 厂站实时车辆总数
        jsonObject.put("numberOfPassengerCars", "10"); // 小客车数量
        jsonObject.put("numberOfFreightTrucks", "10"); // 货运车数量
        jsonObject.put("numberOfResidents", "10"); // 派驻人数
        jsonObject.put("numberOfExternalAppointments", "10"); // 外部预约人数
        return jsonObject;
    }

    /**
     * 地图部分
     * */
    @Override
    public JSONObject mapSection() {

        JSONArray locationArrays = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("siteName", "崇州站"); // 站点名称
        jsonObject.put("longitudeOfTheSite", "10"); // 站点经度
        jsonObject.put("siteLatitude", "10"); // 站点纬度
        jsonObject.put("numberOfPeopleAtTheSite", "10"); // 站点人数
        jsonObject.put("numberOfStops", "10"); // 站点车数
        locationArrays.add(jsonObject);

        JSONObject jsonObjectReturn = new JSONObject();
        jsonObjectReturn.put("sitelocation", locationArrays);
        return jsonObjectReturn;
    }
}