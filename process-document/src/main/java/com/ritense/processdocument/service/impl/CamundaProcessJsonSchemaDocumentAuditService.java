/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.service.impl;

import com.ritense.audit.domain.AuditRecord;
import com.ritense.audit.service.AuditService;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.EntityAuthorizationRequest;
import com.ritense.authorization.AuthorizationService;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.event.JsonSchemaDocumentCreatedEvent;
import com.ritense.document.domain.impl.event.JsonSchemaDocumentModifiedEvent;
import com.ritense.document.event.DocumentAssigneeChangedEvent;
import com.ritense.document.event.DocumentUnassignedEvent;
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.processdocument.event.BesluitAddedEvent;
import com.ritense.processdocument.service.ProcessDocumentAuditService;
import com.ritense.valtimo.camunda.processaudit.ProcessEndedEvent;
import com.ritense.valtimo.camunda.processaudit.ProcessStartedEvent;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.document.event.DocumentRelatedFileAddedEvent;
import com.ritense.valtimo.contract.document.event.DocumentRelatedFileRemovedEvent;
import com.ritense.valtimo.contract.documentgeneration.event.DossierDocumentGeneratedEvent;
import com.ritense.valtimo.contract.event.TaskAssignedEvent;
import com.ritense.valtimo.contract.event.TaskCompletedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

import static com.ritense.document.service.JsonSchemaDocumentActionProvider.VIEW;

public class CamundaProcessJsonSchemaDocumentAuditService implements ProcessDocumentAuditService {

    private final AuditService auditService;
    private final JsonSchemaDocumentService documentService;

    private final AuthorizationService authorizationService;

    public CamundaProcessJsonSchemaDocumentAuditService(AuditService auditService,
                                                        JsonSchemaDocumentService documentService,
                                                        AuthorizationService authorizationService
    ) {
        this.auditService = auditService;
        this.documentService = documentService;
        this.authorizationService = authorizationService;
    }

    @Override
    public Page<AuditRecord> getAuditLog(
        final Document.Id id,
        final Pageable pageable
    ) {
        final List<Class<? extends AuditEvent >> eventTypes = List.of(
            JsonSchemaDocumentCreatedEvent.class,
            JsonSchemaDocumentModifiedEvent.class,
            TaskAssignedEvent.class,
            TaskCompletedEvent.class,
            ProcessStartedEvent.class,
            ProcessEndedEvent.class,
            DossierDocumentGeneratedEvent.class,
            DocumentRelatedFileAddedEvent.class,
            DocumentRelatedFileRemovedEvent.class,
            BesluitAddedEvent.class,
            DocumentAssigneeChangedEvent.class,
            DocumentUnassignedEvent.class
        );

        final var document = documentService.getDocumentBy(id);
        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                VIEW,
                document
            )
        );

        return AuthorizationContext.runWithoutAuthorization(() ->
            auditService.findByEventAndDocumentId(eventTypes, UUID.fromString(id.toString()), pageable));
    }

}