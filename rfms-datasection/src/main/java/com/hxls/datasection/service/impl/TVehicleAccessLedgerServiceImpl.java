package com.hxls.datasection.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hxls.api.feign.system.VehicleFeign;
import com.hxls.datasection.config.StorageImagesProperties;
import com.hxls.datasection.entity.TPersonAccessRecordsEntity;
import com.hxls.datasection.entity.TVehicleAccessRecordsEntity;
import com.hxls.framework.common.constant.Constant;
import com.hxls.framework.security.user.UserDetail;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.PageResult;
import com.hxls.framework.mybatis.service.impl.BaseServiceImpl;
import com.hxls.datasection.convert.TVehicleAccessLedgerConvert;
import com.hxls.datasection.entity.TVehicleAccessLedgerEntity;
import com.hxls.datasection.query.TVehicleAccessLedgerQuery;
import com.hxls.datasection.vo.TVehicleAccessLedgerVO;
import com.hxls.datasection.dao.TVehicleAccessLedgerDao;
import com.hxls.datasection.service.TVehicleAccessLedgerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 车辆进出厂展示台账
 *
 * @author zhaohong 
 * @since 1.0.0 2024-04-18
 */
@Service
@AllArgsConstructor
public class TVehicleAccessLedgerServiceImpl extends BaseServiceImpl<TVehicleAccessLedgerDao, TVehicleAccessLedgerEntity> implements TVehicleAccessLedgerService {

    public StorageImagesProperties properties;
    private VehicleFeign vehicleFeign;

    @Override
    public PageResult<TVehicleAccessLedgerVO> page(TVehicleAccessLedgerQuery query, UserDetail baseUser) {
        IPage<TVehicleAccessLedgerEntity> page = baseMapper.selectPage(getPage(query), getWrapper(query, baseUser));
        List<TVehicleAccessLedgerEntity> records = page.getRecords();
        String domain = properties.getConfig().getDomain();

        if (CollectionUtil.isNotEmpty(records)){
            for (int i = 0; i < records.size(); i++) {
                TVehicleAccessLedgerEntity tVehicleAccessLedgerEntity = records.get(i);
                if (StringUtils.isNotEmpty(tVehicleAccessLedgerEntity.getLicenseImage())){
                    String licenseImage = tVehicleAccessLedgerEntity.getLicenseImage();
                    boolean isHttplicenseImage = licenseImage.startsWith("http");
                    if (isHttplicenseImage){
                        // 是http开头 不处理
                    }else {
                        String newCarUrl = domain + licenseImage;
                        tVehicleAccessLedgerEntity.setLicenseImage(newCarUrl);
                    }
                }
                if (StringUtils.isNotEmpty(tVehicleAccessLedgerEntity.getEnvirList())){
                    String envirList = tVehicleAccessLedgerEntity.getEnvirList();
                    boolean isHttpenvirList = envirList.startsWith("http");
                    if (isHttpenvirList){
                        // 是http开头 不处理
                    }else {
                        String newenvirList = domain + envirList;
                        tVehicleAccessLedgerEntity.setEnvirList(newenvirList);
                    }
                }
                if (StringUtils.isNotEmpty(tVehicleAccessLedgerEntity.getInPic())){
                    String inPic = tVehicleAccessLedgerEntity.getInPic();
                    boolean isHttpinPic = inPic.startsWith("http");
                    if (isHttpinPic){
                        // 是http开头 不处理
                    }else {
                        String newinPic = domain + inPic;
                        tVehicleAccessLedgerEntity.setInPic(newinPic);
                    }
                }

                if (StringUtils.isNotEmpty(tVehicleAccessLedgerEntity.getOutPic())){
                    String outPic = tVehicleAccessLedgerEntity.getOutPic();
                    boolean isHttpoutPic = outPic.startsWith("http");
                    if (isHttpoutPic){
                        // 是http开头 不处理
                    }else {
                        String newoutPic = domain + outPic;
                        tVehicleAccessLedgerEntity.setOutPic(newoutPic);
                    }
                }
            }
        }

        return new PageResult<>(TVehicleAccessLedgerConvert.INSTANCE.convertList(page.getRecords()), page.getTotal());
    }

