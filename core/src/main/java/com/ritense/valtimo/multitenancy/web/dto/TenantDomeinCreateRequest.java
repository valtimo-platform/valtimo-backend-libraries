package com.ritense.valtimo.multitenancy.web.dto;

import javax.validation.constraints.NotNull;

public class TenantDomeinCreateRequest {
    @NotNull
    private String tenantId;
    @NotNull
    private String domain;

    public TenantDomeinCreateRequest() {
    }

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
