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

import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.AuthorizationRequest;
import com.ritense.authorization.AuthorizationService;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.processdocument.domain.ProcessDocumentDefinition;
import com.ritense.processdocument.domain.ProcessInstanceId;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinition;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstanceId;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentForRunningProcessRequest;
import com.ritense.processdocument.domain.impl.request.StartProcessForDocumentRequest;
import com.ritense.processdocument.domain.request.Request;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.processdocument.service.ProcessDocumentService;
import com.ritense.processdocument.service.impl.result.ModifyDocumentAndCompleteTaskResultFailed;
import com.ritense.processdocument.service.impl.result.ModifyDocumentAndCompleteTaskResultSucceeded;
import com.ritense.processdocument.service.impl.result.ModifyDocumentAndStartProcessResultFailed;
import com.ritense.processdocument.service.impl.result.ModifyDocumentAndStartProcessResultSucceeded;
import com.ritense.processdocument.service.impl.result.NewDocumentAndStartProcessResultFailed;
import com.ritense.processdocument.service.impl.result.NewDocumentAndStartProcessResultSucceeded;
import com.ritense.processdocument.service.impl.result.NewDocumentForRunningProcessResultFailed;
import com.ritense.processdocument.service.impl.result.NewDocumentForRunningProcessResultSucceeded;
import com.ritense.processdocument.service.impl.result.StartProcessForDocumentResultFailed;
import com.ritense.processdocument.service.impl.result.StartProcessForDocumentResultSucceeded;
import com.ritense.processdocument.service.result.DocumentFunctionResult;
import com.ritense.processdocument.service.result.ModifyDocumentAndCompleteTaskResult;
import com.ritense.processdocument.service.result.ModifyDocumentAndStartProcessResult;
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult;
import com.ritense.processdocument.service.result.NewDocumentForRunningProcessResult;
import com.ritense.processdocument.service.result.StartProcessForDocumentResult;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.domain.ProcessInstanceWithDefinition;
import com.ritense.valtimo.contract.result.FunctionResult;
import com.ritense.valtimo.contract.result.OperationError;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.ritense.document.service.JsonSchemaDocumentActionProvider.CREATE;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.MODIFY;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.VIEW;

