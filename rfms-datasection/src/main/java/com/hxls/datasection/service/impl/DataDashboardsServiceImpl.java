package com.hxls.datasection.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.api.feign.system.StationFeign;
import com.hxls.api.feign.system.UserFeign;
import com.hxls.api.feign.system.VehicleFeign;
import com.hxls.datasection.service.DataDashboardsService;
import com.hxls.datasection.service.TPersonAccessRecordsService;
import com.hxls.datasection.service.TVehicleAccessRecordsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * @author admin
 */
@Service
@AllArgsConstructor
public class DataDashboardsServiceImpl implements DataDashboardsService {

    private final UserFeign userFeign;
    private final VehicleFeign vehicleFeign;
    private final StationFeign stationFeign;
    private final TPersonAccessRecordsService tPersonAccessRecordsService;
    private final AppointmentFeign appointmentFeign;
    private final TVehicleAccessRecordsService tVehicleAccessRecordsService;
    // 序号
    private static final String SERIAL_NUMBER = "serialNumber";


    /**
     * 人员信息部分
     * */
    @Override
    public JSONObject personnelInformationSection(Long stationId) {

        // 内部员工数量与id集合
        JSONObject jsonObject1 = userFeign.queryInformationOnkanbanPersonnelStation(stationId);
        List<Long> numberOfPeopleRegisteredIdList = jsonObject1.getObject("numberOfPeopleRegisteredIdList", ArrayList.class);
        Integer numberOfPeopleRegistered = jsonObject1.getInteger("numberOfPeopleRegistered");

        // 派驻员工数量，与id集合
        JSONObject jsonObject3 = appointmentFeign.queryTheNumberOfResidencies(stationId);
        Integer pzNum = 0;
        pzNum += jsonObject3.getInteger("pzNum");
        List<Long> pzAllIds = jsonObject3.getObject("pzAllIds", ArrayList.class);
        JSONObject postAll = jsonObject3.getJSONObject("postAll");

        // 内部员工+派驻员工 在厂人数，实时在厂总人数，内部员工在厂数量，
        JSONObject jsonObject2 = tPersonAccessRecordsService.queryInformationOnkanbanPersonnelStation(stationId, numberOfPeopleRegisteredIdList, pzAllIds, numberOfPeopleRegistered);
        Long zccn = jsonObject2.getLong("inTheRegisteredFactory");
        Long realTimeTotalNumberOfPeople = jsonObject2.getLong("realTimeTotalNumberOfPeople");
        Long nbzc = jsonObject2.getLong("nbzc");
        Long wbzp = jsonObject2.getLong("wbzp");

        JSONObject entries = jsonObject1.getJSONObject("jobs");

        JSONObject jsonObject = new JSONObject();
        // 在册人数
        jsonObject.put("numberOfPeopleRegistered", numberOfPeopleRegistered+pzNum);
        // 在册厂内
        jsonObject.put("inTheRegisteredFactory", zccn);
        // 在册厂外
        jsonObject.put("outsideTheRegisteredFactory", numberOfPeopleRegistered+pzNum-zccn);
        // 预拌人数 预制人数 等业务与人数
        jsonObject.put("numberOfPeopleReadyToMix", entries);
        // 实时总人数
        jsonObject.put("realTimeTotalNumberOfPeople", realTimeTotalNumberOfPeople);
        // 公司人员
        jsonObject.put("companyPersonnel", nbzc);
        // 派驻人员
        jsonObject.put("residency", wbzp);
        // 外部预约
        jsonObject.put("externalAppointments", "10");

        // 工种人员数量集合
        jsonObject.put("jobs", postAll);

        return jsonObject;
    }
    /**
     * 车辆信息部分
     * */
    @Override
    public JSONObject vehicleInformationSection(Long stationId) {
        // 查询在册车辆总数
        JSONObject entries = vehicleFeign.checkTheTotalNumberOfRegisteredVehicles(stationId);
        Long siteCarNumberTotal = entries.getLong("siteCarNumberTotal");

        // 实时总数,小车数量,货车数量
        JSONObject entries1 = tVehicleAccessRecordsService.QueryRealtimeTotalAndNumberVariousClasses(stationId);
        Long realTimeTotals = entries1.getLong("realTimeTotals");
        Long numberOfTrolleys = entries1.getLong("numberOfTrolleys");
        Long theNumberOfShipments = entries1.getLong("theNumberOfShipments");

        JSONObject jsonObject = new JSONObject();
        // 在册车辆总数
        jsonObject.put("totalNumberOfRegisteredVehicles", siteCarNumberTotal);
        // 实时总数
        jsonObject.put("realTimeTotals", realTimeTotals);
        // 小车数量
        jsonObject.put("numberOfTrolleys", numberOfTrolleys);
        // 货运数量
        jsonObject.put("theNumberOfShipments", theNumberOfShipments);
        return jsonObject;
    }
    /**
     * 站点人员明细部分
     * */
    @Override
    public JSONObject sitePersonnelBreakdownSection(Long stationId) {
        JSONArray siteArray = tPersonAccessRecordsService.queryTheDetailsOfSitePersonnel(stationId);
        JSONArray objects = new JSONArray();
        int serialNumber = 0;
        for (int i = 0; i < siteArray.size(); i++) {
            JSONObject jsonObject = siteArray.getJSONObject(i);
            JSONObject jsonObject1 = new JSONObject();
            serialNumber += 1;
            // 序号
            jsonObject1.put(SERIAL_NUMBER, serialNumber);
            // 姓名
            jsonObject1.put("name", jsonObject.getString("name"));
            // 公司
            jsonObject1.put("firm", jsonObject.getString("fire"));
            // 岗位
            jsonObject1.put("post", jsonObject.getString("post"));
            // 所在区域
            jsonObject1.put("region", jsonObject.getString("region"));
            objects.add(jsonObject1);
        }

        JSONObject jsonObject = new JSONObject();
        // 站点人员明细
        jsonObject.put("sitePersonnelDetails", objects);
        return jsonObject;
    }
    /**
     * 车辆出入明细部分
     * */
    @Override
    public JSONObject vehicleAccessDetails(Long stationId) {
        JSONArray siteArray = tVehicleAccessRecordsService.queryTheDetailsOfSiteCar(stationId);

        JSONArray objects = new JSONArray();
        int serialNumber = 0;
        for (int i = 0; i < siteArray.size(); i++) {
            JSONObject jsonObject = siteArray.getJSONObject(i);
            JSONObject jsonObject1 = new JSONObject();
            serialNumber += 1;
            // 序号
            jsonObject1.put(SERIAL_NUMBER, serialNumber);
            // 车牌
            jsonObject1.put("licensePlateNumber", jsonObject.getString("licensePlateNumber"));
            // 司机
            jsonObject1.put("driver", jsonObject.getString("driver"));
            // 车型
            jsonObject1.put("models", jsonObject.getString("models"));
            // 排放标准
            jsonObject1.put("emissionStandards", jsonObject.getString("emissionStandards"));
            // 时间
            jsonObject1.put("time", jsonObject.getString("time"));
            // 进出类型
            jsonObject1.put("typeOfEntryAndExit", jsonObject.getString("typeOfEntryAndExit"));
            objects.add(jsonObject1);
        }

        JSONObject jsonObject = new JSONObject();
        // 车辆出入明细
        jsonObject.put("vehicleEntryAndExitDetails", objects);
        return jsonObject;
    }
    /**
     * 外部预约人员明细部分
     * */
    @Override
    public JSONObject breakdownOfExternalAppointments(Long stationId) {
        JSONArray objects1 = appointmentFeign.checkTheDetailsOfExternalAppointments(stationId, 1, 999);

        JSONArray entries = new JSONArray();
        int serialNumber = 0;
        for (int i = 0; i < objects1.size(); i++) {
            JSONObject jsonObject = objects1.getJSONObject(i);
            serialNumber += 1;
            JSONObject jsonObject1 = new JSONObject();
            // 序号
            jsonObject1.put(SERIAL_NUMBER, serialNumber);
            // 预约人
            jsonObject1.put("thePersonWhoMadeTheReservation", jsonObject.get("thePersonWhoMadeTheReservation" ));
            // 总人数
            jsonObject1.put("totalNumberOfPeople", jsonObject.getLong("totalNumberOfPeople"));
            // 公司
            jsonObject1.put("firm", jsonObject.get("firm" ));
            // 入厂事由
            jsonObject1.put("reasonForEnteringTheFactory", jsonObject.get("reasonForEnteringTheFactory" ));
            entries.add(jsonObject1);
        }

        JSONObject jsonObject = new JSONObject();
        // 外部预约员明细
        jsonObject.put("detailsOfExternalReservationStaff", entries);
        return jsonObject;
    }

