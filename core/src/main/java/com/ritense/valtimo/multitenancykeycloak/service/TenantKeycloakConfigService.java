package com.ritense.valtimo.multitenancykeycloak.service;

import com.ritense.valtimo.multitenancykeycloak.domain.TenantKeycloakConfig;
import com.ritense.valtimo.multitenancykeycloak.repository.TenantKeycloakConfigRepository;

public class TenantKeycloakConfigService {
    private final TenantKeycloakConfigRepository tenantKeycloakConfigRepository;

    public TenantKeycloakConfigService(TenantKeycloakConfigRepository tenantKeycloakConfigRepository) {
        this.tenantKeycloakConfigRepository = tenantKeycloakConfigRepository;
    }

    public TenantKeycloakConfig findByTenantId(String tenantId) {
        return tenantKeycloakConfigRepository.findByTenantId(tenantId).orElse(null);
    }

    public TenantKeycloakConfig save(TenantKeycloakConfig tenantKeycloakConfig) {
        return tenantKeycloakConfigRepository.save(tenantKeycloakConfig);
    }
}
