package com.hxls.api.feign.system;

import com.hxls.api.dto.StorageDTO;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import com.hxls.api.feign.ServerNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传
 *
 *  @author
 */
@FeignClient(name = ServerNames.SYSTEM_SERVER_NAME, contextId = "storage")
public interface StorageFeign {

    /**
     * 文件上传
     */
    @PostMapping(value = "api/storage/upload", produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    StorageDTO upload(@RequestPart("file") MultipartFile file) throws IOException;


    @PostMapping(value = "api/storage/httpUpload", produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    StorageDTO httpUpload(@RequestPart("file") MultipartFile file, @RequestParam("sitePri") String sitePri) throws IOException;
    class MultipartSupportConfig {
        @Bean
        public Encoder feignFormEncoder() {
            return new SpringFormEncoder();
        }
    }
}
