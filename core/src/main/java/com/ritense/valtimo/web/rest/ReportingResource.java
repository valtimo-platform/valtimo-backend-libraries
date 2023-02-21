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

import com.ritense.valtimo.repository.CamundaReportingRepository;
import com.ritense.valtimo.repository.camunda.dto.ChartInstance;
import com.ritense.valtimo.repository.camunda.dto.ChartInstanceSeries;
import com.ritense.valtimo.repository.camunda.dto.InstanceCountChart;
import com.ritense.valtimo.web.rest.dto.ProcessInstanceStatisticsDTO;
import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
public class ReportingResource {

    private static final String ACTIVITY_USER_TASK = "userTask";
    private final SqlSession session;
    private final HistoryService historyService;
    private final CamundaReportingRepository camundaReportingRepository;

    public ReportingResource(SqlSession session, HistoryService historyService, CamundaReportingRepository camundaReportingRepository) {
        this.session = session;
        this.historyService = historyService;
        this.camundaReportingRepository = camundaReportingRepository;
    }

    @GetMapping(value = "/v1/reporting/instancecount")
    public ResponseEntity<InstanceCountChart> instanceCount(
        @RequestParam(value = "processFilter", required = false) String processId
    ) {
        InstanceCountChart instanceCounts = camundaReportingRepository.getProcessInstanceCounts(processId);
        return new ResponseEntity<>(instanceCounts, HttpStatus.OK);
    }

