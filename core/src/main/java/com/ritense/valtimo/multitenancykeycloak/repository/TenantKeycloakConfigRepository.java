package com.ritense.valtimo.multitenancykeycloak.repository;

import com.ritense.valtimo.multitenancykeycloak.domain.TenantKeycloakConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantKeycloakConfigRepository extends JpaRepository<TenantKeycloakConfig, UUID> {
    Optional<TenantKeycloakConfig> findByTenantId(String tenantId);
}
