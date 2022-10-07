package com.ritense.valtimo.multitenancy.web.dto;

public class TenantDomeinCreateRequest {
    private final String tenantId;
    private final String domain;

    public TenantDomeinCreateRequest(String tenantId, String domain) {
        this.tenantId = tenantId;
        this.domain = domain;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getDomain() {
        return domain;
    }
}
