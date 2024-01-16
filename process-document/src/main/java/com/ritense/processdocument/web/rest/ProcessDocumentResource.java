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

import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.processdocument.domain.ProcessDocumentDefinition;
import com.ritense.processdocument.domain.ProcessDocumentInstance;
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
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
import com.ritense.valtimo.contract.annotation.SkipComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@SkipComponentScan
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
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

    @PostMapping(value = "/v1/process-document/definition", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<? extends ProcessDocumentDefinition> createProcessDocumentDefinition(
        @Valid @RequestBody ProcessDocumentDefinitionRequest request
    ) {
        //Protected by HTTP security on role ADMIN
        return AuthorizationContext.runWithoutAuthorization(() -> processDocumentAssociationService.createProcessDocumentDefinition(request))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping(value = "/v1/process-document/definition", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteProcessDocumentDefinition(
        @Valid @RequestBody ProcessDocumentDefinitionRequest request
    ) {
        //Protected by HTTP security on role ADMIN
        AuthorizationContext.runWithoutAuthorization(() -> {
            processDocumentAssociationService.deleteProcessDocumentDefinition(request);
            return null;
        });
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/v1/process-document/definition/document/{document-definition-name}")
    public ResponseEntity<List<? extends ProcessDocumentDefinition>> findProcessDocumentDefinitions(
        @PathVariable(name = "document-definition-name") String documentDefinitionName
    ) {
        return ResponseEntity.ok(processDocumentAssociationService.findProcessDocumentDefinitions(documentDefinitionName));
    }

    @GetMapping("/v1/process-document/definition/document/{document-definition-name}/version/{document-definition-version}")
    public ResponseEntity<List<? extends ProcessDocumentDefinition>> findProcessDocumentDefinitions(
        @PathVariable(name = "document-definition-name") String documentDefinitionName,
        @PathVariable(name = "document-definition-version") Long documentDefinitionVersion
    ) {
        return ResponseEntity.ok(processDocumentAssociationService.findProcessDocumentDefinitions(documentDefinitionName, documentDefinitionVersion));
    }

    @GetMapping("/management/v1/process-document/definition/document/{document-definition-name}")
    public ResponseEntity<List<? extends ProcessDocumentDefinition>> findManagementProcessDocumentDefinitions(
        @PathVariable(name = "document-definition-name") String documentDefinitionName
    ) {
        return ResponseEntity.ok(AuthorizationContext.runWithoutAuthorization(() ->
            processDocumentAssociationService.findProcessDocumentDefinitions(documentDefinitionName)));
    }

    @GetMapping("/v1/process-document/definition/process/{process-definition-key}")
    public ResponseEntity<List<? extends ProcessDocumentDefinition>> findProcessDocumentDefinitionsByProcessDefinitionKey(
        @PathVariable(name = "process-definition-key") String processDefinitionKey
    ) {
        //Protected by HTTP security on role ADMIN
        List<? extends ProcessDocumentDefinition> processDocumentDefinitions = AuthorizationContext.runWithoutAuthorization(() ->
            processDocumentAssociationService.findProcessDocumentDefinitionsByProcessDefinitionKey(processDefinitionKey)
        );
        return ResponseEntity.ok(processDocumentDefinitions);
    }

    @GetMapping("/v1/process-document/definition/processinstance/{processInstanceId}")
    public ResponseEntity<ProcessDocumentDefinition> getProcessDocumentDefinition(
        @PathVariable String processInstanceId
    ) {
        return AuthorizationContext.runWithoutAuthorization(() -> processDocumentService.findProcessDocumentDefinition(new CamundaProcessInstanceId(processInstanceId)))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.badRequest().build());
    }


    @GetMapping("/v1/process-document/instance/document/{documentId}")
    public ResponseEntity<List<? extends ProcessDocumentInstance>> findProcessDocumentInstances(
        @PathVariable UUID documentId
    ) {
        return ResponseEntity.ok(
            processDocumentAssociationService.findProcessDocumentInstanceDtos(JsonSchemaDocumentId.existingId(documentId)));
    }

    @PostMapping(value = "/v1/process-document/operation/new-document-and-start-process", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<NewDocumentAndStartProcessResult> newDocumentAndStartProcess(
        @Valid @RequestBody NewDocumentAndStartProcessRequest request
    ) {
        final var result = processDocumentService.newDocumentAndStartProcess(request);
        final var httpStatus = getHttpStatus(result.resultingDocument());
        return ResponseEntity.status(httpStatus).body(result);
    }

    @PostMapping(value = "/v1/process-document/operation/modify-document-and-complete-task", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<ModifyDocumentAndCompleteTaskResult> modifyDocumentAndCompleteTask(
        @Valid @RequestBody ModifyDocumentAndCompleteTaskRequest request
    ) {
        final var result = processDocumentService.modifyDocumentAndCompleteTask(request);
        final var httpStatus = getHttpStatus(result.resultingDocument());
        return ResponseEntity.status(httpStatus).body(result);
    }

    @PostMapping(value = "/v1/process-document/operation/modify-document-and-start-process", consumes = APPLICATION_JSON_VALUE)
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
    @GetMapping("/v1/process-document/demo/{documentDefinitionName}/process")
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
    @PutMapping("/v1/process-document/demo/{documentDefinitionName}/process")
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
    @DeleteMapping("/v1/process-document/demo/{documentDefinitionName}/process")
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
