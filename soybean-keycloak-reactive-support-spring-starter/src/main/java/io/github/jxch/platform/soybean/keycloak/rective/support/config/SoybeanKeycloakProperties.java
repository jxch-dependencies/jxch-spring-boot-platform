package io.github.jxch.platform.soybean.keycloak.rective.support.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.keycloak")
public class SoybeanKeycloakProperties {
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private boolean pwdRsa = false;
    private SoybeanKeycloakAuthControllerProperties authController;
}
