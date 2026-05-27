package io.github.jxch.platform.frp.client.core.model;

public record ProxyRuntimeView(
        String name,
        String type,
        String localIp,
        int localPort,
        int remotePort,
        boolean desiredEnabled,
        boolean localReachable
) {
}
