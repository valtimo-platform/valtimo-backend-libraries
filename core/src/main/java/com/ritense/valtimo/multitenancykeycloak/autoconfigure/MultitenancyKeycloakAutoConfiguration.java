package com.ritense.valtimo.multitenancykeycloak.autoconfigure;

import com.ritense.valtimo.multitenancy.security.MultitenancyHttpSecurityConfigurer;
import com.ritense.valtimo.multitenancy.service.TenantDomainService;
import com.ritense.valtimo.multitenancykeycloak.provider.MultitenancyKeycloakSecretKeyProvider;
import com.ritense.valtimo.multitenancykeycloak.repository.TenantKeycloakConfigRepository;
import com.ritense.valtimo.multitenancykeycloak.security.MultitenancyKeycloakHttpSecurityConfigurer;
import com.ritense.valtimo.multitenancykeycloak.service.TenantKeycloakConfigService;
import com.ritense.valtimo.multitenancykeycloak.web.rest.TenantKeycloakConfigResource;
import io.netty.handler.logging.LogLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Configuration
@EnableJpaRepositories(basePackages = "com.ritense.valtimo.multitenancykeycloak.repository")
@EntityScan("com.ritense.valtimo.multitenancykeycloak.domain")
public class MultitenancyKeycloakAutoConfiguration {
    @Order(290)
    @Bean
    public MultitenancyKeycloakHttpSecurityConfigurer multitenancyKeycloakHttpSecurityConfigurer() {
        return new MultitenancyKeycloakHttpSecurityConfigurer();
    }

    @Bean
    public TenantKeycloakConfigService tenantKeycloakConfigService(
        TenantKeycloakConfigRepository tenantKeycloakConfigRepository
    ) {
        return new TenantKeycloakConfigService(tenantKeycloakConfigRepository);
    }

    @Bean
    public TenantKeycloakConfigResource tenantKeycloakConfigResource(
        TenantKeycloakConfigService tenantKeycloakConfigService
    ) {
        return new TenantKeycloakConfigResource(tenantKeycloakConfigService);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().clientConnector(
            new ReactorClientHttpConnector(
                HttpClient.create().wiretap(
                    "reactor.netty.http.client.HttpClient",
                    LogLevel.INFO,
                    AdvancedByteBufFormat.TEXTUAL
                )
            )
        ).build();
    }

    @Bean
    @ConditionalOnMissingBean(MultitenancyKeycloakSecretKeyProvider.class)
    @ConditionalOnProperty("valtimo.app.multitenant")
    public MultitenancyKeycloakSecretKeyProvider multitenancyKeycloakSecretKeyProvider(
        TenantKeycloakConfigService tenantKeycloakConfigService,
        WebClient webClient
    ) {
        return new MultitenancyKeycloakSecretKeyProvider(
            tenantKeycloakConfigService,
            webClient
        );
    }
}
