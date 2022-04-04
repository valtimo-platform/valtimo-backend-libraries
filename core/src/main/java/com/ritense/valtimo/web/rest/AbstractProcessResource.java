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

package com.ritense.valtimo.web.rest;

import com.ritense.valtimo.web.rest.dto.HeatmapTaskCountDTO;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;
import org.camunda.bpm.engine.task.TaskQuery;
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

public abstract class AbstractProcessResource {

    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final TaskService taskService;

    public AbstractProcessResource(HistoryService historyService, RepositoryService repositoryService, TaskService taskService) {
        this.historyService = historyService;
        this.repositoryService = repositoryService;
        this.taskService = taskService;
    }

    public ProcessDefinition getProcessDefinition(String processDefinitionKey, Integer version) {
        return repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .processDefinitionVersion(version)
            .singleResult();
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

    public List<org.camunda.bpm.engine.task.Task> getAllActiveTasks(
        ProcessDefinition processDefinition,
        String searchStatus,
        Date fromDate,
        Date toDate,
        Integer duration
    ) {
        // Get, group and count all task instances
        TaskQuery taskQuery = taskService.createTaskQuery().processDefinitionId(processDefinition.getId());

        if (StringUtils.isNotBlank(searchStatus)) {
            taskQuery.taskName(searchStatus);
        }

        if (fromDate != null) {
            taskQuery.taskCreatedAfter(fromDate);
        }

        if (toDate != null) {
            taskQuery.taskCreatedBefore(toDate);
        }

        if (duration != null) {
            LocalDate dayinPast = LocalDate.now().minusDays(duration);
            taskQuery.taskCreatedBefore(Date.from(dayinPast.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        return taskQuery.list();
    }

    public Map<String, HeatmapTaskCountDTO> getActiveTasksCounts(
        ProcessDefinition processDefinition,
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
        List<org.camunda.bpm.engine.task.Task> taskList = getAllActiveTasks(
            processDefinition,
            searchStatus,
            fromDate,
            toDate,
            duration
        );
        Map<String, Long> groupedList = taskList.stream()
            .collect(Collectors.groupingBy(org.camunda.bpm.engine.task.Task::getTaskDefinitionKey, Collectors.counting()));

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

    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        return historyService
            .createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();
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
