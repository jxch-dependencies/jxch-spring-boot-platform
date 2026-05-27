package io.github.jxch.platform.frp.client.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class UpdateMappingRequest {
    private String type = "tcp";
    private String localIp = "127.0.0.1";

    @Min(1)
    @Max(65535)
    private Integer localPort;

    @Min(1)
    @Max(65535)
    private Integer remotePort;

    private Boolean enabled = true;

    private Map<String, String> annotations = new LinkedHashMap<>();
}
