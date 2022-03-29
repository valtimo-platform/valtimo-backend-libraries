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

package com.ritense.audit.service.impl;

import com.ritense.audit.AbstractTestHelper;
import com.ritense.audit.exception.AuditRuntimeException;
import com.ritense.audit.repository.impl.AuditRecordImplRepository;
import com.ritense.audit.service.AuditEventProcessor;
import com.ritense.valtimo.contract.audit.AuditEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AuditEventProcessorImplTest extends AbstractTestHelper {

    private AuditEventProcessor auditEventProcessor;
    private AuditRecordImplRepository auditRecordRepository;

    @BeforeEach
    public void setUp() {
        auditRecordRepository = mock(AuditRecordImplRepository.class);
        auditEventProcessor = new AuditEventProcessorImpl(auditRecordRepository);
    }

    @Test
    public void shouldFailProcessingEventWhenNull() {

        assertThrows(AuditRuntimeException.class, () -> {
            auditEventProcessor.process(null);
        });
        verify(auditRecordRepository, times(0)).save(any());
    }

    @Test
    public void shouldProcessEvent() {
        final AuditEvent testEvent = testEvent(LocalDateTime.now());
        auditEventProcessor.process(testEvent);
        verify(auditRecordRepository).saveAndFlush(any());
    }

}