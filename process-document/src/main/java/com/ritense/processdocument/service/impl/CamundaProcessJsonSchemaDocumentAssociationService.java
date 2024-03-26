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

import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.exception.UnknownDocumentDefinitionException;
import com.ritense.document.repository.DocumentDefinitionRepository;
import com.ritense.document.service.DocumentDefinitionService;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.JsonSchemaDocumentActionProvider;
import com.ritense.processdocument.domain.ProcessDefinitionKey;
import com.ritense.processdocument.domain.ProcessDocumentDefinition;
import com.ritense.processdocument.domain.ProcessDocumentInstanceId;
import com.ritense.processdocument.domain.ProcessInstanceId;
import com.ritense.processdocument.domain.impl.CamundaProcessDefinitionKey;
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinition;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinitionId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstance;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstanceId;
import com.ritense.processdocument.domain.impl.ProcessDocumentInstanceDto;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.processdocument.exception.DuplicateProcessDocumentDefinitionException;
import com.ritense.processdocument.exception.ProcessDocumentDefinitionNotFoundException;
import com.ritense.processdocument.exception.UnknownProcessDefinitionException;
import com.ritense.processdocument.repository.ProcessDocumentDefinitionRepository;
import com.ritense.processdocument.repository.ProcessDocumentInstanceRepository;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.result.FunctionResult;
import com.ritense.valtimo.contract.result.OperationError;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byKey;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertStateTrue;

public class CamundaProcessJsonSchemaDocumentAssociationService implements ProcessDocumentAssociationService {

    private static final Logger logger = LoggerFactory.getLogger(CamundaProcessJsonSchemaDocumentAssociationService.class);
    private final ProcessDocumentDefinitionRepository processDocumentDefinitionRepository;
    private final ProcessDocumentInstanceRepository processDocumentInstanceRepository;
    private final DocumentDefinitionRepository<JsonSchemaDocumentDefinition> documentDefinitionRepository;
    private final DocumentDefinitionService documentDefinitionService;
    private final CamundaRepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final AuthorizationService authorizationService;
    private final DocumentService documentService;
    private final UserManagementService userManagementService;

    public CamundaProcessJsonSchemaDocumentAssociationService(
        ProcessDocumentDefinitionRepository processDocumentDefinitionRepository,
        ProcessDocumentInstanceRepository processDocumentInstanceRepository,
        DocumentDefinitionRepository<JsonSchemaDocumentDefinition> documentDefinitionRepository,
        DocumentDefinitionService documentDefinitionService,
        CamundaRepositoryService repositoryService,
        RuntimeService runtimeService,
        HistoryService historyService,
        AuthorizationService authorizationService,
        DocumentService documentService,
        UserManagementService userManagementService
    ) {
        this.processDocumentDefinitionRepository = processDocumentDefinitionRepository;
        this.processDocumentInstanceRepository = processDocumentInstanceRepository;
        this.documentDefinitionRepository = documentDefinitionRepository;
        this.documentDefinitionService = documentDefinitionService;
        this.repositoryService = repositoryService;
        this.historyService = historyService;
        this.runtimeService = runtimeService;
        this.authorizationService = authorizationService;
        this.documentService = documentService;
        this.userManagementService = userManagementService;
    }

