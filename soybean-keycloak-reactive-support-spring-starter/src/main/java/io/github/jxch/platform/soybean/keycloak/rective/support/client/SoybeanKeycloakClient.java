package io.github.jxch.platform.soybean.keycloak.rective.support.client;


import io.github.jxch.platform.soybean.keycloak.rective.support.config.SoybeanKeycloakProperties;
import io.github.jxch.platform.soybean.keycloak.rective.support.dto.TokenResponse;
import io.github.jxch.platform.soybean.keycloak.rective.support.rsa.SoybeanRsaKeyHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SoybeanKeycloakClient {
    private final SoybeanKeycloakProperties properties;
    private final SoybeanRsaKeyHolder soybeanRsaKeyHolder;

    private WebClient webClient() {
        return WebClient.builder().baseUrl(properties.getServerUrl()).build();
    }

    private String tokenUri() {
        return "/realms/" + properties.getRealm() + "/protocol/openid-connect/token";
    }

    /**
     * 密码模式登录（自动顶掉该用户的旧 session）
     */
    public Mono<TokenResponse> passwordLogin(String username, String encryptedPassword, String keyId) {
        // 1. 解密
        if (properties.isPwdRsa()) {
            try {
                encryptedPassword = soybeanRsaKeyHolder.decrypt(keyId, encryptedPassword);
            } catch (Exception e) {
                log.warn("密码解密失败: {}", e.getMessage());
                return Mono.error(new AuthException("密码格式错误或公钥已过期"));
            }
        }
        final String password = encryptedPassword;

        // 2. 顶号：注销旧 session（失败也不影响登录）
        return kickUserSessions(username)
                .onErrorResume(e -> {
                    log.warn("顶号失败，继续登录流程: {}", e.getMessage());
                    return Mono.empty();
                })
                // 2. 再走正常登录
                .then(Mono.defer(() -> {
                    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
                    form.add("grant_type", "password");
                    form.add("client_id", properties.getClientId());
                    form.add("client_secret", properties.getClientSecret());
                    form.add("username", username);
                    form.add("password", password);
                    form.add("scope", "openid");
                    return postForToken(form);
                }));
    }

    /**
     * 刷新 token
     */
    public Mono<TokenResponse> refresh(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("refresh_token", refreshToken);
        return postForToken(form);
    }

    /**
     * 退出登录
     */
    public Mono<Void> logout(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("refresh_token", refreshToken);
        return webClient().post()
                .uri("/realms/" + properties.getRealm() + "/protocol/openid-connect/logout")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * 通过 Admin API 注销指定用户的所有 session
     */
    public Mono<Void> kickUserSessions(String username) {
        return getAdminToken()
                .flatMap(adminToken -> getUserId(username, adminToken)
                        .flatMap(userId -> doLogoutUser(userId, adminToken))
                );
    }

    /**
     * 用 client_credentials 拿一个 admin token
     * 前提：client 已开启 Service Accounts 并分配了 realm-management 的
     *      view-users + manage-users 角色
     */
    private Mono<String> getAdminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        return postForToken(form).map(TokenResponse::getAccessToken);
    }

    /**
     * 通过 username 查 userId
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Mono<String> getUserId(String username, String adminToken) {
        return webClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/realms/" + properties.getRealm() + "/users")
                        .queryParam("username", username)
                        .queryParam("exact", true)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .bodyToFlux(Map.class)
                .next()
                .map(u -> (String) u.get("id"))
                .switchIfEmpty(Mono.empty());
    }

    /**
     * 调用 Admin API：注销该用户全部 session
     */
    private Mono<Void> doLogoutUser(String userId, String adminToken) {
        return webClient().post()
                .uri("/admin/realms/" + properties.getRealm() + "/users/" + userId + "/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("已注销用户 {} 的所有 session", userId));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Mono<TokenResponse> postForToken(MultiValueMap<String, String> form) {
        return webClient().post()
                .uri(tokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> {
                    TokenResponse t = new TokenResponse();
                    t.setAccessToken((String) body.get("access_token"));
                    t.setRefreshToken((String) body.get("refresh_token"));
                    Object exp = body.get("expires_in");
                    Object rexp = body.get("refresh_expires_in");
                    if (exp != null)  t.setExpiresIn(((Number) exp).longValue());
                    if (rexp != null) t.setRefreshExpiresIn(((Number) rexp).longValue());
                    return t;
                })
                .onErrorMap(WebClientResponseException.class,
                        e -> new AuthException(e.getResponseBodyAsString()));
    }

    public static class AuthException extends RuntimeException {
        public AuthException(String msg) {
            super(msg);
        }
    }
}

