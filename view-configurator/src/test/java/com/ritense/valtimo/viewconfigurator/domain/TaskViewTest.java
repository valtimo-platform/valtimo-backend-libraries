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

package com.ritense.valtimo.viewconfigurator.domain;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TaskViewTest {

    @Test
    public void createTaskViewDetail() {
        final String taskId = taskId();

        TaskView taskViewDetail = new TaskView(taskId, TaskViewType.DETAIL);

        assertEquals(taskId, taskViewDetail.getTaskId());
        assertEquals(TaskViewType.DETAIL, taskViewDetail.getType());
        assertEquals(0, taskViewDetail.getSelectedProcessDefinitionVariables().size());
    }

    @Test
    public void createTaskViewList() {
        final String taskId = taskId();

        TaskView taskViewList = new TaskView(taskId, TaskViewType.LIST);

        assertEquals(taskId, taskViewList.getTaskId());
        assertEquals(TaskViewType.LIST, taskViewList.getType());
        assertEquals(0, taskViewList.getSelectedProcessDefinitionVariables().size());
    }

    @Test
    public void createTaskViewWithoutTaskIdShouldFail() {
        assertThrows(NullPointerException.class, () -> new TaskView(null, TaskViewType.LIST));
    }

    @Test
    public void createTaskViewWithEmptyTaskIdShouldFail() {
        final String taskId = StringUtils.EMPTY;
        assertThrows(IllegalArgumentException.class, () -> new TaskView(taskId, TaskViewType.LIST));
    }

    @Test
    public void createTaskViewWithoutViewTypeShouldFail() {
        final String taskId = taskId();
        assertThrows(NullPointerException.class, () -> new TaskView(taskId, null));
    }

    private String taskId() {
        return UUID.randomUUID().toString();
    }

}