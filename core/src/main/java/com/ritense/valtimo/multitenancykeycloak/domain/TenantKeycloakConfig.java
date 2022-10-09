package com.ritense.valtimo.multitenancykeycloak.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "tenant_keycloak_config")
public class TenantKeycloakConfig {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "keycloak_server_url")
    private String keycloakServerUrl;

    @Column(name = "keycloak_realm")
    private String keycloakRealm;

    @Column(name = "keycloak_m2m_client")
    private String keycloakM2mClient;

    public TenantKeycloakConfig() {
    }

    public TenantKeycloakConfig(
        String tenantId,
        String keycloakServerUrl,
        String keycloakRealm,
        String keycloakM2mClient) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.keycloakServerUrl = keycloakServerUrl;
        this.keycloakRealm = keycloakRealm;
        this.keycloakM2mClient = keycloakM2mClient;
    }

    public UUID getId() {
        return id;
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
