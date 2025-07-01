/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static com.ritense.authorization.AuthorizationContext.runWithoutAuthorization;
import static com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.VERSION;
import static com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.byKey;
import static com.ritense.valtimo.operaton.repository.OperatonProcessDefinitionSpecificationHelper.byLatestVersion;
import static com.ritense.valtimo.operaton.repository.OperatonTaskSpecificationHelper.byActive;
import static com.ritense.valtimo.operaton.repository.OperatonTaskSpecificationHelper.byProcessInstanceId;
import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;
import static java.time.ZoneId.systemDefault;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.ritense.logging.LoggableResource;
import com.ritense.valtimo.operaton.domain.OperatonExecution;
import com.ritense.valtimo.operaton.domain.OperatonHistoricProcessInstance;
import com.ritense.valtimo.operaton.domain.OperatonProcessDefinition;
import com.ritense.valtimo.operaton.domain.OperatonTask;
import com.ritense.valtimo.operaton.dto.OperatonHistoricProcessInstanceDto;
import com.ritense.valtimo.operaton.dto.OperatonProcessDefinitionDto;
import com.ritense.valtimo.operaton.dto.OperatonTaskDto;
import com.ritense.valtimo.operaton.service.OperatonHistoryService;
import com.ritense.valtimo.operaton.service.OperatonRepositoryService;
import com.ritense.valtimo.contract.annotation.SkipComponentScan;
import com.ritense.valtimo.contract.exception.DocumentParserException;
import com.ritense.valtimo.contract.exception.ProcessNotFoundException;
import com.ritense.valtimo.exception.BpmnParseException;
import com.ritense.valtimo.repository.OperatonSearchProcessInstanceRepository;
import com.ritense.valtimo.repository.operaton.dto.ProcessInstance;
import com.ritense.valtimo.repository.operaton.dto.TaskInstanceWithIdentityLink;
import com.ritense.valtimo.service.OperatonProcessService;
import com.ritense.valtimo.service.OperatonTaskService;
import com.ritense.valtimo.service.ProcessPropertyService;
import com.ritense.valtimo.service.ProcessShortTimerService;
import com.ritense.valtimo.web.rest.dto.CommentDto;
import com.ritense.valtimo.web.rest.dto.DefinitionDeploymentResponseDto;
import com.ritense.valtimo.web.rest.dto.FlowNodeMigrationDTO;
import com.ritense.valtimo.web.rest.dto.HeatmapTaskAverageDurationDTO;
import com.ritense.valtimo.web.rest.dto.HeatmapTaskCountDTO;
import com.ritense.valtimo.web.rest.dto.ProcessDefinitionDiagramWithPropertyDto;
import com.ritense.valtimo.web.rest.dto.ProcessDefinitionWithPropertiesDto;
import com.ritense.valtimo.web.rest.dto.ProcessInstanceDiagramDto;
import com.ritense.valtimo.web.rest.dto.ProcessInstanceSearchDTO;
import com.ritense.valtimo.web.rest.util.PaginationUtil;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.operaton.bpm.engine.HistoryService;
import org.operaton.bpm.engine.ParseException;
import org.operaton.bpm.engine.ProcessEngines;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.batch.Batch;
import org.operaton.bpm.engine.history.HistoricActivityInstance;
import org.operaton.bpm.engine.history.HistoricActivityInstanceQuery;
import org.operaton.bpm.engine.history.UserOperationLogEntry;
import org.operaton.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.operaton.bpm.engine.migration.MigrationPlan;
import org.operaton.bpm.engine.migration.MigrationPlanBuilder;
import org.operaton.bpm.engine.repository.DeploymentWithDefinitions;
import org.operaton.bpm.engine.rest.dto.batch.BatchDto;
import org.operaton.bpm.engine.rest.dto.history.HistoricActivityInstanceDto;
import org.operaton.bpm.engine.rest.dto.history.UserOperationLogEntryDto;
import org.operaton.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;
import org.operaton.bpm.engine.rest.dto.runtime.ActivityInstanceDto;
import org.operaton.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.operaton.bpm.engine.runtime.ProcessInstanceQuery;
import org.operaton.bpm.engine.task.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@SkipComponentScan
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class ProcessResource extends AbstractProcessResource {

    private final HistoryService historyService;
    private final RuntimeService runtimeService;
    private final OperatonRepositoryService repositoryService;
    private final OperatonTaskService operatonTaskService;
    private final OperatonProcessService operatonProcessService;
    private final ProcessShortTimerService processShortTimerService;
    private final OperatonSearchProcessInstanceRepository operatonSearchProcessInstanceRepository;
    private final ProcessPropertyService processPropertyService;

    public ProcessResource(
            final HistoryService historyService,
            final OperatonHistoryService operatonHistoryService,
            final RuntimeService runtimeService,
            final RepositoryService repositoryService,
            final OperatonRepositoryService operatonRepositoryService,
            final OperatonTaskService operatonTaskService,
            final OperatonProcessService operatonProcessService,
            final ProcessShortTimerService processShortTimerService,
            final OperatonSearchProcessInstanceRepository operatonSearchProcessInstanceRepository,
            final ProcessPropertyService processPropertyService
    ) {
        super(operatonHistoryService, repositoryService, operatonRepositoryService, operatonTaskService);
        this.historyService = historyService;
        this.runtimeService = runtimeService;
        this.repositoryService = operatonRepositoryService;
        this.operatonTaskService = operatonTaskService;
        this.operatonProcessService = operatonProcessService;
        this.processShortTimerService = processShortTimerService;
        this.operatonSearchProcessInstanceRepository = operatonSearchProcessInstanceRepository;
        this.processPropertyService = processPropertyService;
    }

    @GetMapping("/v1/process/definition")
    public ResponseEntity<List<ProcessDefinitionWithPropertiesDto>> getProcessDefinitions() {
        final List<ProcessDefinitionWithPropertiesDto> definitions = runWithoutAuthorization(() -> operatonProcessService
                .getDeployedDefinitions()
                .stream()
                .map(ProcessDefinitionWithPropertiesDto::fromProcessDefinition)
                .collect(Collectors.toList()));
        definitions.forEach(definition ->
                definition.setReadOnly(processPropertyService.isReadOnly(definition.getKey()))
        );
        return ResponseEntity.ok(definitions);
    }

    @GetMapping("/v1/process/definition/{processDefinitionKey}")
    public ResponseEntity<OperatonProcessDefinitionDto> getProcessDefinition(
        @LoggableResource(resourceTypeName = "processDefinitionKey") @PathVariable String processDefinitionKey
    ) {
        OperatonProcessDefinition processDefinition = runWithoutAuthorization(() -> repositoryService.findProcessDefinition(
                byKey(processDefinitionKey)
                    .and(byLatestVersion())
            )
        );
        return Optional.ofNullable(processDefinition)
                .map(process -> ResponseEntity.ok(OperatonProcessDefinitionDto.of(processDefinition)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/v1/process/definition/{processDefinitionKey}/versions")
    public ResponseEntity<List<OperatonProcessDefinitionDto>> getProcessDefinitionVersions(
        @LoggableResource(resourceTypeName = "processDefinitionKey") @PathVariable String processDefinitionKey
    ) {
        List<OperatonProcessDefinition> deployedDefinitions = runWithoutAuthorization(() -> repositoryService.findProcessDefinitions(
                byKey(processDefinitionKey),
                Sort.by(VERSION)
            ));
        List<OperatonProcessDefinitionDto> result = deployedDefinitions.stream()
                .map(OperatonProcessDefinitionDto::of)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/v1/process/definition/{processDefinitionId}/xml")
    public ResponseEntity<ProcessDefinitionDiagramWithPropertyDto> getProcessDefinitionXml(
        @LoggableResource(resourceType = OperatonProcessDefinition.class) @PathVariable String processDefinitionId
    ) {
        try {
            final var definitionDiagramDto = createProcessDefinitionDiagramDto(processDefinitionId);
            if (definitionDiagramDto == null) {
                return ResponseEntity.notFound().build();
            }
            final var definitionWithDiagramAndProperties = new ProcessDefinitionDiagramWithPropertyDto(
                    definitionDiagramDto,
                    processPropertyService.isReadOnlyById(processDefinitionId),
                    processPropertyService.isSystemProcessById(processDefinitionId)
            );
            return ResponseEntity.ok(definitionWithDiagramAndProperties);
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/v1/process/definition/{sourceProcessDefinitionId}/{targetProcessDefinitionId}/flownodes")
    public ResponseEntity<FlowNodeMigrationDTO> getFlowNodes(
        @LoggableResource(resourceType = OperatonProcessDefinition.class) @PathVariable String sourceProcessDefinitionId,
        @PathVariable String targetProcessDefinitionId
    ) {
        final Map<String, String> sourceFlowNodeMap = findAllTasksEventsAndGatewaysForProcessDefinitionId(
                sourceProcessDefinitionId);
        final Map<String, String> targetFlowNodeMap = findAllTasksEventsAndGatewaysForProcessDefinitionId(
                targetProcessDefinitionId);
        final Map<String, String> uniqueFlowNodeMap = getUniqueFlowNodeMap(sourceFlowNodeMap, targetFlowNodeMap);
        final FlowNodeMigrationDTO flowNodeMigrationDTO = new FlowNodeMigrationDTO(
                sourceFlowNodeMap, targetFlowNodeMap, uniqueFlowNodeMap);
        return ResponseEntity.ok(flowNodeMigrationDTO);
    }

    @GetMapping("/v1/process/definition/{processDefinitionKey}/heatmap/count")
    public ResponseEntity<Map<String, HeatmapTaskCountDTO>> getProcessDefinitionHeatmap(
        @LoggableResource(resourceTypeName = "processDefinitionKey") @PathVariable String processDefinitionKey,
            @RequestParam Integer version,
            @RequestParam(required = false) String searchStatus,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) Integer duration
    ) {
        OperatonProcessDefinition processDefinition = getProcessDefinition(processDefinitionKey, version);

        HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery()
                .processDefinitionId(processDefinition.getId());

        if (StringUtils.isNotBlank(searchStatus)) {
            historicActivityInstanceQuery.activityName(searchStatus);
        }

        if (fromDate != null) {
            historicActivityInstanceQuery.startedAfter(Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        if (toDate != null) {
            historicActivityInstanceQuery.startedBefore(Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        if (Optional.ofNullable(duration).isPresent()) {
            LocalDate dayinPast = LocalDate.now().minusDays(duration);
            historicActivityInstanceQuery.startedBefore(
                    Date.from(dayinPast.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        List<HistoricActivityInstance> historicActivityInstances = historicActivityInstanceQuery.finished()
                .orderPartiallyByOccurrence()
                .asc()
                .list();

        Map<String, Long> heatmapDataCount = historicActivityInstances.stream()
                .collect(Collectors.groupingBy(HistoricActivityInstance::getActivityId, Collectors.counting()));

        LocalDateTime fromDateStart = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime toDateStart = toDate == null ? null : toDate.atStartOfDay();
        Map<String, HeatmapTaskCountDTO> activeTasksCount = getActiveTasksCounts(
                processDefinition, searchStatus, fromDateStart, toDateStart, duration);

        for (Map.Entry<String, Long> entry : heatmapDataCount.entrySet()) {
            HeatmapTaskCountDTO heatmapTaskCountDTO = activeTasksCount.get(entry.getKey());

            if (heatmapTaskCountDTO == null) {
                heatmapTaskCountDTO = new HeatmapTaskCountDTO();
                activeTasksCount.put(entry.getKey(), heatmapTaskCountDTO);
            }

            if (entry.getValue() != null) {
                heatmapTaskCountDTO.setTotalCount(entry.getValue());
            }
        }
        return ResponseEntity.ok(activeTasksCount);
    }

    @GetMapping("/v1/process/definition/{processDefinitionKey}/heatmap/duration")
    public ResponseEntity<Map<String, HeatmapTaskAverageDurationDTO>> getProcessDefinitionDurationBasedHeatmap(
        @LoggableResource(resourceTypeName = "processDefinitionKey") @PathVariable String processDefinitionKey,
            @RequestParam Integer version,
            @RequestParam(required = false) String searchStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer duration
    ) {
        OperatonProcessDefinition processDefinition = getProcessDefinition(processDefinitionKey, version);

        HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery()
                .processDefinitionId(processDefinition.getId());

        if (StringUtils.isNotBlank(searchStatus)) {
            historicActivityInstanceQuery.activityName(searchStatus);
        }

        if (fromDate != null) {
            historicActivityInstanceQuery.startedAfter(Date.from(fromDate.atStartOfDay(systemDefault()).toInstant()));
        }

        if (toDate != null) {
            historicActivityInstanceQuery.startedBefore(Date.from(toDate.atStartOfDay(systemDefault()).toInstant()));
        }

        if (Optional.ofNullable(duration).isPresent()) {
            LocalDate dayinPast = LocalDate.now().minusDays(duration);
            historicActivityInstanceQuery.startedBefore(
                    Date.from(dayinPast.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        List<HistoricActivityInstance> historicActivityInstances = historicActivityInstanceQuery.finished()
                .orderPartiallyByOccurrence()
                .asc()
                .list();

        Map<String, HeatmapTaskAverageDurationDTO> allTasksAverageDuration = new HashMap<>();

        for (HistoricActivityInstance h : historicActivityInstances) {
            HeatmapTaskAverageDurationDTO heatmapTaskAverageDurationDTO = allTasksAverageDuration.get(
                    h.getActivityId());
            if (heatmapTaskAverageDurationDTO == null) {
                allTasksAverageDuration.put(
                        h.getActivityId(),
                        new HeatmapTaskAverageDurationDTO(h.getActivityName(), 0, 1, h.getDurationInMillis())
                );
            } else {
                heatmapTaskAverageDurationDTO.setTotalCount(heatmapTaskAverageDurationDTO.getTotalCount() + 1);
                heatmapTaskAverageDurationDTO.setAverageDurationInMilliseconds(
                        heatmapTaskAverageDurationDTO.getAverageDurationInMilliseconds());
            }
        }

        List<OperatonTask> taskList = getAllActiveTasks(
            processDefinition,
            searchStatus,
            fromDate != null ? fromDate.atStartOfDay() : null,
            toDate != null ? toDate.atStartOfDay() : null,
            duration
        );
        Map<String, Long> groupedList = taskList.stream()
                .collect(Collectors.groupingBy(OperatonTask::getTaskDefinitionKey, Collectors.counting()));

        allTasksAverageDuration.forEach((k, v) -> {
            v.setAverageDurationInMilliseconds(v.getAverageDurationInMilliseconds() / v.getTotalCount());
            if (groupedList.containsKey(k)) {
                Long count = groupedList.get(k);
                if (count == null) {
                    count = 0L;
                }
                v.setCount(count);
            }

        });
        return ResponseEntity.ok(allTasksAverageDuration);
    }

    @PostMapping(value = "/v1/process/definition/{processDefinitionKey}/{businessKey}/start", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProcessInstanceDto> startProcessInstance(
        @LoggableResource(resourceTypeName = "processDefinitionKey") @PathVariable String processDefinitionKey,
        @LoggableResource(resourceTypeName = "businessKey") @PathVariable String businessKey,
        @RequestBody Map<String, Object> variables
    ) {
        final var processInstanceWithDefinition = operatonProcessService
            .startProcess(processDefinitionKey, businessKey, variables);
        return ResponseEntity.ok(processInstanceWithDefinition.getProcessInstanceDto());
    }

    @GetMapping("/v1/process/{processInstanceId}")
    public ResponseEntity<OperatonHistoricProcessInstanceDto> getProcessInstance(
        @LoggableResource(resourceType = OperatonExecution.class) @PathVariable String processInstanceId
    ) {
        OperatonHistoricProcessInstance historicProcessInstance = runWithoutAuthorization(() ->
            getHistoricProcessInstance(processInstanceId)
        );
        return Optional.ofNullable(historicProcessInstance)
                .map(process -> ResponseEntity.ok(
                    OperatonHistoricProcessInstanceDto.of(historicProcessInstance)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/v1/process/{processInstanceId}/history")
    public ResponseEntity<List<HistoricActivityInstanceDto>> getProcessInstanceHistory(
        @LoggableResource(resourceType = OperatonExecution.class) @PathVariable String processInstanceId
    ) {
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderPartiallyByOccurrence()
                .asc()
                .list();

        List<HistoricActivityInstanceDto> result = historicActivityInstances.stream()
                .map(instance -> {
                    HistoricActivityInstanceDto historicActivityInstanceDto = new HistoricActivityInstanceDto();
                    HistoricActivityInstanceDto.fromHistoricActivityInstance(historicActivityInstanceDto, instance);
                    return historicActivityInstanceDto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/v1/process/{processInstanceId}/log")
    public ResponseEntity<List<UserOperationLogEntryDto>> getProcessInstanceOperationLog(
        @LoggableResource(resourceType = OperatonExecution.class) @PathVariable String processInstanceId
    ) {
        List<UserOperationLogEntry> userOperationLogEntries = historyService.createUserOperationLogQuery()
                .processDefinitionId(processInstanceId)
                .list();
        List<UserOperationLogEntryDto> result = userOperationLogEntries.stream()
                .map(UserOperationLogEntryDto::map)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/v1/process/{processInstanceId}/tasks")
    public ResponseEntity<List<TaskInstanceWithIdentityLink>> getProcessInstanceTasks(
        @LoggableResource(resourceType = OperatonExecution.class) @PathVariable String processInstanceId
    ) {
        return runWithoutAuthorization(
                () -> operatonProcessService.findProcessInstanceById(processInstanceId)
            )
                .map(processInstance -> ResponseEntity.ok(
                        operatonTaskService.getProcessInstanceTasks(processInstance.getId(), processInstance.getBusinessKey())))
                .orElse(ResponseEntity.noContent().build()
            );
    }

    @GetMapping("/v1/process/{processInstanceId}/activetask")
    public ResponseEntity<OperatonTaskDto> getProcessInstanceActiveTask(
        @LoggableResource(resourceType = OperatonExecution.class) @PathVariable String processInstanceId
    ) {
        OperatonTask task = operatonTaskService.findTask(
            byActive()
                .and(byProcessInstanceId(processInstanceId))
        );
        return Optional.ofNullable(task)
                .map(taskResult -> ResponseEntity.ok(OperatonTaskDto.of(taskResult)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/v1/process/{processInstanceId}/xml")
    public ResponseEntity<ProcessInstanceDiagramDto> getProcessInstanceXml(
        @LoggableResource(resourceType = OperatonExecution.class) @PathVariable String processInstanceId
    ) {
        OperatonHistoricProcessInstance processInstance = runWithoutAuthorization(() ->
            getHistoricProcessInstance(processInstanceId)
        );
        try {
            ProcessDefinitionDiagramDto definitionDiagramDto = createProcessDefinitionDiagramDto(
                    processInstance.getProcessDefinitionId());
            List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .orderPartiallyByOccurrence()
                    .asc()
                    .list();
            return Optional.ofNullable(definitionDiagramDto)
                    .map(process -> ResponseEntity.ok(
                            ProcessInstanceDiagramDto.create(definitionDiagramDto, historicActivityInstances)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/v1/process/{processInstanceId}/activities")
    public ResponseEntity<ActivityInstanceDto> getProcessInstanceActivity(
        @LoggableResource(resourceType = OperatonExecution.class) @PathVariable String processInstanceId
    ) {
        final var activityInstance = runtimeService.getActivityInstance(processInstanceId);
        return Optional
                .ofNullable(activityInstance)
                .map(process -> ResponseEntity.ok(ActivityInstanceDto.fromActivityInstance(activityInstance)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieve a list of comments that are associated to a process instance.
     * @deprecated Task comments will be removed in the future.
     */
    @Deprecated(since = "11.1.0", forRemoval = true)
    @GetMapping("/v1/process/{processInstanceId}/comments")
    public ResponseEntity<List<Comment>> getProcessInstanceComments(
        @LoggableResource(resourceType = OperatonExecution.class) @PathVariable String processInstanceId
    ) {
        List<Comment> processInstanceComments = operatonTaskService.getProcessInstanceComments(processInstanceId);
        processInstanceComments.sort((Comment c1, Comment c2) -> c2.getTime().compareTo(c1.getTime()));
        return ResponseEntity.ok(processInstanceComments);
    }

    /**
     * Endpoint to search for process instances.
     *
     * @deprecated since 12.0.0, use v2 instead
     */
    @PostMapping("/v1/process/{processDefinitionName}/search")
    @Deprecated(since = "12.0.0", forRemoval = true)
    public ResponseEntity<List<ProcessInstance>> searchProcessInstancesV2(
        @LoggableResource(resourceTypeName = "processDefinitionName") @PathVariable String processDefinitionName,
        @RequestBody ProcessInstanceSearchDTO processInstanceSearchDTO,
        Pageable pageable
    ) {
        final Page<ProcessInstance> page = operatonSearchProcessInstanceRepository.searchInstances(
                processDefinitionName,
                processInstanceSearchDTO,
                pageable
        );
        final HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                page, "/v1/process/{processDefinitionName}/search");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/v2/process/{processDefinitionName}/search")
    public ResponseEntity<Page<ProcessInstance>> searchProcessInstancesPaged(
        @LoggableResource(resourceTypeName = "processDefinitionName") @PathVariable String processDefinitionName,
        @RequestBody ProcessInstanceSearchDTO processInstanceSearchDTO,
        Pageable pageable
    ) {
        final Page<ProcessInstance> page = operatonSearchProcessInstanceRepository.searchInstances(
            processDefinitionName,
            processInstanceSearchDTO,
            pageable
        );
        return ResponseEntity.ok(page);
    }

    @PostMapping("/v1/process/{processDefinitionName}/count")
    public ResponseEntity<ResultCount> searchProcessInstanceCountV2(
        @LoggableResource(resourceTypeName = "processDefinitionName") @PathVariable String processDefinitionName,
        @RequestBody ProcessInstanceSearchDTO processInstanceSearchDTO
    ) {
        final Long count = operatonSearchProcessInstanceRepository.searchInstancesCountByDefinitionName(
                processDefinitionName,
                processInstanceSearchDTO
        );
        return ResponseEntity.ok(new ResultCount(count));
    }

    @PostMapping("/v1/process/definition/{processDefinitionId}/count")
    public ResponseEntity<ResultCount> getProcessInstanceCountForProcessDefinitionIdV2(
        @LoggableResource(resourceType = OperatonProcessDefinition.class) @PathVariable String processDefinitionId,
        @RequestBody ProcessInstanceSearchDTO processInstanceSearchDTO
    ) {
        final Long count = operatonSearchProcessInstanceRepository.searchInstancesCountByDefinitionId(
                processDefinitionId, processInstanceSearchDTO);
        return ResponseEntity.ok(new ResultCount(count));
    }

    @PostMapping("/v1/process/definition/{sourceProcessDefinitionId}/{targetProcessDefinitionId}/migrate")
    @ResponseBody
    @Transactional
    public ResponseEntity<BatchDto> migrateProcessInstancesByProcessDefinitionIds(
        @LoggableResource(resourceType = OperatonProcessDefinition.class) @PathVariable String sourceProcessDefinitionId,
        @PathVariable String targetProcessDefinitionId,
        @RequestBody(required = false) Map<String, String> instructions
    ) {
        if (processPropertyService.isReadOnlyById(targetProcessDefinitionId)) {
            return ResponseEntity.status(FORBIDDEN).build();
        }
        MigrationPlanBuilder migrationPlanBuilder = ProcessEngines.getDefaultProcessEngine()
                .getRuntimeService()
                .createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId);

        migrationPlanBuilder.mapEqualActivities();
        if (instructions != null) {
            for (Map.Entry<String, String> i : instructions.entrySet()) {
                migrationPlanBuilder.mapActivities(i.getKey(), i.getValue());
            }
        }
        MigrationPlan migrationPlan = migrationPlanBuilder.build();
        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(
                sourceProcessDefinitionId);
        Batch migrationBatch = runtimeService.newMigration(migrationPlan).processInstanceQuery(
                processInstanceQuery).executeAsync();
        return new ResponseEntity<>(BatchDto.fromBatch(migrationBatch), HttpStatus.OK);
    }

    /**
     * Create a comment and associate that comment to either a task or a process instance.
     * @deprecated Task comments will be removed in the future.
     */
    @Deprecated(since = "11.1.0", forRemoval = true)
    @PostMapping("/v1/process/{processInstanceId}/comment")
    public ResponseEntity<Void> createComment(
        @LoggableResource(resourceType = OperatonExecution.class) @PathVariable String processInstanceId,
        @RequestBody CommentDto comment
    ) {
        operatonTaskService.createComment(null, processInstanceId, comment.getText());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/process/{processInstanceId}/delete")
    public ResponseEntity<Void> delete(
        @LoggableResource(resourceType = OperatonExecution.class) @PathVariable String processInstanceId,
        @RequestBody String reason
    ) {
        runWithoutAuthorization(() -> {
            operatonProcessService.deleteProcessInstanceById(processInstanceId, reason);
            return null;
        });
        return ResponseEntity.ok().build();
    }

    @PutMapping("/v1/process/definition/{processDefinitionId}/xml/timer")
    public ResponseEntity<Void> modifyProcessDefinitionIntoShortTimerVersionAndDeploy(
        @LoggableResource(resourceType = OperatonProcessDefinition.class) @PathVariable String processDefinitionId
    ) throws ProcessNotFoundException, DocumentParserException {
        if (processPropertyService.isReadOnlyById(processDefinitionId)) {
            return ResponseEntity.status(FORBIDDEN).build();
        }
        processShortTimerService.modifyAndDeployShortTimerVersion(processDefinitionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/v1/process/definition/deployment", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> deployProcessDefinition(
        @RequestPart(name = "file") MultipartFile bpmn) {
        boolean correctFileExtension = Objects.requireNonNull(bpmn.getOriginalFilename()).endsWith(".bpmn")
            || Objects.requireNonNull(bpmn.getOriginalFilename()).endsWith(".dmn");

        if (!correctFileExtension) {
            return ResponseEntity.badRequest().body("Invalid file name. Must have '.bpmn' or '.dmn' suffix.");
        }
        try {
            return runWithoutAuthorization(
                () -> {
                    DeploymentWithDefinitions deployment = operatonProcessService.deploy(
                        null,
                        bpmn.getOriginalFilename(),
                        new ByteArrayInputStream(bpmn.getBytes())
                    );

                    return ResponseEntity.ok(DefinitionDeploymentResponseDto.Companion.of((DeploymentEntity) deployment));
                });
        } catch (ParseException e) {
            throw new BpmnParseException(e);
        }
    }

}
