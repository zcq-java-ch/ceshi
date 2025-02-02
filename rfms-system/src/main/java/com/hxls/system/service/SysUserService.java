package com.hxls.system.service;

import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import com.hxls.framework.security.user.UserDetail;
import com.hxls.system.entity.SysUserEntity;
import com.hxls.system.query.SysRoleUserQuery;
import com.hxls.system.query.SysUserQuery;
import com.hxls.system.vo.MainUserVO;
import com.hxls.system.vo.SysUserBaseVO;
import com.hxls.system.vo.SysUserVO;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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
    void importByExcel(String file, String password,Long orgId);

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

    /**
     * 小程序修改用户信息
     * @param vo
     */
    void updateByUser(SysUserVO vo);

    /**
     * @Author zhaohong
     * @Description  同步组织结构
     * @Date 18:08 2024/3/26
     **/
    void synOrg();


    /**
     * @Author zhaohong
     * @Description  同步人员
     * @Date 2024年5月10日11:18:36
     **/
    void synUser();

    /**
     * 批量修改所属站点
     * @param list
     */
    void updateStationIdList(List<SysUserVO> list);

    /**
      * @author Mryang
      * @description 上传excel带图片
      * @date 9:39 2024/5/21
      * @param
      * @return
      */
    void importByExcelWithPictures(String imageUrl, String hxls123456, Long orgId) throws NoSuchAlgorithmException, KeyManagementException, IOException;
}