    /**
     * 基本信息部分
     * */
    @SuppressWarnings("checkstyle:Indentation")
    @Override
    public JSONObject basicInformationSection() {
        // 查询站点数量，车辆通道数量，人员通道数据量
        JSONObject sitenum = stationFeign.querySiteNumAndChannelNum();
        Integer numberOfSites = sitenum.getInteger("numberOfSites");
        Integer vehicularAccess = sitenum.getInteger("vehicularAccess");
        Integer personnelAccess = sitenum.getInteger("personnelAccess");

        // 查询 车牌和人脸的设备数量
        JSONObject vehiclesAndFace = userFeign.QueryNumberVehiclesAndFacesOnlineAndOffline();
        Integer licensePlateRecognitionOnline = vehiclesAndFace.getInteger("licensePlateRecognitionOnline");
        Integer licensePlateRecognitionOffline = vehiclesAndFace.getInteger("licensePlateRecognitionOffline");
        Integer faceRecognitionOnline = vehiclesAndFace.getInteger("faceRecognitionOnline");
        Integer faceRecognitionOffline = vehiclesAndFace.getInteger("faceRecognitionOffline");
        JSONObject jsonObject = new JSONObject();
        // 站点数量
        jsonObject.put("numberOfSites", numberOfSites);
        // 车辆通道
        jsonObject.put("vehicularAccess", vehicularAccess);
        // 人员通道
        jsonObject.put("personnelAccess", personnelAccess);
        // 车牌识别（在线）
        jsonObject.put("licensePlateRecognitionOnline", licensePlateRecognitionOnline);
        // 车牌识别（离线）
        jsonObject.put("licensePlateRecognitionOffline", licensePlateRecognitionOffline);
        // 人脸识别（在线）
        jsonObject.put("faceRecognitionOnline", faceRecognitionOnline);
        // 人脸识别（离线）
        jsonObject.put("faceRecognitionOffline", faceRecognitionOffline);
        return jsonObject;
    }

