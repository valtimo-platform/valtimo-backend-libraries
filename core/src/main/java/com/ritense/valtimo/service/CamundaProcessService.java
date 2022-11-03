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

package com.ritense.valtimo.service;

import com.ritense.valtimo.camunda.domain.ProcessInstanceWithDefinition;
import com.ritense.valtimo.service.util.FormUtils;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CamundaProcessService {

    private static final String UNDEFINED_BUSINESS_KEY = "UNDEFINED_BUSINESS_KEY";
    private static final Logger logger = LoggerFactory.getLogger(CamundaProcessService.class);

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final FormService formService;
    private final HistoryService historyService;

    public CamundaProcessService(RuntimeService runtimeService, RepositoryService repositoryService, FormService formService, HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.repositoryService = repositoryService;
        this.formService = formService;
        this.historyService = historyService;
    }

    public ProcessDefinition findProcessDefinitionById(String processDefinitionId) {
        return repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionId(processDefinitionId)
            .singleResult();
    }

    public boolean processDefinitionExistsByKey(String processDefinitionKey) {
        return repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .count() >= 1;
    }

    public Optional<ProcessInstance> findProcessInstanceById(String processInstanceId) {
        return Optional.ofNullable(runtimeService
            .createProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult());
    }

    public void deleteProcessInstanceById(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
    }

    public void removeProcessVariables(String processInstanceId, Collection<String> variableNames) {
        runtimeService.removeVariables(processInstanceId, variableNames);
    }

    public ProcessInstanceWithDefinition startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        final ProcessDefinition processDefinition = getProcessDefinition(processDefinitionKey);
        if (processDefinition == null) {
            throw new IllegalStateException("No process definition found with key: '" + processDefinitionKey + "'");
        }
        businessKey = businessKey.equals(UNDEFINED_BUSINESS_KEY) ? null : businessKey;
        ProcessInstance processInstance = formService.submitStartForm(
            processDefinition.getId(),
            businessKey,
            FormUtils.createTypedVariableMap(variables)
        );
        return new ProcessInstanceWithDefinition(processInstance, processDefinition);
    }

    public ProcessDefinition getProcessDefinition(String processDefinitionKey) {
        return repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .latestVersion()
            .singleResult();
    }

    public Map<String, Object> getProcessInstanceVariables(String processInstanceId, List<String> variableNames) {
        List<HistoricVariableInstance> historicVariableInstances = historyService
            .createHistoricVariableInstanceQuery()
            .processInstanceId(processInstanceId)
            .orderByVariableName()
            .desc()
            .list();

        return historicVariableInstances
            .stream()
            .filter(historicVariableInstance -> historicVariableInstance.getValue() != null && variableNames.contains(historicVariableInstance.getName()))
            .collect(Collectors.toMap(HistoricVariableInstance::getName, HistoricVariableInstance::getValue));
    }

    public List<HistoricProcessInstance> getAllActiveContextProcessesStartedByCurrentUser(Set<String> processes, String userLogin) {
        List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
            .startedBy(userLogin)
            .unfinished()
            .list();

        return historicProcessInstances
            .stream()
            .filter(p -> processes.contains(p.getProcessDefinitionKey()))
            .sorted(Comparator.comparing(HistoricProcessInstance::getStartTime).reversed())
            .collect(Collectors.toList());
    }

    public List<ProcessDefinition> getDeployedDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
            .orderByProcessDefinitionName().asc()
            .active()
            .latestVersion()
            .list();
    }

    @Transactional
    public void deleteAllProcesses(String processDefinitionKey, String reason) {
        logger.debug("delete all running process instances for processes with key: {}", processDefinitionKey);

        List<ProcessInstance> runningInstances = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey(processDefinitionKey)
            .list();

        runningInstances.forEach(i -> deleteProcessInstanceById(i.getProcessInstanceId(), reason));
    }

}
