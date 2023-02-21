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

package com.ritense.processdocument.web.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.audit.domain.AuditRecord;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.processdocument.service.ProcessDocumentAuditService;
import com.ritense.valtimo.contract.audit.view.AuditView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_VALUE)
public class ProcessDocumentAuditResource {

    private final ProcessDocumentAuditService processDocumentAuditService;

    public ProcessDocumentAuditResource(ProcessDocumentAuditService processDocumentAuditService) {
        this.processDocumentAuditService = processDocumentAuditService;
    }

    @GetMapping(value = "/v1/process-document/instance/document/{documentId}/audit")
    @JsonView(AuditView.Public.class)
    public ResponseEntity<Page<AuditRecord>> getAuditLog(
        @PathVariable UUID documentId,
        Pageable pageable
    ) {
        return ResponseEntity.ok(processDocumentAuditService.getAuditLog(JsonSchemaDocumentId.existingId(documentId), pageable));
    }

}