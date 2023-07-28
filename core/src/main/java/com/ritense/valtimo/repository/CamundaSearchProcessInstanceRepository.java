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

import com.ritense.valtimo.helper.CamundaOrderByHelper;
import com.ritense.valtimo.repository.camunda.dto.ProcessInstance;
import com.ritense.valtimo.repository.queryparameter.ProcessInstanceQueryParametersV2;
import com.ritense.valtimo.web.rest.dto.ProcessInstanceSearchDTO;
import org.apache.ibatis.session.SqlSession;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;

public class CamundaSearchProcessInstanceRepository {

    private final SqlSession session;
    private final static String SEARCH_INSTANCE_COUNT_STATEMENT = "com.ritense.valtimo.camunda.processinstance.searchInstancesCount";
    private final static String SEARCH_INSTANCES_STATEMENT = "com.ritense.valtimo.camunda.processinstance.searchInstances";

    public CamundaSearchProcessInstanceRepository(SqlSession session) {
        this.session = session;
    }

    public Long searchInstancesCountByDefinitionId(String processDefinitionId, ProcessInstanceSearchDTO processInstanceSearchDTO) {
        ProcessInstanceQueryParametersV2 processInstanceQueryParametersV2 = new ProcessInstanceQueryParametersV2()
            .processDefinitionId(processDefinitionId)
            .processVariables(processInstanceSearchDTO.getProcessVariables());

        ListQueryParameterObject queryParameterObject = new ListQueryParameterObject();
        queryParameterObject.setParameter(processInstanceQueryParametersV2.createParameters());

        return session.selectOne(SEARCH_INSTANCE_COUNT_STATEMENT, queryParameterObject);
    }

    public Long searchInstancesCountByDefinitionName(String processDefinitionName, ProcessInstanceSearchDTO processInstanceSearchDTO) {
        ProcessInstanceQueryParametersV2 processInstanceQueryParametersV2 = new ProcessInstanceQueryParametersV2()
            .processDefinitionName(processDefinitionName)
            .processVariables(processInstanceSearchDTO.getProcessVariables());

        ListQueryParameterObject queryParameterObject = new ListQueryParameterObject();
        queryParameterObject.setParameter(processInstanceQueryParametersV2.createParameters());

        return session.selectOne(SEARCH_INSTANCE_COUNT_STATEMENT, queryParameterObject);
    }

    public Page<ProcessInstance> searchInstances(String processDefinitionName, ProcessInstanceSearchDTO processInstanceSearchDTO, Pageable pageable) {
        var processInstanceQueryParametersV2 = new ProcessInstanceQueryParametersV2()
            .processDefinitionName(processDefinitionName)
            .processVariables(processInstanceSearchDTO.getProcessVariables());

        var query = new ListQueryParameterObject(
            processInstanceQueryParametersV2.createParameters(),
            pageable.getPageNumber() * pageable.getPageSize(),
            pageable.getPageSize()
        );
        query.setOrderingProperties(CamundaOrderByHelper.sortToOrders("HistoricProcessInstance", pageable.getSort()));
        List<ProcessInstance> processInstances = session.selectList(SEARCH_INSTANCES_STATEMENT, query);
        Long processInstanceCount = session.selectOne(SEARCH_INSTANCE_COUNT_STATEMENT, query);

        return new PageImpl<>(processInstances, pageable, processInstanceCount);
    }

}
