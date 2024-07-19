package com.hxls.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hxls.api.dto.message.MessageDTO;
import com.hxls.api.module.message.SmsApi;
import com.hxls.framework.common.utils.ClassToMapUtils;
import com.hxls.framework.common.utils.ExceptionUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.framework.operatelog.dto.OperateLogDTO;
import com.hxls.system.entity.SysLogOperateEntity;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.entity.TDeviceManagementEntity;
import com.hxls.system.query.SysLogOperateQuery;
import com.hxls.system.service.*;
import com.hxls.system.vo.SysLogOperateVO;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.cache.RedisCache;
import com.hxls.framework.common.cache.RedisKeys;
import com.hxls.system.convert.SysLogOperateConvert;
import com.hxls.system.dao.SysLogOperateDao;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 操作日志
 *
 * @author
 */
@Service
@AllArgsConstructor
public class SysLogOperateServiceImpl extends BaseServiceImpl<SysLogOperateDao, SysLogOperateEntity> implements SysLogOperateService {
    private final RedisCache redisCache;
    private final TDeviceManagementService deviceManagementService;
    private final SmsApi smsApi;
    private final SysOrgService sysOrgService;
    private final SysUserService sysUserService;
    private final SysOnlineLogService sysOnlineLogService;

    @Override
    public PageResult<SysLogOperateVO> page(SysLogOperateQuery query) {
        IPage<SysLogOperateEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query));
        return new PageResult<>(SysLogOperateConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private LambdaQueryWrapper<SysLogOperateEntity> getWrapper(SysLogOperateQuery query) {
        LambdaQueryWrapper<SysLogOperateEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(query.getStatus() != null, SysLogOperateEntity::getStatus, query.getStatus());
        wrapper.like(StrUtil.isNotBlank(query.getRealName()), SysLogOperateEntity::getRealName, query.getRealName());
        wrapper.like(StrUtil.isNotBlank(query.getModule()), SysLogOperateEntity::getModule, query.getModule());
        wrapper.like(StrUtil.isNotBlank(query.getReqUri()), SysLogOperateEntity::getReqUri, query.getReqUri());
        wrapper.orderByDesc(SysLogOperateEntity::getId);
        return wrapper;
    }

    /**
     * 启动项目时，从Redis队列获取操作日志并保存
     */
    @PostConstruct
    public void saveLog() {
//        ScheduledExecutorService scheduledService = ThreadUtil.createScheduledExecutor(1);
//
//        // 每隔10秒钟，执行一次
//        scheduledService.scheduleWithFixedDelay(() -> {
//            try {
//                String key = RedisKeys.getLogKey();
//                // 每次插入10条
//                int count = 10;
//                for (int i = 0; i < count; i++) {
//                    OperateLogDTO log = (OperateLogDTO) redisCache.rightPop(key);
//                    if (log == null) {
//                        return;
//                    }
//                    SysLogOperateEntity entity = BeanUtil.copyProperties(log, SysLogOperateEntity.class);
//                    baseMapper.insert(entity);
//                }
//            } catch (Exception e) {
//                log.error("SysLogOperateServiceImpl.saveLog Error：" + ExceptionUtils.getExceptionMessage(e));
//            }
//        }, 1, 10, TimeUnit.SECONDS);
    }

    /*
     *检查设备是否在线
     */
    @PostConstruct
    public void checkOnline() {

//        // 每隔5分钟，执行一次
//        ScheduledExecutorService scheduledService = ThreadUtil.createScheduledExecutor(1);
//        Set<String> device = new HashSet<>();
//        scheduledService.scheduleWithFixedDelay(() -> {
//
//            List<TDeviceManagementEntity> list = deviceManagementService.list(new LambdaQueryWrapper<TDeviceManagementEntity>().eq(TDeviceManagementEntity::getStatus, 1));
//            for (TDeviceManagementEntity tDeviceManagementEntity : list) {
//                String deviceSn = tDeviceManagementEntity.getDeviceSn();
//                if (tDeviceManagementEntity.getDeviceType().equals("1")) {
//                    if (redisCache.get("DEVICES_STATUS:FACE:" + deviceSn) == null) {
//                        //表示设备已经不在线了
//                        device.add(tDeviceManagementEntity.getMasterIp());
//                    }
//                }
//                if (tDeviceManagementEntity.getDeviceType().equals("2")) {
//                    if (redisCache.get("DEVICES_STATUS:CAR:" + deviceSn) == null) {
//                        //表示设备已经不在线了
//                        device.add(tDeviceManagementEntity.getMasterIp());
//                    }
//                }
//            }
//            if (!device.isEmpty()) {
//                //需要发送短信
//                for (String ip : device) {
//
//                    //判断是否已经发送过短信了
//                    if (redisCache.get("MESSAGE:" + ip) != null) {
//                        continue;
//                    }
//                    MessageDTO.Offline offline = new MessageDTO.Offline();
//                    offline.setMasterName(ip);
//                    String name = getNameByIp(ip);
//                    offline.setStationName(name.split("_")[0]);
//                    List<String> phoneBySiteId = getPhoneBySiteId(Long.parseLong(name.split("_")[1]));
//                    Map<String, String> map = ClassToMapUtils.objectToStringMap(offline);
//                    if (map.isEmpty()) {
//                        return;
//                    }
//                    //2次判断是否还在离线
//                    if (checkOffLine(ip)) {
//                        smsApi.sendById(phoneBySiteId, map, 2L);
//                        redisCache.set("MESSAGE:" + ip, ip, 60 * 60 * 4);
//                        sysOnlineLogService.save(ip, Long.parseLong(name.split("_")[1]));
//                    }
//                }
//            }
//        }, 1, 60 * 6, TimeUnit.SECONDS);
    }

    private boolean checkOffLine(String ip) {

        Set<String> set = new HashSet<>();
        List<TDeviceManagementEntity> list = deviceManagementService.list(new LambdaQueryWrapper<TDeviceManagementEntity>().eq(TDeviceManagementEntity::getStatus, 1));
        for (TDeviceManagementEntity tDeviceManagementEntity : list) {
            String deviceSn = tDeviceManagementEntity.getDeviceSn();
            if (tDeviceManagementEntity.getDeviceType().equals("1")) {
                if (redisCache.get("DEVICES_STATUS:FACE:" + deviceSn) == null) {
                    //表示设备已经不在线了
                    set.add(tDeviceManagementEntity.getMasterIp());
                }
            }
            if (tDeviceManagementEntity.getDeviceType().equals("2")) {
                if (redisCache.get("DEVICES_STATUS:CAR:" + deviceSn) == null) {
                    //表示设备已经不在线了
                    set.add(tDeviceManagementEntity.getMasterIp());
                }
            }
        }
        return set.contains(ip);
    }

    private String getNameByIp(String ip) {

        List<TDeviceManagementEntity> list = deviceManagementService.list(new LambdaQueryWrapper<TDeviceManagementEntity>()
                .eq(TDeviceManagementEntity::getMasterIp, ip));

        return list.get(0).getSiteName() + "_" + list
                .get(0).getSiteId();

    }

    private List<String> getPhoneBySiteId(Long stationId) {
        SysOrgEntity sysOrgEntities = sysOrgService.getById(stationId);
        if (ObjectUtil.isNotEmpty(sysOrgEntities)) {
            String siteAdminIds = sysOrgEntities.getSiteAdminIds();
            if (StringUtils.isNotEmpty(siteAdminIds)) {
                List<Long> toList = stringToList(siteAdminIds);
                List<SysUserEntity> sysUserEntities = sysUserService.listByIds(toList);

                if (CollectionUtils.isNotEmpty(sysUserEntities)) {
                    return sysUserEntities.stream().map(SysUserEntity::getMobile).toList();
                }
            }
        }
        return new ArrayList<>();
    }

    private List<Long> stringToList(String siteAdminIds) {

        // 去掉开头和结尾的方括号
        siteAdminIds = siteAdminIds.substring(1, siteAdminIds.length() - 1);
        // 使用逗号分割字符串
        String[] idArray = siteAdminIds.split(",");
        // 创建List<String>并添加分割后的元素
        List<Long> idList = new ArrayList<>();
        for (String str : idArray) {
            // 去掉空格并添加到List中
            idList.add(Long.parseLong(str.trim()));
        }
        return idList;
    }


}
