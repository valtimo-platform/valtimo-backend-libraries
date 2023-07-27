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

package com.ritense.valtimo.repository;

import com.ritense.authorization.AuthorizationContext;
import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition;
import com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.repository.camunda.dto.ChartInstance;
import com.ritense.valtimo.repository.camunda.dto.ChartInstanceSeries;
import com.ritense.valtimo.repository.camunda.dto.InstanceCount;
import com.ritense.valtimo.repository.camunda.dto.InstanceCountChart;
import com.ritense.valtimo.repository.camunda.dto.Serie;
import org.apache.ibatis.session.SqlSession;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byKey;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byLatestVersion;

public class CamundaReportingRepository {

    private final SqlSession session;
    private final CamundaRepositoryService repositoryService;

    public CamundaReportingRepository(SqlSession session, CamundaRepositoryService repositoryService) {
        this.session = session;
        this.repositoryService = repositoryService;
    }

    public InstanceCountChart getProcessInstanceCounts(String processDefinitionKey) {
        Map<String, Object> parameters = new HashMap<>();
        LocalDate defaultFromDate = LocalDate.now().minusDays(14);
        Instant instant = defaultFromDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        parameters.put("dateFrom", java.util.Date.from(instant));

        var processDefinitionQuery = CamundaProcessDefinitionSpecificationHelper
            .byActive()
            .and(byLatestVersion());
        if (Optional.ofNullable(processDefinitionKey).isPresent()) {
            processDefinitionQuery.and(byKey(processDefinitionKey));
        }
        List<CamundaProcessDefinition> deploydDefinitions = AuthorizationContext
            .runWithoutAuthorization(() -> repositoryService.findProcessDefinitions(processDefinitionQuery));
        List<InstanceCount> instanceCounts = session.selectList("com.ritense.valtimo.mapper.getInstanceCount", parameters);
        return new InstanceCountChart(deploydDefinitions, instanceCounts);
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