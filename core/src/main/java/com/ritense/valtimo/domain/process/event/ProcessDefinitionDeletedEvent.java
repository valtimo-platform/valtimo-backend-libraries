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

package com.ritense.valtimo.domain.process.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class ProcessDefinitionDeletedEvent extends AuditMetaData implements AuditEvent {

    private final String processDefinitionKey;

    @JsonCreator
    public ProcessDefinitionDeletedEvent(UUID id, String origin, LocalDateTime occurredOn, String user, String processDefinitionKey) {
        super(id, origin, occurredOn, user);
        assertArgumentNotNull(processDefinitionKey, "process definition deleted id is required");
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProcessDefinitionDeletedEvent)) {
            return false;
        }
        ProcessDefinitionDeletedEvent that = (ProcessDefinitionDeletedEvent) o;

        return super.equals(o) && getProcessDefinitionKey().equals(that.getProcessDefinitionKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getProcessDefinitionKey());
    }

}