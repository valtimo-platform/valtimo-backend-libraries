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

package com.ritense.valtimo.web.rest.dto;

import java.io.Serializable;
import java.util.Map;

public class FlowNodeMigrationDTO implements Serializable {

    private Map<String, String> sourceFlowNodeMap;
    private Map<String, String> targetFlowNodeMap;
    private Map<String, String> uniqueFlowNodeMap;

    public FlowNodeMigrationDTO() {
    }

    public FlowNodeMigrationDTO(Map<String, String> sourceFlowNodeMap, Map<String, String> targetFlowNodeMap, Map<String, String> uniqueFlowNodeMap) {
        this.sourceFlowNodeMap = sourceFlowNodeMap;
        this.targetFlowNodeMap = targetFlowNodeMap;
        this.uniqueFlowNodeMap = uniqueFlowNodeMap;
    }

    public Map<String, String> getSourceFlowNodeMap() {
        return sourceFlowNodeMap;
    }

    public void setSourceFlowNodeMap(Map<String, String> sourceFlowNodeMap) {
        this.sourceFlowNodeMap = sourceFlowNodeMap;
    }

    public Map<String, String> getTargetFlowNodeMap() {
        return targetFlowNodeMap;
    }

    public void setTargetFlowNodeMap(Map<String, String> targetFlowNodeMap) {
        this.targetFlowNodeMap = targetFlowNodeMap;
    }

    public Map<String, String> getUniqueFlowNodeMap() {
        return uniqueFlowNodeMap;
    }

    public void setUniqueFlowNodeMap(Map<String, String> uniqueFlowNodeMap) {
        this.uniqueFlowNodeMap = uniqueFlowNodeMap;
    }
}