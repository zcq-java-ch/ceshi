package com.hxls.system.controller;

import com.hxls.framework.operatelog.annotations.OperateLog;
import com.hxls.framework.operatelog.enums.OperateTypeEnum;
import com.hxls.system.vo.SysFileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import com.hxls.framework.common.utils.Result;
import com.hxls.storage.service.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传
 *
 * @author
 *
 */
@RestController
@RequestMapping("sys/file")
@Tag(name = "文件上传")
@AllArgsConstructor
public class SysFileUploadController {
    private final StorageService storageService;

    @PostMapping("upload")
    @Operation(summary = "上传")
    @OperateLog(type = OperateTypeEnum.INSERT)
    public Result<SysFileUploadVO> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return Result.error("请选择需要上传的文件");
        }

        // 上传路径
        String path = storageService.getPath(file.getOriginalFilename());
        // 上传文件
        String url = storageService.upload(file.getBytes(), path);

        SysFileUploadVO vo = new SysFileUploadVO();
        vo.setUrl(url);
        vo.setSize(file.getSize());
        vo.setName(file.getOriginalFilename());
        vo.setPlatform(storageService.properties.getConfig().getType().name());

        return Result.ok(vo);
    }

    @PostMapping("httpUpload")
    @Operation(summary = "agent上传专用")
    @OperateLog(type = OperateTypeEnum.INSERT)
    public Result<SysFileUploadVO> httpUpload(@RequestParam("file") MultipartFile file, @RequestParam("sitePri") String sitePri) throws Exception {
        if (file.isEmpty()) {
            return Result.error("请选择需要上传的文件");
        }
        if (StringUtils.isEmpty(sitePri)){
            return Result.error("远程上传必须有站点全拼接");
        }

        // 上传路径
        String path = sitePri + "/" + storageService.getPath(file.getOriginalFilename());
        // 上传文件
        String url = storageService.upload(file.getBytes(), path);

        SysFileUploadVO vo = new SysFileUploadVO();
        vo.setUrl(url);
        vo.setSize(file.getSize());
        vo.setName(file.getOriginalFilename());
        vo.setPlatform(storageService.properties.getConfig().getType().name());

        return Result.ok(vo);
    }

}
