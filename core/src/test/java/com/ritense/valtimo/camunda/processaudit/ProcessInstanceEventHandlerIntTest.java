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

package com.ritense.valtimo.camunda.processaudit;

import com.ritense.valtimo.BaseIntegrationTest;
import com.ritense.valtimo.contract.audit.AuditEvent;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.event.EventListener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@Tag("integration")
class ProcessInstanceEventHandlerIntTest extends BaseIntegrationTest {

    @Autowired
    private RuntimeService runtimeService;

    @MockBean
    private AuditEventListener auditEventListener;

    @Test
    @Disabled
    void shouldNotFindSearchMatch() {
        runtimeService.startProcessInstanceByKey("test-process");

        verify(auditEventListener).handle(any(ProcessStartedEvent.class));
        verify(auditEventListener).handle(any(ProcessEndedEvent.class));
    }

    public interface AuditEventListener {
        @EventListener(classes = AuditEvent.class)
        void handle(AuditEvent auditEvent);
    }

}
