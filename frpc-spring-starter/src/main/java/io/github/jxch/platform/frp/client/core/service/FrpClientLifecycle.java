package io.github.jxch.platform.frp.client.core.service;

import jakarta.annotation.PreDestroy;
import org.springframework.context.SmartLifecycle;

public class FrpClientLifecycle implements SmartLifecycle {

    private final FrpClientService service;
    private volatile boolean running = false;

    public FrpClientLifecycle(FrpClientService service) {
        this.service = service;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        service.stop();
        running = false;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @PreDestroy
    public void destroy() {
        stop();
    }
}

