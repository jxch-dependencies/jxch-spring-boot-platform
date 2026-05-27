package io.github.jxch.platform.frp.client.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class CreateMappingRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String type = "tcp";

    @NotBlank
    private String localIp = "127.0.0.1";

    @Min(1)
    @Max(65535)
    private Integer localPort;

    @Min(1)
    @Max(65535)
    private Integer remotePort;

    private boolean enabled = true;

    private Map<String, String> annotations = new LinkedHashMap<>();
}
