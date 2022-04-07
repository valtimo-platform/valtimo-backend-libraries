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

package com.ritense.valtimo.helper;

import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.service.BpmnModelService;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.rest.dto.history.HistoricTaskInstanceDto;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static com.ritense.valtimo.helper.DelegateTaskHelper.PUBLIC_TASK_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DelegateTaskHelperTest {

    private static final String TASK_IS_PUBLIC_PROPERTY_VALUE = "true";
    private UserManagementService userManagementService;
    private ActivityHelper activityHelper;
    private BpmnModelService bpmnModelService;
    private CamundaProperty camundaProperty;
    private DelegateTaskHelper delegateTaskHelper;
    private DelegateTask delegateTask;
    private Task task;
    private List<CamundaProperty> camundaProperties;
    private HistoricTaskInstanceDto historicTaskInstanceDto;
    private HistoricTaskInstance historicTaskInstance;
    private org.camunda.bpm.engine.task.Task taskInterface;

    @BeforeEach
    void setUp() {
        userManagementService = mock(UserManagementService.class);
        activityHelper = mock(ActivityHelper.class);
        bpmnModelService = mock(BpmnModelService.class);
        delegateTaskHelper = new DelegateTaskHelper(userManagementService, activityHelper, bpmnModelService);
        camundaProperty = mock(CamundaProperty.class);
        delegateTask = mock(DelegateTask.class);
        task = mock(Task.class);
        camundaProperties = Collections.singletonList(camundaProperty);
        historicTaskInstanceDto = mock(HistoricTaskInstanceDto.class);
        historicTaskInstance = mock(HistoricTaskInstance.class);
        taskInterface = mock(org.camunda.bpm.engine.task.Task.class);
    }

    @Test
    void taskIsNotPublicDelegateTask() {
        //given
        when(camundaProperty.getCamundaValue()).thenReturn(null);
        when(activityHelper.getCamundaProperties(delegateTask, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(camundaProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(delegateTask);

        //then
        assertFalse(isTaskPublic);
    }

    @Test
    void taskIsPublicDelegateTask() {
        //given
        when(camundaProperty.getCamundaValue()).thenReturn(TASK_IS_PUBLIC_PROPERTY_VALUE);
        when(activityHelper.getCamundaProperties(delegateTask, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(camundaProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(delegateTask);

        //then
        assertTrue(isTaskPublic);
    }

    @Test
    void taskIsNotPublicTask() {
        //given
        when(camundaProperty.getCamundaValue()).thenReturn(null);
        when(activityHelper.getCamundaProperties(task, TASK_IS_PUBLIC_PROPERTY_VALUE)).thenReturn(camundaProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(task);

        //then
        assertFalse(isTaskPublic);
    }

    @Test
    void taskIsPublicTask() {
        //given
        when(camundaProperty.getCamundaValue()).thenReturn(TASK_IS_PUBLIC_PROPERTY_VALUE);
        when(activityHelper.getCamundaProperties(task, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(camundaProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(task);

        //then
        assertTrue(isTaskPublic);
    }

    @Test
    void taskIsNotPublicHistoricTaskDtoInstance() {
        //given
        when(camundaProperty.getCamundaValue()).thenReturn(null);
        when(activityHelper.getCamundaProperties(historicTaskInstanceDto, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(camundaProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(historicTaskInstanceDto);

        //then
        assertFalse(isTaskPublic);
    }

    @Test
    void taskIsPublicHistoricTaskDto() {
        //given
        when(camundaProperty.getCamundaValue()).thenReturn(TASK_IS_PUBLIC_PROPERTY_VALUE);
        when(activityHelper.getCamundaProperties(historicTaskInstanceDto, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(camundaProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(historicTaskInstanceDto);

        //then
        assertTrue(isTaskPublic);
    }

    @Test
    void taskIsNotPublicHistoricTaskInstance() {
        //given
        when(camundaProperty.getCamundaValue()).thenReturn(null);
        when(activityHelper.getCamundaProperties(historicTaskInstance, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(camundaProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(historicTaskInstance);

        //then
        assertFalse(isTaskPublic);
    }

    @Test
    void taskIsPublicHistoricTaskInstance() {
        //given
        when(camundaProperty.getCamundaValue()).thenReturn(TASK_IS_PUBLIC_PROPERTY_VALUE);
        when(activityHelper.getCamundaProperties(historicTaskInstance, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(camundaProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(historicTaskInstance);

        //then
        assertTrue(isTaskPublic);
    }

    @Test
    void taskIsNotPublicTaskInterfaceInstance() {
        //given
        when(camundaProperty.getCamundaValue()).thenReturn(null);
        when(activityHelper.getCamundaProperties(task, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(camundaProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(taskInterface);

        //then
        assertFalse(isTaskPublic);
    }

    @Test
    void taskIsPublicTaskInterfaceInstance() {
        //given
        when(camundaProperty.getCamundaValue()).thenReturn(TASK_IS_PUBLIC_PROPERTY_VALUE);
        when(bpmnModelService.getTask(taskInterface)).thenReturn(task);
        when(activityHelper.getCamundaProperties(task, PUBLIC_TASK_PROPERTY_NAME)).thenReturn(camundaProperties);

        //when
        boolean isTaskPublic = delegateTaskHelper.isTaskPublic(taskInterface);

        //then
        assertTrue(isTaskPublic);
    }

}