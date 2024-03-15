package com.hxls.system.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.system.entity.SysThirdLoginConfigEntity;
import com.hxls.system.vo.SysThirdLoginConfigVO;
import me.zhyd.oauth.request.AuthRequest;
import com.hxls.framework.common.query.Query;
import com.hxls.framework.common.utils.PageResult;

import java.util.List;

/**
 * 第三方登录配置
 *
 * @author
 *
 */
public interface SysThirdLoginConfigService extends BaseService<SysThirdLoginConfigEntity> {

    PageResult<SysThirdLoginConfigVO> page(Query query);

    void save(SysThirdLoginConfigVO vo);

    void update(SysThirdLoginConfigVO vo);

    void delete(List<Long> idList);

    /**
     * 根据类型，获取授权请求
     *
     * @param openType 第三方登录类型
     */
    AuthRequest getAuthRequest(String openType);
}
