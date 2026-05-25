package io.github.jxch.platform.soybean.keycloak.rective.support.dto;

import lombok.Data;

@Data
public class LoginReq {
    private String userName;
    private String password;
    private String keyId;
}
