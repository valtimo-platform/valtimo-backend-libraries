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

package com.ritense.valtimo.service;

import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.AuthorizationService;
import com.ritense.valtimo.camunda.domain.CamundaHistoricProcessInstance;
import com.ritense.valtimo.camunda.service.CamundaHistoryService;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.camunda.service.CamundaRuntimeService;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CamundaProcessServiceTest {

    private static final String userMock = "user";
    private CamundaHistoricProcessInstance latestProcessInstance;
    private CamundaHistoricProcessInstance middleProcessInstance;
    private CamundaHistoricProcessInstance oldestProcessInstance;

    private static final LocalDateTime FIRST_OF_JANUARY_2018 = getDate(2018,1, 1);
    private static final LocalDateTime FIRST_OF_JANUARY_2017 = getDate(2017,1, 1);
    private static final LocalDateTime FIRST_OF_JANUARY_2016 = getDate(2016,1, 1);

    private static final String BUSINESSKEY1 = "businessKey1";
    private static final String BUSINESSKEY2 = "businessKey2";
    private static final String BUSINESSKEY3 = "businessKey3";

    private CamundaProcessService camundaProcessService;

    @Mock
    private RuntimeService runtimeService = mock(RuntimeService.class, RETURNS_DEEP_STUBS);

    @Mock
    private CamundaRuntimeService camundaRuntimeService;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private CamundaRepositoryService camundaRepositoryService;

    @Mock
    private ProcessPropertyService processPropertyService;

    @Mock
    private ValtimoProperties valtimoProperties;

    @Mock
    private FormService formService;

    @Mock
    private AuthorizationService authorizationService;

    private CamundaHistoryService historyService = mock(CamundaHistoryService.class, RETURNS_DEEP_STUBS);

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllActiveContextProcessesStartedByCurrentUserTestExpectAll() {
        camundaProcessService = new CamundaProcessService(runtimeService, camundaRuntimeService, repositoryService, camundaRepositoryService, formService, historyService, processPropertyService, valtimoProperties, authorizationService);

        //when
        when(historyService.findHistoricProcessInstances(any()))
            .thenReturn(getHistoricProcessInstances());

        //method call
        var allActiveContextProcessesStartedByCurrentUser =
            AuthorizationContext.runWithoutAuthorization(
                () -> camundaProcessService
                    .getAllActiveContextProcessesStartedByCurrentUser(contextProcessesTest1(), userMock)
            );
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
        camundaProcessService = new CamundaProcessService(runtimeService, camundaRuntimeService, repositoryService, camundaRepositoryService, formService, historyService, processPropertyService, valtimoProperties, authorizationService);

        //when
        when(historyService.findHistoricProcessInstances(any()))
            .thenReturn(getHistoricProcessInstances());

        //method call
        var allActiveContextProcessesStartedByCurrentUser = AuthorizationContext.runWithoutAuthorization(() ->
            camundaProcessService.getAllActiveContextProcessesStartedByCurrentUser(contextProcessesTest2(), userMock));
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

    private List<CamundaHistoricProcessInstance> getHistoricProcessInstances() {
        latestProcessInstance = new CamundaHistoricProcessInstance(
            UUID.randomUUID().toString(),
            null,
            BUSINESSKEY1,
            "testprocess1",
            null,
            FIRST_OF_JANUARY_2018,
            null,null,null,null,null,null,null,null,null,null,null,null,null
        );

        middleProcessInstance = new CamundaHistoricProcessInstance(
            UUID.randomUUID().toString(),
            null,
            BUSINESSKEY2,
            "testprocess2",
            null,
            FIRST_OF_JANUARY_2017,
            null,null,null,null,null,null,null,null,null,null,null,null,null
        );

        oldestProcessInstance = new CamundaHistoricProcessInstance(
            UUID.randomUUID().toString(),
            null,
            BUSINESSKEY3,
            "testprocess3",
            null,
            FIRST_OF_JANUARY_2016,
            null,null,null,null,null,null,null,null,null,null,null,null,null
        );

        List<CamundaHistoricProcessInstance> historicProcessInstances = new ArrayList<>();

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

    private static LocalDateTime getDate(int year, int month, int date) {
        return LocalDate.of(year, month, date).atStartOfDay();
    }

    private Matcher<Object> withBusinessKey(String businessKey) {
        return hasProperty("businessKey", IsEqual.equalTo(businessKey));
    }

    private Matcher<Object> withStartTime(LocalDateTime date) {
        return hasProperty("startTime", IsEqual.equalTo(date));
    }
}