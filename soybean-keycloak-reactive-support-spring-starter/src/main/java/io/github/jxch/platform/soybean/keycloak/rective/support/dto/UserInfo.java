package io.github.jxch.platform.soybean.keycloak.rective.support.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInfo {
    private String userId;
    private String userName;
    private List<String> roles;
    private List<String> buttons;
}
