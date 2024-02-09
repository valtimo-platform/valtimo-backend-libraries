/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.contract.documentgeneration.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class DossierDocumentGeneratedEvent extends AuditMetaData implements AuditEvent {

    private final String templateIdentifier;
    private final String dossierId;

    @JsonCreator
    public DossierDocumentGeneratedEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        String templateIdentifier,
        String dossierId
    ) {
        super(id, origin, occurredOn, user);
        this.templateIdentifier = templateIdentifier;
        this.dossierId = dossierId;
    }

    public String getTemplateIdentifier() {
        return templateIdentifier;
    }

    public String getDossierId() {
        return dossierId;
    }

    @Override
    public UUID getDocumentId() {
        return UUID.fromString(dossierId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DossierDocumentGeneratedEvent that = (DossierDocumentGeneratedEvent) o;
        return Objects.equals(templateIdentifier, that.templateIdentifier) && Objects.equals(dossierId, that.dossierId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), templateIdentifier, dossierId);
    }
}
