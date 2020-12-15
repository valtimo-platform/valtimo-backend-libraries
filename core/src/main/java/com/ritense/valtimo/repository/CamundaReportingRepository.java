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

package com.ritense.valtimo.repository;

import com.ritense.valtimo.helper.CamundaOrderByHelper;
import com.ritense.valtimo.repository.camunda.dto.ChartInstance;
import com.ritense.valtimo.repository.camunda.dto.ChartInstanceSeries;
import com.ritense.valtimo.repository.camunda.dto.InstanceCount;
import com.ritense.valtimo.repository.camunda.dto.InstanceCountChart;
import com.ritense.valtimo.repository.camunda.dto.ProcessInstance;
import com.ritense.valtimo.repository.camunda.dto.Serie;
import com.ritense.valtimo.web.rest.parameters.ProcessVariables;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CamundaReportingRepository {

    private final SqlSession session;
    private final RepositoryService repositoryService;

    public InstanceCountChart getProcessInstanceCounts(String processDefinitionKey) {
        Map<String, Object> parameters = new HashMap<>();
        LocalDate defaultFromDate = LocalDate.now().minusDays(14);
        Instant instant = defaultFromDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        parameters.put("dateFrom", Date.from(instant));

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
            .active()
            .latestVersion();
        if (Optional.ofNullable(processDefinitionKey).isPresent()) {
            processDefinitionQuery.processDefinitionKey(processDefinitionKey);
        }
        List<ProcessDefinition> deploydDefinitions = processDefinitionQuery.list();
        List<InstanceCount> instanceCounts = session.selectList("com.ritense.valtimo.mapper.getInstanceCount", parameters);
        return new InstanceCountChart(deploydDefinitions, instanceCounts);
    }

    @Deprecated
    public Long searchInstancesCount(
        String processDefinitionName,
        String searchStatus,
        Boolean active,
        LocalDate fromDate,
        LocalDate toDate,
        Integer duration,
        ProcessVariables processVariables,
        String businessKey
    ) {
        var parameters = new ProcessInstanceQueryParameters()
            .processDefinitionName(processDefinitionName)
            .searchStatus(searchStatus)
            .active(active)
            .fromDate(fromDate)
            .toDate(toDate)
            .duration(duration)
            .businessKey(businessKey)
            .processVariables(processVariables);

        ListQueryParameterObject queryParameterObject = new ListQueryParameterObject();
        queryParameterObject.setParameter(parameters.createParameters());

        return session.selectOne("com.ritense.valtimo.mapper.searchInstancesCount", queryParameterObject);
    }

    @Deprecated
    public Long searchInstancesCount(String processDefinitionId) {
        var parameters = new ProcessInstanceQueryParameters()
            .processDefinitionId(processDefinitionId);
        ListQueryParameterObject queryParameterObject = new ListQueryParameterObject();
        queryParameterObject.setParameter(parameters.createParameters());
        return session.selectOne("com.ritense.valtimo.mapper.searchInstancesCount", queryParameterObject);
    }

    @Deprecated
    public Page<ProcessInstance> searchInstances(
        String processDefinitionName,
        String searchStatus,
        Boolean active,
        LocalDate fromDate,
        LocalDate toDate,
        Integer duration,
        Pageable pageable,
        ProcessVariables processVariables,
        String businessKey
    ) {
        var parameters = new ProcessInstanceQueryParameters()
            .processDefinitionName(processDefinitionName)
            .searchStatus(searchStatus)
            .active(active)
            .fromDate(fromDate)
            .toDate(toDate)
            .duration(duration)
            .businessKey(businessKey)
            .processVariables(processVariables);

        var query = new ListQueryParameterObject(
            parameters.createParameters(),
            pageable.getPageNumber() * pageable.getPageSize(),
            pageable.getPageSize()
        );
        query.setOrderingProperties(CamundaOrderByHelper.sortToOrders("HistoricProcessInstance", pageable.getSort()));
        List<ProcessInstance> processInstances = session.selectList("com.ritense.valtimo.mapper.searchInstances", query);
        Long processInstanceCount = session.selectOne("com.ritense.valtimo.mapper.searchInstancesCount", query);
        return new PageImpl<>(processInstances, pageable, processInstanceCount);
    }

    @Deprecated
    private class ProcessInstanceQueryParameters {
        private String processDefinitionName;
        private String searchStatus;
        private Boolean active;
        private LocalDate fromDate;
        private LocalDate toDate;
        private Integer duration;
        private String processDefinitionId;
        private ProcessVariables processVariables;
        private String businessKey;

        public ProcessInstanceQueryParameters businessKey(String businessKey) {
            this.businessKey = businessKey;
            return this;
        }

        public ProcessInstanceQueryParameters processDefinitionName(String processDefinitionName) {
            this.processDefinitionName = processDefinitionName;
            return this;
        }

        public ProcessInstanceQueryParameters searchStatus(String searchStatus) {
            this.searchStatus = searchStatus;
            return this;
        }

        public ProcessInstanceQueryParameters active(Boolean active) {
            this.active = active;
            return this;
        }

        public ProcessInstanceQueryParameters fromDate(LocalDate fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public ProcessInstanceQueryParameters toDate(LocalDate toDate) {
            this.toDate = toDate;
            return this;
        }

        public ProcessInstanceQueryParameters duration(Integer duration) {
            this.duration = duration;
            return this;
        }


        public ProcessInstanceQueryParameters processDefinitionId(String processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
            return this;
        }

        public ProcessInstanceQueryParameters processVariables(ProcessVariables processVariables) {
            this.processVariables = processVariables;
            return this;
        }

        public Map<String, Object> createParameters() {
            Map<String, Object> parameters = new HashMap<>();
            if (StringUtils.isNotBlank(searchStatus)) {
                parameters.put("searchStatus", searchStatus);
            }
            if (Optional.ofNullable(active).isPresent()) {
                parameters.put("active", active);
            }
            if (fromDate != null) {
                parameters.put("fromDate", Date.valueOf(fromDate));
            }
            if (toDate != null) {
                parameters.put("toDate", Date.valueOf(toDate));
            }
            if (Optional.ofNullable(duration).isPresent()) {
                LocalDate dayinPast = LocalDate.now().minusDays(duration);
                parameters.put("dayinPast", Date.valueOf(dayinPast));
            }
            if (Optional.ofNullable(processDefinitionName).isPresent()) {
                parameters.put("processDefinitionName", processDefinitionName);
            }
            if (Optional.ofNullable(processDefinitionId).isPresent()) {
                parameters.put("processDefinitionId", processDefinitionId);
            }
            if (processVariables != null && processVariables.getVariables() != null && processVariables.getVariables().size() > 0) {
                // Create like params
                Map<String, String> variableParameters = new HashMap<>();
                processVariables.getVariables()
                    .entrySet()
                    .forEach(x -> variableParameters.put(x.getKey(), '%' + x.getValue().toUpperCase() + '%'));
                parameters.put("variables", variableParameters);
            }
            if (Optional.ofNullable(businessKey).isPresent()) {
                parameters.put("businessKey", businessKey);
            }

            return parameters;
        }

    }

    public ChartInstance getTasksPerRole(String processDefinitionKey, LocalDate begin, LocalDate end) {
        Map<String, Object> parameters = new HashMap<>();
        if (Optional.ofNullable(processDefinitionKey).isPresent()) {
            parameters.put("processDefinitionKey", processDefinitionKey);
        }
        if (begin != null && end != null) {
            parameters.put("begin", Date.valueOf(begin));
            parameters.put("end", Date.valueOf(end));
        }
        List<Serie> series = session.selectList("com.ritense.valtimo.mapper.pendingTasksPerRole", parameters);
        return mapQuery(series, "byRoleLabel");
    }

    private ChartInstance mapQuery(List<Serie> series, String name) {
        List<String> categories = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        for (Serie serie : series) {
            categories.add(serie.getName());
            data.add(serie.getCount());
        }
        ChartInstanceSeries ics = new ChartInstanceSeries(name, data);
        HashMap<String, ChartInstanceSeries> map = new HashMap<>();
        map.put("Tasks", ics);
        return new ChartInstance(categories, map);
    }

}