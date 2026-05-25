package io.github.jxch.platform.soybean.keycloak.rective.support.config;

import lombok.Data;

@Data
public class SoybeanKeycloakAuthControllerProperties {
    private boolean enabled = true;
    private String prefix = "/auth";
}

