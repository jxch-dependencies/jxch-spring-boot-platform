package io.github.jxch.platform.frp.client.config;


import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

@Data
@Validated
@ConfigurationProperties(prefix = "spring.frp.client")
public class FrpClientProperties {

    private boolean enabled = true;

    @NotNull
    private Path binary;

    @NotNull
    private Path workDir;

    @NotNull
    private Path configFile;

    @NotBlank
    private String serverAddr;

    @Min(1)
    @Max(65535)
    private int serverPort = 7000;

    @Valid
    private Auth auth = new Auth();

    @Valid
    private WebServer webServer = new WebServer();

    @Valid
    private Process process = new Process();

    @Valid
    private Reconcile reconcile = new Reconcile();

    @Valid
    private Management management = new Management();

    @Valid
    private Security security = new Security();

    @Valid
    @NotEmpty
    private List<Proxy> proxies = new ArrayList<>();

    @Data
    public static class Auth {
        @NotBlank
        private String method = "token";
        private String token;
    }

    @Data
    public static class WebServer {
        private boolean enabled = true;
        @NotBlank
        private String addr = "127.0.0.1";
        @Min(1)
        @Max(65535)
        private int port = 17400;
        private String username = "admin";
        private String password = "changeit";
    }

    @Data
    public static class Process {
        private boolean autoStart = true;
        private boolean verifyBeforeStart = true;
        private boolean verifyBeforeReload = true;
        private Duration shutdownTimeout = Duration.ofSeconds(10);
        private Duration statusTimeout = Duration.ofSeconds(3);
    }

    @Data
    public static class Reconcile {
        private boolean startupReconcile = true;
        private boolean rollbackOnFailure = true;
        private Duration reloadWait = Duration.ofSeconds(10);
    }

    @Data
    public static class Management {
        private boolean restEnabled = true;
        @NotBlank
        private String basePath = "/internal/frp/client";
    }

    @Data
    public static class Security {
        private boolean enabled = true;
        @NotBlank
        private String username = "frpadmin";
        @NotBlank
        private String password = "frpadmin-change-me";
        @NotBlank
        private String role = "FRP_ADMIN";
    }

    @Data
    public static class Proxy {
        @NotBlank
        private String name;
        @NotBlank
        private String type = "tcp";
        @NotBlank
        private String localIp = "127.0.0.1";
        @Min(1)
        @Max(65535)
        private Integer localPort;
        @Min(1)
        @Max(65535)
        private Integer remotePort;
        private boolean enabled = true;
        private Map<String, String> annotations = new LinkedHashMap<>();
    }
}
