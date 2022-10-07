package com.ritense.valtimo.multitenancy.autoconfigure;

import com.ritense.valtimo.multitenancy.repository.TenantDomainRepository;
import com.ritense.valtimo.multitenancy.service.TenantDomainService;
import com.ritense.valtimo.multitenancy.web.rest.TenantDomainResource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.ritense.valtimo.multitenancy.repository")
@EntityScan("com.ritense.valtimo.multitenancy.domain")
public class MultitenancyAutoConfiguration {
    @Bean
    public TenantDomainService tenantDomainService(
        TenantDomainRepository tenantDomainRepository
    ) {
        return new TenantDomainService(tenantDomainRepository);
    }

    @Bean
    public TenantDomainResource tenantDomainResource(
        TenantDomainService tenantDomainService
    ) {
        return new TenantDomainResource(tenantDomainService);
    }
}
