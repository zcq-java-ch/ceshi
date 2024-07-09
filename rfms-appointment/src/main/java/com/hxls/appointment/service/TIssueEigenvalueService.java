package com.hxls.appointment.service;

import cn.hutool.json.JSONObject;
import com.hxls.api.dto.appointment.TIssueEigenvalueDTO;
import com.hxls.appointment.pojo.entity.TIssueEigenvalue;
import com.hxls.appointment.pojo.vo.TIssueEigenvalueVO;
import com.hxls.appointment.pojo.vo.TIssueVO;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;

/**
* @author admin
* @description 针对表【t_issue_eigenvalue(下发特征值表)】的数据库操作Service
* @createDate 2024-06-12 16:57:41
*/
public interface TIssueEigenvalueService extends BaseService<TIssueEigenvalue> {

    Long save(TIssueEigenvalueVO data);


    /**
     * 分页查询
     * @param data
     * @return
     */
    PageResult<TIssueEigenvalueVO> pageList(TIssueEigenvalueDTO data);


    /**
     * 重新下发
     * @param id
     * @return
     */
    void issue(Long id);

    /**
     * 修改状态
     * @param data
     */
    String updateTIssueEigenvalue(TIssueVO data);
}
