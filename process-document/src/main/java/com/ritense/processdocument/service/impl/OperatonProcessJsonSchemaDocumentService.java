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

package com.ritense.processdocument.service.impl;

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.CREATE;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.MODIFY;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.VIEW;
import static com.ritense.valtimo.operaton.authorization.OperatonTaskActionProvider.COMPLETE;

import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.processdocument.domain.ProcessInstanceId;
import com.ritense.processdocument.domain.impl.OperatonProcessDefinitionId;
import com.ritense.processdocument.domain.impl.OperatonProcessInstanceId;
import com.ritense.processdocument.domain.impl.OperatonProcessJsonSchemaDocumentInstanceId;
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
import com.ritense.valtimo.operaton.domain.OperatonExecution;
import com.ritense.valtimo.operaton.domain.OperatonTask;
import com.ritense.valtimo.operaton.domain.ProcessInstanceWithDefinition;
import com.ritense.valtimo.contract.result.FunctionResult;
import com.ritense.valtimo.contract.result.OperationError;
import com.ritense.valtimo.service.OperatonProcessService;
import com.ritense.valtimo.service.OperatonTaskService;
import java.util.Map;
import java.util.UUID;
import org.operaton.bpm.engine.delegate.BaseDelegateExecution;
import org.operaton.bpm.engine.delegate.DelegateTask;
import org.operaton.bpm.engine.delegate.VariableScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

