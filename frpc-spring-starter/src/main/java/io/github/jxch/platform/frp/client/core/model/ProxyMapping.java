package io.github.jxch.platform.frp.client.core.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public record ProxyMapping(
        String name,
        String type,
        String localIp,
        int localPort,
        int remotePort,
        boolean enabled,
        Map<String, String> annotations,
        Instant createdAt,
        Instant updatedAt
) {
    public ProxyMapping {
        annotations = annotations == null ? new LinkedHashMap<>() : new LinkedHashMap<>(annotations);
    }
}
