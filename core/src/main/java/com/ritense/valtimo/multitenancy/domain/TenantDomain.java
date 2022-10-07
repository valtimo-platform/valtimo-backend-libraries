package com.ritense.valtimo.multitenancy.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "tenant_domain")
public class TenantDomain {
    @Id
    @Column(name = "id")
    private UUID id;
    @Column(name = "tenant_id")
    private String tenantId;
    @Column(name = "domain", unique = true)
    private String domain;

    private TenantDomain() {
    }

    public TenantDomain(String tenantId, String domain) {
        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.domain = domain;
    }

    public UUID getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getDomain() {
        return domain;
    }
}
