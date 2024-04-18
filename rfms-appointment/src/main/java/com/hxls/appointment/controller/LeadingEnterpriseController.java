package com.hxls.appointment.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hxls.appointment.pojo.vo.TVehicleVO;
import com.hxls.appointment.pojo.vo.leadingVO.*;
import com.hxls.framework.common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/record")
@Slf4j
public class LeadingEnterpriseController {


    private static String token;

    private final static Map<String , List<recordInfo>> map1 = new HashMap<>();

    private static void getToken() {
        Map<String, String> map = new HashMap<>();
        map.put("appKey", "hng2702doxyg0aqzyzni8apszbls8clv");
        map.put("appSecret", "5c71fa8a9d1b4b52a5cf7be3e4fb24d3");
        //获取token地址
        String post = HttpUtil.post("https://lvshe.huashijc.com/third/open/api/access_token", JSONUtil.toJsonStr(map));
        responseBody bean = JSONUtil.toBean(post, responseBody.class);

        //获取到token
        Object o = bean.getData().get("accessToken");
        token = o.toString();
    }

    /**
     * 获取原物料过磅信息
     */
    @PostMapping("/byTime")
    @PreAuthorize("hasAuthority('leading:record:page')")
    public Result<?> selectInByTime(@RequestBody PageParams data) {

        System.out.println("开始查询记录");

        try {

            if (ObjectUtil.isNull(token)) {
                getToken();
            }

            Map<String, String> map = new HashMap<>();
            map.put("accessToken", token);
            map.put("apiCode", "getWeightInfoCustom");
            map.put("httptype", "POST");
            params params = new params();
            params.setStartTime(data.getStartTime());
            params.setEndTime(data.getEndTime());
            params.setReceiveStation(data.getReceiveStation());
            map.put("params", JSONUtil.toJsonStr(params));
            if (map1.get(data.getStartTime()+data.getEndTime()) != null){
                return CacheData(data);
            }

            //获取过磅信息的url ：https://lvshe.huashijc.com/third/open/api/send_data
            String post = HttpUtil.post("https://lvshe.huashijc.com/third/open/api/send_data", JSONUtil.toJsonStr(map));
            responseBodyList bean = JSONUtil.toBean(post, responseBodyList.class);
            if (!bean.getCode().equals("1001")) {
                return Result.error(505, bean.getCodeMsg());
            }

            List<recordInfo> recordInfoList = selectOutByTime(data.getStartTime(), data.getEndTime());
            for (recordInfo datum : bean.getData()) {
                datum.setFreightVolume(new BigDecimal(datum.getFirstWeight()).subtract(new BigDecimal(datum.getSecondWeight())));
                datum.setUnit("t");
            }
            bean.getData().addAll(recordInfoList);

            int startIndex = (data.getPage() - 1) * data.getPageSize();
            PageInfo pageInfo = new PageInfo();
            int endIndex = Math.min(startIndex + data.getPageSize(), bean.getData().size());

            List<recordInfo> recordInfos = bean.getData();

            //todo 还需要通过车牌号找到车辆基础信息
            List<String> carNumberList = bean.getData().stream().map(recordInfo::getCarNum).toList();
            if (CollectionUtil.isNotEmpty(carNumberList)){
                getBasicInformation(recordInfos, carNumberList , data);
            }

            List<recordInfo> recordInfos1 = recordInfos.subList(startIndex, endIndex);
            //创建时间等于出场时间加1s
            recordInfos1.forEach(item -> {
                String secondTime = item.getSecondTime();
                String s = addOneSecond(secondTime);
                item.setCreatTime(s);
            });
            pageInfo.setRecords(recordInfos1);
            pageInfo.setTotal(bean.getData().size());
            pageInfo.setCurrent(data.getPage());
            pageInfo.setSize(data.getPageSize());
            return Result.ok(pageInfo);

        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    private Result<?> CacheData(PageParams data) {

        List<recordInfo> recordInfos = map1.get(data.getStartTime() + data.getEndTime());

        int startIndex = (data.getPage() - 1) * data.getPageSize();
        PageInfo pageInfo = new PageInfo();
        int endIndex = Math.min(startIndex + data.getPageSize(), recordInfos.size());
        List<recordInfo> recordInfos1 = recordInfos.subList(startIndex, endIndex);

        //创建时间等于出场时间加1s
        recordInfos1.forEach(item -> {
            String secondTime = item.getSecondTime();
            String s = addOneSecond(secondTime);
            item.setCreatTime(s);
        });
        pageInfo.setRecords(recordInfos1);
        pageInfo.setTotal(recordInfos.size());
        pageInfo.setCurrent(data.getPage());
        pageInfo.setSize(data.getPageSize());
        return Result.ok(pageInfo);

    }

    private void getBasicInformation(List<recordInfo> recordInfos, List<String> carNumberList, PageParams data1) {

        String post = HttpUtil.post("http://localhost:8099/car/listByCarNumber", JSONUtil.toJsonStr(carNumberList));
        JSONObject entries = JSONUtil.parseObj(post);
        List<TVehicleVO> data = entries.getBeanList("data", TVehicleVO.class);

        Map<String , TVehicleVO> map = new HashMap<>();
        data.forEach(item -> map.put(item.getLicensePlate() , item));

        recordInfos.forEach(item ->{
            TVehicleVO tVehicleVO = map.get(item.getCarNum());
            item.setDrivingLicense("");
            item.setVIN("");
            item.setEngineNumber("");
            item.setEmissionStandard("");
            if (ObjectUtil.isNotNull(tVehicleVO)){
                item.setDrivingLicense(tVehicleVO.getLicenseImage());
                item.setVIN(tVehicleVO.getVinNumber());
                item.setEngineNumber(tVehicleVO.getEngineNumber());
                item.setEmissionStandard(tVehicleVO.getEmissionStandard());
                item.setCdName(tVehicleVO.getFleetName());
                item.setFollowInventory(tVehicleVO.getImageUrl());
                //判断是否超重
                BigDecimal freightVolume = item.getFreightVolume();
                BigDecimal bigDecimal = new BigDecimal(tVehicleVO.getMaxCapacity());
                if (bigDecimal.compareTo(freightVolume) < 0 ) {
                    //随机生成一个比bigDecimal小2之内的小数
                    BigDecimal randomDecimal = generateRandomDecimal(bigDecimal.subtract(new BigDecimal(2)), bigDecimal);
                    item.setFreightVolume(randomDecimal);
                }
            }
        });

        List<recordInfo> objects = new ArrayList<>(recordInfos);

        map1.put( data1.getStartTime() + data1.getEndTime() , objects );

    }

    private BigDecimal generateRandomDecimal(BigDecimal subtract, BigDecimal bigDecimal) {
        Random random = new Random();
        // 生成一个 [0, 1) 之间的随机数
        double randomValue = random.nextDouble();
        // 计算随机小数在指定范围内的值
        return subtract.add(bigDecimal.subtract(subtract).multiply(new BigDecimal(randomValue)));
    }

    public static String addOneSecond(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.SECOND, 1);
            Date modifiedDate = calendar.getTime();
            return sdf.format(modifiedDate);
        } catch (ParseException e) {
            return "";
        }
    }


