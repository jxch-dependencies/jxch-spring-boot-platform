package io.github.jxch.platform.frp.client.core.process;

import io.github.jxch.platform.frp.client.config.FrpClientProperties;
import io.github.jxch.platform.frp.client.core.model.FrpcBinaryInfo;
import io.github.jxch.platform.frp.client.core.model.FrpcProcessStatus;
import io.github.jxch.platform.frp.client.ex.FrpcCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocalFrpcProcessManager implements FrpcProcessManager {

    private static final Logger log = LoggerFactory.getLogger(LocalFrpcProcessManager.class);

    private final FrpClientProperties properties;

    private volatile Process process;
    private volatile Instant startedAt;
    private volatile Integer lastExitCode;
    private volatile String lastError;

    public LocalFrpcProcessManager(FrpClientProperties properties) {
        this.properties = properties;
    }

    @Override
    public FrpcBinaryInfo inspectBinary() {
        Path binary = properties.getBinary();
        boolean exists = Files.exists(binary);
        boolean executable = Files.isExecutable(binary);
        String version = null;

        if (exists && executable) {
            try {
                CommandResult result = runCommand(List.of(binary.toString(), "--version"), properties.getWorkDir());
                version = result.stdout().isBlank() ? result.stderr() : result.stdout();
            } catch (Exception e) {
                version = "unknown";
            }
        }

        return new FrpcBinaryInfo(binary.toString(), exists, executable, version);
    }

    @Override
    public FrpcProcessStatus status() {
        boolean running = process != null && process.isAlive();
        Long pid = running ? process.pid() : null;
        return new FrpcProcessStatus(running, pid, startedAt, lastExitCode, lastError);
    }

    @Override
    public CommandResult verify(Path configFile) {
        return runCommand(
                List.of(properties.getBinary().toString(), "verify", "-c", configFile.toString()),
                properties.getWorkDir()
        );
    }

    @Override
    public synchronized void start(Path configFile) {
        if (process != null && process.isAlive()) {
            log.info("frpc already running, pid={}", process.pid());
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(properties.getBinary().toString(), "-c", configFile.toString());
            pb.directory(properties.getWorkDir().toFile());
            process = pb.start();
            startedAt = Instant.now();
            lastExitCode = null;
            lastError = null;

            Thread.ofVirtual().name("frpc-stdout-reader").start(() -> consume(process.inputReader(), false));
            Thread.ofVirtual().name("frpc-stderr-reader").start(() -> consume(process.errorReader(), true));

            log.info("frpc started, pid={}", process.pid());
        } catch (Exception e) {
            lastError = e.getMessage();
            throw new FrpcCommandException("Failed to start frpc", e);
        }
    }

    @Override
    public synchronized void stop() {
        if (process == null) {
            return;
        }
        try {
            process.destroy();
            boolean exited = process.waitFor(properties.getProcess().getShutdownTimeout().toSeconds(), TimeUnit.SECONDS);
            if (!exited) {
                process.destroyForcibly();
            }
            lastExitCode = process.exitValue();
            log.info("frpc stopped, exitCode={}", lastExitCode);
        } catch (Exception e) {
            throw new FrpcCommandException("Failed to stop frpc", e);
        }
    }

    @Override
    public CommandResult reload(Path configFile) {
        return runCommand(
                List.of(properties.getBinary().toString(), "reload", "-c", configFile.toString()),
                properties.getWorkDir()
        );
    }

    private CommandResult runCommand(List<String> command, Path workDir) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir.toFile());
            Process p = pb.start();

            String stdout;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                stdout = br.lines().reduce("", (a, b) -> a + b + "\n");
            }

            String stderr;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                stderr = br.lines().reduce("", (a, b) -> a + b + "\n");
            }

            int exit = p.waitFor();
            return new CommandResult(exit == 0, exit, stdout.strip(), stderr.strip());
        } catch (Exception e) {
            throw new FrpcCommandException("Failed to execute command: " + String.join(" ", command), e);
        }
    }

    private void consume(BufferedReader reader, boolean error) {
        reader.lines().forEach(line -> {
            if (error) {
                log.warn("[frpc] {}", line);
                lastError = line;
            } else {
                log.info("[frpc] {}", line);
            }
        });
    }
}
