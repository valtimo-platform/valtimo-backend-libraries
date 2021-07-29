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

package com.ritense.audit;

import com.ritense.audit.domain.AuditRecord;
import com.ritense.audit.domain.MetaData;
import com.ritense.audit.domain.MetaDataBuilder;
import com.ritense.audit.domain.event.TestEvent;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.event.TaskCompletedEvent;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractTestHelper {

    public TestEvent testEvent(String uuid, LocalDateTime occurredOn) {
        return new TestEvent(UUID.fromString(uuid), "somewhere", occurredOn, "somebody", "John Doe", 21, "USA", "M", "myProcessInstanceId");
    }

    public TestEvent testEvent(LocalDateTime occurredOn) {
        return new TestEvent(UUID.randomUUID(), "somewhere", occurredOn, "somebody", "John Doe", 21, "USA", "M", "myProcessInstanceId");
    }

    public TestEvent testEvent(LocalDateTime occurredOn, String name) {
        return new TestEvent(UUID.randomUUID(), "somewhere", occurredOn, "somebody", name, 21, "USA", "M", "myProcessInstanceId");
    }

    public TaskCompletedEvent taskCompletedEvent(String uuid, LocalDateTime dateTime) {
        return new TaskCompletedEvent(
            UUID.fromString(uuid),
            "somewhere",
            dateTime,
            "somebody",
            "test@test.com",
            dateTime,
            "taskId",
            "aTaskName",
            "aProcessDefinitionId",
            "aProcessInstanceId", Map.of("Key", "Value"),
            null
        );
    }

    public AuditRecord auditRecord(AuditEvent event, MetaData metaData) {
        return AuditRecord.builder()
            .id(event.getId())
            .metaData(metaData)
            .auditEvent(event)
            .build();
    }

    public MetaData metaData(AuditEvent event) {
        return new MetaDataBuilder()
            .origin(event.getOrigin())
            .occurredOn(event.getOccurredOn())
            .user(event.getUser())
            .build();
    }
}
