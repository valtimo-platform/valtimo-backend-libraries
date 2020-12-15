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

package com.ritense.valtimo.contract.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import com.ritense.valtimo.contract.audit.ProcessIdentity;
import com.ritense.valtimo.contract.audit.TaskIdentity;
import com.ritense.valtimo.contract.audit.TaskMetaData;
import com.ritense.valtimo.contract.audit.VariableScope;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class TaskCompletedEvent extends AuditMetaData
    implements AuditEvent, TaskIdentity, TaskMetaData, ProcessIdentity, VariableScope {

    private String assignee;
    private LocalDateTime createdOn;
    private String taskId;
    private String taskName;
    private String processDefinitionId;
    private String processInstanceId;
    private Map<String, Object> variables;
    private String businessKey;

    @JsonCreator
    public TaskCompletedEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        String assignee,
        LocalDateTime createdOn,
        String taskId,
        String taskName,
        String processDefinitionId,
        String processInstanceId,
        Map<String, Object> variables,
        String businessKey
    ) {
        super(id, origin, occurredOn, user);
        assertArgumentNotNull(createdOn, "createdOn is required");
        assertArgumentNotNull(taskId, "taskId is required");
        assertArgumentNotNull(taskName, "taskName is required");
        assertArgumentNotNull(processDefinitionId, "processDefinitionId is required");
        assertArgumentNotNull(processInstanceId, "processInstanceId is required");
        this.assignee = assignee;
        this.createdOn = createdOn;
        this.taskId = taskId;
        this.taskName = taskName;
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
        this.variables = variables;

        if (businessKey != null) {
            assertArgumentNotEmpty(businessKey, "businessKey cannot be empty");
            this.businessKey = businessKey;
        }
    }

    @Override
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    @Override
    public String getAssignee() {
        return assignee;
    }

    @Override
    public LocalDateTime createdOn() {
        return createdOn;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public Map<String, Object> getVariables() {
        return variables;
    }

    @Override
    public String getBusinessKey() {
        return businessKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskCompletedEvent)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        TaskCompletedEvent that = (TaskCompletedEvent) o;

        return taskId.equals(that.taskId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + taskId.hashCode();
        return result;
    }
}