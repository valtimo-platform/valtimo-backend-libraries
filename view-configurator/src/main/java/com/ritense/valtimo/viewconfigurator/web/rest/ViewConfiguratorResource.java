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

package com.ritense.valtimo.viewconfigurator.web.rest;

import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.viewconfigurator.domain.ProcessDefinitionVariable;
import com.ritense.valtimo.viewconfigurator.domain.ViewConfig;
import com.ritense.valtimo.viewconfigurator.domain.ViewConfigurationRequestGroup;
import com.ritense.valtimo.viewconfigurator.service.ViewConfigService;
import com.ritense.valtimo.viewconfigurator.web.rest.view.Views;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ViewConfiguratorResource {

    private final ViewConfigService viewConfigService;

    public ViewConfiguratorResource(ViewConfigService viewConfigService) {
        this.viewConfigService = viewConfigService;
    }

    @JsonView(Views.ViewConfig.class)
    @GetMapping(value = "/viewconfig/{processDefinitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ViewConfig> configure(@PathVariable String processDefinitionId) throws Exception {
        return viewConfigService.createViewConfiguration(processDefinitionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @JsonView(Views.ViewConfig.class)
    @PostMapping(value = "/viewconfig/{viewConfigId}/view/{viewId}/variables", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ViewConfig> addVariablesToView(
        @PathVariable Long viewConfigId,
        @PathVariable Long viewId,
        @RequestBody LinkedHashSet<Long> listOfIdsInSequence
    ) {
        return viewConfigService.assignVariablesToView(viewConfigId, viewId, listOfIdsInSequence)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @JsonView(Views.ViewConfig.class)
    @PostMapping(value = "/viewconfig/{viewConfigId}/view/{viewId}/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ViewConfig> configureViewWithGroups(
        @PathVariable Long viewConfigId,
        @PathVariable Long viewId,
        @RequestBody List<ViewConfigurationRequestGroup> viewConfigurationRequestGroups
    ) {
        return viewConfigService.assignGroupsToView(viewConfigId, viewId, viewConfigurationRequestGroups)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @JsonView(Views.ViewConfig.class)
    @PostMapping(value = "/viewconfig/{viewConfigId}/variables", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ViewConfig> changeLabels(
        @PathVariable Long viewConfigId,
        @RequestBody Set<ProcessDefinitionVariable> variables
    ) {
        return viewConfigService.changeLabels(viewConfigId, variables)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

}