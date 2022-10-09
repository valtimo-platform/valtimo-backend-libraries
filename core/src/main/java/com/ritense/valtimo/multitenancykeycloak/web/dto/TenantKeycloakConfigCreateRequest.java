package com.ritense.valtimo.multitenancykeycloak.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Column;

public class TenantKeycloakConfigCreateRequest {
    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("keycloak_server_url")
    private String keycloakServerUrl;

    @JsonProperty("keycloak_realm")
    private String keycloakRealm;

    @JsonProperty("keycloak_m2m_client")
    private String keycloakM2mClient;

    public TenantKeycloakConfigCreateRequest() {
    }

    public TenantKeycloakConfigCreateRequest(
        String tenantId,
        String keycloakServerUrl,
        String keycloakRealm,
        String keycloakM2mClient
    ) {
        this.tenantId = tenantId;
        this.keycloakServerUrl = keycloakServerUrl;
        this.keycloakRealm = keycloakRealm;
        this.keycloakM2mClient = keycloakM2mClient;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getKeycloakServerUrl() {
        return keycloakServerUrl;
    }

    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    public String getKeycloakM2mClient() {
        return keycloakM2mClient;
    }
}