    @GetMapping(value = "/v1/reporting/instancesstatistics")
    public ResponseEntity<List<ProcessInstanceStatisticsDTO>> instanceStatistics(
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "processFilter", required = false) String processDefinitionKey
    ) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("processDefinitionKey", null);
        parameters.put("startDate", null);
        parameters.put("endDate", null);

        if (Optional.ofNullable(processDefinitionKey).isPresent()) {
            parameters.put("processDefinitionKey", processDefinitionKey);
        }
        if (fromDate != null) {
            parameters.put("startDate", Date.valueOf(fromDate));
        }
        if (toDate != null) {
            parameters.put("endDate", Date.valueOf(toDate));
        }

        ListQueryParameterObject queryParameterObject = new ListQueryParameterObject();
        queryParameterObject.setParameter(parameters);
        String statement = "com.ritense.valtimo.mapper.processInstancesDuration";
        List<ProcessInstanceStatisticsDTO> processInstanceStatisticsList = session.selectList(statement, queryParameterObject);

        return new ResponseEntity<>(processInstanceStatisticsList, HttpStatus.OK);
    }

    @GetMapping(value = "/v1/reporting/tasksAverage")
    public ResponseEntity<ChartInstance> tasksHistory(
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "processFilter", required = false) String processKey
    ) {
        List<HistoricActivityInstance> historicTaskInstances = getFinishedHistoricTaskInstances(fromDate, toDate);
        //getting data into map
        List<String> categories = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        Integer count = 0;
        long time = 0;

        // the order in the query helps the countability
        for (HistoricActivityInstance hti : historicTaskInstances) {
            // filter here by ProcessKey... Query can't do it
            if (processKey == null || hti.getProcessDefinitionKey().equals(processKey)) {
                if (!categories.contains(hti.getActivityName())) {
                    // not first time? we already have something to add
                    if (!categories.isEmpty()) {
                        data.add(getHours(time, count));
                        count = 0;
                        time = 0;
                    }
                    categories.add(hti.getActivityName());
                }
                count++;
                time += hti.getDurationInMillis();
                // if is the last
                if (hti.getId().equals(historicTaskInstances.get(historicTaskInstances.size() - 1).getId())) {
                    data.add(getHours(time, count));
                }
            }
        }

        ChartInstanceSeries instanceSeries = new ChartInstanceSeries("averageLabel", data);
        Map<String, ChartInstanceSeries> series = new HashMap<>();
        series.put(instanceSeries.getName(), instanceSeries);
        return new ResponseEntity<>(new ChartInstance(categories, series), HttpStatus.OK);
    }

    @GetMapping(value = "/v1/reporting/tasksPerPerson")
    public ResponseEntity<ChartInstance> tasksPerPerson(
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "processFilter", required = false) String processKey
    ) {
        HistoricActivityInstanceQuery query = historyService
            .createHistoricActivityInstanceQuery()
            .finished()
            .activityType(ACTIVITY_USER_TASK);
        if (fromDate != null) {
            query.startedAfter(fromLocalDateToDate(fromDate));
        }
        if (toDate != null) {
            query.startedBefore(fromLocalDateToDate(toDate));
        }

        List<HistoricActivityInstance> historicTaskInstances = query.orderByHistoricActivityInstanceStartTime().asc().list();
        List<String> categories = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        HashMap<String, Long> chartMap = new HashMap<>();

        for (HistoricActivityInstance hti : historicTaskInstances) {
            if (processKey == null || hti.getProcessDefinitionKey().equals(processKey)) {
                if (chartMap.containsKey(hti.getAssignee())) {
                    chartMap.put(hti.getAssignee(), chartMap.get(hti.getAssignee()) + 1);
                } else {
                    chartMap.put(hti.getAssignee(), 1L);
                }
            }
        }
        for (Map.Entry<String, Long> pair : chartMap.entrySet()) {
            data.add(pair.getValue());
            categories.add(pair.getKey());
        }
        ChartInstanceSeries instanceSeries = new ChartInstanceSeries("byPersonLabel", data);
        Map<String, ChartInstanceSeries> series = new HashMap<>();
        series.put(instanceSeries.getName(), instanceSeries);
        return new ResponseEntity<>(new ChartInstance(categories, series), HttpStatus.OK);
    }

    @GetMapping(value = "/v1/reporting/pendingTasksByRole")
    public ResponseEntity<ChartInstance> pendingTasksByRole(
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "processFilter", required = false) String processId
    ) {
        return new ResponseEntity<>(camundaReportingRepository.getTasksPerRole(processId, fromDate, toDate), HttpStatus.OK);
    }

    @GetMapping(value = "/v1/reporting/unfinishedTasksPerType")
    public ResponseEntity<ChartInstance> unfinishedTasksPerType(
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "processFilter", required = false) String processDefinitionKey
    ) {
        ArrayList<String> categories = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        List<HistoricActivityInstance> historicTaskInstances = getUnfinishedHistoricTaskInstances(fromDate, toDate);

        int count = 0;
        for (HistoricActivityInstance historicActivityInstance : historicTaskInstances) {
            if (processDefinitionKey == null || historicActivityInstance.getProcessDefinitionKey().equals(processDefinitionKey)) {
                if (!categories.contains(historicActivityInstance.getActivityName())) {
                    if (!categories.isEmpty()) {
                        data.add((long) count);
                        count = 0;
                    }
                    categories.add(historicActivityInstance.getActivityName());
                }
                count++;

                if (historicActivityInstance.getId().equals(historicTaskInstances.get(historicTaskInstances.size() - 1).getId())) {
                    data.add((long) count);
                }
            }
        }

        ChartInstanceSeries instanceSeries = new ChartInstanceSeries("unfinishedTasksPerTypeLabel", data);
        Map<String, ChartInstanceSeries> series = new HashMap<>();
        series.put(instanceSeries.getName(), instanceSeries);
        return new ResponseEntity<>(new ChartInstance(categories, series), HttpStatus.OK);
    }

    @GetMapping(value = "/v1/reporting/finishedAndUnfinishedInstances")
    public ResponseEntity<ChartInstance> finishedAndUnfinishedInstances(
        @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @RequestParam(value = "processFilter", required = false) String processDefinitionKey
    ) {
        HistoricProcessInstanceQuery historicProcessInstanceQueryFinished = historyService.createHistoricProcessInstanceQuery();
        HistoricProcessInstanceQuery historicProcessInstanceQueryUnfinished = historyService.createHistoricProcessInstanceQuery();

        if (Optional.ofNullable(processDefinitionKey).isPresent()) {
            historicProcessInstanceQueryFinished.processDefinitionKey(processDefinitionKey);
            historicProcessInstanceQueryUnfinished.processDefinitionKey(processDefinitionKey);
        }

        if (fromDate != null) {
            historicProcessInstanceQueryFinished.finishedAfter(fromLocalDateToDate(fromDate));
            historicProcessInstanceQueryUnfinished.finishedAfter(fromLocalDateToDate(fromDate));
        }

        if (toDate != null) {
            historicProcessInstanceQueryFinished.finishedBefore(fromLocalDateToDate(toDate));
            historicProcessInstanceQueryUnfinished.finishedBefore(fromLocalDateToDate(toDate));
        }

        Long unfinishedInstances = historicProcessInstanceQueryUnfinished.unfinished().count();
        Long finishedInstances = historicProcessInstanceQueryFinished.finished().count();

        List<Long> data = new ArrayList<>();
        data.add(unfinishedInstances);
        data.add(finishedInstances);

        ChartInstanceSeries instanceSeries = new ChartInstanceSeries("finishedAndUnfinishedInstancesLabel", data);
        Map<String, ChartInstanceSeries> series = new HashMap<>();
        series.put(instanceSeries.getName(), instanceSeries);

        ArrayList<String> categories = new ArrayList<>();
        return new ResponseEntity<>(new ChartInstance(categories, series), HttpStatus.OK);
    }

    private java.util.Date fromLocalDateToDate(LocalDate date) {
        Instant instant = date.atStartOfDay().atZone(ZoneId.systemDefault())
            .toInstant();
        return java.util.Date.from(instant);
    }

    public Long getHours(Long time, Integer count) {
        return time > 0 ? time / count / 1000 / 60 / 60 : 0;
    }

    private List<HistoricActivityInstance> getFinishedHistoricTaskInstances(LocalDate fromDate, LocalDate toDate) {
        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().finished().activityType(ACTIVITY_USER_TASK);
        if (fromDate != null) {
            query.startedAfter(fromLocalDateToDate(fromDate));
        }
        if (toDate != null) {
            query.startedBefore(fromLocalDateToDate(toDate));
        }
        return query.orderByActivityName().asc().list();
    }

    private List<HistoricActivityInstance> getUnfinishedHistoricTaskInstances(LocalDate fromDate, LocalDate toDate){
        HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery()
            .unfinished()
            .activityType(ACTIVITY_USER_TASK);

        if (fromDate != null) {
            historicActivityInstanceQuery.startedAfter(fromLocalDateToDate(fromDate));
        }
        if (toDate != null) {
            historicActivityInstanceQuery.startedBefore(fromLocalDateToDate(toDate));
        }

        return historicActivityInstanceQuery.orderByActivityName().asc().list();
    }
}
