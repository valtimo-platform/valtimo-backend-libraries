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

package com.ritense.processdocument.web.rest;

import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.processdocument.domain.ProcessDocumentDefinition;
import com.ritense.processdocument.domain.ProcessDocumentInstance;
import com.ritense.processdocument.domain.impl.DocumentDefinitionProcess;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessLinkResponse;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentService;
import com.ritense.processdocument.service.result.ModifyDocumentAndCompleteTaskResult;
import com.ritense.processdocument.service.result.ModifyDocumentAndStartProcessResult;
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/process-document", produces = APPLICATION_JSON_VALUE)
public class ProcessDocumentResource {

    private final ProcessDocumentService processDocumentService;
    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final DocumentDefinitionProcessLinkService documentDefinitionProcessLinkService;

    public ProcessDocumentResource(
        ProcessDocumentService processDocumentService,
        ProcessDocumentAssociationService processDocumentAssociationService,
        DocumentDefinitionProcessLinkService documentDefinitionProcessLinkService
    ) {
        this.processDocumentService = processDocumentService;
        this.processDocumentAssociationService = processDocumentAssociationService;
        this.documentDefinitionProcessLinkService = documentDefinitionProcessLinkService;
    }

    @GetMapping(value = "/definition")
    public ResponseEntity<Page<? extends ProcessDocumentDefinition>> getProcessDocumentDefinitions(
        @PageableDefault(sort = {"processDocumentDefinitionId.documentDefinitionName.name"}, direction = DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(processDocumentAssociationService.getAllProcessDocumentDefinitions(pageable));
    }

    @PostMapping(value = "/definition", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<? extends ProcessDocumentDefinition> createProcessDocumentDefinition(
        @Valid @RequestBody ProcessDocumentDefinitionRequest request
    ) {
        return processDocumentAssociationService.createProcessDocumentDefinition(request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping(value = "/definition", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteProcessDocumentDefinition(
        @Valid @RequestBody ProcessDocumentDefinitionRequest request
    ) {
        processDocumentAssociationService.deleteProcessDocumentDefinition(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/definition/document/{document-definition-name}")
    public ResponseEntity<List<? extends ProcessDocumentDefinition>> findProcessDocumentDefinitions(
        @PathVariable(name = "document-definition-name") String documentDefinitionName
    ) {
        return ResponseEntity.ok(processDocumentAssociationService.findProcessDocumentDefinitions(documentDefinitionName));
    }

    @GetMapping(value = "/instance/document/{documentId}")
    public ResponseEntity<List<? extends ProcessDocumentInstance>> findProcessDocumentInstances(
        @PathVariable UUID documentId
    ) {
        return ResponseEntity.ok(
            processDocumentAssociationService.findProcessDocumentInstances(JsonSchemaDocumentId.existingId(documentId)));
    }

    @PostMapping(value = "/operation/new-document-and-start-process", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<NewDocumentAndStartProcessResult> newDocumentAndStartProcess(
        @Valid @RequestBody NewDocumentAndStartProcessRequest request
    ) {
        final var result = processDocumentService.newDocumentAndStartProcess(request);
        final var httpStatus = getHttpStatus(result.resultingDocument());
        return ResponseEntity.status(httpStatus).body(result);
    }

    @PreAuthorize("hasAuthority('ROLE_USER') and hasPermission(#request.taskId(), 'taskAccess')")
    @PostMapping(value = "/operation/modify-document-and-complete-task", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<ModifyDocumentAndCompleteTaskResult> modifyDocumentAndCompleteTask(
        @Valid @RequestBody ModifyDocumentAndCompleteTaskRequest request
    ) {
        final var result = processDocumentService.modifyDocumentAndCompleteTask(request);
        final var httpStatus = getHttpStatus(result.resultingDocument());
        return ResponseEntity.status(httpStatus).body(result);
    }

    @PostMapping(value = "/operation/modify-document-and-start-process", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<ModifyDocumentAndStartProcessResult> modifyDocumentAndStartProcess(
        @Valid @RequestBody ModifyDocumentAndStartProcessRequest request
    ) {
        final var result = processDocumentService.modifyDocumentAndStartProcess(request);
        final var httpStatus = getHttpStatus(result.resultingDocument());
        return ResponseEntity.status(httpStatus).body(result);
    }

    /**
     * @deprecated This is only a demo endpoint.
     * A generic endpoint that can link processes of different types/tags will be made in the future
     */
    @Deprecated(since = "9.21.0", forRemoval = true)
    @GetMapping("/demo/{documentDefinitionName}/process")
    public ResponseEntity<DocumentDefinitionProcess> getDocumentDefinitionProcess(
        @PathVariable String documentDefinitionName
    ) {
        var result = documentDefinitionProcessLinkService.getDocumentDefinitionProcess(documentDefinitionName);
        return ResponseEntity.ok(result);
    }

    /**
     * @deprecated This is only a demo endpoint.
     * A generic endpoint that can link processes of different types/tags will be made in the future
     */
    @Deprecated(since = "9.21.0", forRemoval = true)
    @PutMapping("/demo/{documentDefinitionName}/process")
    public ResponseEntity<DocumentDefinitionProcessLinkResponse> putDocumentDefinitionProcess(
        @PathVariable String documentDefinitionName,
        @Valid @RequestBody DocumentDefinitionProcessRequest request
    ) {
        var response = documentDefinitionProcessLinkService.saveDocumentDefinitionProcess(documentDefinitionName, request);
        return ResponseEntity.ok(response);
    }

    /**
     * @deprecated This is only a demo endpoint.
     * A generic endpoint that can link processes of different types/tags will be made in the future
     */
    @Deprecated(since = "9.21.0", forRemoval = true)
    @DeleteMapping("/demo/{documentDefinitionName}/process")
    public ResponseEntity<Void> deleteDocumentDefinitionProcess(
        @PathVariable String documentDefinitionName
    ) {
        documentDefinitionProcessLinkService.deleteDocumentDefinitionProcess(documentDefinitionName);
        return ResponseEntity.ok().build();
    }

    private HttpStatus getHttpStatus(Optional<? extends Document> document) {
        return document.isPresent() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
    }

}