public class OperatonProcessJsonSchemaDocumentService implements ProcessDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(OperatonProcessJsonSchemaDocumentService.class);
    private final JsonSchemaDocumentService documentService;
    private final OperatonTaskService operatonTaskService;
    private final OperatonProcessService operatonProcessService;
    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final AuthorizationService authorizationService;

    public OperatonProcessJsonSchemaDocumentService(
        JsonSchemaDocumentService documentService, OperatonTaskService operatonTaskService,
        OperatonProcessService operatonProcessService,
        ProcessDocumentAssociationService processDocumentAssociationService,
        AuthorizationService authorizationService
    ) {
        this.documentService = documentService;
        this.operatonTaskService = operatonTaskService;
        this.operatonProcessService = operatonProcessService;
        this.processDocumentAssociationService = processDocumentAssociationService;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional
    public DocumentFunctionResult dispatch(Request request) {
        if (request instanceof NewDocumentAndStartProcessRequest newDocumentAndStartProcessRequest) {
            return newDocumentAndStartProcess(newDocumentAndStartProcessRequest);
        } else if (request instanceof ModifyDocumentAndCompleteTaskRequest modifyDocumentAndCompleteTaskRequest) {
            return modifyDocumentAndCompleteTask(modifyDocumentAndCompleteTaskRequest);
        } else if (request instanceof NewDocumentForRunningProcessRequest newDocumentForRunningProcessRequest) {
            return newDocumentForRunningProcess(newDocumentForRunningProcessRequest);
        } else if (request instanceof ModifyDocumentAndStartProcessRequest modifyDocumentAndStartProcessRequest) {
            return modifyDocumentAndStartProcess(modifyDocumentAndStartProcessRequest);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    @Transactional
    public NewDocumentAndStartProcessResult newDocumentAndStartProcess(
        final NewDocumentAndStartProcessRequest request
    ) {
        try {
            final var processDefinitionKey = new OperatonProcessDefinitionId(request.processDefinitionKey());
            final var newDocumentRequest = request.newDocumentRequest();

            final var newDocumentResult = runWithoutAuthorization(
                () -> documentService.createDocument(newDocumentRequest));

            if (newDocumentResult.resultingDocument().isEmpty()) {
                return new NewDocumentAndStartProcessResultFailed(newDocumentResult.errors());
            }

            final var document = newDocumentResult.resultingDocument().orElseThrow();

            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    CREATE,
                    document
                )
            );

            final var processInstanceWithDefinition = startProcess(
                document,
                processDefinitionKey.toString(),
                request.getProcessVars()
            );

            final var operatonProcessInstanceId = new OperatonProcessInstanceId(
                processInstanceWithDefinition.getProcessInstanceDto().getId());
            runWithoutAuthorization(() ->
                processDocumentAssociationService.createProcessDocumentInstance(
                    operatonProcessInstanceId.toString(),
                    UUID.fromString(document.id().toString()),
                    processInstanceWithDefinition.getProcessDefinition().getName()
                )
            );

            request.doAdditionalModifications(document);

            return new NewDocumentAndStartProcessResultSucceeded(
                document,
                operatonProcessInstanceId
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

            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    OperatonTask.class,
                    COMPLETE,
                    task
                )
            );

            final var modifyDocumentRequest = request.modifyDocumentRequest();
            final var modifiedDocumentId = JsonSchemaDocumentId.existingId(
                UUID.fromString(modifyDocumentRequest.documentId()));
            final var processInstanceId = new OperatonProcessInstanceId(task.getProcessInstanceId());

            final var processDocumentInstanceResult = runWithoutAuthorization(() ->
                processDocumentAssociationService.getProcessDocumentInstanceResult(
                    OperatonProcessJsonSchemaDocumentInstanceId.existingId(processInstanceId, modifiedDocumentId)
                )
            );

            if (!processDocumentInstanceResult.hasResult()) {
                return new ModifyDocumentAndCompleteTaskResultFailed(processDocumentInstanceResult.errors());
            }

            final var modifyDocumentResult = runWithoutAuthorization(
                () -> documentService.modifyDocument(modifyDocumentRequest));
            if (modifyDocumentResult.resultingDocument().isEmpty()) {
                return new ModifyDocumentAndCompleteTaskResultFailed(modifyDocumentResult.errors());
            }

            final var document = modifyDocumentResult.resultingDocument().orElseThrow();

            request.doAdditionalModifications(document);

            AuthorizationContext.runWithoutAuthorization(
                () -> {
                    operatonTaskService.completeTaskWithFormData(request.taskId(), request.getProcessVars());
                    return null;
                });

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
            final var processInstanceId = new OperatonProcessInstanceId(request.processInstanceId());
            final var newDocumentRequest = request.newDocumentRequest();

            final var newDocumentResult = runWithoutAuthorization(
                () -> documentService.createDocument(newDocumentRequest));

            if (newDocumentResult.resultingDocument().isEmpty()) {
                return new NewDocumentForRunningProcessResultFailed(newDocumentResult.errors());
            }

            final var document = newDocumentResult.resultingDocument().orElseThrow();

            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    CREATE,
                    document
                )
            );

            final String processName = runWithoutAuthorization(
                () -> operatonProcessService.getProcessDefinition(request.processDefinitionKey()).getName());
            processDocumentAssociationService.createProcessDocumentInstance(
                request.processInstanceId(),
                UUID.fromString(document.id().toString()),
                processName
            );

            request.doAdditionalModifications(document);

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
            final var modifyDocumentResult = runWithoutAuthorization(
                () -> documentService.modifyDocument(request.modifyDocumentRequest()));

            if (modifyDocumentResult.resultingDocument().isEmpty()) {
                return new ModifyDocumentAndStartProcessResultFailed(modifyDocumentResult.errors());
            }
            final var document = modifyDocumentResult.resultingDocument().orElseThrow();

            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    MODIFY,
                    document
                )
            );

            request.doAdditionalModifications(document);

            //Part 2 process start
            final var processDefinitionKey = new OperatonProcessDefinitionId(request.processDefinitionKey());
            final var processInstanceWithDefinition = startProcess(
                document, processDefinitionKey.toString(), request.getProcessVars());
            final var operatonProcessInstanceId = new OperatonProcessInstanceId(
                processInstanceWithDefinition.getProcessInstanceDto().getId()
            );

            runWithoutAuthorization(() -> processDocumentAssociationService.createProcessDocumentInstance(
                operatonProcessInstanceId.toString(),
                UUID.fromString(document.id().toString()),
                processInstanceWithDefinition.getProcessDefinition().getName()
            ));

            return new ModifyDocumentAndStartProcessResultSucceeded(document, operatonProcessInstanceId);
        } catch (RuntimeException ex) {
            return new ModifyDocumentAndStartProcessResultFailed(parseAndLogException(ex));
        }
    }

    public StartProcessForDocumentResult startProcessForDocument(StartProcessForDocumentRequest request) {
        try {
            //Part 1 find document
            final var optionalDocument = runWithoutAuthorization(() -> documentService.findBy(request.getDocumentId()));

            if (optionalDocument.isEmpty()) {
                return new StartProcessForDocumentResultFailed(
                    new OperationError.FromString("Document could not be found"));
            }

            final var document = optionalDocument.get();

            authorizationService.requirePermission(
                new EntityAuthorizationRequest<>(
                    JsonSchemaDocument.class,
                    VIEW,
                    document
                )
            );

            //Part 2 process start
            final var processDefinitionKey = new OperatonProcessDefinitionId(request.getProcessDefinitionKey());
            final var processInstanceWithDefinition = startProcess(
                document, processDefinitionKey.toString(), request.getProcessVars());
            final var operatonProcessInstanceId = new OperatonProcessInstanceId(
                processInstanceWithDefinition.getProcessInstanceDto().getId()
            );

            runWithoutAuthorization(() -> processDocumentAssociationService.createProcessDocumentInstance(
                operatonProcessInstanceId.toString(),
                document.id().getId(),
                processInstanceWithDefinition.getProcessDefinition().getName()
            ));

            request.doAdditionalModifications(document);

            return new StartProcessForDocumentResultSucceeded(document, operatonProcessInstanceId);
        } catch (RuntimeException ex) {
            return new StartProcessForDocumentResultFailed(parseAndLogException(ex));
        }
    }

    public JsonSchemaDocumentId getDocumentId(
        ProcessInstanceId processInstanceId,
        @Nullable VariableScope variableScope
    ) {
        denyAuthorization();
        var processDocumentInstance = processDocumentAssociationService
            .findProcessDocumentInstance(processInstanceId)
            .orElse(null);
        if (processDocumentInstance != null) {
            var jsonSchemaDocumentId = processDocumentInstance.processDocumentInstanceId().documentId();
            return JsonSchemaDocumentId.existingId(jsonSchemaDocumentId);
        } else {
            // In case a process has no token wait state ProcessDocumentInstance is not yet created,
            // therefore out business-key is our last chance which is populated with the documentId also.
            var businessKey = getBusinessKey(processInstanceId, variableScope);
            if (businessKey != null && businessKey.matches("[a-f0-9]{8}(?:-[a-f0-9]{4}){4}[a-f0-9]{8}")) {
                return JsonSchemaDocumentId.existingId(businessKey);
            } else {
                return null;
            }
        }
    }

    public JsonSchemaDocument getDocument(ProcessInstanceId processInstanceId, VariableScope variableScope) {
        final var document = runWithoutAuthorization(
            () -> documentService.get(getDocumentId(processInstanceId, variableScope).toString())
        );

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                VIEW,
                document
            )
        );

        return document;
    }

    private String getBusinessKey(ProcessInstanceId processInstanceId, VariableScope variableScope) {
        if (variableScope instanceof BaseDelegateExecution delegateExecution) {
            return delegateExecution.getBusinessKey();
        } else if (variableScope instanceof DelegateTask delegateTask) {
            return delegateTask.getExecution().getBusinessKey();
        } else if (variableScope instanceof OperatonExecution operatonExecution) {
            return operatonExecution.getBusinessKey();
        } else if (variableScope instanceof OperatonTask operatonTask && operatonTask.getExecution() != null) {
            return operatonTask.getExecution().getBusinessKey();
        } else {
            var processInstance =
                runWithoutAuthorization(
                    () -> operatonProcessService.findProcessInstanceById(processInstanceId.toString())
                        .orElseThrow(() -> new RuntimeException("Process instance not found by id $processInstanceId"))
                );
            return processInstance.getBusinessKey();
        }
    }

    private ProcessInstanceWithDefinition startProcess(
        Document document,
        String processDefinitionKey,
        Map<String, Object> processVars
    ) {
        return runWithoutAuthorization(() -> operatonProcessService.startProcess(
            processDefinitionKey,
            document.id().toString(),
            document.definitionId().caseDefinitionId(),
            processVars
        ));
    }

    private FunctionResult<OperatonTask, OperationError> findTaskById(String taskId) {
        try {
            final var task = runWithoutAuthorization(() ->
                operatonTaskService.findTaskById(taskId)
            );
            return
                new FunctionResult.Successful<>(task);
        } catch (RuntimeException ex) {
            var error = new OperationError.FromException(ex);
            return new FunctionResult.Erroneous<>(error);
        }
    }

    private OperationError parseAndLogException(Exception ex) {
        final UUID referenceId = UUID.randomUUID();
        logger.error("Unexpected error occurred - {}", referenceId, ex);
        return new OperationError.FromString(
            "Unexpected error occurred, please contact support - referenceId: " + referenceId);
    }

    private void denyAuthorization() {
        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                Action.deny()
            )
        );
    }

}
