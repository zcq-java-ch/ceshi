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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


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
     */
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
//        JSONObject postAll = jsonObject3.getJSONObject("postAll");

        // 内部员工+派驻员工 在厂人数，实时在厂总人数，内部员工在厂数量，
        JSONObject jsonObject2 = tPersonAccessRecordsService.queryInformationOnkanbanPersonnelStation(stationId, numberOfPeopleRegisteredIdList, pzAllIds, numberOfPeopleRegistered);
        Long zccn = jsonObject2.getLong("inTheRegisteredFactory");
        Long realTimeTotalNumberOfPeople = jsonObject2.getLong("realTimeTotalNumberOfPeople");
        Long nbzc = jsonObject2.getLong("nbzc");
        Long wbzp = jsonObject2.getLong("wbzp");

//        JSONObject entries = jsonObject1.getJSONObject("jobs");

        // 查询站点工种统计
        JSONObject jsonObject5 = tPersonAccessRecordsService.queryTheStatisticsOfTheTypeOfWorkBySiteId(stationId);
        JSONObject postAll = jsonObject5.getJSONObject("postAll");
        JSONObject busisAll = jsonObject5.getJSONObject("busisAll");
        // 查询外部预约总数
        JSONObject jsonObject4 = appointmentFeign.queryTotalAppointments(stationId);
        Integer wbyyzs = jsonObject4.getInteger("numberOfExternalAppointments");
        JSONObject jsonObject = new JSONObject();
        // 在册人数
        jsonObject.put("numberOfPeopleRegistered", numberOfPeopleRegistered + pzNum);
        // 在册厂内
        jsonObject.put("inTheRegisteredFactory", zccn);
        // 在册厂外
        jsonObject.put("outsideTheRegisteredFactory", numberOfPeopleRegistered + pzNum - zccn);
        // 预拌人数 预制人数 等业务与人数
        jsonObject.put("numberOfPeopleReadyToMix", busisAll);
        // 实时总人数
        jsonObject.put("realTimeTotalNumberOfPeople", realTimeTotalNumberOfPeople);
        // 公司人员
        jsonObject.put("companyPersonnel", nbzc);
        // 派驻人员
        jsonObject.put("residency", wbzp);
        // 外部预约
        jsonObject.put("externalAppointments", wbyyzs);
        // 工种人员数量集合
        jsonObject.put("jobs", postAll);
        return jsonObject;
    }

    /**
     * 车辆信息部分
     */
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
     */
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

            jsonObject1.put("personId", jsonObject.getString("personId"));

            objects.add(jsonObject1);
        }

        JSONObject jsonObject = new JSONObject();
        // 站点人员明细
        jsonObject.put("sitePersonnelDetails", objects);
        return jsonObject;
    }

    /**
     * 车辆出入明细部分
     */
    @Override
    public JSONObject vehicleAccessDetails(Long stationId)  {
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
            String time = jsonObject.getString("time");
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = "";
            try {
                Date date = inputFormat.parse(time);
                formattedDate = outputFormat.format(date);
            } catch (ParseException e) {
//                e.printStackTrace();
                System.out.println("时间转化异常");
            }
            jsonObject1.put("time", formattedDate.substring(10));
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
     */
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
            jsonObject1.put("thePersonWhoMadeTheReservation", jsonObject.get("thePersonWhoMadeTheReservation"));
            // 总人数
            jsonObject1.put("totalNumberOfPeople", jsonObject.getLong("totalNumberOfPeople"));
            // 公司
            jsonObject1.put("firm", jsonObject.get("firm"));
            // 入厂事由
            jsonObject1.put("reasonForEnteringTheFactory", jsonObject.get("reasonForEnteringTheFactory"));
            entries.add(jsonObject1);
        }

        JSONObject jsonObject = new JSONObject();
        // 外部预约员明细
        jsonObject.put("detailsOfExternalReservationStaff", entries);
        return jsonObject;
    }

    /**
     * 基本信息部分
     */
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
     */
    @Override
    public JSONObject realNameInformationSection() {

        // 查询全部厂站的人员和车辆统计信息
        JSONObject statistics = tPersonAccessRecordsService.queryAllVehicleAndPersonStatistics();
        Integer numberOfFactoryStation = statistics.getInteger("numberOfFactoryStation");
        JSONObject busisStatistics = statistics.getJSONObject("busisStatistics");
        Integer numberOfCarStation = statistics.getInteger("numberOfCarStation");
        JSONObject catTypeStatistics = statistics.getJSONObject("catTypeStatistics");
        Integer numberOfResidents = statistics.getInteger("numberOfResidents");
        Integer numberOfExternalAppointments = statistics.getInteger("numberOfExternalAppointments");


        // 在预约服务中查询派驻人数和外部预约人数
//        JSONObject appstatistics = appointmentFeign.queryStatisticsallPeopleReservation();
//        Integer numberOfResidents = appstatistics.getInteger("numberOfResidents");
//        Integer numberOfExternalAppointments = appstatistics.getInteger("numberOfExternalAppointments");

        JSONObject jsonObject = new JSONObject();
        // 厂站实时总人数
        jsonObject.put("numberOfFactoryStation", numberOfFactoryStation);
        // 业务类型统计
        jsonObject.put("busisStatistics", busisStatistics);
        // 厂站实时车辆总数
        jsonObject.put("numberOfCarStation", numberOfCarStation);
        // 车型统计
        jsonObject.put("catTypeStatistics", catTypeStatistics);
        // 派驻人数
        jsonObject.put("numberOfResidents", numberOfResidents);
        // 外部预约人数
        jsonObject.put("numberOfExternalAppointments", numberOfExternalAppointments);
        return jsonObject;
    }

    /**
     * 地图部分
     */
    @Override
    public JSONObject mapSection() {
        // 查询站点坐标
        JSONArray siteCoor = stationFeign.querySiteCoordinates();

        // 给每个站点加上 在厂人员数量和车辆数量
        JSONArray siteCoorAfter = tPersonAccessRecordsService.numberOfAssemblersAndVehicles(siteCoor);

        JSONObject jsonObjectReturn = new JSONObject();
        jsonObjectReturn.put("sitelocation", siteCoorAfter);
        return jsonObjectReturn;
    }

    @Override
    public JSONObject sitePersonnelBreakdownSectionTj(JSONObject jsonsite) {

        List<String> userList = userFeign.userList("1");
        List<String> userList2 = userFeign.userList("2");
        int company = 0;
        int other = 0;

        JSONObject regionCount = new JSONObject();
        regionCount.put("未设置区域", 0);
        JSONArray jsonArray = jsonsite.getJSONArray("sitePersonnelDetails");
        if (CollectionUtils.isNotEmpty(jsonArray)) {
            // 遍历JSONArray中的每一个JSONObject
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String region = jsonObject.getString("region");
                // 如果map中已经有这个region，就增加计数，否则初始化计数为1
                if (StringUtils.isNotEmpty(region)) {
                    if (regionCount.containsKey(region)) {
                        Integer integer = regionCount.getInteger(region);
                        integer += 1;
                        regionCount.put(region, integer);
                    } else {
                        regionCount.put(region, 1);
                    }
                } else {
                    Integer integer = regionCount.getInteger("未设置区域");
                    integer += 1;
                    regionCount.put("未设置区域", integer);
                }

                //判断人员是否是
                String personId = jsonObject.getString("personId");

                if (CollectionUtils.isNotEmpty(userList) && userList.contains(personId)) {
                    company++;
                }else if (CollectionUtils.isNotEmpty(userList2) && userList2.contains(personId)) {
                    other++;
                }
            }
        }
        regionCount.put("sum", jsonArray.size());
        regionCount.put("company", company);
        regionCount.put("supplier", other);
        regionCount.put("other",jsonArray.size()-company-other);

        return regionCount;
    }
}
