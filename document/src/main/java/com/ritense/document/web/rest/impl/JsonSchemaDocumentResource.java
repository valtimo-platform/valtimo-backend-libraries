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

package com.ritense.document.web.rest.impl;

import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.domain.impl.request.UpdateAssigneeRequest;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.result.CreateDocumentResult;
import com.ritense.document.service.result.DocumentResult;
import com.ritense.document.service.result.ModifyDocumentResult;
import com.ritense.document.web.rest.DocumentResource;
import com.ritense.tenancy.TenantResolver;
import com.ritense.valtimo.contract.authentication.NamedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class JsonSchemaDocumentResource implements DocumentResource {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaDocumentResource.class);

    private final DocumentService documentService;
    private final DocumentDefinitionService documentDefinitionService;
    private final TenantResolver tenantResolver;

    public JsonSchemaDocumentResource(
        final DocumentService documentService,
        final DocumentDefinitionService documentDefinitionService,
        final TenantResolver tenantResolver
    ) {
        this.documentService = documentService;
        this.documentDefinitionService = documentDefinitionService;
        this.tenantResolver = tenantResolver;
    }

    @Override
    @GetMapping(value = "/v1/document/{id}")
    public ResponseEntity<? extends Document> getDocument(@PathVariable(name = "id") UUID id) {
        return documentService.findBy(JsonSchemaDocumentId.existingId(id), tenantResolver.getTenantId())
            .filter(it -> hasAccessToDefinitionName(it.definitionId().name()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping(value = "/v1/document", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateDocumentResult> createNewDocument(
        @RequestBody @Valid NewDocumentRequest request
    ) {
        if (!hasAccessToDefinitionName(request.documentDefinitionName())) {
            return ResponseEntity.badRequest().build();
        }
        return applyResult(documentService.createDocument(request));
    }

    @Override
    @PutMapping(value = "/v1/document", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ModifyDocumentResult> modifyDocumentContent(
        @RequestBody @Valid ModifyDocumentRequest request
    ) {
        var tenantId = tenantResolver.getTenantId();
        if (!hasAccessToDocumentId(request.documentId(), tenantId)) {
            return ResponseEntity.badRequest().build();
        }
        request.withTenantId(tenantId);
        return applyResult(documentService.modifyDocument(request));
    }

    @Override
    @PostMapping(value = "/v1/document/{document-id}/resource/{resource-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> assignResource(
        @PathVariable(name = "document-id") UUID documentId,
        @PathVariable(name = "resource-id") UUID resourceId
    ) {
        var tenantId = tenantResolver.getTenantId();
        if (!hasAccessToDocumentId(documentId, tenantId)) {
            return ResponseEntity.badRequest().build();
        }
        documentService.assignResource(
            JsonSchemaDocumentId.existingId(documentId),
            resourceId,
            tenantId
        );
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping(value = "/v1/document/{document-id}/resource/{resource-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> removeRelatedFile(
        @PathVariable(name = "document-id") UUID documentId,
        @PathVariable(name = "resource-id") UUID resourceId
    ) {
        var tenantId = tenantResolver.getTenantId();
        if (!hasAccessToDocumentId(documentId, tenantId)) {
            return ResponseEntity.badRequest().build();
        }
        documentService.removeRelatedFile(
            JsonSchemaDocumentId.existingId(documentId),
            resourceId,
            tenantId
        );
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping(value = "/v1/document/{documentId}/assign")
    public ResponseEntity<Void> assignHandlerToDocument(
        @PathVariable(name = "documentId") UUID documentId,
        @RequestBody @Valid UpdateAssigneeRequest request
    ) {
        logger.debug(String.format("REST call /api/v1/document/%s/assign", documentId));
        try {
            var tenantId = tenantResolver.getTenantId();
            if (!hasAccessToDocumentId(documentId, tenantId)) {
                return ResponseEntity.badRequest().build();
            }
            documentService.assignUserToDocument(
                documentId,
                request.getAssigneeId(),
                tenantId
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to assign a user to a document", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @PostMapping(value = "/v1/document/{documentId}/unassign")
    public ResponseEntity<Void> unassignHandlerFromDocument(
        @PathVariable(name = "documentId") UUID documentId
    ) {
        logger.debug(String.format("REST call /api/v1/document/%s/unassign", documentId));
        try {
            var tenantId = tenantResolver.getTenantId();
            if (!hasAccessToDocumentId(documentId, tenantId)) {
                return ResponseEntity.badRequest().build();
            }
            documentService.unassignUserFromDocument(documentId, tenantId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to unassign a user to a document", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @GetMapping("/v1/document/{document-id}/candidate-user")
    public ResponseEntity<List<NamedUser>> getCandidateUsers(
        @PathVariable(name = "document-id") UUID documentId
    ) {
        var tenantId = tenantResolver.getTenantId();
        if (!hasAccessToDocumentId(documentId, tenantId)) {
            return ResponseEntity.badRequest().build();
        }
        var users = documentService.getCandidateUsers(
            JsonSchemaDocumentId.existingId(documentId),
            tenantId
        );
        return ResponseEntity.ok(users);
    }

    private boolean hasAccessToDocumentId(UUID documentId, String tenantId) {
        return hasAccessToDocumentId(documentId.toString(), tenantId);
    }

    private boolean hasAccessToDocumentId(String documentId, String tenantId) {
        return hasAccessToDefinitionName(
            documentService.get(documentId, tenantId).definitionId().name()
        );
    }

    private boolean hasAccessToDefinitionName(String definitionName) {
        return documentDefinitionService.currentUserCanAccessDocumentDefinition(
            definitionName
        );
    }

    <T extends DocumentResult> ResponseEntity<T> applyResult(T result) {
        var httpStatus = result.resultingDocument().isPresent() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(result);
    }

}