    @Override
    public Optional<CamundaProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinition(ProcessDefinitionKey processDefinitionKey) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(processDefinitionKey);
    }

    @Override
    public CamundaProcessJsonSchemaDocumentDefinition getProcessDocumentDefinition(ProcessDefinitionKey processDefinitionKey) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        return findProcessDocumentDefinition(processDefinitionKey)
            .orElseThrow(() -> new ProcessDocumentDefinitionNotFoundException("for processDefinitionKey '" + processDefinitionKey + "'"));
    }

    @Override
    public List<CamundaProcessJsonSchemaDocumentDefinition> findAllProcessDocumentDefinitions(ProcessDefinitionKey processDefinitionKey) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findAllByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(processDefinitionKey);
    }

    @Override
    public Optional<CamundaProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinition(ProcessDefinitionKey processDefinitionKey, long documentDefinitionVersion) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findByProcessDefinitionKeyAndDocumentDefinitionVersion(processDefinitionKey, documentDefinitionVersion);
    }

    @Override
    public CamundaProcessJsonSchemaDocumentDefinition getProcessDocumentDefinition(ProcessDefinitionKey processDefinitionKey, long documentDefinitionVersion) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        return findProcessDocumentDefinition(processDefinitionKey, documentDefinitionVersion)
            .orElseThrow(() -> new ProcessDocumentDefinitionNotFoundException("for processDefinitionKey '" + processDefinitionKey + "' and version '" + documentDefinitionVersion + "'"));
    }

    @Override
    public List<CamundaProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitions(String documentDefinitionName, Boolean startableByUser) {
        return processDocumentDefinitionRepository
            .findAllByDocumentDefinitionNameAndLatestDocumentDefinitionVersionAndStartableByUser(documentDefinitionName, startableByUser);
    }

    @Override
    public List<CamundaProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitions(String documentDefinitionName, Long documentDefinitionVersion) {
        return processDocumentDefinitionRepository
            .findAllByDocumentDefinitionNameAndVersion(documentDefinitionName, documentDefinitionVersion);
    }

    @Override
    public List<CamundaProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitionsByProcessDefinitionKey(String processDefinitionKey) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findAllByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(processDefinitionKey);
    }

    @Override
    public Optional<? extends ProcessDocumentDefinition> findByDocumentDefinitionName(String documentDefinitionName) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findByDocumentDefinitionName(documentDefinitionName);
    }

    @Override
    public Optional<CamundaProcessJsonSchemaDocumentInstance> findProcessDocumentInstance(ProcessInstanceId processInstanceId) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentInstance.class);
        return processDocumentInstanceRepository.findByProcessInstanceId(processInstanceId);
    }

    @Override
    public List<CamundaProcessJsonSchemaDocumentInstance> findProcessDocumentInstances(Document.Id documentId) {
        var document = documentService.findBy(documentId).orElseThrow();

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                JsonSchemaDocumentActionProvider.VIEW,
                (JsonSchemaDocument) document
            )
        );

        var processes = processDocumentInstanceRepository.findAllByProcessDocumentInstanceIdDocumentId(documentId);
        for (var process : processes) {
            CamundaProcessJsonSchemaDocumentInstanceId id = process.getId();
            if (id != null) {
                var camundaProcess = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(id.processInstanceId().toString())
                    .singleResult();
                process.setActive(camundaProcess != null && !camundaProcess.isEnded());
            }
        }
        return processes;
    }

    @Override
    public List<ProcessDocumentInstanceDto> findProcessDocumentInstanceDtos(Document.Id documentId) {
        var document = documentService.findBy(documentId).orElseThrow();

        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                JsonSchemaDocumentActionProvider.VIEW,
                (JsonSchemaDocument) document
            )
        );

        return processDocumentInstanceRepository.findAllByProcessDocumentInstanceIdDocumentId(documentId).stream()
            .map(process -> {
                if (process.getId() != null) {
                    var camundaProcess = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(process.getId().processInstanceId().toString())
                        .singleResult();
                    process.setActive(camundaProcess != null && camundaProcess.getEndTime() == null);
                    var camundaProcessDefinition = runWithoutAuthorization(() ->
                        repositoryService.findLatestProcessDefinition(camundaProcess.getProcessDefinitionKey())
                    );
                    var startDateTime = LocalDateTime.ofInstant(
                        camundaProcess.getStartTime().toInstant(),
                        ZoneId.systemDefault()
                    );
                    var startedBy = camundaProcess.getStartUserId() == null ? null :
                        userManagementService.findByEmail(camundaProcess.getStartUserId()).orElseThrow().getFullName();
                    return new ProcessDocumentInstanceDto(
                        process.getId(),
                        process.processName(),
                        process.isActive(),
                        camundaProcess.getProcessDefinitionVersion(),
                        camundaProcessDefinition.getVersion(),
                        startedBy,
                        startDateTime
                    );
                }

                return new ProcessDocumentInstanceDto(
                    process.getId(),
                    process.processName(),
                    process.isActive()
                );
            })
            .toList();
    }

    @Override
    @Transactional
    public void deleteProcessDocumentInstances(String processName) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentInstance.class);

        logger.debug("Remove all running process document instances for process: {}", processName);
        processDocumentInstanceRepository.deleteAllByProcessName(processName);
    }

    @Override
    @Transactional
    public Optional<CamundaProcessJsonSchemaDocumentDefinition> createProcessDocumentDefinition(ProcessDocumentDefinitionRequest request) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        JsonSchemaDocumentDefinitionId documentDefinitionId;
        if (request.getDocumentDefinitionVersion().isPresent()) {
            documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId(
                request.documentDefinitionName(),
                request.getDocumentDefinitionVersion().orElseThrow()
            );
        } else {
            documentDefinitionId = documentDefinitionService.findIdByName(request.documentDefinitionName());
        }

        return createProcessDocumentDefinition(
            new CamundaProcessDefinitionKey(request.processDefinitionKey()),
            documentDefinitionId,
            request.canInitializeDocument(),
            request.startableByUser()
        );
    }

    private Optional<CamundaProcessJsonSchemaDocumentDefinition> createProcessDocumentDefinition(
        CamundaProcessDefinitionKey processDefinitionKey,
        JsonSchemaDocumentDefinitionId documentDefinitionId,
        boolean canInitializeDocument,
        boolean startableByUser
    ) {
        if (!AuthorizationContext.runWithoutAuthorization(
            () -> repositoryService.processDefinitionExists(byKey(processDefinitionKey.toString())))
        ) {
            throw new UnknownProcessDefinitionException(processDefinitionKey.toString());
        }
        if (!documentDefinitionRepository.existsById(documentDefinitionId)) {
            throw new UnknownDocumentDefinitionException(documentDefinitionId.toString());
        }

        var knownProcessDocumentDefinitions = processDocumentDefinitionRepository
            .findAllByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(processDefinitionKey);

        assertStateTrue(knownProcessDocumentDefinitions.isEmpty(), "Process is already in use within the context of another dossier.");

        final var id = CamundaProcessJsonSchemaDocumentDefinitionId.newId(
            processDefinitionKey,
            documentDefinitionId
        );
        if (processDocumentDefinitionRepository.existsById(id)) {
            throw new DuplicateProcessDocumentDefinitionException(processDefinitionKey.toString(), documentDefinitionId.toString());
        }

        final var association = processDocumentDefinitionRepository.saveAndFlush(
            new CamundaProcessJsonSchemaDocumentDefinition(id, canInitializeDocument, startableByUser)
        );
        logger.info(
            "Created ProcessDocumentDefinition - associated process-definition - {} - with document-definition - {} ",
            processDefinitionKey,
            documentDefinitionId
        );
        return Optional.of(association);
    }

    @Transactional
    @Override
    public void deleteProcessDocumentDefinition(ProcessDocumentDefinitionRequest request) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        logger.debug("Remove process document definition for document definition: {}", request.documentDefinitionName());

        JsonSchemaDocumentDefinitionId documentDefinitionId;
        if (request.getDocumentDefinitionVersion().isPresent()) {
            documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId(
                request.documentDefinitionName(),
                request.getDocumentDefinitionVersion().orElseThrow()
            );
        } else {
            documentDefinitionId = documentDefinitionService.findIdByName(request.documentDefinitionName());
        }

        final var id = CamundaProcessJsonSchemaDocumentDefinitionId.existingId(
            new CamundaProcessDefinitionKey(request.processDefinitionKey()),
            documentDefinitionId
        );
        processDocumentDefinitionRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void deleteProcessDocumentDefinition(String documentDefinitionName) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        processDocumentDefinitionRepository.deleteByDocumentDefinition(documentDefinitionName);
    }

    @Transactional
    @Override
    public Optional<CamundaProcessJsonSchemaDocumentInstance> createProcessDocumentInstance(
        String processInstanceId,
        UUID documentId,
        String processName
    ) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentInstance.class);

        final var id = CamundaProcessJsonSchemaDocumentInstanceId.newId(
            new CamundaProcessInstanceId(processInstanceId),
            JsonSchemaDocumentId.existingId(documentId)
        );
        final var association = processDocumentInstanceRepository.saveAndFlush(
            new CamundaProcessJsonSchemaDocumentInstance(id, processName)
        );
        logger.info(
            "Created PDI - associated - processInstanceId {} with documentId - {} for process - {}",
            processInstanceId,
            processName,
            documentId
        );
        return Optional.of(association);
    }

    @Override
    public FunctionResult<CamundaProcessJsonSchemaDocumentInstance, OperationError> getProcessDocumentInstanceResult(
        ProcessDocumentInstanceId processDocumentInstanceId
    ) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentInstance.class);

        final var result = processDocumentInstanceRepository.findById(processDocumentInstanceId);
        if (result.isPresent()) {
            return new FunctionResult.Successful<>(result.get());
        } else {
            final String msg = "Corresponding process-document-instance is not associated with process-document-instance-id";
            return new FunctionResult.Erroneous<>(new OperationError.FromString(msg));
        }
    }

    private <T> void denyAuthorization(Class<T> clazz) {
        authorizationService
            .requirePermission(
                new EntityAuthorizationRequest(
                    clazz,
                    Action.deny()
                )
            );
    }
}
