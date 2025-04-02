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

package com.ritense.processdocument.web.rest;

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.ritense.case_.service.ActiveCaseDefinitionService;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinition;
import com.ritense.processdocument.domain.ProcessDocumentInstance;
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentService;
import com.ritense.processdocument.service.result.ModifyDocumentAndCompleteTaskResult;
import com.ritense.processdocument.service.result.ModifyDocumentAndStartProcessResult;
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult;
import com.ritense.valtimo.contract.annotation.SkipComponentScan;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SkipComponentScan
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class ProcessDocumentResource {

    private final ProcessDocumentService processDocumentService;
    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final ProcessDefinitionCaseDefinitionService processDefinitionCaseDefinitionService;
    private final ActiveCaseDefinitionService activeCaseDefinitionService;

    public ProcessDocumentResource(
        ProcessDocumentService processDocumentService,
        ProcessDocumentAssociationService processDocumentAssociationService,
        ProcessDefinitionCaseDefinitionService processDefinitionCaseDefinitionService,
        ActiveCaseDefinitionService activeCaseDefinitionService
    ) {
        this.processDocumentService = processDocumentService;
        this.processDocumentAssociationService = processDocumentAssociationService;
        this.processDefinitionCaseDefinitionService = processDefinitionCaseDefinitionService;
        this.activeCaseDefinitionService = activeCaseDefinitionService;
    }

    @GetMapping("/v1/case-definition/{caseDefinitionKey}/case-process-link")
    public ResponseEntity<List<ProcessDefinitionCaseDefinition>> findProcessDocumentDefinitions(
        @PathVariable(name = "caseDefinitionKey") String caseDefinitionKey,
        @RequestParam(value = "startableByUser", required = false) @Nullable Boolean startableByUser,
        @RequestParam(value = "canInitializeDocument", required = false) @Nullable Boolean canInitializeDocument
    ) {
        CaseDefinitionId caseDefinitionId = activeCaseDefinitionService.getActiveCaseDefinition(caseDefinitionKey).getId();
        List<ProcessDefinitionCaseDefinition> processDocumentDefinitions = processDefinitionCaseDefinitionService.findProcessDefinitionCaseDefinitions(
            caseDefinitionId,
            startableByUser,
            canInitializeDocument
        );

        return ResponseEntity.ok(processDocumentDefinitions);
    }

    @GetMapping("/v1/document-instance/{documentId}/case-process-link")
    public ResponseEntity<List<ProcessDefinitionCaseDefinition>> findProcessDocumentDefinitions(
        @PathVariable(name = "documentId") UUID documentId,
        @RequestParam(value = "startableByUser", required = false) @Nullable Boolean startableByUser,
        @RequestParam(value = "canInitializeDocument", required = false) @Nullable Boolean canInitializeDocument
    ) {
        return ResponseEntity.ok(processDefinitionCaseDefinitionService.findProcessDefinitionCaseDefinitions(
            documentId,
            startableByUser,
            canInitializeDocument
        ));
    }

    @GetMapping("/v1/process-instance/{processInstanceId}/case-process-link")
    public ResponseEntity<ProcessDefinitionCaseDefinition> getProcessDocumentDefinition(
        @PathVariable String processInstanceId
    ) {
        return runWithoutAuthorization(() ->
            ResponseEntity.ok(processDefinitionCaseDefinitionService.findProcessDefinitionCaseDefinition(
                new CamundaProcessInstanceId(processInstanceId))));
    }

/*    @PostMapping(value = "/v1/process-document/definition", consumes = APPLICATION_JSON_VALUE)
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
        @PathVariable(name = "document-definition-name") String documentDefinitionName,
        @RequestParam(value = "startableByUser", required = false) @Nullable Boolean startableByUser,
        @RequestParam(value = "canInitializeDocument", required = false) @Nullable Boolean canInitializeDocument
    ) {
        return ResponseEntity.ok(processDocumentAssociationService.findProcessDocumentDefinitions(
            documentDefinitionName,
            startableByUser,
            canInitializeDocument
        ));
    }

    @GetMapping("/v2/process-document/definition/document/{document-id}")
    public ResponseEntity<List<? extends ProcessDocumentDefinition>> findProcessDocumentDefinitions(
        @PathVariable(name = "document-id") UUID documentId,
        @RequestParam(value = "startableByUser", required = false) @Nullable Boolean startableByUser,
        @RequestParam(value = "canInitializeDocument", required = false) @Nullable Boolean canInitializeDocument
    ) {
        return ResponseEntity.ok(processDocumentAssociationService.findProcessDocumentDefinitions(
            documentId,
            startableByUser,
            canInitializeDocument
        ));
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
        @PathVariable(name = "document-definition-name") String documentDefinitionName,
        @RequestParam(value = "startableByUser", required = false) Boolean startableByUser,
        @RequestParam(value = "canInitializeDocument", required = false) Boolean canInitializeDocument
    ) {
        return ResponseEntity.ok(AuthorizationContext.runWithoutAuthorization(() ->
            processDocumentAssociationService.findProcessDocumentDefinitions(
                documentDefinitionName,
                startableByUser,
                canInitializeDocument
            ))
        );
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
        return AuthorizationContext.runWithoutAuthorization(() -> processDocumentService.findProcessDocumentDefinition(new CamundaProcessInstanceId(
                processInstanceId)))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.badRequest().build());
    }*/


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

    private HttpStatus getHttpStatus(Optional<? extends Document> document) {
        return document.isPresent() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
    }

}
