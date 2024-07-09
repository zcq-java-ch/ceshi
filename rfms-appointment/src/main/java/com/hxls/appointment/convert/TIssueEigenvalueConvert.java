package com.hxls.appointment.convert;


import com.hxls.appointment.pojo.entity.TIssueEigenvalue;
import com.hxls.appointment.pojo.entity.TSupplementRecord;
import com.hxls.appointment.pojo.vo.TIssueEigenvalueVO;
import com.hxls.appointment.pojo.vo.TSupplementRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TIssueEigenvalueConvert {


    TIssueEigenvalueConvert INSTANCE = Mappers.getMapper(TIssueEigenvalueConvert.class);

    TIssueEigenvalue convert(TIssueEigenvalueVO vo);

    TIssueEigenvalueVO convert(TIssueEigenvalue entity);

    List<TIssueEigenvalueVO> convertList(List<TIssueEigenvalue> list);

}
