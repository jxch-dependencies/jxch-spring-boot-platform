package io.github.jxch.platform.frp.client.core.admin;

import io.github.jxch.platform.frp.client.config.FrpClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class FrpcAdminApiClient {

    private static final Logger log = LoggerFactory.getLogger(FrpcAdminApiClient.class);

    private final FrpClientProperties properties;
    private final RestClient restClient;

    public FrpcAdminApiClient(FrpClientProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl("http://" + properties.getWebServer().getAddr() + ":" + properties.getWebServer().getPort())
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth(
                        properties.getWebServer().getUsername(),
                        properties.getWebServer().getPassword()))
                .build();
    }

    public boolean ping() {
        if (!properties.getWebServer().isEnabled()) {
            return false;
        }
        try {
            restClient.get()
                    .uri("/")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.debug("frpc admin api ping failed: {}", e.getMessage());
            return false;
        }
    }

    public String getConfig() {
        return restClient.get()
                .uri("/api/config")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    public void reload() {
        restClient.get()
                .uri("/api/reload")
                .retrieve()
                .toBodilessEntity();
    }

    private String basicAuth(String username, String password) {
        String raw = username + ":" + password;
        String token = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }
}
