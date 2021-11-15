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

import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.viewconfigurator.web.rest.view.Views;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
@DiscriminatorValue(value = "taskView")
public class TaskView extends View {

    @Column(name = "task_id")
    private String taskId;

    @JsonView(Views.ViewConfig.class)
    @Column(name = "task_view_type")
    private TaskViewType type;

    private TaskView() {
        super();
    }

    public TaskView(String taskId, TaskViewType taskViewType) {
        Objects.requireNonNull(taskId, "taskId cannot be null");
        if (taskId.isEmpty()) {
            throw new IllegalArgumentException("taskId must be provided");
        }

        Objects.requireNonNull(taskViewType, "type cannot be null");

        this.taskId = taskId;
        this.type = taskViewType;
    }

    @Override
    protected boolean supportsGroups() {
        return false;
    }

    @Override
    protected boolean equivalentOf(View otherView) {
        if (otherView instanceof TaskView) {
            TaskView otherTaskView = (TaskView)otherView;
            return otherTaskView.getType().equals(this.getType()) && otherTaskView.getTaskId().equals(this.getTaskId());
        }
        return false;
    }

    public String getTaskId() {
        return taskId;
    }

    public TaskViewType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskView)) {
            return false;
        }
        TaskView taskView = (TaskView) o;
        return Objects.equals(getTaskId(), taskView.getTaskId())
            && getType() == taskView.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTaskId(), getType());
    }
}