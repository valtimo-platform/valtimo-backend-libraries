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

import com.ritense.authorization.AuthorizationContext;
import com.ritense.valtimo.camunda.domain.CamundaHistoricProcessInstance;
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.dto.CamundaHistoricProcessInstanceDto;
import com.ritense.valtimo.camunda.dto.CamundaProcessDefinitionDto;
import com.ritense.valtimo.camunda.dto.CamundaTaskDto;
import com.ritense.valtimo.camunda.service.CamundaHistoryService;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.contract.exception.DocumentParserException;
import com.ritense.valtimo.contract.exception.ProcessNotFoundException;
import com.ritense.valtimo.repository.CamundaSearchProcessInstanceRepository;
import com.ritense.valtimo.repository.camunda.dto.ProcessInstance;
import com.ritense.valtimo.repository.camunda.dto.TaskInstanceWithIdentityLink;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.CamundaTaskService;
import com.ritense.valtimo.service.ProcessPropertyService;
import com.ritense.valtimo.service.ProcessShortTimerService;
import com.ritense.valtimo.web.rest.dto.CommentDto;
import com.ritense.valtimo.web.rest.dto.FlowNodeMigrationDTO;
import com.ritense.valtimo.web.rest.dto.HeatmapTaskAverageDurationDTO;
import com.ritense.valtimo.web.rest.dto.HeatmapTaskCountDTO;
import com.ritense.valtimo.web.rest.dto.ProcessDefinitionDiagramWithPropertyDto;
import com.ritense.valtimo.web.rest.dto.ProcessDefinitionWithPropertiesDto;
import com.ritense.valtimo.web.rest.dto.ProcessInstanceDiagramDto;
import com.ritense.valtimo.web.rest.dto.ProcessInstanceSearchDTO;
import com.ritense.valtimo.web.rest.util.PaginationUtil;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.history.UserOperationLogEntryDto;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.runtime.ActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Comment;
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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.VERSION;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byKey;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byLatestVersion;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byActive;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byProcessInstanceId;
import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class ProcessResource extends AbstractProcessResource {

    private final HistoryService historyService;
    private final RuntimeService runtimeService;
    private final CamundaRepositoryService repositoryService;
    private final CamundaTaskService camundaTaskService;
    private final CamundaProcessService camundaProcessService;
    private final ProcessShortTimerService processShortTimerService;
    private final CamundaSearchProcessInstanceRepository camundaSearchProcessInstanceRepository;
    private final ProcessPropertyService processPropertyService;

    public ProcessResource(
            final HistoryService historyService,
            final CamundaHistoryService camundaHistoryService,
            final RuntimeService runtimeService,
            final RepositoryService repositoryService,
            final CamundaRepositoryService camundaRepositoryService,
            final CamundaTaskService camundaTaskService,
            final CamundaProcessService camundaProcessService,
            final ProcessShortTimerService processShortTimerService,
            final CamundaSearchProcessInstanceRepository camundaSearchProcessInstanceRepository,
            final ProcessPropertyService processPropertyService
    ) {
        super(camundaHistoryService, repositoryService, camundaRepositoryService, camundaTaskService);
        this.historyService = historyService;
        this.runtimeService = runtimeService;
        this.repositoryService = camundaRepositoryService;
        this.camundaTaskService = camundaTaskService;
        this.camundaProcessService = camundaProcessService;
        this.processShortTimerService = processShortTimerService;
        this.camundaSearchProcessInstanceRepository = camundaSearchProcessInstanceRepository;
        this.processPropertyService = processPropertyService;
    }

    @GetMapping("/v1/process/definition")
    public ResponseEntity<List<ProcessDefinitionWithPropertiesDto>> getProcessDefinitions() {
        final List<ProcessDefinitionWithPropertiesDto> definitions = AuthorizationContext
            .runWithoutAuthorization(() -> camundaProcessService
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
    public ResponseEntity<CamundaProcessDefinitionDto> getProcessDefinition(@PathVariable String processDefinitionKey) {
        CamundaProcessDefinition processDefinition = AuthorizationContext
            .runWithoutAuthorization(() -> repositoryService.findProcessDefinition(
                byKey(processDefinitionKey)
                    .and(byLatestVersion())
            )
        );
        return Optional.ofNullable(processDefinition)
                .map(process -> ResponseEntity.ok(CamundaProcessDefinitionDto.of(processDefinition)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/v1/process/definition/{processDefinitionKey}/versions")
    public ResponseEntity<List<CamundaProcessDefinitionDto>> getProcessDefinitionVersions(
            @PathVariable String processDefinitionKey
    ) {
        List<CamundaProcessDefinition> deployedDefinitions = AuthorizationContext
            .runWithoutAuthorization(() -> repositoryService.findProcessDefinitions(
                byKey(processDefinitionKey),
                Sort.by(VERSION)
            ));
        List<CamundaProcessDefinitionDto> result = deployedDefinitions.stream()
                .map(CamundaProcessDefinitionDto::of)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/v1/process/definition/{processDefinitionId}/xml")
    public ResponseEntity<ProcessDefinitionDiagramWithPropertyDto> getProcessDefinitionXml(
            @PathVariable String processDefinitionId
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
            @PathVariable String sourceProcessDefinitionId, @PathVariable String targetProcessDefinitionId
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
            @PathVariable String processDefinitionKey,
            @RequestParam Integer version,
            @RequestParam(required = false) String searchStatus,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) Integer duration
    ) {
        CamundaProcessDefinition processDefinition = getProcessDefinition(processDefinitionKey, version);

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

        Map<String, HeatmapTaskCountDTO> activeTasksCount = getActiveTasksCounts(
                processDefinition, searchStatus, fromDate.atStartOfDay(), toDate.atStartOfDay(), duration);

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
            @PathVariable String processDefinitionKey,
            @RequestParam Integer version,
            @RequestParam(required = false) String searchStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer duration
    ) {
        CamundaProcessDefinition processDefinition = getProcessDefinition(processDefinitionKey, version);

        HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery()
                .processDefinitionId(processDefinition.getId());

        if (StringUtils.isNotBlank(searchStatus)) {
            historicActivityInstanceQuery.activityName(searchStatus);
        }

        if (fromDate != null) {
            historicActivityInstanceQuery.startedAfter(Date.from(fromDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant()));
        }

        if (toDate != null) {
            historicActivityInstanceQuery.startedBefore(Date.from(toDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant()));
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

        List<CamundaTask> taskList = getAllActiveTasks(processDefinition, searchStatus, fromDate.atStartOfDay(), toDate.atStartOfDay(), duration);
        Map<String, Long> groupedList = taskList.stream()
                .collect(Collectors.groupingBy(CamundaTask::getTaskDefinitionKey, Collectors.counting()));

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
            @PathVariable String processDefinitionKey,
            @PathVariable String businessKey,
            @RequestBody Map<String, Object> variables
    ) {
        final var processInstanceWithDefinition = AuthorizationContext.runWithoutAuthorization(() ->
            camundaProcessService.startProcess(processDefinitionKey, businessKey, variables));
        return ResponseEntity.ok(processInstanceWithDefinition.getProcessInstanceDto());
    }

    @GetMapping("/v1/process/{processInstanceId}")
    public ResponseEntity<CamundaHistoricProcessInstanceDto> getProcessInstance(@PathVariable String processInstanceId) {
        CamundaHistoricProcessInstance historicProcessInstance = AuthorizationContext.runWithoutAuthorization(() ->
            getHistoricProcessInstance(processInstanceId)
        );
        return Optional.ofNullable(historicProcessInstance)
                .map(process -> ResponseEntity.ok(
                    CamundaHistoricProcessInstanceDto.of(historicProcessInstance)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/v1/process/{processInstanceId}/history")
    public ResponseEntity<List<HistoricActivityInstanceDto>> getProcessInstanceHistory(
            @PathVariable String processInstanceId
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
            @PathVariable String processInstanceId
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
            @PathVariable String processInstanceId
    ) {
        return AuthorizationContext
            .runWithoutAuthorization(
                () -> camundaProcessService.findProcessInstanceById(processInstanceId)
                    .map(processInstance -> ResponseEntity.ok(
                            camundaTaskService.getProcessInstanceTasks(processInstance.getId(), processInstance.getBusinessKey())))
                    .orElse(ResponseEntity.noContent().build())
            );
    }

    @GetMapping("/v1/process/{processInstanceId}/activetask")
    public ResponseEntity<CamundaTaskDto> getProcessInstanceActiveTask(@PathVariable String processInstanceId) {
        CamundaTask task = camundaTaskService.findTask(
            byActive()
                .and(byProcessInstanceId(processInstanceId))
        );
        return Optional.ofNullable(task)
                .map(taskResult -> ResponseEntity.ok(CamundaTaskDto.of(taskResult)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/v1/process/{processInstanceId}/xml")
    public ResponseEntity<ProcessInstanceDiagramDto> getProcessInstanceXml(@PathVariable String processInstanceId) {
        CamundaHistoricProcessInstance processInstance = AuthorizationContext.runWithoutAuthorization(() ->
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
    public ResponseEntity<ActivityInstanceDto> getProcessInstanceActivity(@PathVariable String processInstanceId) {
        final var activityInstance = runtimeService.getActivityInstance(processInstanceId);
        return Optional
                .ofNullable(activityInstance)
                .map(process -> ResponseEntity.ok(ActivityInstanceDto.fromActivityInstance(activityInstance)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/v1/process/{processInstanceId}/comments")
    public ResponseEntity<List<Comment>> getProcessInstanceComments(@PathVariable String processInstanceId) {
        List<Comment> processInstanceComments = camundaTaskService.getProcessInstanceComments(processInstanceId);
        processInstanceComments.sort((Comment c1, Comment c2) -> c2.getTime().compareTo(c1.getTime()));
        return ResponseEntity.ok(processInstanceComments);
    }

    @PostMapping("/v1/process/{processDefinitionName}/search")
    public ResponseEntity<List<ProcessInstance>> searchProcessInstancesV2(
            @PathVariable String processDefinitionName,
            @RequestBody ProcessInstanceSearchDTO processInstanceSearchDTO,
            Pageable pageable
    ) {
        final Page<ProcessInstance> page = camundaSearchProcessInstanceRepository.searchInstances(
                processDefinitionName,
                processInstanceSearchDTO,
                pageable
        );
        final HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(
                page, "/v1/process/{processDefinitionName}/search");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/v1/process/{processDefinitionName}/count")
    public ResponseEntity<ResultCount> searchProcessInstanceCountV2(
            @PathVariable String processDefinitionName,
            @RequestBody ProcessInstanceSearchDTO processInstanceSearchDTO
    ) {
        final Long count = camundaSearchProcessInstanceRepository.searchInstancesCountByDefinitionName(
                processDefinitionName,
                processInstanceSearchDTO
        );
        return ResponseEntity.ok(new ResultCount(count));
    }

    @PostMapping("/v1/process/definition/{processDefinitionId}/count")
    public ResponseEntity<ResultCount> getProcessInstanceCountForProcessDefinitionIdV2(
            @PathVariable String processDefinitionId,
            @RequestBody ProcessInstanceSearchDTO processInstanceSearchDTO
    ) {
        final Long count = camundaSearchProcessInstanceRepository.searchInstancesCountByDefinitionId(
                processDefinitionId, processInstanceSearchDTO);
        return ResponseEntity.ok(new ResultCount(count));
    }

    @PostMapping("/v1/process/definition/{sourceProcessDefinitionId}/{targetProcessDefinitionId}/migrate")
    @ResponseBody
    @Transactional
    public ResponseEntity<BatchDto> migrateProcessInstancesByProcessDefinitionIds(
            @PathVariable String sourceProcessDefinitionId,
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

    @PostMapping("/v1/process/{processInstanceId}/comment")
    public ResponseEntity<Void> createComment(@PathVariable String processInstanceId, @RequestBody CommentDto comment) {
        camundaTaskService.createComment(null, processInstanceId, comment.getText());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/process/{processInstanceId}/delete")
    public ResponseEntity<Void> delete(@PathVariable String processInstanceId, @RequestBody String reason) {
        AuthorizationContext.runWithoutAuthorization(() -> {
            camundaProcessService.deleteProcessInstanceById(processInstanceId, reason);
            return null;
        });
        return ResponseEntity.ok().build();
    }

    @PutMapping("/v1/process/definition/{processDefinitionId}/xml/timer")
    public ResponseEntity<Void> modifyProcessDefinitionIntoShortTimerVersionAndDeploy(
            @PathVariable String processDefinitionId
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
        if (!Objects.requireNonNull(bpmn.getOriginalFilename()).endsWith(".bpmn")) {
            return ResponseEntity.badRequest().body("Invalid file name. Must have '.bpmn' suffix.");
        }
        try {
            AuthorizationContext.runWithoutAuthorization(() -> {
                camundaProcessService.deploy(bpmn.getOriginalFilename(), new ByteArrayInputStream(bpmn.getBytes()));
                return null;
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

}
