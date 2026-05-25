package io.github.jxch.platform.soybean.keycloak.rective.support.handler;

import io.github.jxch.platform.soybean.keycloak.rective.support.client.SoybeanKeycloakClient;
import io.github.jxch.platform.soybean.keycloak.rective.support.dto.*;
import io.github.jxch.platform.soybean.keycloak.rective.support.rsa.SoybeanRsaKeyHolder;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.*;

public class SoybeanAuthHandler {

    private final SoybeanKeycloakClient keycloak;
    private final SoybeanRsaKeyHolder keyHolder;

    public SoybeanAuthHandler(SoybeanKeycloakClient keycloak, SoybeanRsaKeyHolder keyHolder) {
        this.keycloak = keycloak;
        this.keyHolder = keyHolder;
    }

    /**
     * 前端登录前先调这个，拿 keyId + 公钥
     */
    public Mono<ServerResponse> publicKey(ServerRequest request) {
        return Mono.just(keyHolder.currentPublicKey())
                .map(ApiResponse::ok)
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginReq.class)
                .flatMap(req -> keycloak.passwordLogin(req.getUserName(), req.getPassword(), req.getKeyId()))
                .map(t -> {
                    LoginToken r = new LoginToken();
                    r.setToken(t.getAccessToken());
                    r.setRefreshToken(t.getRefreshToken());
                    return ApiResponse.ok(r);
                })
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> refreshToken(ServerRequest request) {
        return request.bodyToMono(RefreshReq.class)
                .flatMap(req -> keycloak.refresh(req.getRefreshToken()))
                .map(t -> {
                    LoginToken r = new LoginToken();
                    r.setToken(t.getAccessToken());
                    r.setRefreshToken(t.getRefreshToken());
                    return ApiResponse.ok(r);
                })
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> getUserInfo(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (Jwt) ctx.getAuthentication().getPrincipal())
                .map(jwt -> {
                    UserInfo u = new UserInfo();
                    u.setUserId(jwt.getSubject());
                    u.setUserName(jwt.getClaimAsString("preferred_username"));

                    List<String> roles = new ArrayList<>();
                    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                    if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> rs) {
                        rs.forEach(x -> roles.add(String.valueOf(x)));
                    }

                    u.setRoles(roles);
                    u.setButtons(Collections.emptyList());
                    return ApiResponse.ok(u);
                })
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    public Mono<ServerResponse> logout(ServerRequest request) {
        String refreshToken = request.queryParam("refreshToken").orElse(null);
        if (refreshToken == null || refreshToken.isBlank()) {
            return ServerResponse.badRequest()
                    .bodyValue(ApiResponse.fail(BizCode.AUTH_FAIL, "refreshToken不能为空"));
        }

        return keycloak.logout(refreshToken)
                .then(ServerResponse.ok().bodyValue(ApiResponse.ok(null)));
    }
}
