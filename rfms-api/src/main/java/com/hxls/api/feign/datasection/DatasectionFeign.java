package com.hxls.api.feign.datasection;

import com.hxls.api.dto.datasection.TPersonAccessRecordsDTO;
import com.hxls.api.feign.ServerNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ServerNames.DATASECTION_SERVER_NAME)
public interface DatasectionFeign {

    /**
     * 出入补录生成进出记录
     */
    @PostMapping(value = "api/tPersonAccess/savePersonAccessRecords")
    public boolean savePersonAccessRecords(@RequestBody TPersonAccessRecordsDTO accessRecordsDTO);

}
