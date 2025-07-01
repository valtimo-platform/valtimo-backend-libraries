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

package com.ritense.valtimo.operaton.processaudit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProcessInstanceEventTest {

    @Test
    public void shouldReturnNullDocumentIdOnNullBusinessKey() {
        ProcessInstanceEvent event = new ProcessInstanceEvent(
                UUID.randomUUID(),
                "origin",
                LocalDateTime.now(),
                "user",
                "definitionId",
                UUID.randomUUID().toString(),
                null
        ) {
        };

        assertThat(event.getDocumentId()).isNull();
    }

    @Test
    public void shouldReturnNullDocumentIdOnInvalidBusinessKey() {
        ProcessInstanceEvent event = new ProcessInstanceEvent(
                UUID.randomUUID(),
                "origin",
                LocalDateTime.now(),
                "user",
                "definitionId",
                UUID.randomUUID().toString(),
                "invalid"
        ) {
        };

        assertThat(event.getDocumentId()).isNull();
    }

    @Test
    public void shouldReturnDocumentIdOnValidBusinessKey() {
        UUID businessKey = UUID.randomUUID();
        ProcessInstanceEvent event = new ProcessInstanceEvent(
                UUID.randomUUID(),
                "origin",
                LocalDateTime.now(),
                "user",
                "definitionId",
                UUID.randomUUID().toString(),
                businessKey.toString()
        ) {
        };

        assertThat(event.getDocumentId()).isEqualTo(businessKey);
    }

}