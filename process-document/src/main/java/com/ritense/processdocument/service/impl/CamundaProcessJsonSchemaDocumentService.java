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

import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentService;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinitionId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstanceId;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndCompleteTaskRequest;
import com.ritense.processdocument.domain.impl.request.ModifyDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest;
import com.ritense.processdocument.domain.impl.request.NewDocumentForRunningProcessRequest;
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
import com.ritense.processdocument.service.result.DocumentFunctionResult;
import com.ritense.processdocument.service.result.ModifyDocumentAndCompleteTaskResult;
import com.ritense.processdocument.service.result.ModifyDocumentAndStartProcessResult;
import com.ritense.processdocument.service.result.NewDocumentAndStartProcessResult;
import com.ritense.processdocument.service.result.NewDocumentForRunningProcessResult;
import com.ritense.valtimo.camunda.domain.ProcessInstanceWithDefinition;
import com.ritense.valtimo.contract.result.FunctionResult;
import com.ritense.valtimo.contract.result.OperationError;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.task.Task;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CamundaProcessJsonSchemaDocumentService implements ProcessDocumentService {

    private final DocumentService documentService;
    private final DocumentDefinitionService documentDefinitionService;
    private final CamundaTaskService camundaTaskService;
    private final CamundaProcessService camundaProcessService;
    private final ProcessDocumentAssociationService processDocumentAssociationService;

    @Override
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
            final var documentDefinitionId = documentDefinitionService.findIdByNameAndVersion(
                newDocumentRequest.documentDefinitionName(),
                null
            );
            final var processDocumentDefinitionId = CamundaProcessJsonSchemaDocumentDefinitionId.existingId(
                processDefinitionKey,
                documentDefinitionId
            );
            final var processDocumentDefinitionResult = processDocumentAssociationService.getProcessDocumentDefinitionResult(processDocumentDefinitionId);

            if (!processDocumentDefinitionResult.hasResult()) {
                return new NewDocumentAndStartProcessResultFailed(processDocumentDefinitionResult.errors());
            }

            final var newDocumentResult = documentService.createDocument(newDocumentRequest);

            if (newDocumentResult.resultingDocument().isEmpty()) {
                return new NewDocumentAndStartProcessResultFailed(newDocumentResult.errors());
            }

            final var document = newDocumentResult.resultingDocument().orElseThrow();
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

            final var modifyDocumentResult = documentService.modifyDocument(modifyDocumentRequest);
            if (modifyDocumentResult.resultingDocument().isEmpty()) {
                return new ModifyDocumentAndCompleteTaskResultFailed(modifyDocumentResult.errors());
            }
            camundaTaskService.completeTask(request.taskId(), request.getProcessVars());

            return new ModifyDocumentAndCompleteTaskResultSucceeded(modifyDocumentResult.resultingDocument().orElseThrow());
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
            final var processDefinitionKey = new CamundaProcessDefinitionKey(request.processDefinitionKey());
            final var processInstanceId = new CamundaProcessInstanceId(request.processInstanceId());
            final var newDocumentRequest = request.newDocumentRequest();
            final var documentDefinitionId = documentDefinitionService.findIdByNameAndVersion(
                newDocumentRequest.documentDefinitionName(),
                null
            );

            final var processDocumentDefinitionId = CamundaProcessJsonSchemaDocumentDefinitionId.newId(
                processDefinitionKey,
                documentDefinitionId
            );
            final var processDocumentDefinitionResult = processDocumentAssociationService.getProcessDocumentDefinitionResult(processDocumentDefinitionId);

            if (!processDocumentDefinitionResult.hasResult()) {
                return new NewDocumentForRunningProcessResultFailed(processDocumentDefinitionResult.errors());
            }

            final var newDocumentResult = documentService.createDocument(newDocumentRequest);

            if (newDocumentResult.resultingDocument().isEmpty()) {
                return new NewDocumentForRunningProcessResultFailed(newDocumentResult.errors());
            }

            final var document = newDocumentResult.resultingDocument().orElseThrow();

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
            final var modifyDocumentResult = documentService.modifyDocument(request.modifyDocumentRequest());

            if (modifyDocumentResult.resultingDocument().isEmpty()) {
                return new ModifyDocumentAndStartProcessResultFailed(modifyDocumentResult.errors());
            }
            final var document = modifyDocumentResult.resultingDocument().get();

            //Part 2 process start
            final var documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId(document.definitionId());
            final var processDefinitionKey = new CamundaProcessDefinitionKey(request.processDefinitionKey());
            final var processDocumentDefinitionId = CamundaProcessJsonSchemaDocumentDefinitionId.existingId(processDefinitionKey, documentDefinitionId);
            final var processDocumentDefinitionResult = processDocumentAssociationService.getProcessDocumentDefinitionResult(processDocumentDefinitionId);

            if (!processDocumentDefinitionResult.hasResult()) {
                return new ModifyDocumentAndStartProcessResultFailed(processDocumentDefinitionResult.errors());
            }

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

    private FunctionResult<Task, OperationError> findTaskById(String taskId) {
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