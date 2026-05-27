package io.github.jxch.platform.frp.client.autoconfig;

import io.github.jxch.platform.frp.client.actuator.FrpClientHealthIndicator;
import io.github.jxch.platform.frp.client.config.FrpClientProperties;
import io.github.jxch.platform.frp.client.core.admin.FrpcAdminApiClient;
import io.github.jxch.platform.frp.client.core.config.FrpcConfigRenderer;
import io.github.jxch.platform.frp.client.core.config.TomlFrpcConfigRenderer;
import io.github.jxch.platform.frp.client.core.process.FrpcProcessManager;
import io.github.jxch.platform.frp.client.core.process.LocalFrpcProcessManager;
import io.github.jxch.platform.frp.client.core.repository.InMemoryProxyMappingRepository;
import io.github.jxch.platform.frp.client.core.repository.ProxyMappingRepository;
import io.github.jxch.platform.frp.client.core.service.FrpClientLifecycle;
import io.github.jxch.platform.frp.client.core.service.FrpClientService;
import io.github.jxch.platform.frp.client.core.service.LocalPortChecker;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(FrpClientProperties.class)
@ConditionalOnProperty(prefix = "spring.frp.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FrpClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProxyMappingRepository proxyMappingRepository() {
        return new InMemoryProxyMappingRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public FrpcConfigRenderer frpcConfigRenderer() {
        return new TomlFrpcConfigRenderer();
    }

    @Bean
    @ConditionalOnMissingBean
    public FrpcProcessManager frpcProcessManager(FrpClientProperties properties) {
        return new LocalFrpcProcessManager(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FrpcAdminApiClient frpcAdminApiClient(FrpClientProperties properties) {
        return new FrpcAdminApiClient(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public LocalPortChecker localPortChecker() {
        return new LocalPortChecker();
    }

    @Bean
    @ConditionalOnMissingBean
    public FrpClientService frpClientService(
            FrpClientProperties properties,
            ProxyMappingRepository repository,
            FrpcConfigRenderer renderer,
            FrpcProcessManager processManager,
            FrpcAdminApiClient adminApiClient,
            LocalPortChecker localPortChecker,
            ObjectProvider<MeterRegistry> meterRegistryProvider
    ) {
        return new FrpClientService(
                properties,
                repository,
                renderer,
                processManager,
                adminApiClient,
                localPortChecker,
                meterRegistryProvider.getIfAvailable()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public FrpClientLifecycle frpClientLifecycle(FrpClientService service) {
        return new FrpClientLifecycle(service);
    }

    @Bean
    @ConditionalOnMissingBean
    public HealthIndicator frpClientHealthIndicator(FrpClientService service) {
        return new FrpClientHealthIndicator(service);
    }
}
