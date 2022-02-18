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

package com.ritense.document.web.rest.impl;

import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.impl.request.ModifyDocumentRequest;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.result.CreateDocumentResult;
import com.ritense.document.service.result.DocumentResult;
import com.ritense.document.service.result.ModifyDocumentResult;
import com.ritense.document.web.rest.DocumentResource;
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
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/document", produces = MediaType.APPLICATION_JSON_VALUE)
public class JsonSchemaDocumentResource implements DocumentResource {

    private final DocumentService documentService;
    private final DocumentDefinitionService documentDefinitionService;

    public JsonSchemaDocumentResource(
        final DocumentService documentService,
        final DocumentDefinitionService documentDefinitionService
    ) {
        this.documentService = documentService;
        this.documentDefinitionService = documentDefinitionService;
    }

    @Override
    @GetMapping(value = "/{id}")
    public ResponseEntity<? extends Document> getDocument(@PathVariable(name = "id") UUID id) {
        return documentService.findBy(JsonSchemaDocumentId.existingId(id))
            .filter(it -> hasAccessToDefinitionName(it.definitionId().name()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateDocumentResult> createNewDocument(
        @RequestBody @Valid NewDocumentRequest request
    ) {
        if (!hasAccessToDefinitionName(request.documentDefinitionName())) {
            return ResponseEntity.badRequest().build();
        }
        return applyResult(documentService.createDocument(request));
    }

    @Override
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ModifyDocumentResult> modifyDocumentContent(
        @RequestBody @Valid ModifyDocumentRequest request
    ) {
        if (!hasAccessToDocumentId(request.documentId())) {
            return ResponseEntity.badRequest().build();
        }
        return applyResult(documentService.modifyDocument(request));
    }

    @Override
    @PostMapping(value = "/{document-id}/resource/{resource-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> assignResource(
        @PathVariable(name = "document-id") UUID documentId,
        @PathVariable(name = "resource-id") UUID resourceId
    ) {
        if (!hasAccessToDocumentId(documentId)) {
            return ResponseEntity.badRequest().build();
        }

        documentService.assignResource(JsonSchemaDocumentId.existingId(documentId), resourceId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping(value = "/{document-id}/resource/{resource-id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> removeRelatedFile(
        @PathVariable(name = "document-id") UUID documentId,
        @PathVariable(name = "resource-id") UUID resourceId
    ) {
        if (!hasAccessToDocumentId(documentId)) {
            return ResponseEntity.badRequest().build();
        }

        documentService.removeRelatedFile(JsonSchemaDocumentId.existingId(documentId), resourceId);
        return ResponseEntity.noContent().build();
    }

    private boolean hasAccessToDocumentId(UUID documentId) {
        return hasAccessToDocumentId(documentId.toString());
    }

    private boolean hasAccessToDocumentId(String documentId) {
        return hasAccessToDefinitionName(
            documentService.get(documentId).definitionId().name()
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