package io.github.jxch.platform.frp.client.core.model;

import java.time.Instant;
import java.util.List;

public record FrpClientStatusView(
        FrpcBinaryInfo binary,
        FrpcProcessStatus process,
        AdminApiStatus adminApi,
        Instant lastReconcileTime,
        boolean lastReconcileSuccess,
        String lastReconcileMessage,
        List<ProxyRuntimeView> proxies
) {
}
