package io.github.jxch.platform.soybean.keycloak.rective.support.config;

import io.github.jxch.platform.soybean.keycloak.rective.support.client.SoybeanKeycloakClient;
import io.github.jxch.platform.soybean.keycloak.rective.support.handler.SoybeanAuthHandler;
import io.github.jxch.platform.soybean.keycloak.rective.support.rsa.SoybeanRsaKeyHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@ConditionalOnClass(RouterFunction.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class SoybeanAuthControllerConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "soybean.keycloak.auth-controller",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public SoybeanAuthHandler soybeanAuthHandler(
            SoybeanKeycloakClient keycloakClient,
            SoybeanRsaKeyHolder soybeanRsaKeyHolder
    ) {
        return new SoybeanAuthHandler(keycloakClient, soybeanRsaKeyHolder);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "soybean.keycloak.auth-controller",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    @ConditionalOnMissingBean(name = "soybeanAuthRouterFunction")
    public RouterFunction<ServerResponse> soybeanAuthRouterFunction(
            SoybeanAuthHandler handler,
            SoybeanKeycloakProperties properties
    ) {
        String prefix = properties.getAuthController().getPrefix();

        return RouterFunctions.route()
                .path(prefix, builder -> builder
                        .GET("/public-key", handler::publicKey)
                        .POST("/login", handler::login)
                        .POST("/refreshToken", handler::refreshToken)
                        .GET("/getUserInfo", handler::getUserInfo)
                        .GET("/logout", handler::logout)
                )
                .build();
    }

}
