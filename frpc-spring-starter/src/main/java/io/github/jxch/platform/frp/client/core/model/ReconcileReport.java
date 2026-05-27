package io.github.jxch.platform.frp.client.core.model;

import java.time.Instant;
import java.util.List;

public record ReconcileReport(
        boolean success,
        Instant startedAt,
        Instant endedAt,
        String message,
        List<String> warnings
) {
}
