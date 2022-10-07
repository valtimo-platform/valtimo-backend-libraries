package com.ritense.valtimo.multitenancy.repository;

import com.ritense.valtimo.multitenancy.domain.TenantDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantDomainRepository extends JpaRepository<TenantDomain, UUID> {
    Optional<TenantDomain> findByDomain(String domain);
}
