package com.hxls.system.service;

import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.query.SysRoleUserQuery;
import com.hxls.system.query.SysUserQuery;
import com.hxls.system.vo.MainUserVO;
import com.hxls.system.vo.SysOrgVO;
import com.hxls.system.vo.SysUserBaseVO;
import com.hxls.system.vo.SysUserVO;
import com.hxls.framework.common.utils.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户管理
 *
 * @author
 *
 */
public interface SysUserService extends BaseService<SysUserEntity> {

    PageResult<SysUserVO> page(SysUserQuery query);

    PageResult<SysUserVO> pageByGys(SysUserQuery query);

    void save(SysUserVO vo);

    void update(SysUserVO vo);

    void updateLoginInfo(SysUserBaseVO vo , UserDetail user);

    void delete(List<Long> idList);

    SysUserVO getByMobile(String mobile);

    /**
     * 修改密码
     *
     * @param id          用户ID
     * @param newPassword 新密码
     */
    void updatePassword(Long id, String newPassword);

    /**
     * 分配角色，用户列表
     */
    PageResult<SysUserVO> roleUserPage(SysRoleUserQuery query);

    /**
     * 批量导入用户
     *
     * @param file     excel文件
     * @param password 密码
     * @param orgId 所属供应商
     */
    void importByExcel(MultipartFile file, String password,Long orgId);

    /**
     * 导出用户信息表格
     */
    void export();


    /**
    * @Author zhaohong
    * @Description  主数据密钥登录
    * @Date 16:15 2024/3/29
    **/
    void cardLogin();


    /**
    * @Author zhaohong
    * @Description 下拉获取主数据员工数据
    * @Date 16:35 2024/3/29
    **/

    List<MainUserVO> queryByMainUsers();

    void updateStatus(List<SysUserVO> list);


    PageResult<SysUserVO> pageByNoAuth(SysUserQuery query);
}
