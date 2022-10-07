package com.ritense.valtimo.multitenancy.web.rest;

import com.ritense.valtimo.multitenancy.domain.TenantDomain;
import com.ritense.valtimo.multitenancy.service.TenantDomainService;
import com.ritense.valtimo.multitenancy.web.dto.TenantDomeinCreateRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/tenant/domain")
public class TenantDomainResource {
    private final TenantDomainService tenantDomainService;

    public TenantDomainResource(TenantDomainService tenantDomainService) {
        this.tenantDomainService = tenantDomainService;
    }

    @PostMapping
    public void addTenantDomain(@RequestBody @Valid TenantDomeinCreateRequest request) {
        TenantDomain tenantDomain = new TenantDomain(
            request.getTenantId(),
            request.getDomain()
        );
        tenantDomainService.save(tenantDomain);
    }
}