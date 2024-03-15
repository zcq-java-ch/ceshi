package com.hxls.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;
import com.hxls.framework.mybatis.entity.BaseEntity;

/**
 * banner管理
 *
 * @author zhaohong 
 * @since 1.0.0 2024-03-13
 */
@EqualsAndHashCode(callSuper=false)
@Data
@TableName("t_banner")
public class TBannerEntity extends BaseEntity {

	/**
	* 图片地址
	*/
	private String imageUrl;

	/**
	* 排序
	*/
	private Integer sort;


	/**
	* 状态 0:停用, 1:启用
	*/
	private Integer status;






}