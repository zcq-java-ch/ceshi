package com.hxls.system.service.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.system.convert.SysOrgConvert;
import com.hxls.system.entity.SysOnlineLog;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.query.SysOrgQuery;
import com.hxls.system.service.SysOnlineLogService;
import com.hxls.system.dao.SysOnlineLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author admin
* @description 针对表【sys_online_log】的数据库操作Service实现
* @createDate 2024-07-17 10:06:40
*/
@Service
public class SysOnlineLogServiceImpl extends BaseServiceImpl<SysOnlineLogMapper, SysOnlineLog>
    implements SysOnlineLogService{

    @Override
    public void save(String ip, Long siteId) {
        LocalDateTime now = LocalDateTime.now();
        SysOnlineLog sysOnlineLog = new SysOnlineLog();

        List<SysOnlineLog> list = list(new LambdaQueryWrapper<SysOnlineLog>().eq(SysOnlineLog::getMasterIp,ip)
                .eq(SysOnlineLog::getSiteId,siteId).isNull(SysOnlineLog::getOnDate));
        if (CollectionUtils.isNotEmpty(list)){
            //如果一直离线还没有在线，则不进行记录
            return;
        }

        sysOnlineLog.setMasterIp(ip);
        sysOnlineLog.setSiteId(siteId);
        sysOnlineLog.setOffDate(now);
        save(sysOnlineLog);
    }

    @Override
    public PageResult<SysOnlineLog> pageList(SysOrgQuery query) {
        IPage<SysOnlineLog> page = baseMapper.selectPage(getPage(query), getWrapper(query));

        return new PageResult<>(page.getRecords(), page.getTotal());

    }

    private Wrapper<SysOnlineLog> getWrapper(SysOrgQuery query) {

        LambdaQueryWrapper<SysOnlineLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOnlineLog::getSiteId , query.getId());

        return wrapper;
    }
}




