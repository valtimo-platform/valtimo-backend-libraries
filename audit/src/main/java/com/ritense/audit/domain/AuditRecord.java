/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.audit.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.view.AuditView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.domain.Persistable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Entity
@Table(name = "audit_record", indexes = {
    @Index(name = "created_on_index", columnList = "created_on"),
    @Index(name = "origin_index", columnList = "origin"),
    @Index(name = "occurred_on_index", columnList = "occurred_on"),
    @Index(name = "user_index", columnList = "user")
})
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditRecord implements Persistable<AuditRecordId> {

    @EmbeddedId
    @JsonView(AuditView.Internal.class)
    private AuditRecordId auditRecordId;

    @Embedded
    @JsonView(AuditView.Public.class)
    private MetaData metaData;

    @JsonView(AuditView.Public.class)
    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonStringType")
    @Column(name = "audit_event", columnDefinition = "json", updatable = false)
    @JsonView(AuditView.Public.class)
    private AuditEvent auditEvent;

    public AuditRecord(AuditRecordId auditRecordId, MetaData metaData, AuditEvent auditEvent) {
        assertArgumentNotNull(auditRecordId, "auditRecordId is required");
        assertArgumentNotNull(metaData, "metaData is required");
        assertArgumentNotNull(auditEvent, "auditEvent is required");
        this.auditRecordId = auditRecordId;
        this.metaData = metaData;
        this.auditEvent = auditEvent;
        this.createdOn = LocalDateTime.now();
    }

    public AuditRecordId getAuditRecordId() {
        return auditRecordId;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public AuditEvent getAuditEvent() {
        return auditEvent;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditRecord)) {
            return false;
        }
        AuditRecord that = (AuditRecord) o;
        return getAuditRecordId().equals(that.getAuditRecordId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAuditRecordId());
    }

    @JsonIgnore
    @Override
    public AuditRecordId getId() {
        return auditRecordId;
    }

    @JsonIgnore
    @Override
    public boolean isNew() {
        return auditRecordId.isNew();
    }

    @Override
    public String toString() {
        return "AuditRecord{" +
            "auditRecordId=" + auditRecordId +
            ", metaData=" + metaData +
            ", createdOn=" + createdOn +
            ", auditEvent=" + auditEvent +
            '}';
    }
}