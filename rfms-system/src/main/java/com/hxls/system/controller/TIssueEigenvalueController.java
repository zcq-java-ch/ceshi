package com.hxls.system.controller;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hxls.api.dto.appointment.TIssueEigenvalueDTO;
import com.hxls.api.feign.appointment.AppointmentFeign;
import com.hxls.api.vo.PageResult;
import com.hxls.api.vo.TIssueEigenvalueVO;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.dao.SysOrgDao;
import com.hxls.system.dao.SysUserDao;
import com.hxls.system.entity.SysOrgEntity;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.service.SysOrgService;
import com.hxls.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("sys/issue/eigenvalue")
@Tag(name = "下发记录管理")
@AllArgsConstructor
public class TIssueEigenvalueController {

    private final AppointmentFeign feign;
    private final SysUserDao sysUserDao;
    private final SysOrgService sysOrgService;

    @GetMapping("pageList")
    @PreAuthorize("hasAuthority('sys:issue:page')")
    @Operation(summary = "分页")
    public Result<com.hxls.framework.common.utils.PageResult<TIssueEigenvalueVO>> pageListIssue(@ParameterObject TIssueEigenvalueDTO data) {

        PageResult<TIssueEigenvalueVO> tIssueEigenvalueVOPageResult = feign.pageListIssue(data);
        List<TIssueEigenvalueVO> list = tIssueEigenvalueVOPageResult.getList();
        //做数据处理
        for (TIssueEigenvalueVO tIssueEigenvalueVO : list) {
            Long stationId = tIssueEigenvalueVO.getStationId();
            if (stationId != null ){
                SysOrgEntity sysOrgEntity = sysOrgService.getById(stationId);
                tIssueEigenvalueVO.setStationName(sysOrgEntity ==null? "" : sysOrgEntity.getName());
            }
            Long creator = tIssueEigenvalueVO.getCreator();
            if (creator != null){
                SysUserEntity byId = sysUserDao.getById(creator);
                tIssueEigenvalueVO.setCreatorName(byId.getRealName());
            }
            switch (tIssueEigenvalueVO.getType()) {
                case 1 -> {
                    //{"type":"5","startTime":"2024-06-20 00:00:00","deadline":"2024-06-20 23:59:59",
                    // "peopleName":"测试1","peopleCode":72621334,
                    // "faceUrl":"https://rns.huashijc.com/upload/20240620/1718872656489.jpg",
                    // "masterIp":"192.205.0.92","deviceInfos":"[{\"password\":\"\",\"code\":\"768D9E-C5CECB-0000FF\",
                    // \"ip\":\"192.205.0.43\",\"username\":\"\"},{\"password\":\"\",\"code\":\"C534D7-454FCB-0000FF\",
                    // \"ip\":\"192.205.0.44\",\"username\":\"\"},{\"password\":\"\",\"code\":\"111\",\"ip\":\"11\",
                    // \"username\":\"\"}]","password":""}
                    JSONObject entries = JSONUtil.parseObj(tIssueEigenvalueVO.getData());
                    tIssueEigenvalueVO.setPeopleName(entries.getStr("peopleName"));
                    tIssueEigenvalueVO.setPeopleCode(entries.getStr("peopleCode"));
                    tIssueEigenvalueVO.setFaceUrl(entries.getStr("faceUrl"));
                    tIssueEigenvalueVO.setTime(entries.getStr("startTime")+ "至" +entries.getStr("deadline"));
                    tIssueEigenvalueVO.setPostName(sysUserDao.getPostByCode(tIssueEigenvalueVO.getPeopleCode()));
                }

                case 2 -> {
                //{"type":"9","startTime":"2024-04-01 00:00:00","
                    // deadline":"2034-04-01 00:00:00","carNumber":"测G32111","status":"add",
                    // "masterIp":"192.205.0.92","databaseName":"","username":"","password":""}
                    JSONObject entries = JSONUtil.parseObj(tIssueEigenvalueVO.getData());
                    tIssueEigenvalueVO.setCarNumber(entries.getStr("carNumber"));
                    tIssueEigenvalueVO.setMasterIp(entries.getStr("masterIp"));
                    tIssueEigenvalueVO.setTime(entries.getStr("startTime")+ "至" +entries.getStr("deadline"));
                }
            }
        }
        return Result.ok( new com.hxls.framework.common.utils.PageResult<>(list, tIssueEigenvalueVOPageResult.getTotal()));
    }


    @GetMapping("sendIssue")
    @PreAuthorize("hasAuthority('sys:issue:page')")
    @Operation(summary = "分页")
    public Result<Void> sendIssue(@RequestParam("id") Long id) {

       feign.issued(id);

        return Result.ok();

    }

}
