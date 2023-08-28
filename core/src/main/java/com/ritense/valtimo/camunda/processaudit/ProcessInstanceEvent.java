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

package com.ritense.valtimo.camunda.processaudit;

import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class ProcessInstanceEvent extends AuditMetaData implements AuditEvent {

    private final String processDefinitionId;
    private final String processInstanceId;
    private final String businessKey;

    public ProcessInstanceEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        String processDefinitionId,
        String processInstanceId,
        String businessKey
    ) {
        super(id, origin, occurredOn, user);
        this.processDefinitionId = processDefinitionId;
        this.processInstanceId = processInstanceId;
        this.businessKey = businessKey;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getBusinessKey() {
        return businessKey;
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
        if (!(o instanceof ProcessInstanceEvent)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ProcessInstanceEvent that = (ProcessInstanceEvent) o;
        return Objects.equals(processDefinitionId, that.processDefinitionId)
            && Objects.equals(processInstanceId, that.processInstanceId)
            && Objects.equals(businessKey, that.businessKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), processDefinitionId, processInstanceId, businessKey);
    }
}
