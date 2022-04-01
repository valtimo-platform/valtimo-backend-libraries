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

package com.ritense.valtimo.viewconfigurator.service.impl;

import com.ritense.valtimo.contract.exception.ProcessNotFoundException;
import com.ritense.valtimo.viewconfigurator.domain.ProcessDefinitionVariable;
import com.ritense.valtimo.viewconfigurator.domain.transformer.CamundaFormFieldTransformer;
import com.ritense.valtimo.viewconfigurator.service.ProcessDefinitionVariableService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormData;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProcessDefinitionVariableServiceImpl implements ProcessDefinitionVariableService {

    private RepositoryService repositoryService;

    public ProcessDefinitionVariableServiceImpl(final RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @Override
    public Set<ProcessDefinitionVariable> extractVariables(String processDefinitionId) throws ProcessNotFoundException {
        BpmnModelInstance bpmnModelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);
        if (bpmnModelInstance == null) {
            throw new ProcessNotFoundException("Process not found for: " + processDefinitionId);
        }
        return getProcessVariables(bpmnModelInstance);
    }

    private Set<ProcessDefinitionVariable> getProcessVariables(BpmnModelInstance bpmnModelInstance) {
        return bpmnModelInstance
            .getModelElementsByType(CamundaFormData.class)
            .stream()
            .flatMap(camundaFormData -> camundaFormData.getCamundaFormFields().stream())
            .filter(distinctByKey(CamundaFormField::getCamundaId))
            .map(CamundaFormFieldTransformer.transform)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> function) {
        Set<Object> map = ConcurrentHashMap.newKeySet();
        return t -> map.add(function.apply(t));
    }

}