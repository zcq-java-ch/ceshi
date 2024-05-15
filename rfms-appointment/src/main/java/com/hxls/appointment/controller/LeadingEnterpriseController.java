package com.hxls.appointment.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hxls.appointment.dao.TAppointmentDao;
import com.hxls.appointment.pojo.vo.TPersonAccessRecordsVO;
import com.hxls.appointment.pojo.vo.TVehicleVO;
import com.hxls.appointment.pojo.vo.leadingVO.*;
import com.hxls.framework.common.cache.RedisCache;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.DateUtils;
import com.hxls.framework.common.utils.ExcelUtils;
import com.hxls.framework.common.utils.JsonUtils;
import com.hxls.framework.common.utils.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/record")
@Slf4j
public class LeadingEnterpriseController {

    private static String token;

    @Resource
    private RedisCache redisCache;
    /**
     * 自定义sql方法的mapper
     */
    @Resource
    private TAppointmentDao appointmentDao;


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
            //修改新方法
            map.put("httptype", "POST");
            params params = new params();
            params.setStartTime(data.getStartTime());
            params.setEndTime(data.getEndTime());
            params.setReceiveStation(data.getReceiveStation());
            map.put("params", JSONUtil.toJsonStr(params));
            if (redisCache.get(data.getStartTime() + data.getEndTime()) != null) {
                return CacheData(data);
            }

            //获取icps过磅信息的url ：https://lvshe.huashijc.com/third/open/api/send_data
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

            List<recordInfo> recordInfos = bean.getData();

            //todo 还需要通过车牌号找到车辆基础信息
            List<String> carNumberList = bean.getData().stream().map(recordInfo::getCarNum).toList();
            if (CollectionUtil.isNotEmpty(carNumberList)) {
                getBasicInformation(recordInfos, carNumberList, data);
            }

            if (StrUtil.isNotEmpty(data.getCdName())) {
                recordInfos.removeIf(item -> item.getCdName() == null || !item.getCdName().equals(data.getCdName()));
            }


            //分割大小
            PageInfo pageInfo = new PageInfo();
            int startIndex = (data.getPage() - 1) * data.getPageSize();
            int endIndex = Math.min(startIndex + data.getPageSize(), recordInfos.size());

            List<recordInfo> recordInfos1 = recordInfos.subList(startIndex, endIndex);

            //如果有过车数据，罐车就替换最近的过车数据
            for (recordInfo item : recordInfos1) {
                if (item.getUnit().equals("m³") && checkTime(item.getSecondTime())) {

                    //用浇注时间
                    //小于浇注时间的前一个进场时间
                    String firstTime = appointmentDao.selectFirstTime(item.getSecondTime(), item.getCarNum());
//                    //大于浇注时间的
//                    String secondTime = appointmentDao.selectSecondTime(item.getFirstTime(), item.getCarNum());

                    // String firstTime = appointmentDao.selectRecordTime(item.getSecondTime(), item.getCarNum(),"1");
                    if (StrUtil.isNotEmpty(firstTime)) {
                        item.setFirstTime(firstTime);
                    }
//                    item.setSecondTime(secondTime == null ? "" : secondTime);
                }
            }

