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
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.document.service.result.CreateDocumentResult;
import com.ritense.document.service.result.DocumentResult;
import com.ritense.document.service.result.ModifyDocumentResult;
import com.ritense.document.web.rest.DocumentResource;
import com.ritense.valtimo.contract.authentication.NamedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class JsonSchemaDocumentResource implements DocumentResource {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaDocumentResource.class);

    private final JsonSchemaDocumentService documentService;

    public JsonSchemaDocumentResource(
        final JsonSchemaDocumentService documentService
    ) {
        this.documentService = documentService;
    }

    @Override
    @GetMapping("/v1/document/{id}")
    public ResponseEntity<? extends Document> getDocument(@PathVariable(name = "id") UUID id) {
        return documentService.findBy(JsonSchemaDocumentId.existingId(id))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping(value = "/v1/document", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateDocumentResult> createNewDocument(
        @RequestBody @Valid NewDocumentRequest request
    ) {
        return applyResult(documentService.createDocument(request));
    }

    @Override
    @PutMapping(value = "/v1/document", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ModifyDocumentResult> modifyDocumentContent(
        @RequestBody @Valid ModifyDocumentRequest request
    ) {
        return applyResult(documentService.modifyDocument(request));
    }

    @Override
    @PostMapping(value = "/v1/document/{document-id}/resource/{resource-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> assignResource(
        @PathVariable(name = "document-id") UUID documentId,
        @PathVariable(name = "resource-id") UUID resourceId
    ) {
        documentService.assignResource(JsonSchemaDocumentId.existingId(documentId), resourceId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping(value = "/v1/document/{document-id}/resource/{resource-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> removeRelatedFile(
        @PathVariable(name = "document-id") UUID documentId,
        @PathVariable(name = "resource-id") UUID resourceId
    ) {
        documentService.removeRelatedFile(JsonSchemaDocumentId.existingId(documentId), resourceId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/v1/document/{documentId}/assign")
    public ResponseEntity<Void> assignHandlerToDocument(
        @PathVariable(name = "documentId")UUID documentId,
        @RequestBody @Valid UpdateAssigneeRequest request) {
        logger.debug(String.format("REST call /api/v1/document/%s/assign", documentId));

        try {
            documentService.assignUserToDocument(documentId, request.getAssigneeId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to assign a user to a document", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @PostMapping("/v1/document/{documentId}/unassign")
    public ResponseEntity<Void> unassignHandlerFromDocument(@PathVariable(name = "documentId")UUID documentId) {
        logger.debug(String.format("REST call /api/v1/document/%s/unassign", documentId));

        try {
            documentService.unassignUserFromDocument(documentId);
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
        List<NamedUser> users = documentService.getCandidateUsers(JsonSchemaDocumentId.existingId(documentId));
        return ResponseEntity.ok(users);
    }

    <T extends DocumentResult> ResponseEntity<T> applyResult(T result) {
        var httpStatus = result.resultingDocument().isPresent() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus).body(result);
    }

}
