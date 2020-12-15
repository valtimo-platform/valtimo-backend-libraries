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

package com.ritense.processdocument.service.impl;

import com.ritense.audit.domain.AuditRecord;
import com.ritense.audit.service.AuditSearchService;
import com.ritense.audit.service.impl.SearchCriteria;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.event.JsonSchemaDocumentCreatedEvent;
import com.ritense.document.domain.impl.event.JsonSchemaDocumentModifiedEvent;
import com.ritense.processdocument.service.ProcessDocumentAuditService;
import com.ritense.valtimo.camunda.processaudit.ProcessEndedEvent;
import com.ritense.valtimo.camunda.processaudit.ProcessStartedEvent;
import com.ritense.valtimo.contract.document.event.DocumentRelatedFileAddedEvent;
import com.ritense.valtimo.contract.document.event.DocumentRelatedFileRemovedEvent;
import com.ritense.valtimo.contract.documentgeneration.event.DossierDocumentGeneratedEvent;
import com.ritense.valtimo.contract.event.TaskAssignedEvent;
import com.ritense.valtimo.contract.event.TaskCompletedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class CamundaProcessJsonSchemaDocumentAuditService implements ProcessDocumentAuditService {

    private final AuditSearchService auditSearchService;

    public CamundaProcessJsonSchemaDocumentAuditService(AuditSearchService auditSearchService) {
        this.auditSearchService = auditSearchService;
    }

    @Override
    public Page<AuditRecord> getAuditLog(
        final Document.Id id,
        final Pageable pageable
    ) {
        final List<SearchCriteria> criteriaList = List.of(
            new SearchCriteria("$.documentId", JsonSchemaDocumentCreatedEvent.class, id.toString()),
            new SearchCriteria("$.documentId", JsonSchemaDocumentModifiedEvent.class, id.toString()),
            new SearchCriteria("$.businessKey", TaskAssignedEvent.class, id.toString()),
            new SearchCriteria("$.businessKey", TaskCompletedEvent.class, id.toString()),
            new SearchCriteria("$.businessKey", ProcessStartedEvent.class, id.toString()),
            new SearchCriteria("$.businessKey", ProcessEndedEvent.class, id.toString()),
            new SearchCriteria("$.dossierId", DossierDocumentGeneratedEvent.class, id.toString()),
            new SearchCriteria("$.documentId", DocumentRelatedFileAddedEvent.class, id.toString()),
            new SearchCriteria("$.documentId", DocumentRelatedFileRemovedEvent.class, id.toString())
        );
        return auditSearchService.search(criteriaList, pageable);
    }

}