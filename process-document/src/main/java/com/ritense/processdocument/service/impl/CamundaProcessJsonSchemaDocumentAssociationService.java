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

import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.document.domain.Document;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.JsonSchemaDocumentActionProvider;
import com.ritense.processdocument.domain.ProcessDocumentInstanceId;
import com.ritense.processdocument.domain.ProcessInstanceId;
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstance;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstanceId;
import com.ritense.processdocument.domain.impl.ProcessDocumentInstanceDto;
import com.ritense.processdocument.repository.ProcessDocumentInstanceRepository;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.result.FunctionResult;
import com.ritense.valtimo.contract.result.OperationError;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class CamundaProcessJsonSchemaDocumentAssociationService implements ProcessDocumentAssociationService {

    private static final Logger logger = LoggerFactory.getLogger(CamundaProcessJsonSchemaDocumentAssociationService.class);
    private final ProcessDocumentInstanceRepository processDocumentInstanceRepository;
    private final CamundaRepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final AuthorizationService authorizationService;
    private final DocumentService documentService;
    private final UserManagementService userManagementService;

    public CamundaProcessJsonSchemaDocumentAssociationService(
        ProcessDocumentInstanceRepository processDocumentInstanceRepository,
        CamundaRepositoryService repositoryService,
        RuntimeService runtimeService,
        HistoryService historyService,
        AuthorizationService authorizationService,
        DocumentService documentService,
        UserManagementService userManagementService
    ) {
        this.processDocumentInstanceRepository = processDocumentInstanceRepository;
        this.repositoryService = repositoryService;
        this.historyService = historyService;
        this.runtimeService = runtimeService;
        this.authorizationService = authorizationService;
        this.documentService = documentService;
        this.userManagementService = userManagementService;
    }

    /*
    @Override
    public List<CamundaProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitions(
        String documentDefinitionName,
        @Nullable Boolean startableByUser,
        @Nullable Boolean canInitializeDocument
    ) {
        List<CamundaProcessJsonSchemaDocumentDefinition> results = processDocumentDefinitionRepository
            .findAll(documentDefinitionName, startableByUser, canInitializeDocument);

        return results.stream().filter(result -> {
            CamundaProcessDefinition processDefinition = AuthorizationContext.runWithoutAuthorization(() ->
                repositoryService.findLatestProcessDefinition(
                    result.processDocumentDefinitionId().processDefinitionKey().toString()
                )
            );

            return authorizationService.hasPermission(
                new RelatedEntityAuthorizationRequest<>(
                    CamundaExecution.class,
                    CamundaExecutionActionProvider.CREATE,
                    CamundaProcessDefinition.class,
                    processDefinition.getId()
                )
            );
        }).toList();
    }

    @Override
    public List<CamundaProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitions(
        UUID documentId,
        @Nullable Boolean startableByUser,
        @Nullable Boolean canInitializeDocument
    ) {
        Document document = documentService.findBy(JsonSchemaDocumentId.existingId(documentId)).
            orElseThrow(() -> new DocumentNotFoundException("Document not found with id " + documentId));

        List<CamundaProcessJsonSchemaDocumentDefinition> results = processDocumentDefinitionRepository
            .findAll(document.definitionId().name(), startableByUser, canInitializeDocument);

        return results.stream().filter(result -> {
            CamundaProcessDefinition processDefinition = AuthorizationContext.runWithoutAuthorization(() ->
                repositoryService.findLatestProcessDefinition(
                    result.processDocumentDefinitionId().processDefinitionKey().toString()
                )
            );

            return authorizationService.hasPermission(
                new RelatedEntityAuthorizationRequest<>(
                    CamundaExecution.class,
                    CamundaExecutionActionProvider.CREATE,
                    CamundaProcessDefinition.class,
                    processDefinition.getId()
                ).withContext(
                    new AuthorizationResourceContext(
                        JsonSchemaDocument.class,
                        document
                    )
                )
            );
        }).toList();
    }*/

/*    @Override
    public List<CamundaProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitions(
        String documentDefinitionName,
        Long documentDefinitionVersion
    ) {
        return processDocumentDefinitionRepository.findAllByDocumentDefinitionNameAndVersion(documentDefinitionName, documentDefinitionVersion);
    }

    @Override
    public List<CamundaProcessJsonSchemaDocumentDefinition> findProcessDocumentDefinitionsByProcessDefinitionKey(String processDefinitionKey) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findAllByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(processDefinitionKey);
    }*/

/*    @Override
    public Optional<? extends ProcessDocumentDefinition> findByDocumentDefinitionName(String documentDefinitionName) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        return processDocumentDefinitionRepository.findByDocumentDefinitionName(documentDefinitionName);
    }*/

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
                        userManagementService.findByEmail(camundaProcess.getStartUserId()).map(ManageableUser::getFullName).orElse(null);

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
    public void deleteProcessDocumentInstance(ProcessDocumentInstanceId processDocumentInstanceId) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentInstance.class);

        logger.debug("Deleting process document instance: {}", processDocumentInstanceId);
        processDocumentInstanceRepository.deleteById(processDocumentInstanceId);
    }

    /*
    @Override
    @Transactional
    public Optional<CamundaProcessJsonSchemaDocumentDefinition> createProcessDocumentDefinition(
        ProcessDocumentDefinitionRequest request
    ) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        JsonSchemaDocumentDefinitionId documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId(
            request.documentDefinitionName(),
            request.getCaseDefinitionId()
        );

        return createProcessDocumentDefinition(
            new CamundaProcessDefinitionId(request.processDefinitionKey()),
            documentDefinitionId,
            request.canInitializeDocument(),
            request.startableByUser()
        );
    }
*/

/*    private Optional<CamundaProcessJsonSchemaDocumentDefinition> createProcessDocumentDefinition(
        CamundaProcessDefinitionId processDefinitionKey,
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

        assertStateTrue(
            knownProcessDocumentDefinitions.isEmpty(),
            "Process is already in use within the context of another dossier."
        );

        final var id = CamundaProcessJsonSchemaDocumentDefinitionId.newId(
            processDefinitionKey,
            documentDefinitionId
        );
        if (processDocumentDefinitionRepository.existsById(id)) {
            throw new DuplicateProcessDocumentDefinitionException(
                processDefinitionKey.toString(),
                documentDefinitionId.toString()
            );
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

        if (logger.isDebugEnabled()) {
            logger.debug(
                "Remove process document definition for document definition: {}",
                request.documentDefinitionName()
            );
        }

        JsonSchemaDocumentDefinitionId documentDefinitionId = JsonSchemaDocumentDefinitionId.existingId(
            request.documentDefinitionName(),
            request.getCaseDefinitionId()
        );

        final var id = CamundaProcessJsonSchemaDocumentDefinitionId.existingId(
            new CamundaProcessDefinitionId(request.processDefinitionKey()),
            documentDefinitionId
        );
        processDocumentDefinitionRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void deleteProcessDocumentDefinition(String documentDefinitionName) {
        denyAuthorization(CamundaProcessJsonSchemaDocumentDefinition.class);

        processDocumentDefinitionRepository.deleteByDocumentDefinition(documentDefinitionName);
    }*/

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
        authorizationService.requirePermission(
            new EntityAuthorizationRequest(
                clazz,
                Action.deny()
            )
        );
    }
}
