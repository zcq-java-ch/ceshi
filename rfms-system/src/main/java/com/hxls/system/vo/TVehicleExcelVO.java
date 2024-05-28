package com.hxls.system.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.TransPojo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 通用车辆导入VO
*
* @author zhaohong
* @since 1.0.0 2024年4月25日
*/
@Data
public class TVehicleExcelVO implements Serializable, TransPojo {
	private static final long serialVersionUID = 1L;

	/**
	 * 本属性对于导出无用，只是用于翻译
	 */
	@ExcelIgnore
	private Long id;


	@ExcelProperty("车牌号")
	private String licensePlate;


	@ExcelIgnore
	@Trans(type = TransType.DICTIONARY, key = "car_type", ref = "carTypeName")
	private String carType;

	@ExcelProperty("车型")
	private String carTypeName;

	@ExcelIgnore
	@Trans(type = TransType.DICTIONARY, key = "emission_standard", ref = "emissionStandardName")
	private String emissionStandard;

	@ExcelProperty("车辆排放标准")
	private String emissionStandardName;

	@ExcelProperty("车辆注册日期")
	private Date registrationDate;

	@ExcelProperty("车辆识别代码")
	private String vinNumber;

	@ExcelProperty("发动机号")
	private String engineNumber;

	@ExcelProperty("车队名称")
	private String fleetName;

	@ExcelProperty("最大运输量")
	private String maxCapacity;

	@ExcelProperty("行驶证照片名（不能重复）")
	private String licenseImage;

	@ExcelProperty("车辆照片名（不能重复）")
	private String imageUrl;

	@ExcelProperty("随车清单")
	private String images;

	@ExcelProperty("司机手机号")
	private String driverMobile;


}