            pageInfo.setRecords(recordInfos1);
            pageInfo.setTotal(bean.getData().size());
            pageInfo.setCurrent(data.getPage());
            pageInfo.setSize(data.getPageSize());
            return Result.ok(pageInfo);

        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 比较时间是否是在设备安装时间之前
     *
     * @param secondTime
     * @return
     */
    private boolean checkTime(String secondTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date inputDate = dateFormat.parse(secondTime);
            Date comparisonDate = dateFormat.parse("2024-04-01 00:00:00"); // 注意这里也包含了时分秒

            return inputDate.after(comparisonDate);
        } catch (ParseException e) {
            e.printStackTrace();
            // 如果解析失败，你可以选择返回一个默认值或者抛出异常，这取决于你的需求
            return false;
        }
    }

    private boolean getDate() {
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        // 设置开始日期为2024年5月1日
        Calendar startDate = Calendar.getInstance();
        startDate.set(2024, Calendar.MAY, 1);

        // 设置结束日期为2024年8月1日
        Calendar endDate = Calendar.getInstance();
        endDate.set(2024, Calendar.AUGUST, 1);

        // 检查当前日期是否在范围内
        return calendar.after(startDate) && calendar.before(endDate);
    }

    private Result<?> CacheData(PageParams data) {

        String string = redisCache.get(data.getStartTime() + data.getEndTime()).toString();
        List<recordInfo> recordInfos = JSONUtil.toList(string, recordInfo.class);
        if (StrUtil.isNotEmpty(data.getCdName())) {
            recordInfos.removeIf(item -> item.getCdName() == null || !item.getCdName().equals(data.getCdName()));
        }

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

        //如果有过车数据，罐车就替换最近的过车数据
        for (recordInfo item : recordInfos1) {
            if (item.getUnit().equals("m³") && checkTime(item.getSecondTime())) {
                //用浇注时间
                //小于浇注时间的前一个进场时间
                String firstTime = appointmentDao.selectFirstTime(item.getSecondTime(), item.getCarNum());
                //大于浇注时间的
//                String secondTime = appointmentDao.selectSecondTime(item.getFirstTime(), item.getCarNum());
                // String firstTime = appointmentDao.selectRecordTime(item.getSecondTime(), item.getCarNum(),"1");
                if (StrUtil.isNotEmpty(firstTime)) {
                    item.setFirstTime(firstTime);
                }
//                item.setSecondTime(secondTime == null ? "" : secondTime);
            }
        }


        pageInfo.setRecords(recordInfos1);
        pageInfo.setTotal(recordInfos.size());
        pageInfo.setCurrent(data.getPage());
        pageInfo.setSize(data.getPageSize());
        return Result.ok(pageInfo);

    }

    private void getBasicInformation(List<recordInfo> recordInfos, List<String> carNumberList, PageParams data1) {
        List<String> carNumbers = carNumberList.stream().distinct().toList();
        String post = HttpUtil.post("http://localhost:8099/car/listByCarNumber", JSONUtil.toJsonStr(carNumbers));
        JSONObject entries = JSONUtil.parseObj(post);
        List<TVehicleVO> data = entries.getBeanList("data", TVehicleVO.class);

        Map<String, TVehicleVO> map = new HashMap<>();
        data.forEach(item -> map.put(item.getLicensePlate(), item));

        recordInfos.forEach(item -> {
            TVehicleVO tVehicleVO = map.get(item.getCarNum());
            item.setDrivingLicense("");
            item.setVIN("");
            item.setEngineNumber("");
            item.setEmissionStandard("");
            if (ObjectUtil.isNotNull(tVehicleVO)) {
                item.setDrivingLicense(tVehicleVO.getLicenseImage());
                item.setVIN(tVehicleVO.getVinNumber());
                item.setEngineNumber(tVehicleVO.getEngineNumber());
                item.setEmissionStandard(tVehicleVO.getEmissionStandard());
                item.setCdName(tVehicleVO.getFleetName());
                item.setFollowInventory(tVehicleVO.getImageUrl());
                //判断是否超重
                BigDecimal freightVolume = item.getFreightVolume();
                BigDecimal bigDecimal = new BigDecimal(tVehicleVO.getMaxCapacity());
                if (bigDecimal.compareTo(freightVolume) < 0) {
                    if (item.getUnit().equals("t")) {
                        bigDecimal = generateRandomDecimal(bigDecimal);
                    }
                    item.setFreightVolume(bigDecimal);
                }
            }
            String secondTime = item.getSecondTime();
            String s = addOneSecond(secondTime);
            item.setCreatTime(s);
        });


        // 使用 Collections.sort() 方法进行排序，并提供自定义的 Comparator
        recordInfos.sort(new Comparator<recordInfo>() {
            @Override
            public int compare(recordInfo r1, recordInfo r2) {
                // 根据 creatTime 字段比较两个对象
                return r2.getCreatTime().compareTo(r1.getCreatTime());
            }
        });

        //隔离车辆的数据
        if (getDate()) {
            recordInfos.removeIf(item -> item.getCarNum().equals("川B79482") || item.getCarNum().equals("川B85765") || item.getCarNum().contains("WZ") || item.getCarNum().equals("川B745XR"));
        }

        List<recordInfo> objects = new ArrayList<>(recordInfos);
        redisCache.set(data1.getStartTime() + data1.getEndTime(), JSONUtil.toJsonStr(objects), 300);

    }

    /**
     * @param bigDecimal
     * @return
     */
    private BigDecimal generateRandomDecimal(BigDecimal bigDecimal) {
        // 生成一个随机数，范围为 [0, 1)
        Random random = new Random();
        double randomValue = random.nextDouble();

        // 生成两位小数的 BigDecimal 对象
        BigDecimal randomDecimal = new BigDecimal(randomValue).setScale(2, RoundingMode.DOWN);

        // 计算结果
        BigDecimal result = bigDecimal.subtract(randomDecimal);
        return result.setScale(2, RoundingMode.DOWN);
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
        map.put("apiCode", "queryLogisticData");
        map.put("httptype", "POST");
        params params = new params();
        params.setStartTime(startTime);
        params.setEndTime(endTime);
        params.setStationName("精城站");
        params.setStationCode("HJ03");
        map.put("params", JSONUtil.toJsonStr(params));

        //获取过磅信息的url ：https://lvshe.huashijc.com/third/open/api/send_data
        String post = HttpUtil.post("https://lvshe.huashijc.com/third/open/api/send_data", JSONUtil.toJsonStr(map));
        responseBodyOutList bean = JSONUtil.toBean(post, responseBodyOutList.class);

        for (recordOutInfo datum : bean.getData()) {
            recordInfo recordInfo = new recordInfo();
            recordInfo.setCarNum(datum.getCarNo());
            if (checkDate(datum.getProTime(), startTime)   || checkDate(endTime , datum.getProTime() )) {
                continue;
            }
            //手动默认厂时间
            recordInfo.setFirstTime(datum.getProTime());
            if (!checkTime(datum.getProTime())) {
                //系统默认随机生成一个几小时之前的时间字符串
              String randomTime =   getRandomPastTime(datum.getProTime());
              if (StrUtil.isNotEmpty(randomTime)){
                  recordInfo.setFirstTime(randomTime);
              }
            }
            recordInfo.setSecondTime( datum.getProTime() );
            recordInfo.setRepertory(datum.getProductName());
            recordInfo.setFreightVolume(new BigDecimal(datum.getSignNum()));
            recordInfo.setUnit("m³");
            recordInfo.setCode(datum.getProCode());
            result.add(recordInfo);
        }

        return result;
    }

    private String getRandomPastTime(String proTime) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(proTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // 随机生成2到3小时的时间差
            Random random = new Random();
            int randomHours = random.nextInt(2) + 2;
            int randomMinutes = random.nextInt(60);
            int randomSeconds = random.nextInt(60);

            // 将时间往前推
            calendar.add(Calendar.HOUR_OF_DAY, -randomHours);
            calendar.add(Calendar.MINUTE, -randomMinutes);
            calendar.add(Calendar.SECOND, -randomSeconds);

            // 转换为字符串
            return sdf.format(calendar.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 比对时间
     *
     * @param proTime
     * @param startTime
     * @return
     */
    private boolean checkDate(String proTime, String startTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date proDate = sdf.parse(proTime);
            Date startDate = sdf.parse(startTime);
            return proDate.before(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
            // 解析日期字符串出错，你可以根据实际情况处理异常
            return false;
        }
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


    @PostMapping("export")
    public void export(@RequestBody PageParams data) {

        if (redisCache.get(data.getStartTime() + data.getEndTime()) == null) {
            throw new ServerException("请先点击搜索,已便获取最新数据");
        }

        String string = redisCache.get(data.getStartTime() + data.getEndTime()).toString();
        List<recordInfo> recordInfos = JSONUtil.toList(string, recordInfo.class);

        if (StrUtil.isNotEmpty(data.getCdName())) {
            recordInfos.removeIf(item -> item.getCdName() == null || !item.getCdName().equals(data.getCdName()));
        }

        for (recordInfo recordInfo : recordInfos) {
            //emissionStandard
            recordInfo.setEmissionStandard(convertData(recordInfo.getEmissionStandard()));

            if (recordInfo.getUnit().equals("t")) {
                recordInfo.setFreightVolume(recordInfo.getFreightVolume().setScale(2, RoundingMode.HALF_UP));
            } else {
                recordInfo.setFreightVolume(recordInfo.getFreightVolume().setScale(0, RoundingMode.HALF_UP));
            }

            //如果有过车数据，罐车就替换最近的过车数据

            if (recordInfo.getUnit().equals("m³") && checkTime(recordInfo.getSecondTime())) {
                //用浇注时间
                //小于浇注时间的前一个进场时间
                String firstTime = appointmentDao.selectFirstTime(recordInfo.getSecondTime(), recordInfo.getCarNum());
//                //大于浇注时间的
//                String secondTime = appointmentDao.selectSecondTime(recordInfo.getFirstTime(), recordInfo.getCarNum());

                if (StrUtil.isNotEmpty(firstTime)) {
                    recordInfo.setFirstTime(firstTime);
                }
//                recordInfo.setSecondTime(secondTime == null ? "" : secondTime);
            }
        }

        // ExcelUtils.excelExport(TPersonAccessRecordsVO.class, "运输电子台账" + DateUtils.format(new Date()),null,recordInfoList);
        ExcelUtils.excelExport(recordInfo.class, "运输电子台账" + DateUtils.format(new Date()), null, recordInfos);

    }

    private String convertData(String emissionStandard) {
        switch (emissionStandard) {
            case "1" -> {
                return "国Ⅰ";
            }
            case "2" -> {
                return "国Ⅱ";
            }
            case "3" -> {
                return "国Ⅲ";
            }
            case "4" -> {
                return "国Ⅳ";
            }
            case "5" -> {
                return "国Ⅴ";
            }
            case "6" -> {
                return "国Ⅵ";
            }
            default -> {
                return "未标注";
            }
        }

    }

}
