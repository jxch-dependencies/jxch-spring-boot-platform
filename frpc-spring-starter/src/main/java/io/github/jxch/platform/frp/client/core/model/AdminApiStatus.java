package io.github.jxch.platform.frp.client.core.model;

public record AdminApiStatus(
        boolean enabled,
        boolean reachable,
        String baseUrl
) {
}
