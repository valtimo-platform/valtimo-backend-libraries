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

package com.ritense.valtimo.service;

import static com.ritense.valtimo.camunda.repository.CamundaHistoricProcessInstanceSpecificationHelper.byStartUserId;
import static com.ritense.valtimo.camunda.repository.CamundaHistoricProcessInstanceSpecificationHelper.byUnfinished;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.NAME;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.VERSION;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byActive;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byKey;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byLatestVersion;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byLatestVersionTag;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byNotLinkedToCaseDefinition;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byVersionTag;

import com.fasterxml.jackson.core.JsonPointer;
import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.valtimo.camunda.authorization.CamundaExecutionActionProvider;
import com.ritense.valtimo.camunda.domain.CamundaDeploymentSource;
import com.ritense.valtimo.camunda.domain.CamundaExecution;
import com.ritense.valtimo.camunda.domain.CamundaHistoricProcessInstance;
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition;
import com.ritense.valtimo.camunda.domain.ProcessInstanceWithDefinition;
import com.ritense.valtimo.camunda.repository.CamundaExecutionRepository;
import com.ritense.valtimo.camunda.service.CamundaHistoryService;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.camunda.service.CamundaRuntimeService;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.event.ProcessDefinitionDeleted;
import com.ritense.valtimo.exception.FileExtensionNotSupportedException;
import com.ritense.valtimo.exception.NoFileExtensionFoundException;
import com.ritense.valtimo.exception.ProcessDefinitionNotFoundException;
import com.ritense.valtimo.exception.ProcessNotDeployableException;
import com.ritense.valtimo.helper.CamundaDeploymentSourceHelper;
import com.ritense.valtimo.service.util.FormUtils;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BusinessRuleTask;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.TimeDuration;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

public class CamundaProcessService {

    public static final String CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX = "CD:";

    private static final String UNDEFINED_BUSINESS_KEY = "UNDEFINED_BUSINESS_KEY";
    private static final String SYSTEM_PROCESS_PROPERTY = "systemProcess";
    private static final Logger logger = LoggerFactory.getLogger(CamundaProcessService.class);

    private final RuntimeService runtimeService;
    private final CamundaRuntimeService camundaRuntimeService;
    private final RepositoryService repositoryService;
    private final CamundaRepositoryService camundaRepositoryService;
    private final FormService formService;
    private final CamundaHistoryService historyService;
    private final ProcessPropertyService processPropertyService;
    private final ValtimoProperties valtimoProperties;
    private final AuthorizationService authorizationService;
    private final ProcessDefinitionCaseDefinitionLinker processDefinitionCaseDefinitionLinker;
    private final CamundaByteArrayService camundaByteArrayService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final CamundaExecutionRepository camundaExecutionRepository;

    private final CamundaDeploymentSourceHelper camundaDeploymentSourceHelper;

    public CamundaProcessService(
        RuntimeService runtimeService,
        CamundaRuntimeService camundaRuntimeService,
        RepositoryService repositoryService,
        CamundaRepositoryService camundaRepositoryService,
        FormService formService,
        CamundaHistoryService historyService,
        ProcessPropertyService processPropertyService,
        ValtimoProperties valtimoProperties,
        AuthorizationService authorizationService,
        CamundaExecutionRepository camundaExecutionRepository,
        ProcessDefinitionCaseDefinitionLinker processDefinitionCaseDefinitionLinker,
        CamundaByteArrayService camundaByteArrayService,
        ApplicationEventPublisher applicationEventPublisher,
        CamundaDeploymentSourceHelper camundaDeploymentSourceHelper
    ) {
        this.runtimeService = runtimeService;
        this.camundaRuntimeService = camundaRuntimeService;
        this.repositoryService = repositoryService;
        this.camundaRepositoryService = camundaRepositoryService;
        this.formService = formService;
        this.historyService = historyService;
        this.processPropertyService = processPropertyService;
        this.valtimoProperties = valtimoProperties;
        this.authorizationService = authorizationService;
        this.camundaExecutionRepository = camundaExecutionRepository;
        this.processDefinitionCaseDefinitionLinker = processDefinitionCaseDefinitionLinker;
        this.camundaByteArrayService = camundaByteArrayService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.camundaDeploymentSourceHelper = camundaDeploymentSourceHelper;
    }

