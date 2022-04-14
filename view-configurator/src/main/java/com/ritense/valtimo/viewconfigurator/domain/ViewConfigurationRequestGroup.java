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

package com.ritense.valtimo.viewconfigurator.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ViewConfigurationRequestGroup {

    private Long id;

    private String label;

    private int sequence;

    @JsonProperty("selectedProcessDefinitionVariables")
    private List<ViewConfigurationRequestVariable> viewConfigurationRequestVariables;

    public ViewConfigurationRequestGroup() {
    }

    public ViewConfigurationRequestGroup(Long id, String label, int sequence, List<ViewConfigurationRequestVariable> viewConfigurationRequestVariables) {
        this.id = id;
        this.label = label;
        this.sequence = sequence;
        this.viewConfigurationRequestVariables = viewConfigurationRequestVariables;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public int getSequence() {
        return sequence;
    }

    public List<ViewConfigurationRequestVariable> getViewConfigurationRequestVariables() {
        return viewConfigurationRequestVariables;
    }
}
