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

package com.ritense.valtimo.helper;

import static com.ritense.valtimo.helper.DelegateTaskHelper.PUBLIC_TASK_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.operaton.domain.OperatonTask;
import com.ritense.valtimo.service.BpmnModelService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.delegate.DelegateTask;
import org.operaton.bpm.engine.history.HistoricTaskInstance;
import org.operaton.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.operaton.bpm.model.bpmn.instance.Task;
import org.operaton.bpm.model.bpmn.instance.operaton.OperatonProperty;

class DelegateTaskHelperTest {

    private static final String TASK_IS_PUBLIC_PROPERTY_VALUE = "true";
    private UserManagementService userManagementService;
    private ActivityHelper activityHelper;
    private BpmnModelService bpmnModelService;
    private OperatonProperty operatonProperty;
    private DelegateTaskHelper delegateTaskHelper;
    private DelegateTask delegateTask;
    private Task task;
    private List<OperatonProperty> operatonProperties;
    private HistoricTaskInstanceDto historicTaskInstanceDto;
    private HistoricTaskInstance historicTaskInstance;
    private OperatonTask taskInterface;

    @BeforeEach
    void setUp() {
        userManagementService = mock(UserManagementService.class);
        activityHelper = mock(ActivityHelper.class);
        bpmnModelService = mock(BpmnModelService.class);
        delegateTaskHelper = new DelegateTaskHelper(userManagementService, activityHelper, bpmnModelService);
        operatonProperty = mock(OperatonProperty.class);
        delegateTask = mock(DelegateTask.class);
        task = mock(Task.class);
        operatonProperties = Collections.singletonList(operatonProperty);
        historicTaskInstanceDto = mock(HistoricTaskInstanceDto.class);
        historicTaskInstance = mock(HistoricTaskInstance.class);
        taskInterface = mock(OperatonTask.class);
    }

    @Test
    void taskIsNotPublicDelegateTask() {
        //given
        when(operatonProperty.getOperatonValue()).thenReturn(null);
        when(activityHelper.getOperatonProperties(delegateTask, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(operatonProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(delegateTask);

        //then
        assertFalse(isTaskPublic);
    }

    @Test
    void taskIsPublicDelegateTask() {
        //given
        when(operatonProperty.getOperatonValue()).thenReturn(TASK_IS_PUBLIC_PROPERTY_VALUE);
        when(activityHelper.getOperatonProperties(delegateTask, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(operatonProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(delegateTask);

        //then
        assertTrue(isTaskPublic);
    }

    @Test
    void taskIsNotPublicTask() {
        //given
        when(operatonProperty.getOperatonValue()).thenReturn(null);
        when(activityHelper.getOperatonProperties(task, TASK_IS_PUBLIC_PROPERTY_VALUE)).thenReturn(operatonProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(task);

        //then
        assertFalse(isTaskPublic);
    }

    @Test
    void taskIsPublicTask() {
        //given
        when(operatonProperty.getOperatonValue()).thenReturn(TASK_IS_PUBLIC_PROPERTY_VALUE);
        when(activityHelper.getOperatonProperties(task, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(operatonProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(task);

        //then
        assertTrue(isTaskPublic);
    }

    @Test
    void taskIsNotPublicHistoricTaskDtoInstance() {
        //given
        when(operatonProperty.getOperatonValue()).thenReturn(null);
        when(activityHelper.getOperatonProperties(historicTaskInstanceDto, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(operatonProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(historicTaskInstanceDto);

        //then
        assertFalse(isTaskPublic);
    }

    @Test
    void taskIsPublicHistoricTaskDto() {
        //given
        when(operatonProperty.getOperatonValue()).thenReturn(TASK_IS_PUBLIC_PROPERTY_VALUE);
        when(activityHelper.getOperatonProperties(historicTaskInstanceDto, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(operatonProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(historicTaskInstanceDto);

        //then
        assertTrue(isTaskPublic);
    }

    @Test
    void taskIsNotPublicHistoricTaskInstance() {
        //given
        when(operatonProperty.getOperatonValue()).thenReturn(null);
        when(activityHelper.getOperatonProperties(historicTaskInstance, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(operatonProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(historicTaskInstance);

        //then
        assertFalse(isTaskPublic);
    }

    @Test
    void taskIsPublicHistoricTaskInstance() {
        //given
        when(operatonProperty.getOperatonValue()).thenReturn(TASK_IS_PUBLIC_PROPERTY_VALUE);
        when(activityHelper.getOperatonProperties(historicTaskInstance, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(operatonProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(historicTaskInstance);

        //then
        assertTrue(isTaskPublic);
    }

    @Test
    void taskIsNotPublicTaskInterfaceInstance() {
        //given
        when(operatonProperty.getOperatonValue()).thenReturn(null);
        when(activityHelper.getOperatonProperties(task, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(operatonProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(taskInterface);

        //then
        assertFalse(isTaskPublic);
    }

    @Test
    void taskIsPublicTaskInterfaceInstance() {
        //given
        when(operatonProperty.getOperatonValue()).thenReturn(TASK_IS_PUBLIC_PROPERTY_VALUE);
        when(bpmnModelService.getTask(taskInterface)).thenReturn(task);
        when(activityHelper.getOperatonProperties(task, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(operatonProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(taskInterface);

        //then
        assertTrue(isTaskPublic);
    }

}