    public CamundaProcessDefinition findProcessDefinitionById(String processDefinitionId) {
        denyAuthorization();
        return AuthorizationContext
            .runWithoutAuthorization(() -> camundaRepositoryService.findProcessDefinitionById(processDefinitionId));
    }

    public CamundaProcessDefinition getProcessDefinitionById(String processDefinitionId) {
        denyAuthorization();
        var processDefinition = AuthorizationContext
            .runWithoutAuthorization(() -> findProcessDefinitionById(processDefinitionId));
        if (processDefinition == null) {
            throw new ProcessDefinitionNotFoundException("with id '" + processDefinitionId + "'.");
        } else {
            return processDefinition;
        }
    }

    public boolean processDefinitionExistsByKey(String processDefinitionKey) {
        denyAuthorization();
        return AuthorizationContext
            .runWithoutAuthorization(
                () -> camundaRepositoryService.countProcessDefinitions(byKey(processDefinitionKey)) >= 1
            );
    }

    public Optional<ProcessInstance> findProcessInstanceById(String processInstanceId) {
        denyAuthorization();
        return Optional.ofNullable(runtimeService
            .createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult());
    }

    public List<ProcessInstance> findProcessInstancesByIds(Set<String> processInstanceIds) {
        denyAuthorization();
        return runtimeService
            .createProcessInstanceQuery()
            .processInstanceIds(processInstanceIds)
            .list();
    }

