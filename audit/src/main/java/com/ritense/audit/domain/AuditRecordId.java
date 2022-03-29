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

import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.contract.audit.view.AuditView;
import com.ritense.valtimo.contract.domain.AbstractId;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Embeddable
public final class AuditRecordId extends AbstractId<AuditRecordId> {

    @JsonView(AuditView.Public.class)
    @Column(name = "audit_record_id", columnDefinition = "BINARY(16)", updatable = false)
    private UUID id;

    private AuditRecordId(UUID id) {
        assertArgumentNotNull(id, "id is required");
        this.id = id;
    }

    private AuditRecordId() {
    }

    public UUID id() {
        return id;
    }

    public static AuditRecordId existingId(UUID id) {
        return new AuditRecordId(id);
    }

    public static AuditRecordId newId(UUID id) {
        return new AuditRecordId(id).newIdentity();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditRecordId)) {
            return false;
        }
        AuditRecordId that = (AuditRecordId) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}