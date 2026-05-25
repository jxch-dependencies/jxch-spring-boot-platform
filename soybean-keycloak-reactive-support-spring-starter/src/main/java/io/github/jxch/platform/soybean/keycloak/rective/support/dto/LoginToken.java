package io.github.jxch.platform.soybean.keycloak.rective.support.dto;

import lombok.Data;

@Data
public class LoginToken {
    private String token;
    private String refreshToken;
}
