package com.hxls.appointment.service;

import com.hxls.appointment.pojo.entity.TSupplementRecord;
import com.hxls.appointment.pojo.query.TSupplementRecordQuery;
import com.hxls.appointment.pojo.vo.TSupplementRecordVO;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.BaseService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author admin
* @description 针对表【t_supplement_record(预约补录表)】的数据库操作Service
* @createDate 2024-03-26 14:47:54
*/
public interface TSupplementRecordService extends BaseService<TSupplementRecord> {

    /**
     * 分页查询
     * @param query
     * @return
     */
    PageResult<TSupplementRecordVO> page(TSupplementRecordQuery query);

    /**
     * 保存记录
     * @param vo
     */
    void save(TSupplementRecordVO vo);

    /**
     * 修改记录
     * @param vo
     */
    void update(TSupplementRecordVO vo);

    /**
     * 删除记录表
     * @param idList
     */
    void delete(List<Long> idList);

    /**
     * 查看详情
     * @param id
     * @return
     */
    TSupplementRecordVO getDetailById(Long id);

    /**
     * 导入文件
     * @param file
     */
    void export(MultipartFile file);
}
