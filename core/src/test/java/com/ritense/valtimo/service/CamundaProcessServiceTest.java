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

package com.ritense.valtimo.service;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

class CamundaProcessServiceTest {

    private static final String userMock = "user";
    private HistoricProcessInstanceEntity latestProcessInstance;
    private HistoricProcessInstanceEntity middleProcessInstance;
    private HistoricProcessInstanceEntity oldestProcessInstance;

    private static final Date FIRST_OF_JANUARY_2018 = getDate(1, 1, 2018);
    private static final Date FIRST_OF_JANUARY_2017 = getDate(1, 1, 2017);
    private static final Date FIRST_OF_JANUARY_2016 = getDate(1, 1, 2016);

    private static final String BUSINESSKEY1 = "businessKey1";
    private static final String BUSINESSKEY2 = "businessKey2";
    private static final String BUSINESSKEY3 = "businessKey3";

    private CamundaProcessService camundaProcessService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private FormService formService;

    private HistoryService historyService = Mockito.mock(HistoryService.class, RETURNS_DEEP_STUBS);

    @Test
    void getAllActiveContextProcessesStartedByCurrentUserTestExpectAll() {
        camundaProcessService = new CamundaProcessService(runtimeService, repositoryService, formService, historyService);

        //when
        when(historyService.createHistoricProcessInstanceQuery()
            .startedBy(any())
            .unfinished()
            .list())
            .thenReturn(getHistoricProcessInstances());

        //method call
        List<HistoricProcessInstance> allActiveContextProcessesStartedByCurrentUser =
                camundaProcessService.getAllActiveContextProcessesStartedByCurrentUser(contextProcessesTest1(), userMock);
        //assert
        assertThat(allActiveContextProcessesStartedByCurrentUser, hasSize(3));
        assertThat(allActiveContextProcessesStartedByCurrentUser, contains(latestProcessInstance, middleProcessInstance, oldestProcessInstance));
        assertThat(allActiveContextProcessesStartedByCurrentUser, hasItem(hasProperty("businessKey", is("businessKey1"))));
        assertThat(allActiveContextProcessesStartedByCurrentUser, hasItem(
                both(withBusinessKey(BUSINESSKEY1))
                .and(withStartTime(FIRST_OF_JANUARY_2018))
        ));
        assertThat(allActiveContextProcessesStartedByCurrentUser, hasItem(
                both(withBusinessKey(BUSINESSKEY2))
                .and(withStartTime(FIRST_OF_JANUARY_2017))
        ));
        assertThat(allActiveContextProcessesStartedByCurrentUser, hasItem(
                both(withBusinessKey(BUSINESSKEY3))
                .and(withStartTime(FIRST_OF_JANUARY_2016))
        ));
    }

    @Test
    void getAllActiveContextProcessesStartedByCurrentUserTestExpectTwo() {
        camundaProcessService = new CamundaProcessService(runtimeService, repositoryService, formService, historyService);

        //when
        when(historyService.createHistoricProcessInstanceQuery()
            .startedBy(any())
            .unfinished()
            .list())
            .thenReturn(getHistoricProcessInstances());

        //method call
        List<HistoricProcessInstance> allActiveContextProcessesStartedByCurrentUser =
                camundaProcessService.getAllActiveContextProcessesStartedByCurrentUser(contextProcessesTest2(), userMock);
        //assert
        assertThat(allActiveContextProcessesStartedByCurrentUser, hasSize(2));
        assertThat(allActiveContextProcessesStartedByCurrentUser, contains(latestProcessInstance, middleProcessInstance));
        assertThat(allActiveContextProcessesStartedByCurrentUser, hasItem(
                both(withBusinessKey(BUSINESSKEY1))
                .and(withStartTime(FIRST_OF_JANUARY_2018))
        ));
        assertThat(allActiveContextProcessesStartedByCurrentUser, hasItem(
                both(withBusinessKey(BUSINESSKEY2))
                .and(withStartTime(FIRST_OF_JANUARY_2017))
        ));

    }

    private List<HistoricProcessInstance> getHistoricProcessInstances() {
        latestProcessInstance = new HistoricProcessInstanceEntity();
        latestProcessInstance.setBusinessKey(BUSINESSKEY1);
        latestProcessInstance.setProcessDefinitionName("processDefName1");
        latestProcessInstance.setProcessInstanceId("processInstanceId1");
        latestProcessInstance.setProcessDefinitionKey("testprocess1");
        latestProcessInstance.setStartTime(FIRST_OF_JANUARY_2018);

        middleProcessInstance = new HistoricProcessInstanceEntity();
        middleProcessInstance.setBusinessKey(BUSINESSKEY2);
        middleProcessInstance.setProcessDefinitionName("processDefName2");
        middleProcessInstance.setProcessInstanceId("processInstanceId2");
        middleProcessInstance.setProcessDefinitionKey("testprocess2");
        middleProcessInstance.setStartTime(FIRST_OF_JANUARY_2017);

        oldestProcessInstance = new HistoricProcessInstanceEntity();
        oldestProcessInstance.setBusinessKey(BUSINESSKEY3);
        oldestProcessInstance.setProcessDefinitionName("processDefName3");
        oldestProcessInstance.setProcessInstanceId("processInstanceId2");
        oldestProcessInstance.setProcessDefinitionKey("testprocess3");
        oldestProcessInstance.setStartTime(FIRST_OF_JANUARY_2016);

        List<HistoricProcessInstance> historicProcessInstances = new ArrayList<>();

        historicProcessInstances.add(latestProcessInstance);
        historicProcessInstances.add(middleProcessInstance);
        historicProcessInstances.add(oldestProcessInstance);

        return historicProcessInstances;
    }

    private Set<String> contextProcessesTest1() {
        Set<String> processes = new HashSet<>();
        processes.add("testprocess1");
        processes.add("testprocess2");
        processes.add("testprocess3");

        return processes;
    }

    private Set<String> contextProcessesTest2() {
        Set<String> processes = new HashSet<>();
        processes.add("testprocess1");
        processes.add("testprocess2");
        processes.add("testprocess4");

        return processes;
    }

    private static Date getDate(int year, int month, int date) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, date);
        return cal.getTime();
    }

    private Matcher<Object> withBusinessKey(String businessKey) {
        return hasProperty("businessKey", IsEqual.equalTo(businessKey));
    }

    private Matcher<Object> withStartTime(Date date) {
        return hasProperty("startTime", IsEqual.equalTo(date));
    }
}