public class CamundaProcessJsonSchemaDocumentService implements ProcessDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(CamundaProcessJsonSchemaDocumentService.class);
    private final JsonSchemaDocumentService documentService;
    private final CamundaTaskService camundaTaskService;
    private final CamundaProcessService camundaProcessService;
    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final AuthorizationService authorizationService;

    public CamundaProcessJsonSchemaDocumentService(
        JsonSchemaDocumentService documentService, CamundaTaskService camundaTaskService, CamundaProcessService camundaProcessService, ProcessDocumentAssociationService processDocumentAssociationService,
        AuthorizationService authorizationService
    ) {
        this.documentService = documentService;
        this.camundaTaskService = camundaTaskService;
        this.camundaProcessService = camundaProcessService;
        this.processDocumentAssociationService = processDocumentAssociationService;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public DocumentFunctionResult dispatch(Request request) {
        if (request instanceof NewDocumentAndStartProcessRequest) {
            return newDocumentAndStartProcess((NewDocumentAndStartProcessRequest) request);
        } else if (request instanceof ModifyDocumentAndCompleteTaskRequest) {
            return modifyDocumentAndCompleteTask((ModifyDocumentAndCompleteTaskRequest) request);
        } else if (request instanceof NewDocumentForRunningProcessRequest) {
            return newDocumentForRunningProcess((NewDocumentForRunningProcessRequest) request);
        } else if (request instanceof ModifyDocumentAndStartProcessRequest) {
            return modifyDocumentAndStartProcess((ModifyDocumentAndStartProcessRequest) request);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional
    public NewDocumentAndStartProcessResult newDocumentAndStartProcess(
        final NewDocumentAndStartProcessRequest request
    ) {
        try {
            final var processDefinitionKey = new CamundaProcessDefinitionKey(request.processDefinitionKey());
            final var newDocumentRequest = request.newDocumentRequest();

            final var newDocumentResult = AuthorizationContext
                .runWithoutAuthorization(() -> documentService.createDocument(newDocumentRequest));

            if (newDocumentResult.resultingDocument().isEmpty()) {
                return new NewDocumentAndStartProcessResultFailed(newDocumentResult.errors());
            }

            final var document = newDocumentResult.resultingDocument().orElseThrow();

            authorizationService.requirePermission(
                new AuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    CREATE
                ),
                document,
                null
            );

            final var processInstanceWithDefinition = startProcess(
                document,
                processDefinitionKey.toString(),
                request.getProcessVars()
            );

            final var camundaProcessInstanceId = new CamundaProcessInstanceId(processInstanceWithDefinition.getProcessInstanceDto().getId());
            processDocumentAssociationService.createProcessDocumentInstance(
                camundaProcessInstanceId.toString(),
                UUID.fromString(document.id().toString()),
                processDefinitionKey.toString()
            );

            return new NewDocumentAndStartProcessResultSucceeded(
                document,
                camundaProcessInstanceId
            );
        } catch (Exception ex) {
            return new NewDocumentAndStartProcessResultFailed(parseAndLogException(ex));
        }
    }

    @Override
    @Transactional
    public ModifyDocumentAndCompleteTaskResult modifyDocumentAndCompleteTask(
        final ModifyDocumentAndCompleteTaskRequest request
    ) {
        try {
            final var taskResult = findTaskById(request.taskId());
            if (!taskResult.hasResult()) {
                return new ModifyDocumentAndCompleteTaskResultFailed(taskResult.errors());
            }

            final var task = taskResult.resultingValue().orElseThrow();
            final var modifyDocumentRequest = request.modifyDocumentRequest();
            final var modifiedDocumentId = JsonSchemaDocumentId.existingId(UUID.fromString(modifyDocumentRequest.documentId()));
            final var processInstanceId = new CamundaProcessInstanceId(task.getProcessInstanceId());

            final var processDocumentInstanceResult = processDocumentAssociationService.getProcessDocumentInstanceResult(
                CamundaProcessJsonSchemaDocumentInstanceId.existingId(processInstanceId, modifiedDocumentId)
            );

            if (!processDocumentInstanceResult.hasResult()) {
                return new ModifyDocumentAndCompleteTaskResultFailed(processDocumentInstanceResult.errors());
            }

            final var modifyDocumentResult = AuthorizationContext
                .runWithoutAuthorization(() -> documentService.modifyDocument(modifyDocumentRequest));
            if (modifyDocumentResult.resultingDocument().isEmpty()) {
                return new ModifyDocumentAndCompleteTaskResultFailed(modifyDocumentResult.errors());
            }

            final var document = modifyDocumentResult.resultingDocument().orElseThrow();

            authorizationService.requirePermission(
                new AuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    MODIFY
                ),
                document,
                null
            );

            camundaTaskService.completeTask(request.taskId(), request.getProcessVars());

            return new ModifyDocumentAndCompleteTaskResultSucceeded(document);
        } catch (Exception ex) {
            return new ModifyDocumentAndCompleteTaskResultFailed(parseAndLogException(ex));
        }
    }

    @Override
    @Transactional
    public NewDocumentForRunningProcessResult newDocumentForRunningProcess(
        final NewDocumentForRunningProcessRequest request
    ) {
        try {
            final var processInstanceId = new CamundaProcessInstanceId(request.processInstanceId());
            final var newDocumentRequest = request.newDocumentRequest();

            final var newDocumentResult = AuthorizationContext
                .runWithoutAuthorization(() -> documentService.createDocument(newDocumentRequest));

            if (newDocumentResult.resultingDocument().isEmpty()) {
                return new NewDocumentForRunningProcessResultFailed(newDocumentResult.errors());
            }

            final var document = newDocumentResult.resultingDocument().orElseThrow();

            authorizationService.requirePermission(
                new AuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    CREATE
                ),
                document,
                null
            );

            final String processName = camundaProcessService.getProcessDefinition(request.processDefinitionKey()).getName();
            processDocumentAssociationService.createProcessDocumentInstance(
                request.processInstanceId(),
                UUID.fromString(document.id().toString()),
                processName
            );

            return new NewDocumentForRunningProcessResultSucceeded(
                document,
                processInstanceId
            );
        } catch (Exception ex) {
            return new NewDocumentForRunningProcessResultFailed(parseAndLogException(ex));
        }
    }

    @Override
    @Transactional
    public ModifyDocumentAndStartProcessResult modifyDocumentAndStartProcess(
        final ModifyDocumentAndStartProcessRequest request
    ) {
        try {
            //Part 1 Modify document
            final var modifyDocumentResult = AuthorizationContext
                .runWithoutAuthorization(() -> documentService.modifyDocument(request.modifyDocumentRequest()));

            if (modifyDocumentResult.resultingDocument().isEmpty()) {
                return new ModifyDocumentAndStartProcessResultFailed(modifyDocumentResult.errors());
            }
            final var document = modifyDocumentResult.resultingDocument().orElseThrow();

            authorizationService.requirePermission(
                new AuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    MODIFY
                ),
                document,
                null
            );

            //Part 2 process start
            final var processDefinitionKey = new CamundaProcessDefinitionKey(request.processDefinitionKey());
            final var processInstanceWithDefinition = startProcess(document, processDefinitionKey.toString(), request.getProcessVars());
            final var camundaProcessInstanceId = new CamundaProcessInstanceId(
                processInstanceWithDefinition.getProcessInstanceDto().getId()
            );

            processDocumentAssociationService.createProcessDocumentInstance(
                camundaProcessInstanceId.toString(),
                UUID.fromString(document.id().toString()),
                processInstanceWithDefinition.getProcessDefinition().getName()
            );
            return new ModifyDocumentAndStartProcessResultSucceeded(document, camundaProcessInstanceId);
        } catch (RuntimeException ex) {
            return new ModifyDocumentAndStartProcessResultFailed(parseAndLogException(ex));
        }
   }

    public StartProcessForDocumentResult startProcessForDocument(StartProcessForDocumentRequest request) {
        try {
            //Part 1 find document
            final var optionalDocument = AuthorizationContext
                .runWithoutAuthorization(() -> documentService.findBy(request.getDocumentId()));

            if (optionalDocument.isEmpty()) {
                return new StartProcessForDocumentResultFailed(new OperationError.FromString("Document could not be found"));
            }

            final var document = optionalDocument.get();

            authorizationService.requirePermission(
                new AuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    VIEW
                ),
                document,
                null
            );

            //Part 2 process start
            final var processDefinitionKey = new CamundaProcessDefinitionKey(request.getProcessDefinitionKey());
            final var processInstanceWithDefinition = startProcess(document, processDefinitionKey.toString(), request.getProcessVars());
            final var camundaProcessInstanceId = new CamundaProcessInstanceId(
                processInstanceWithDefinition.getProcessInstanceDto().getId()
            );

            processDocumentAssociationService.createProcessDocumentInstance(
                camundaProcessInstanceId.toString(),
                document.id().getId(),
                processInstanceWithDefinition.getProcessDefinition().getName()
            );
            return new StartProcessForDocumentResultSucceeded(document, camundaProcessInstanceId);
        } catch (RuntimeException ex) {
            return new StartProcessForDocumentResultFailed(parseAndLogException(ex));
        }
    }

    public Document getDocument(DelegateExecution execution) {
        return getDocument(ProcessInstanceId.fromExecution(execution, CamundaProcessInstanceId.class), execution);
    }

    public JsonSchemaDocumentId getDocumentId(ProcessInstanceId processInstanceId, VariableScope variableScope) {
        var processDocumentInstance = processDocumentAssociationService.findProcessDocumentInstance(processInstanceId).orElse(null);
        if (processDocumentInstance != null) {
            var jsonSchemaDocumentId = processDocumentInstance.processDocumentInstanceId().documentId().toString();
            return JsonSchemaDocumentId.existingId(UUID.fromString(jsonSchemaDocumentId));
        } else {
            // In case a process has no token wait state ProcessDocumentInstance is not yet created,
            // therefore out business-key is our last chance which is populated with the documentId also.
            var businessKey = getBusinessKey(processInstanceId, variableScope);
            return JsonSchemaDocumentId.existingId(UUID.fromString(businessKey));
        }
    }

    public Document getDocument(ProcessInstanceId processInstanceId, VariableScope variableScope) {
        final var document = AuthorizationContext
            .runWithoutAuthorization(
                () -> documentService.get(getDocumentId(processInstanceId, variableScope).toString())
            );

        authorizationService.requirePermission(
            new AuthorizationRequest<>(
                JsonSchemaDocument.class,
                VIEW
            ),
            document,
            null
        );

        return document;
    }

    @Override
    public Optional<ProcessDocumentDefinition> findProcessDocumentDefinition(ProcessInstanceId processInstanceId) {
        return camundaProcessService.findProcessInstanceById(processInstanceId.toString())
            .map(instance -> camundaProcessService.findProcessDefinitionById(instance.getProcessDefinitionId()))
            .map(definition -> new CamundaProcessDefinitionKey(definition.getKey()))
            .map(definitionKey -> AuthorizationContext.runWithoutAuthorization(() ->
                processDocumentAssociationService.findProcessDocumentDefinition(definitionKey))
            ).map(optional -> (CamundaProcessJsonSchemaDocumentDefinition) optional.orElse(null));
    }

    private String getBusinessKey(ProcessInstanceId processInstanceId, VariableScope variableScope) {
        if (variableScope instanceof BaseDelegateExecution) {
            return ((BaseDelegateExecution) variableScope).getBusinessKey();
        } else if (variableScope instanceof DelegateTask) {
            return ((DelegateTask) variableScope).getExecution().getBusinessKey();
        } else {
            var processInstance = camundaProcessService.findProcessInstanceById(processInstanceId.toString())
                .orElseThrow(() -> new RuntimeException("Process instance not found by id $processInstanceId"));
            return processInstance.getBusinessKey();
        }
    }

    private ProcessInstanceWithDefinition startProcess(
        Document document,
        String processDefinitionKey,
        Map<String, Object> processVars
    ) {
        return camundaProcessService.startProcess(
            processDefinitionKey,
            document.id().toString(),
            processVars
        );
    }

    private FunctionResult<CamundaTask, OperationError> findTaskById(String taskId) {
        try {
            return new FunctionResult.Successful<>(camundaTaskService.findTaskById(taskId));
        } catch (RuntimeException ex) {
            var error = new OperationError.FromException(ex);
            return new FunctionResult.Erroneous<>(error);
        }
    }

    private OperationError parseAndLogException(Exception ex) {
        final UUID referenceId = UUID.randomUUID();
        logger.error("Unexpected error occurred - {}", referenceId, ex);
        return new OperationError.FromString("Unexpected error occurred, please contact support - referenceId: " + referenceId);
    }

}