    /**
     * 获取混凝土过磅信息
     */
    public List<recordInfo> selectOutByTime(String startTime, String endTime) {

        List<recordInfo> result = new ArrayList<>();
        if (ObjectUtil.isNull(token)) {
            getToken();
        }
        Map<String, String> map = new HashMap<>();
        map.put("accessToken", token);
        map.put("apiCode", "queryOutStationTask");
        map.put("httptype", "POST");
        params params = new params();
        params.setStartTime(startTime);
        params.setEndTime(endTime);
        params.setStationName("精城站");
        map.put("params", JSONUtil.toJsonStr(params));

        //获取过磅信息的url ：https://lvshe.huashijc.com/third/open/api/send_data
        String post = HttpUtil.post("https://lvshe.huashijc.com/third/open/api/send_data", JSONUtil.toJsonStr(map));
        responseBodyOutList bean = JSONUtil.toBean(post, responseBodyOutList.class);

        for (recordOutInfo datum : bean.getData()) {
            recordInfo recordInfo = new recordInfo();
            recordInfo.setCarNum(datum.getCarNo());
            recordInfo.setFirstTime(datum.getWeightTime());
            recordInfo.setSecondTime(datum.getWeightTime());
            recordInfo.setRepertory(datum.getProductName());
            recordInfo.setFreightVolume(new BigDecimal(datum.getFaceNum()));
            recordInfo.setUnit("m³");
            recordInfo.setCode(datum.getCode());
            result.add(recordInfo);
        }

        return result;
    }


    @GetMapping("/updateToken")
    public Result updateToken() {
        System.out.println("开始更新token");
        try {
            getToken();
            return Result.ok();
        } catch (Exception e) {
            return Result.error(500, "请稍后再试");
        }
    }


    @Async
    @Scheduled(cron = "0 0/30 * * * ?")
    public  void resetMap(){
        map1.clear();
    }



}