    /**
     * 实名制信息部分
     * */
    @Override
    public JSONObject realNameInformationSection() {
        JSONObject jsonObject = new JSONObject();
        // 厂站实时总人数
        jsonObject.put("numberOfFactoryStation", "10");
        // 预拌人数
        jsonObject.put("numberOfPeopleReadyToMix", "10");
        // 预制人数
        jsonObject.put("preMadeNumberOfPeople", "10");
        // 管桩人数
        jsonObject.put("numberOfPipePiles", "10");
        // 资源人数
        jsonObject.put("numberOfResources", "10");
        // 供应链人数
        jsonObject.put("numberOfPeopleInTheSupplyChain", "10");
        // 工程人数
        jsonObject.put("numberOfEngineers", "10");
        // 厂站实时车辆总数
        jsonObject.put("numberOfCarStation", "10");
        // 小客车数量
        jsonObject.put("numberOfPassengerCars", "10");
        // 货运车数量
        jsonObject.put("numberOfFreightTrucks", "10");
        // 派驻人数
        jsonObject.put("numberOfResidents", "10");
        // 外部预约人数
        jsonObject.put("numberOfExternalAppointments", "10");
        return jsonObject;
    }

    /**
     * 地图部分
     * */
    @Override
    public JSONObject mapSection() {

        JSONArray locationArrays = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        // 站点名称
        jsonObject.put("siteName", "崇州站");
        // 站点经度
        jsonObject.put("longitudeOfTheSite", "103.677481");
        // 站点纬度
        jsonObject.put("siteLatitude", "30.637758");
        // 站点人数
        jsonObject.put("numberOfPeopleAtTheSite", "10");
        // 站点车数
        jsonObject.put("numberOfStops", "10");
        locationArrays.add(jsonObject);

        JSONObject jsonObjectReturn = new JSONObject();
        jsonObjectReturn.put("sitelocation", locationArrays);
        return jsonObjectReturn;
    }
}
