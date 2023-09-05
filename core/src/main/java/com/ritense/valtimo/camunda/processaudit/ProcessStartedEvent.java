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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.contract.audit.view.AuditView;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProcessStartedEvent extends ProcessInstanceEvent {

    private final String processDefinitionKey;

    @JsonCreator
    public ProcessStartedEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        String processDefinitionId,
        String processInstanceId,
        String businessKey,
        String processDefinitionKey
    ) {
        super(id, origin, occurredOn, user, processDefinitionId, processInstanceId, businessKey);
        this.processDefinitionKey = processDefinitionKey;
    }

    @JsonIgnore(false)
    @JsonView(AuditView.Public.class)
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }
}
