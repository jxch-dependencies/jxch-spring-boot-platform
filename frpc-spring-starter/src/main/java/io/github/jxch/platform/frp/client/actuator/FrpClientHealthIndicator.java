package io.github.jxch.platform.frp.client.actuator;

import io.github.jxch.platform.frp.client.core.service.FrpClientService;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

public class FrpClientHealthIndicator implements HealthIndicator {

    private final FrpClientService service;

    public FrpClientHealthIndicator(FrpClientService service) {
        this.service = service;
    }

    @Override
    public Health health() {
        var status = service.status();

        boolean up = status.binary().exists()
                && status.binary().executable()
                && status.process().running()
                && status.adminApi().reachable();

        Health.Builder builder = up ? Health.up() : Health.down();

        return builder
                .withDetail("binary", status.binary())
                .withDetail("process", status.process())
                .withDetail("adminApi", status.adminApi())
                .withDetail("lastReconcileTime", status.lastReconcileTime())
                .withDetail("lastReconcileSuccess", status.lastReconcileSuccess())
                .withDetail("lastReconcileMessage", status.lastReconcileMessage())
                .withDetail("proxies", status.proxies())
                .build();
    }
}
