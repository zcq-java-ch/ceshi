package com.hxls.system.feign;

import com.hxls.api.dto.StorageDTO;
import com.hxls.framework.common.utils.Result;
import com.hxls.system.vo.SysFileUploadVO;
import lombok.AllArgsConstructor;
import com.hxls.api.feign.system.StorageFeign;
import com.hxls.storage.service.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传
 *
 *  @author
 */
@RestController
@AllArgsConstructor
public class StorageFeignImpl implements StorageFeign {
    private final StorageService storageService;

    @Override
    public StorageDTO upload(MultipartFile file) throws IOException {
        // 是否为空
        if (file.isEmpty()) {
            return null;
        }

        // 上传路径
        String path = storageService.getPath(file.getOriginalFilename());
        // 上传文件
        String url = storageService.upload(file.getBytes(), path);

        // 上传信息
        StorageDTO storage = new StorageDTO();
        storage.setUrl(url);
        storage.setSize(file.getSize());

        return storage;
    }

    @Override
    public StorageDTO httpUpload(MultipartFile file, String sitePri) throws IOException {
        if (file.isEmpty()) {
            return null;
        }
        if (StringUtils.isEmpty(sitePri)){
            return null;
        }

        // 上传路径
        String path = sitePri + "/" + storageService.getPath(file.getOriginalFilename());
        // 上传文件
        String url = storageService.upload(file.getBytes(), path);

        // 上传信息
        StorageDTO storage = new StorageDTO();
        storage.setUrl(url);
        storage.setSize(file.getSize());

        return storage;
    }
}
