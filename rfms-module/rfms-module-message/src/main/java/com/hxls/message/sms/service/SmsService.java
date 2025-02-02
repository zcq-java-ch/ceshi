package com.hxls.message.sms.service;

import cn.hutool.core.map.MapUtil;
import com.hxls.message.cache.SmsPlatformCache;
import com.hxls.message.entity.SmsLogEntity;
import com.hxls.message.service.SmsLogService;
import com.hxls.message.service.SmsPlatformService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.common.exception.ServerException;
import com.hxls.framework.common.utils.ExceptionUtils;
import com.hxls.framework.common.utils.JsonUtils;
import com.hxls.message.sms.SmsContext;
import com.hxls.message.sms.config.SmsConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 短信服务
 *
 * @author
 *
 */
@Slf4j
@Service
@AllArgsConstructor
public class SmsService {
    private final SmsPlatformService smsPlatformService;
    private final SmsLogService smsLogService;
    private final SmsPlatformCache smsCacheService;

    /**
     * 发送短信
     * @param mobile 手机号
     * @return  是否发送成功
     */
    public boolean send(String mobile){
        return this.send(mobile, MapUtil.newHashMap());
    }



    /**
     * 发送短信
     * @param mobile 手机号
     * @param params 参数
     * @return  是否发送成功
     */
    public boolean send(String mobile, Map<String, String> params){
        SmsConfig config = roundSmsConfig();

        try {
            // 发送短信
            new SmsContext(config).send(mobile, params);

            saveLog(config, mobile, params, null);
            return true;
        }catch (Exception e) {
            log.error("短信发送失败，手机号：{}", mobile, e);

            saveLog(config, mobile, params, e);

            return false;
        }
    }

    /**
     * 保存短信日志
     */
    public void saveLog(SmsConfig config, String mobile, Map<String, String> params, Exception e) {
        SmsLogEntity logEntity = new SmsLogEntity();
        logEntity.setPlatform(config.getPlatform());
        logEntity.setPlatformId(config.getId());
        logEntity.setMobile(mobile);
        logEntity.setParams(JsonUtils.toJsonString(params));

        if(e != null) {
            String error = StringUtils.substring(ExceptionUtils.getExceptionMessage(e), 0, 2000);
            logEntity.setStatus(Constant.FAIL);
            logEntity.setError(error);
        }else {
            logEntity.setStatus(Constant.SUCCESS);
        }

        smsLogService.save(logEntity);
    }

    /**
     *  通过轮询算法，获取短信平台的配置
     */
    private SmsConfig roundSmsConfig() {
        List<SmsConfig> platformList = smsPlatformService.listByEnable();

        // 是否有可用的短信平台
        int count = platformList.size();
        if(count == 0) {
            throw new ServerException("没有可用的短信平台，请先添加");
        }

        // 采用轮询算法，发送短信
        long round = smsCacheService.getRoundValue();

        return platformList.get((int)round % count);
    }



    /**
     * 发送短信
     * @param mobile 手机号
     * @param params 参数
     * @return  是否发送成功
     */
    public boolean sendById(List<String> mobiles, Map<String, String> params , Long id){
        SmsConfig config = roundSmsConfigByCode(id);
        for (String mobile : mobiles) {
            try {
                // 发送短信
                new SmsContext(config).send(mobile, params);
                saveLog(config, mobile, params, null);
            }catch (Exception e) {
                log.error("短信发送失败，手机号：{}", mobile, e);
                saveLog(config, mobile, params, e);
                return false;
            }
        }
        return true;
    }

    private SmsConfig roundSmsConfigByCode(Long id) {
        List<SmsConfig> platformList = smsPlatformService.listByEnable();
        // 是否有可用的短信平台
        int count = platformList.size();
        if(count == 0) {
            throw new ServerException("没有可用的短信平台，请先添加");
        }
        for (SmsConfig smsConfig : platformList) {
            if (Objects.equals(smsConfig.getId(), id)) {
                return smsConfig;
            }
        }
        throw new ServerException("没有可用的短信平台，请先添加");
    }


}