    private QueryWrapper<TVehicleAccessLedgerEntity> getWrapper(TVehicleAccessLedgerQuery query, UserDetail baseUser){
        QueryWrapper<TVehicleAccessLedgerEntity> wrapper = Wrappers.query();
        wrapper.eq("status",1);
        wrapper.eq("deleted",0);
        wrapper.eq(ObjectUtil.isNotEmpty(query.getSiteId()),"site_id",query.getSiteId());
        wrapper.like(StringUtils.isNotBlank(query.getPlateNumber()), "plate_number",query.getPlateNumber());
        wrapper.eq(StringUtils.isNotBlank(query.getVehicleModel()), "vehicle_model",query.getVehicleModel());
        wrapper.like(StringUtils.isNotBlank(query.getFleetName()), "fleet_name",query.getFleetName());
        // 检查数组是否为空，如果不为空再调用 between 方法
        if (CollectionUtils.isNotEmpty(query.getInRecordTimeArr())) {
            wrapper.between("in_time", query.getInRecordTimeArr().get(0), query.getInRecordTimeArr().get(1));
        }

        // 检查数组是否为空，如果不为空再调用 between 方法
        if (CollectionUtils.isNotEmpty(query.getOutRecordTimeArr())) {
            wrapper.between("out_time", query.getOutRecordTimeArr().get(0), query.getOutRecordTimeArr().get(1));
        }
        if (baseUser.getSuperAdmin().equals(Constant.SUPER_ADMIN)){

        }else {
            List<Long> dataScopeList = baseUser.getDataScopeList();
            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(dataScopeList)){
                wrapper.in("site_id", dataScopeList);
            }else {
                wrapper.in("site_id", "null");
            }
        }
        return wrapper;
    }

    @Override
    public void save(TVehicleAccessLedgerVO vo) {
        TVehicleAccessLedgerEntity entity = TVehicleAccessLedgerConvert.INSTANCE.convert(vo);

        baseMapper.insert(entity);
    }

    @Override
    public void update(TVehicleAccessLedgerVO vo) {
        TVehicleAccessLedgerEntity entity = TVehicleAccessLedgerConvert.INSTANCE.convert(vo);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        removeByIds(idList);
    }

    @Override
    public PageResult<TVehicleAccessLedgerVO> makeImages(PageResult<TVehicleAccessLedgerVO> page) {
        String domain = properties.getConfig().getDomain();
        List<TVehicleAccessLedgerVO> tVehicleAccessLedgerVOList = page.getList();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tVehicleAccessLedgerVOList)){
            List<String> plateNumberList = tVehicleAccessLedgerVOList.stream()
                    .map(TVehicleAccessLedgerVO::getPlateNumber)
                    .collect(Collectors.toList());

            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(plateNumberList)){
                // 调用微服务方法，查询这些车牌的所有图片信息，【行驶证】【随车清单】
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("plateNumberList", plateNumberList);
                JSONArray vehiclePhotosThroughLicensePlateList = vehicleFeign.queryVehiclePhotosThroughLicensePlateList(jsonObject);

                for (int i = 0; i < tVehicleAccessLedgerVOList.size(); i++) {
                    TVehicleAccessLedgerVO tVehicleAccessLedgerVO = tVehicleAccessLedgerVOList.get(i);
                    String plateNumber = tVehicleAccessLedgerVO.getPlateNumber();
                    // 查询该车牌在vehiclePhotosThroughLicensePlateList 中的车辆图片数据
                    JSONObject imagesMap = getPlateNumberByList(plateNumber, vehiclePhotosThroughLicensePlateList);
                    if (ObjectUtil.isNotEmpty(imagesMap)){
                        String licenseImage = imagesMap.getString("licenseImage");
                        String images = imagesMap.getString("images");
                        boolean isHttplicenseImage = licenseImage.startsWith("http");
                        if (isHttplicenseImage){
                            // 是http开头 不处理
                            tVehicleAccessLedgerVO.setLicenseImage(licenseImage);
                        }else {
                            String newCarUrl = domain + licenseImage;
                            tVehicleAccessLedgerVO.setLicenseImage(newCarUrl);
                        }

                        boolean isHttpenvirList = images.startsWith("http");
                        if (isHttpenvirList){
                            // 是http开头 不处理
                            tVehicleAccessLedgerVO.setEnvirList(images);
                        }else {
                            String newenvirList = domain + images;
                            tVehicleAccessLedgerVO.setEnvirList(newenvirList);
                        }
                    }
                }
            }
        }
        return page;
    }

    private JSONObject getPlateNumberByList(String plateNumber, JSONArray vehiclePhotosThroughLicensePlateList) {
        JSONObject objectObjectHashMap = new JSONObject();
        for (int i = 0; i < vehiclePhotosThroughLicensePlateList.size(); i++) {
            JSONObject carEntity = vehiclePhotosThroughLicensePlateList.getJSONObject(i);
            String carPlateNumber = carEntity.getString("plateNumber");
            if (plateNumber.equals(carPlateNumber)){
                objectObjectHashMap.put("licenseImage", carEntity.getString("licenseImage"));
                objectObjectHashMap.put("images", carEntity.getString("images"));
            }
        }
        return objectObjectHashMap;
    }
}