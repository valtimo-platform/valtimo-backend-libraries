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

package com.ritense.valtimo.milestones.web.rest;

import com.ritense.valtimo.milestones.service.MilestoneInstanceService;
import com.ritense.valtimo.milestones.web.rest.dto.FlowNodeDTO;
import com.ritense.valtimo.milestones.web.rest.dto.MilestoneInstanceDTO;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MilestoneInstanceResource {

    private static final Logger logger = LoggerFactory.getLogger(MilestoneInstanceResource.class);
    private final RepositoryService repositoryService;
    private final MilestoneInstanceService milestoneInstanceService;

    public MilestoneInstanceResource(RepositoryService repositoryService, MilestoneInstanceService milestoneInstanceService) {
        this.repositoryService = repositoryService;
        this.milestoneInstanceService = milestoneInstanceService;
    }

    @GetMapping(value = "/v1/milestone-instances")
    public ResponseEntity<List<MilestoneInstanceDTO>> getMilestoneInstances() {
        logger.debug("REST request to get all milestone instances");
        return ResponseEntity.ok(milestoneInstanceService.getAllMilestoneInstances());
    }

    @GetMapping(value = "/v1/milestones/{processDefinitionId}/flownodes", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<FlowNodeDTO> getDiagramFlowNodes(@PathVariable String processDefinitionId) {
        final Collection<FlowNode> flowNodeCollection =
            repositoryService
                .getBpmnModelInstance(processDefinitionId)
                .getModelElementsByType(FlowNode.class);

        Map<String, String> flowNodeMap = flowNodeCollection.stream()
            .collect(Collectors.toMap(FlowNode::getId, this::getFlowNodeValue));

        FlowNodeDTO flowNodeDTO = new FlowNodeDTO(flowNodeMap);
        return ResponseEntity.ok(flowNodeDTO);
    }

    private String getFlowNodeValue(FlowNode flowNode) {
        return (flowNode.getName() != null)
            ? flowNode.getName()
            : flowNode.getId();
    }

}