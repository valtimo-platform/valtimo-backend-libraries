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
import com.ritense.valtimo.viewconfigurator.domain.ViewConfig;
import com.ritense.valtimo.viewconfigurator.domain.ViewConfigurationRequestGroup;
import com.ritense.valtimo.viewconfigurator.repository.ViewConfigRepository;
import com.ritense.valtimo.viewconfigurator.service.ProcessDefinitionService;
import com.ritense.valtimo.viewconfigurator.service.ProcessDefinitionVariableService;
import com.ritense.valtimo.viewconfigurator.service.ViewConfigService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Transactional
public class ViewConfigServiceImpl implements ViewConfigService {

    private final ProcessDefinitionVariableService processDefinitionVariableService;
    private final ViewConfigRepository viewConfigRepository;
    private final ProcessDefinitionService processDefinitionService;

    public ViewConfigServiceImpl(
        final ProcessDefinitionVariableService processDefinitionVariableService,
        final ViewConfigRepository viewConfigRepository,
        final ProcessDefinitionService processDefinitionService
    ) {
        this.processDefinitionVariableService = processDefinitionVariableService;
        this.viewConfigRepository = viewConfigRepository;
        this.processDefinitionService = processDefinitionService;
    }

    @Override
    public Optional<ViewConfig> createViewConfiguration(String processDefinitionId) throws ProcessNotFoundException {
        Optional<ViewConfig> viewConfigOptional = viewConfigRepository.findByProcessDefinitionId(processDefinitionId);

        if (viewConfigOptional.isPresent()) {
            return viewConfigOptional;
        } else {
            final Set<ProcessDefinitionVariable> allVariables;
            allVariables = processDefinitionVariableService.extractVariables(processDefinitionId);
            ViewConfig viewConfig = ViewConfig.initialise(processDefinitionId, allVariables);
            getPreviousViewConfig(processDefinitionId).ifPresent(viewConfig::configureFrom);
            viewConfigRepository.saveAndFlush(viewConfig);
            return Optional.of(viewConfig);
        }
    }

    @Override
    public Optional<ViewConfig> assignVariablesToView(Long viewConfigId, Long viewId, LinkedHashSet<Long> variables) {
        Optional<ViewConfig> viewConfigOptional = viewConfigRepository.findById(viewConfigId);
        viewConfigOptional.ifPresent(viewConfig -> {
            viewConfig.assignVariablesToView(viewId, variables);
            viewConfigRepository.saveAndFlush(viewConfig);
        });
        return viewConfigOptional;
    }

    public Optional<ViewConfig> assignGroupsToView(Long viewConfigId, Long viewId, List<ViewConfigurationRequestGroup> viewConfigurationRequestGroups) {
        Optional<ViewConfig> viewConfigOptional = viewConfigRepository.findById(viewConfigId);
        viewConfigOptional.ifPresent(viewConfig -> {
            viewConfig.assignGroupsToView(viewId, viewConfigurationRequestGroups);
            viewConfigRepository.saveAndFlush(viewConfig);
        });
        return viewConfigOptional;
    }

    public Optional<ViewConfig> changeLabels(Long viewConfigId, Set<ProcessDefinitionVariable> variables) {
        Optional<ViewConfig> viewConfigOptional = viewConfigRepository.findById(viewConfigId);
        viewConfigOptional.ifPresent(viewConfig -> {
            viewConfig.changeLabels(variables);
            viewConfigRepository.saveAndFlush(viewConfig);
        });
        return viewConfigOptional;
    }

    public Optional<ViewConfig> assignAdditionalProcessVariables(String processDefinitionId, Set<ProcessDefinitionVariable> variables) {
        Optional<ViewConfig> viewConfigOptional = viewConfigRepository.findByProcessDefinitionId(processDefinitionId);
        viewConfigOptional.ifPresent(viewConfig -> {
            viewConfig.assignAdditionalProcessVariables(variables);
            viewConfigRepository.saveAndFlush(viewConfig);
        });
        return viewConfigOptional;
    }

    private Optional<ViewConfig> getPreviousViewConfig(String processDefinitionId) {
        final List<ProcessDefinition> previousVersions = processDefinitionService.getPreviousVersions(processDefinitionId);
        return previousVersions.stream()
            .map(processDefinition -> {
                return viewConfigRepository.findByProcessDefinitionId(processDefinition.getId()).orElse(null);
            })
            .filter(Objects::nonNull)
            .findFirst();
    }

}