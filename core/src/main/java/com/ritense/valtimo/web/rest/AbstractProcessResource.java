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

package com.ritense.valtimo.web.rest;

import com.ritense.valtimo.camunda.domain.CamundaHistoricProcessInstance;
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.service.CamundaHistoryService;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.service.CamundaTaskService;
import com.ritense.valtimo.web.rest.dto.HeatmapTaskCountDTO;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Task;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ritense.valtimo.camunda.repository.CamundaHistoricProcessInstanceSpecificationHelper.byProcessInstanceId;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byKey;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byVersion;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byCreateTimeAfter;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byCreateTimeBefore;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byName;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byProcessDefinitionId;

public abstract class AbstractProcessResource {

    private final CamundaHistoryService historyService;
    private final RepositoryService repositoryService;
    private final CamundaRepositoryService camundaRepositoryService;
    private final CamundaTaskService taskService;

    public AbstractProcessResource(CamundaHistoryService historyService, RepositoryService repositoryService, CamundaRepositoryService camundaRepositoryService, CamundaTaskService taskService) {
        this.historyService = historyService;
        this.repositoryService = repositoryService;
        this.camundaRepositoryService = camundaRepositoryService;
        this.taskService = taskService;
    }

    public CamundaProcessDefinition getProcessDefinition(String processDefinitionKey, Integer version) {
        return camundaRepositoryService.findProcessDefinition(byKey(processDefinitionKey).and(byVersion(version)));
    }

    public ProcessDefinitionDiagramDto createProcessDefinitionDiagramDto(String processDefinitionId) throws UnsupportedEncodingException {
        InputStream processModelIn = repositoryService.getProcessModel(processDefinitionId);
        if (processModelIn != null) {
            byte[] processModel = IoUtil.readInputStream(processModelIn, "processModelBpmn20Xml");
            return ProcessDefinitionDiagramDto.create(processDefinitionId, new String(processModel, StandardCharsets.UTF_8));
        } else {
            return null;
        }
    }

    public Map<String, String> findAllTasksEventsAndGatewaysForProcessDefinitionId(String processDefinitionId) {
        Map<String, String> flowNodeMap = new HashMap<>();
        final Collection<FlowNode> flowNodeCollection = repositoryService.getBpmnModelInstance(processDefinitionId)
            .getModelElementsByType(FlowNode.class);
        flowNodeCollection.forEach(flowNode -> {
            if (
                !flowNode.getElementType().getTypeName().equals("endEvent")
                    && !flowNode.getElementType().getTypeName().equals("startEvent")
            ) {
                if (flowNode.getName() == null) {
                    flowNodeMap.put(flowNode.getId(), flowNode.getId());
                } else {
                    flowNodeMap.put(flowNode.getId(), flowNode.getName());
                }

            }
        });
        return flowNodeMap;
    }

    public Map<String, String> getUniqueFlowNodeMap(Map<String, String> sourceFlowNodeMap, Map<String, String> targetFlowNodeMap) {
        Map<String, String> uniqueFlowNodeMap = new HashMap<>();
        for (Map.Entry<String, String> flowNode : sourceFlowNodeMap.entrySet()) {
            if (!targetFlowNodeMap.containsKey(flowNode.getKey())) {
                uniqueFlowNodeMap.put(flowNode.getKey(), flowNode.getValue());
            }
        }
        return uniqueFlowNodeMap;
    }

    public List<CamundaTask> getAllActiveTasks(
        CamundaProcessDefinition processDefinition,
        String searchStatus,
        Date fromDate,
        Date toDate,
        Integer duration
    ) {
        // Get, group and count all task instances
        var taskQuery = byProcessDefinitionId(processDefinition.getId());

        if (StringUtils.isNotBlank(searchStatus)) {
            taskQuery.and(byName(searchStatus));
        }

        if (fromDate != null) {
            taskQuery.and(byCreateTimeAfter(fromDate));
        }

        if (toDate != null) {
            taskQuery.and(byCreateTimeBefore(toDate));
        }

        if (duration != null) {
            LocalDate dayinPast = LocalDate.now().minusDays(duration);
            taskQuery.and(byCreateTimeBefore(Date.from(dayinPast.atStartOfDay(ZoneId.systemDefault()).toInstant())));
        }
        return taskService.findTasks(taskQuery);
    }

    public Map<String, HeatmapTaskCountDTO> getActiveTasksCounts(
        CamundaProcessDefinition processDefinition,
        String searchStatus,
        Date fromDate,
        Date toDate,
        Integer duration
    ) {
        // Get all available tasks for this process definition
        final Collection<Task> tasksElements = repositoryService
            .getBpmnModelInstance(processDefinition.getId())
            .getModelElementsByType(Task.class);

        // Get, group and count all task instances
        List<CamundaTask> taskList = getAllActiveTasks(
            processDefinition,
            searchStatus,
            fromDate,
            toDate,
            duration
        );
        Map<String, Long> groupedList = taskList.stream()
            .collect(Collectors.groupingBy(CamundaTask::getTaskDefinitionKey, Collectors.counting()));

        // Map all tasks/counts for json output
        Map<String, HeatmapTaskCountDTO> returnList = new HashMap<>();
        tasksElements.forEach(task -> {
            HeatmapTaskCountDTO heatmapTaskCountDTO = new HeatmapTaskCountDTO();
            heatmapTaskCountDTO.setName(task.getName());
            if (groupedList.containsKey(task.getId())) {
                Long count = groupedList.get(task.getId());
                if (count == null) {
                    count = 0L;
                }
                heatmapTaskCountDTO.setCount(count);
            } else {
                heatmapTaskCountDTO.setCount(0L);
            }
            returnList.put(task.getId(), heatmapTaskCountDTO);
        });

        // Sort the output list by name
        return returnList.entrySet().stream().sorted(
            Map.Entry.comparingByValue((o1, o2) -> {
                try {
                    return o1.getName().compareTo(o2.getName());
                } catch (Exception ex) {
                    return -1;
                }

            })).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public CamundaHistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        return historyService.findHistoricProcessInstance(byProcessInstanceId(processInstanceId));
    }

    public static class ResultCount {
        private Long count;

        public Long getCount() {
            return count;
        }

        public ResultCount(Long count) {
            this.count = count;
        }
    }

}
