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

package com.ritense.valtimo.viewconfigurator.service;

import com.ritense.valtimo.contract.exception.ProcessNotFoundException;
import com.ritense.valtimo.viewconfigurator.domain.ProcessDefinitionVariable;
import com.ritense.valtimo.viewconfigurator.domain.ViewConfig;
import com.ritense.valtimo.viewconfigurator.domain.ViewConfigurationRequestGroup;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ViewConfigService {
    Optional<ViewConfig> createViewConfiguration(String processDefinitionId) throws ProcessNotFoundException;

    Optional<ViewConfig> assignVariablesToView(Long viewConfigId, Long viewId, LinkedHashSet<Long> variables);

    Optional<ViewConfig> assignGroupsToView(Long viewConfigId, Long viewId, List<ViewConfigurationRequestGroup> viewConfigurationRequestGroups);

    Optional<ViewConfig> changeLabels(Long viewConfigId, Set<ProcessDefinitionVariable> variables);

    Optional<ViewConfig> assignAdditionalProcessVariables(String processDefinitionId, Set<ProcessDefinitionVariable> variables);
}