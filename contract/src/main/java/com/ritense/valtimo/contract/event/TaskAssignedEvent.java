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

package com.ritense.valtimo.contract.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import com.ritense.valtimo.contract.audit.ProcessIdentity;
import com.ritense.valtimo.contract.audit.TaskFormerAssignee;
import com.ritense.valtimo.contract.audit.TaskIdentity;
import com.ritense.valtimo.contract.audit.TaskMetaData;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class TaskAssignedEvent extends AuditMetaData
    implements AuditEvent, TaskIdentity, TaskMetaData, ProcessIdentity, TaskFormerAssignee {

    private String formerAssignee;
    private String assignee;
    private String taskId;
    private String taskName;
    private LocalDateTime taskCreatedOn;
    private String processDefinitionId;
    private String processInstanceId;
    private String businessKey;

    @JsonCreator
    public TaskAssignedEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        String formerAssignee,
        String assignee,
        String taskId,
        String taskName,
        LocalDateTime createdOn,
        String processDefinitionId,
        String processInstanceId,
        String businessKey
    ) {
        super(id, origin, occurredOn, user);
        assertArgumentNotNull(assignee, "assignee is required");
        assertArgumentNotNull(createdOn, "createdOn is required");
        assertArgumentNotNull(taskId, "taskId is required");
        assertArgumentNotNull(taskName, "taskName is required");
        assertArgumentNotNull(processDefinitionId, "processDefinitionId is required");
        assertArgumentNotNull(processInstanceId, "processInstanceId is required");
        this.assignee = assignee;
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskCreatedOn = createdOn;
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
        this.formerAssignee = formerAssignee;
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
    public String getFormerAssignee() {
        return this.formerAssignee;
    }

    @Override
    public String getAssignee() {
        return assignee;
    }

    @Override
    public LocalDateTime createdOn() {
        return taskCreatedOn;
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
    public String getBusinessKey() {
        return this.businessKey;
    }

    @Override
    public UUID getDocumentId() {
        try {
            return UUID.fromString(businessKey);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskAssignedEvent)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TaskAssignedEvent that = (TaskAssignedEvent) o;
        return getTaskId().equals(that.getTaskId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getTaskId());
    }
}