    public ProcessDefinition getProcessDefinitionByDeploymentId(String deploymentId) {
        denyAuthorization();

        return AuthorizationContext.runWithoutAuthorization(() -> {
            var processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId)
                .singleResult();

            if (processDefinition == null) {
                throw new ProcessDefinitionNotFoundException("No process definition found for deployment ID: " + deploymentId);
            }

            return processDefinition;
        });
    }

    @Nullable
    public CamundaExecution findExecutionByProcessInstanceId(String processInstanceId) {
        denyAuthorization();
        return camundaExecutionRepository.findById(processInstanceId).orElse(null);
    }

    @Nullable
    public CamundaExecution findExecutionByBusinessKey(String businessKey) {
        denyAuthorization();
        return camundaExecutionRepository.findByBusinessKey(businessKey).orElse(null);
    }

    public void deleteProcessInstanceById(String processInstanceId, String reason) {
        denyAuthorization();
        runtimeService.deleteProcessInstance(processInstanceId, reason, true, true, true, false);
    }

    public void removeProcessVariables(String processInstanceId, Collection<String> variableNames) {
        denyAuthorization();
        runtimeService.removeVariables(processInstanceId, variableNames);
    }

    public ProcessInstanceWithDefinition startProcess(
        String processDefinitionKey,
        String businessKey,
        Map<String, Object> variables
    ) {
        return startProcess(processDefinitionKey, businessKey, null, variables);
    }

    public ProcessInstanceWithDefinition startProcess(
        String processDefinitionKey,
        String businessKey,
        CaseDefinitionId caseDefinitionId,
        Map<String, Object> variables
    ) {
        final CamundaProcessDefinition processDefinition = AuthorizationContext
            .runWithoutAuthorization(() -> {
                if (caseDefinitionId == null) {
                    return camundaRepositoryService.findLatestProcessDefinition(processDefinitionKey);
                } else {
                    // TODO: What to do if we're working on a global process definition? Currently taking latest
                    CamundaProcessDefinition procDef = camundaRepositoryService.findProcessDefinition(
                        byKey(processDefinitionKey).and(byLatestVersionTag("CD:" + caseDefinitionId))
                    );
                    if (procDef == null) {
                        procDef = camundaRepositoryService.findLatestProcessDefinition(processDefinitionKey);
                    }
                    return procDef;
                }
            });
        if (processDefinition == null) {
            throw new IllegalStateException("No process definition found with key: '" + processDefinitionKey + "'");
        }
        businessKey = businessKey.equals(UNDEFINED_BUSINESS_KEY) ? null : businessKey;

        authorizationService.requirePermission(
            new EntityAuthorizationRequest(
                CamundaExecution.class,
                CamundaExecutionActionProvider.CREATE,
                createDummyCamundaExecution(
                    processDefinition,
                    businessKey
                )
            )
        );

        ProcessInstance processInstance = formService.submitStartForm(
            processDefinition.getId(),
            businessKey,
            FormUtils.createTypedVariableMap(variables)
        );

        return new ProcessInstanceWithDefinition(processInstance, processDefinition);
    }

    public CamundaExecution createDummyCamundaExecution(
        @NotNull CamundaProcessDefinition processDefinition,
        String businessKey
    ) {
        CamundaExecution execution = new CamundaExecution(
            UUID.randomUUID().toString(),
            1,
            null,
            null,
            businessKey,
            null,
            processDefinition,
            null,
            null,
            null,
            null,
            null,
            true,
            false,
            false,
            false,
            SuspensionState.ACTIVE.getStateCode(),
            0,
            0,
            null,
            new HashSet<>()
        );
        execution.setProcessInstance(execution);

        return execution;
    }

    public CamundaProcessDefinition getProcessDefinition(String processDefinitionKey) {
        denyAuthorization();
        return AuthorizationContext
            .runWithoutAuthorization(() -> camundaRepositoryService.findLatestProcessDefinition(processDefinitionKey));
    }

    public Map<String, Object> getProcessInstanceVariables(String processInstanceId, List<String> variableNames) {
        denyAuthorization();
        return AuthorizationContext
            .runWithoutAuthorization(() -> camundaRuntimeService.getVariables(processInstanceId, variableNames));
    }

    public Map<String, Object> getProcessInstanceVariablesByJsonPointers(
        String processInstanceId,
        List<JsonPointer> variablePointers
    ) {
        denyAuthorization();
        return AuthorizationContext.runWithoutAuthorization(() ->
            camundaRuntimeService.getVariablesByJsonPointers(processInstanceId, variablePointers)
        );
    }

    public List<CamundaHistoricProcessInstance> getAllActiveContextProcessesStartedByCurrentUser(
        Set<String> processes, String userLogin
    ) {
        denyAuthorization();
        List<CamundaHistoricProcessInstance> historicProcessInstances = AuthorizationContext.runWithoutAuthorization(
            () -> historyService.findHistoricProcessInstances(
                byStartUserId(userLogin).and(byUnfinished())
            )
        );

        return historicProcessInstances
            .stream()
            .filter(p -> processes.contains(p.getProcessDefinitionKey()))
            .sorted(Comparator.comparing(CamundaHistoricProcessInstance::getStartTime).reversed())
            .collect(Collectors.toList());
    }

    public List<CamundaProcessDefinition> getDeployedDefinitions() {
        denyAuthorization();
        return AuthorizationContext.runWithoutAuthorization(() -> camundaRepositoryService.findProcessDefinitions(
            byActive().and(byLatestVersion()),
            Sort.by(NAME)
        ));
    }

    public List<CamundaProcessDefinition> getDeployedDefinitions(CaseDefinitionId caseDefinitionId) {
        denyAuthorization();
        String versionTag = CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId.toString();
        return AuthorizationContext.runWithoutAuthorization(() -> camundaRepositoryService.findProcessDefinitions(
            byActive()
                .and(byLatestVersionTag(versionTag)),
            Sort.by(NAME)
        ));
    }

    public List<CamundaProcessDefinition> getUnlinkedDeployedDefinitions() {
        denyAuthorization();
        return AuthorizationContext.runWithoutAuthorization(() ->
            camundaRepositoryService.findProcessDefinitions(
                    byActive().and(byNotLinkedToCaseDefinition()),
                    Sort.by(NAME)
                ).stream()
                .collect(Collectors.groupingBy(
                    CamundaProcessDefinition::getKey,
                    Collectors.maxBy(Comparator.comparing(CamundaProcessDefinition::getVersion))
                ))
                .values()
                .stream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList())
        );
    }

    public List<CamundaProcessDefinition> getUnlinkedDeployedDefinitionsByKey(String processDefinitionKey) {
        denyAuthorization();
        return AuthorizationContext.runWithoutAuthorization(() ->
            camundaRepositoryService.findProcessDefinitions(
                    byActive().and(byKey(processDefinitionKey)),
                    Sort.by(NAME)
                ).stream()
                .filter(def -> def.getVersionTag() == null || !def.getVersionTag().startsWith("CD:"))
                .collect(Collectors.toList())
        );
    }

    public List<CamundaProcessDefinition> getDefinitionsByKeyAndCaseDefinition(
        CaseDefinitionId caseDefinitionId,
        String processDefinitionKey
    ) {
        denyAuthorization();
        return AuthorizationContext.runWithoutAuthorization(() -> camundaRepositoryService.findProcessDefinitions(
            byVersionTag(CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId.toString())
                .and(byKey(processDefinitionKey))
        ));
    }

    public CamundaProcessDefinition getLatestDefinitionByKeyAndCaseDefinition(
        CaseDefinitionId caseDefinitionId,
        String processDefinitionKey
    ) {
        denyAuthorization();
        String versionTag = CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId.toString();
        return AuthorizationContext.runWithoutAuthorization(() -> camundaRepositoryService.findProcessDefinition(
            byVersionTag(versionTag)
                .and(byKey(processDefinitionKey))
                .and(byLatestVersionTag(versionTag))
        ));
    }

    public byte[] getBpmnModel(CamundaProcessDefinition camundaProcessDefinition) {
        return camundaByteArrayService.getByNameAndDeploymentId(
            camundaProcessDefinition.getResourceName(),
            camundaProcessDefinition.getDeploymentId()
        ).getBytes();
    }

    public List<CamundaProcessDefinition> getDefinitionsByKey(String processDefinitionKey) {
        denyAuthorization();
        return AuthorizationContext.runWithoutAuthorization(() ->
            camundaRepositoryService.findProcessDefinitions(
                byKey(processDefinitionKey)
            )
        );
    }

    @Transactional
    public void deleteAllProcesses(String processDefinitionKey, String reason) {
        denyAuthorization();

        logger.debug("delete all running process instances for processes with key: {}", processDefinitionKey);

        List<ProcessInstance> runningInstances = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey(processDefinitionKey)
            .list();

        AuthorizationContext.runWithoutAuthorization(() -> {
            runningInstances.forEach(i -> deleteProcessInstanceById(i.getProcessInstanceId(), reason));
            return null;
        });
    }

    @Transactional
    public void deleteProcessDefinition(String processDefinitionId) {
        denyAuthorization();

        // TODO: Discuss if cascade = true is the correct way to go about this
        AuthorizationContext.runWithoutAuthorization(() -> {
            repositoryService.deleteProcessDefinition(processDefinitionId, true);
            return null;
        });


    }

    @Transactional
    public DeploymentWithDefinitions deploy(
        CaseDefinitionId caseDefinitionId,
        String fileName,
        ByteArrayInputStream fileInput,
        boolean skipProcessLinksCopy,
        boolean skipIsDeployableCheck,
        @Nullable String originalVersionTag,
        @Nullable String originalProcessDefinitionId
    ) throws ProcessNotDeployableException, FileExtensionNotSupportedException, NoFileExtensionFoundException {
        denyAuthorization();

        if (fileName.endsWith(".bpmn")) {
            BpmnModelInstance bpmnModel = Bpmn.readModelFromStream(fileInput);

            if (!isDeployable(bpmnModel) && !skipIsDeployableCheck) {
                throw new ProcessNotDeployableException(fileName);
            }

            if (caseDefinitionId != null) {
                setProcessesVersionTag(bpmnModel, caseDefinitionId);
            }
            setProcessesExecutable(bpmnModel);
            setToNullWhenServiceTaskExpressionIsEmpty(bpmnModel);
            setToNullWhenSendTaskExpressionIsEmpty(bpmnModel);
            setToCorrelateAllWhenMessageSendEventExpressionIsEmpty(bpmnModel);
            setToPropagateBusinessKeyWhenCallActivityIsNew(bpmnModel);
            setTo60SecondsWhenTimerIsEmpty(bpmnModel);

            if (isProcessDefinitionPreviouslyDeployed(caseDefinitionId, bpmnModel)) {
                return null;
            }

            CamundaProcessDefinition latestProcessDefinition = getExistingProcessForFile(caseDefinitionId, bpmnModel);
            if (latestProcessDefinition != null) {
                // clean up previous process definition, can only be triggered when we're deploying a draft version
                applicationEventPublisher.publishEvent(new ProcessDefinitionDeleted(
                    latestProcessDefinition.getId(),
                    caseDefinitionId
                ));
                repositoryService.deleteDeployment(latestProcessDefinition.getDeploymentId(), true);
            }

            var deploymentBuilder = repositoryService.createDeployment()
                .addModelInstance(fileName, bpmnModel);

            CamundaDeploymentSource deploymentSource = new CamundaDeploymentSource(skipProcessLinksCopy, originalVersionTag, originalProcessDefinitionId);

            String deploymentSourceUuid = camundaDeploymentSourceHelper.store(deploymentSource);

            deploymentBuilder.source(deploymentSourceUuid);

            DeploymentWithDefinitions deployment = deploymentBuilder.deployWithResult();

            if (caseDefinitionId != null) {
                processDefinitionCaseDefinitionLinker.link(
                    caseDefinitionId,
                    deployment.getDeployedProcessDefinitions().get(0).getId()
                );
            }

            return deployment;
        } else if (fileName.endsWith(".dmn")) {
            DmnModelInstance dmnModel = Dmn.readModelFromStream(fileInput);

            if (caseDefinitionId != null) {
                setDecisionsVersionTag(dmnModel, caseDefinitionId);
            }

            String decisionDefinitionKey = dmnModel.getDefinitions()
                .getChildElementsByType(Decision.class)
                .stream()
                .map(Decision::getId)
                .findFirst()
                .orElseThrow();

            DecisionDefinitionQuery decisionDefinitionQuery = repositoryService.createDecisionDefinitionQuery()
                .decisionDefinitionKey(decisionDefinitionKey);

            if (caseDefinitionId != null) {
                decisionDefinitionQuery.versionTag(CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId);
            }

            DecisionDefinition decisionDefinition = decisionDefinitionQuery.singleResult();

            if (decisionDefinition != null) {
                repositoryService.deleteDeployment(decisionDefinition.getDeploymentId());
            }

            return repositoryService.createDeployment().addModelInstance(fileName, dmnModel).deployWithResult();
        } else {
            String[] splitFileName = fileName.split("\\.");

            if (splitFileName.length > 1) {
                String fileExtension = splitFileName[splitFileName.length - 1];
                throw new FileExtensionNotSupportedException(fileExtension);
            } else {
                throw new NoFileExtensionFoundException(fileName);
            }
        }
    }

    @Transactional
    public DeploymentWithDefinitions deploy(
        CaseDefinitionId caseDefinitionId,
        String fileName,
        ByteArrayInputStream fileInput
    ) throws ProcessNotDeployableException, FileExtensionNotSupportedException, NoFileExtensionFoundException {
        return deploy(
            caseDefinitionId,
            fileName,
            fileInput,
            false,
            false,
            null,
            null
        );
    }

    @Transactional
    public DeploymentWithDefinitions deploy(
        CaseDefinitionId caseDefinitionId,
        String fileName,
        ByteArrayInputStream fileInput,
        boolean skipProcessLinksCopy,
        boolean skipIsDeployableCheck
    ) throws ProcessNotDeployableException, FileExtensionNotSupportedException, NoFileExtensionFoundException {
        return deploy(caseDefinitionId, fileName, fileInput, skipProcessLinksCopy, skipIsDeployableCheck, null, null);
    }

    private boolean isProcessDefinitionPreviouslyDeployed(
        CaseDefinitionId caseDefinitionId,
        BpmnModelInstance bpmnModel
    ) throws ProcessNotDeployableException {
        CamundaProcessDefinition latestProcessDefinition = getExistingProcessForFile(caseDefinitionId, bpmnModel);

        if (latestProcessDefinition != null) {
            try {
                byte[] savedBytes = repositoryService.getResourceAsStream(
                        latestProcessDefinition.getDeploymentId(),
                        latestProcessDefinition.getResourceName()
                    )
                    .readAllBytes();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Bpmn.writeModelToStream(outputStream, bpmnModel);

                if (Arrays.equals(outputStream.toByteArray(), savedBytes)) {
                    outputStream.close();
                    return true;
                }

                outputStream.close();

            } catch (IOException e) {
                throw new ProcessNotDeployableException(caseDefinitionId + " and process: " + latestProcessDefinition.getKey());
            }
        }
        return false;
    }

    public CamundaProcessDefinition getExistingProcessForFile(
        CaseDefinitionId caseDefinitionId,
        BpmnModelInstance bpmnModel
    ) {
        String processDefinitionKey = bpmnModel.getModelElementsByType(Process.class).stream()
            .map(Process::getId)
            .findFirst().orElseThrow();

        List<CamundaProcessDefinition> processDefinition = camundaRepositoryService.findProcessDefinitions(
            byKey(processDefinitionKey)
                .and(byActive())
                .and(caseDefinitionId == null ? byNotLinkedToCaseDefinition() : byVersionTag(
                    CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId))
            ,
            Sort.by(Sort.Order.desc(VERSION))
        );

        if (processDefinition.size() > 1 && caseDefinitionId != null) {
            throw new IllegalStateException(
                "Only one process definition should be found for key: " + processDefinitionKey
                    + " and case definition id: " + caseDefinitionId
            );
        } else if (processDefinition.size() > 0) {
            return processDefinition.getFirst();
        } else {
            return null;
        }
    }

    private void setProcessesVersionTag(BpmnModelInstance bpmnModel, CaseDefinitionId caseDefinitionId) {
        bpmnModel.getDefinitions().getChildElementsByType(Process.class).forEach(
            process -> {
                process.setCamundaVersionTag(CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId.toString());
                process.getChildElementsByType(CallActivity.class).forEach(
                    callActivity -> {
                        var elementBinding = callActivity.getCamundaCalledElementBinding();
                        if (elementBinding == null) {
                            callActivity.setCamundaCalledElementBinding("versionTag");
                            callActivity.setCamundaCalledElementVersionTag(CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId);
                        }
                    }
                );

                process.getChildElementsByType(BusinessRuleTask.class).forEach(
                    businessRuleTask -> {
                        var elementBinding = businessRuleTask.getCamundaDecisionRefBinding();
                        if (elementBinding == null) {
                            businessRuleTask.setCamundaDecisionRefBinding("versionTag");
                            businessRuleTask.setCamundaDecisionRefVersionTag(CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId);
                        }
                    }
                );
            }
        );
    }

    private void setDecisionsVersionTag(DmnModelInstance dmnModel, CaseDefinitionId caseDefinitionId) {
        dmnModel.getDefinitions().getChildElementsByType(Decision.class).forEach(
            dmn -> dmn.setVersionTag(CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId.toString())
        );
    }

    @Transactional
    public DeploymentWithDefinitions duplicateProcessDefinitionById(
        CaseDefinitionId caseDefinitionId,
        String processDefinitionId,
        boolean skipProcessLinksCopy,
        boolean skipIsDeployableCheck
    )
        throws ProcessNotDeployableException, FileExtensionNotSupportedException, NoFileExtensionFoundException {
        denyAuthorization();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionId(processDefinitionId)
            .singleResult();

        if (processDefinition == null) {
            throw new ProcessDefinitionNotFoundException("No process definition found for ID: " + processDefinitionId);
        }

        String deploymentId = processDefinition.getDeploymentId();

        if (deploymentId == null) {
            throw new ProcessDefinitionNotFoundException(
                "No deployment ID found for process definition ID: " + processDefinitionId
            );
        }

        List<String> resourceNames = repositoryService.getDeploymentResourceNames(deploymentId);

        if (resourceNames.isEmpty()) {
            throw new ProcessNotDeployableException("No resources found for deployment ID: " + deploymentId);
        }

        // TODO: for old deployments where the whole resource folder was deployed, this might not be correct.
        String fileName = resourceNames.get(0);

        try (ByteArrayInputStream fileInput = new ByteArrayInputStream(
            repositoryService.getResourceAsStream(deploymentId, fileName).readAllBytes())) {
            return deploy(caseDefinitionId, fileName, fileInput, skipProcessLinksCopy, skipIsDeployableCheck);

        } catch (IOException e) {
            logger.error("Error reading resource stream for file: {}", fileName, e);
            throw new ProcessNotDeployableException("Error reading resource stream for file: " + fileName);
        }

    }

    private void setProcessesExecutable(BpmnModelInstance bpmnModel) {
        bpmnModel.getDefinitions().getChildElementsByType(Process.class).forEach(
            process -> process.setExecutable(true)
        );
    }

    private void setToNullWhenServiceTaskExpressionIsEmpty(BpmnModelInstance bpmnModel) {
        bpmnModel.getModelElementsByType(ServiceTask.class).forEach(task -> {
            if (task.getCamundaType() == null
                && task.getCamundaClass() == null
                && task.getCamundaExpression() == null
                && task.getCamundaDelegateExpression() == null) {
                task.setCamundaExpression("${null}");
                task.setCamundaAsyncAfter(true);
            }
        });
    }

    private void setToNullWhenSendTaskExpressionIsEmpty(BpmnModelInstance bpmnModel) {
        bpmnModel.getModelElementsByType(SendTask.class).forEach(task -> {
            if (task.getCamundaType() == null
                && task.getCamundaClass() == null
                && task.getCamundaExpression() == null
                && task.getCamundaDelegateExpression() == null) {
                task.setCamundaExpression("${null}");
                task.setCamundaAsyncAfter(true);
            }
        });
    }

    private void setToCorrelateAllWhenMessageSendEventExpressionIsEmpty(BpmnModelInstance bpmnModel) {
        Stream.of(IntermediateThrowEvent.class, EndEvent.class)
            .flatMap(sendEventClass -> bpmnModel.getModelElementsByType(sendEventClass).stream())
            .filter(sendEvent -> sendEvent.getId().matches("Event_[a-z0-9]{6,8}"))
            .flatMap(sendEvent -> sendEvent.getChildElementsByType(MessageEventDefinition.class).stream())
            .forEach(event -> {
                if (event.getCamundaType() == null
                    && event.getCamundaClass() == null
                    && event.getCamundaExpression() == null
                    && event.getCamundaDelegateExpression() == null) {
                    String messageName = event.getMessage() == null ? "MY_MESSAGE" : event.getMessage().getName();
                    event.setCamundaExpression(
                        "${correlationService.sendMessageToAll(\"" + messageName + "\", execution)}"
                    );
                }
            });
    }

    private void setToPropagateBusinessKeyWhenCallActivityIsNew(BpmnModelInstance bpmnModel) {
        bpmnModel.getModelElementsByType(CallActivity.class).forEach(callActivity -> {
            if (callActivity.getId().matches("Activity_[a-z0-9]{6,8}")
                && callActivity.getCalledElement() != null
                && callActivity.getChildElementsByType(ExtensionElements.class).isEmpty()) {
                ExtensionElements extensionElement = bpmnModel.newInstance(ExtensionElements.class);
                callActivity.addChildElement(extensionElement);
                CamundaIn businessKeyIn = bpmnModel.newInstance(CamundaIn.class);
                businessKeyIn.setCamundaBusinessKey("#{execution.processBusinessKey}");
                extensionElement.addChildElement(businessKeyIn);
                callActivity.setCamundaAsyncAfter(true);
            }
        });
    }

    private void setTo60SecondsWhenTimerIsEmpty(BpmnModelInstance bpmnModel) {
        bpmnModel.getModelElementsByType(TimerEventDefinition.class).forEach(timerEvent -> {
            if (timerEvent.getTimeDate() == null
                && timerEvent.getTimeDuration() == null
                && timerEvent.getTimeCycle() == null) {
                TimeDuration timeDuration = bpmnModel.newInstance(TimeDuration.class);
                timeDuration.setTextContent("PT60S");
                timerEvent.addChildElement(timeDuration);
            }
        });
    }

    private boolean isDeployable(BpmnModelInstance model) {
        AtomicBoolean isDeployable = new AtomicBoolean(true);
        if (valtimoProperties.getProcess().isSystemProcessUpdatable()) {
            return isDeployable.get();
        }
        model.getDefinitions().getChildElementsByType(Process.class).forEach(
            process -> {
                String processDefinitionKey = process.getId();
                if (processDefinitionKey == null || processDefinitionKey.isEmpty() || isSystemProcess(
                    AuthorizationContext
                        .runWithoutAuthorization(
                            () -> camundaRepositoryService.findLatestProcessDefinition(processDefinitionKey)))
                ) {
                    isDeployable.set(false);
                }
            });
        return isDeployable.get();
    }

    private boolean isSystemProcess(CamundaProcessDefinition processDefinition) {
        if (processDefinition == null) {
            return false;
        }
        var processProperties = processPropertyService.findByProcessDefinitionKey(processDefinition.getKey());
        if (processProperties != null) {
            return processProperties.isSystemProcess();
        }
        return false;
    }

    private void denyAuthorization() {
        authorizationService.requirePermission(
            new EntityAuthorizationRequest(
                CamundaProcessDefinition.class,
                Action.deny()
            )
        );
    }
}
