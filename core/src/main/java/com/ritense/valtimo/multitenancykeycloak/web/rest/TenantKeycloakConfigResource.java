package com.ritense.valtimo.multitenancykeycloak.web.rest;

import com.ritense.valtimo.multitenancykeycloak.domain.TenantKeycloakConfig;
import com.ritense.valtimo.multitenancykeycloak.service.TenantKeycloakConfigService;
import com.ritense.valtimo.multitenancykeycloak.web.dto.TenantKeycloakConfigCreateRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant/keycloak-config")
public class TenantKeycloakConfigResource {
    private final TenantKeycloakConfigService tenantKeycloakConfigService;

    public TenantKeycloakConfigResource(TenantKeycloakConfigService tenantKeycloakConfigService) {
        this.tenantKeycloakConfigService = tenantKeycloakConfigService;
    }

    @PostMapping
    public void addTenantKeycloakConfig(@RequestBody TenantKeycloakConfigCreateRequest request) {
        TenantKeycloakConfig tenantKeycloakConfig = new TenantKeycloakConfig(
            request.getTenantId(),
            request.getKeycloakServerUrl(),
            request.getKeycloakRealm(),
            request.getKeycloakM2mClient()
        );

        tenantKeycloakConfigService.save(tenantKeycloakConfig);
    }
}
