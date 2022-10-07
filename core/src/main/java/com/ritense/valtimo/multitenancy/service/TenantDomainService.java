package com.ritense.valtimo.multitenancy.service;

import com.ritense.valtimo.multitenancy.domain.TenantDomain;
import com.ritense.valtimo.multitenancy.repository.TenantDomainRepository;

public class TenantDomainService {
    private final TenantDomainRepository tenantDomainRepository;

    public TenantDomainService(TenantDomainRepository tenantDomainRepository) {
        this.tenantDomainRepository = tenantDomainRepository;
    }

    public TenantDomain save(TenantDomain tenantDomain) {
        return tenantDomainRepository.save(tenantDomain);
    }
}
