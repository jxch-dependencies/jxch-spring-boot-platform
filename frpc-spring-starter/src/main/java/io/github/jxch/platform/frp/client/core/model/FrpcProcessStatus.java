package io.github.jxch.platform.frp.client.core.model;

import java.time.Instant;

public record FrpcProcessStatus(
        boolean running,
        Long pid,
        Instant startedAt,
        Integer lastExitCode,
        String lastError
) {
}
