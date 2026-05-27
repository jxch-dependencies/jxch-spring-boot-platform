package io.github.jxch.platform.frp.client.core.service;

import io.github.jxch.platform.frp.client.config.FrpClientProperties;
import io.github.jxch.platform.frp.client.core.admin.FrpcAdminApiClient;
import io.github.jxch.platform.frp.client.core.config.FrpcConfigRenderer;
import io.github.jxch.platform.frp.client.core.model.*;
import io.github.jxch.platform.frp.client.core.process.CommandResult;
import io.github.jxch.platform.frp.client.core.process.FrpcProcessManager;
import io.github.jxch.platform.frp.client.core.repository.ProxyMappingRepository;
import io.github.jxch.platform.frp.client.dto.CreateMappingRequest;
import io.github.jxch.platform.frp.client.dto.UpdateMappingRequest;
import io.github.jxch.platform.frp.client.ex.MappingConflictException;
import io.github.jxch.platform.frp.client.ex.MappingNotFoundException;
import io.github.jxch.platform.frp.client.ex.ReconcileFailedException;
import io.github.jxch.platform.frp.client.util.FileUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class FrpClientService {

    private static final Logger log = LoggerFactory.getLogger(FrpClientService.class);

    private final FrpClientProperties properties;
    private final ProxyMappingRepository repository;
    private final FrpcConfigRenderer renderer;
    private final FrpcProcessManager processManager;
    private final FrpcAdminApiClient adminApiClient;
    private final LocalPortChecker localPortChecker;

    private final Counter reconcileCounter;
    private final Counter reconcileFailureCounter;

    private final ReentrantLock lock = new ReentrantLock();

    private volatile Instant lastReconcileTime;
    private volatile boolean lastReconcileSuccess;
    private volatile String lastReconcileMessage = "never";

    public FrpClientService(
            FrpClientProperties properties,
            ProxyMappingRepository repository,
            FrpcConfigRenderer renderer,
            FrpcProcessManager processManager,
            FrpcAdminApiClient adminApiClient,
            LocalPortChecker localPortChecker,
            MeterRegistry meterRegistry
    ) {
        this.properties = properties;
        this.repository = repository;
        this.renderer = renderer;
        this.processManager = processManager;
        this.adminApiClient = adminApiClient;
        this.localPortChecker = localPortChecker;
        if (meterRegistry != null) {
            this.reconcileCounter = meterRegistry.counter("frp.client.reconcile.count");
            this.reconcileFailureCounter = meterRegistry.counter("frp.client.reconcile.failure.count");
        } else {
            this.reconcileCounter = null;
            this.reconcileFailureCounter = null;
        }
    }

    @PostConstruct
    public void init() {
        List<ProxyMapping> bootstrap = new ArrayList<>();
        Instant now = Instant.now();

        for (FrpClientProperties.Proxy proxy : properties.getProxies()) {
            if (!"tcp".equalsIgnoreCase(proxy.getType())) {
                throw new IllegalArgumentException("Only tcp proxy is supported in v1: " + proxy.getName());
            }
            bootstrap.add(new ProxyMapping(
                    proxy.getName(),
                    proxy.getType(),
                    proxy.getLocalIp(),
                    proxy.getLocalPort(),
                    proxy.getRemotePort(),
                    proxy.isEnabled(),
                    proxy.getAnnotations(),
                    now,
                    now
            ));
        }

        repository.replaceAll(bootstrap);

        if (properties.getProcess().isAutoStart()) {
            if (properties.getReconcile().isStartupReconcile()) {
                reconcile("startup");
            } else {
                writeConfigOnly();
                processManager.start(properties.getConfigFile());
            }
        }
    }

    public FrpClientStatusView status() {
        FrpcBinaryInfo binaryInfo = processManager.inspectBinary();
        FrpcProcessStatus processStatus = processManager.status();
        boolean adminReachable = adminApiClient.ping();

        List<ProxyRuntimeView> runtimeViews = repository.findAll().stream()
                .map(p -> new ProxyRuntimeView(
                        p.name(),
                        p.type(),
                        p.localIp(),
                        p.localPort(),
                        p.remotePort(),
                        p.enabled(),
                        localPortChecker.reachable(p.localIp(), p.localPort(), properties.getProcess().getStatusTimeout())
                ))
                .toList();

        return new FrpClientStatusView(
                binaryInfo,
                processStatus,
                new AdminApiStatus(properties.getWebServer().isEnabled(), adminReachable,
                        "http://" + properties.getWebServer().getAddr() + ":" + properties.getWebServer().getPort()),
                lastReconcileTime,
                lastReconcileSuccess,
                lastReconcileMessage,
                runtimeViews
        );
    }

    public List<ProxyMapping> listMappings() {
        return repository.findAll();
    }

    public ProxyMapping getMapping(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new MappingNotFoundException("Mapping not found: " + name));
    }

    public ProxyMapping createMapping(CreateMappingRequest request) {
        lock.lock();
        try {
            if (repository.existsByName(request.getName())) {
                throw new MappingConflictException("Mapping already exists: " + request.getName());
            }

            Instant now = Instant.now();
            ProxyMapping mapping = new ProxyMapping(
                    request.getName(),
                    request.getType(),
                    request.getLocalIp(),
                    request.getLocalPort(),
                    request.getRemotePort(),
                    request.isEnabled(),
                    request.getAnnotations(),
                    now,
                    now
            );
            repository.save(mapping);
            reconcile("create:" + request.getName());
            return mapping;
        } finally {
            lock.unlock();
        }
    }

    public ProxyMapping updateMapping(String name, UpdateMappingRequest request) {
        lock.lock();
        try {
            ProxyMapping existing = repository.findByName(name)
                    .orElseThrow(() -> new MappingNotFoundException("Mapping not found: " + name));

            ProxyMapping updated = new ProxyMapping(
                    existing.name(),
                    request.getType() == null ? existing.type() : request.getType(),
                    request.getLocalIp() == null ? existing.localIp() : request.getLocalIp(),
                    request.getLocalPort() == null ? existing.localPort() : request.getLocalPort(),
                    request.getRemotePort() == null ? existing.remotePort() : request.getRemotePort(),
                    request.getEnabled() == null ? existing.enabled() : request.getEnabled(),
                    request.getAnnotations() == null || request.getAnnotations().isEmpty() ? existing.annotations() : request.getAnnotations(),
                    existing.createdAt(),
                    Instant.now()
            );

            repository.save(updated);
            reconcile("update:" + name);
            return updated;
        } finally {
            lock.unlock();
        }
    }

    public void deleteMapping(String name) {
        lock.lock();
        try {
            if (!repository.existsByName(name)) {
                throw new MappingNotFoundException("Mapping not found: " + name);
            }
            repository.deleteByName(name);
            reconcile("delete:" + name);
        } finally {
            lock.unlock();
        }
    }

    public ReconcileReport reconcile(String trigger) {
        lock.lock();
        try {
            Instant started = Instant.now();
            reconcileCounter.increment();

            List<ProxyMapping> mappings = repository.findAll();
            String rendered = renderer.render(properties, mappings);
            Path configFile = properties.getConfigFile();

            FileUtil.writeAtomic(configFile, rendered);

            if (properties.getProcess().isVerifyBeforeStart() || properties.getProcess().isVerifyBeforeReload()) {
                CommandResult verify = processManager.verify(configFile);
                if (!verify.success()) {
                    lastReconcile(started, false, "verify failed: " + verify.stderr());
                    reconcileFailureCounter.increment();
                    throw new ReconcileFailedException("frpc verify failed: " + verify.stderr());
                }
            }

            FrpcProcessStatus processStatus = processManager.status();
            if (!processStatus.running()) {
                processManager.start(configFile);
            } else {
                CommandResult reload = processManager.reload(configFile);
                if (!reload.success()) {
                    if (properties.getReconcile().isRollbackOnFailure()) {
                        FileUtil.restoreBackup(configFile);
                        CommandResult rollbackReload = processManager.reload(configFile);
                        lastReconcile(started, false,
                                "reload failed, rollback attempted: " + rollbackReload.stderr());
                    } else {
                        lastReconcile(started, false, "reload failed: " + reload.stderr());
                    }
                    reconcileFailureCounter.increment();
                    throw new ReconcileFailedException("frpc reload failed: " + reload.stderr());
                }
            }

            try {
                Thread.sleep(properties.getReconcile().getReloadWait().toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            boolean adminOk = adminApiClient.ping();
            if (!adminOk) {
                lastReconcile(started, false, "admin api unreachable after reconcile");
                reconcileFailureCounter.increment();
                throw new ReconcileFailedException("admin api unreachable after reconcile");
            }

            lastReconcile(started, true, "ok, trigger=" + trigger);
            return new ReconcileReport(true, started, Instant.now(), "ok", List.of());
        } finally {
            lock.unlock();
        }
    }

    public void restart() {
        lock.lock();
        try {
            processManager.stop();
            processManager.start(properties.getConfigFile());
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        processManager.stop();
    }

    private void writeConfigOnly() {
        String rendered = renderer.render(properties, repository.findAll());
        FileUtil.writeAtomic(properties.getConfigFile(), rendered);
    }

    private void lastReconcile(Instant started, boolean success, String message) {
        this.lastReconcileTime = Instant.now();
        this.lastReconcileSuccess = success;
        this.lastReconcileMessage = message;
        log.info("frp reconcile finished, startedAt={}, success={}, message={}", started, success, message);
    }
}
