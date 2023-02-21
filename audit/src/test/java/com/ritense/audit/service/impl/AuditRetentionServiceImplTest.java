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

package com.ritense.audit.service.impl;

import com.ritense.audit.service.AuditRetentionService;
import com.ritense.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AuditRetentionServiceImplTest {

    private AuditRetentionService auditRetentionService;
    private AuditService auditService;

    @BeforeEach
    public void setUp() {
        auditService = mock(AuditServiceImpl.class);
        auditRetentionService = new AuditRetentionServiceImpl(auditService, 5);
    }

    @Test
    public void shouldCleanUpAuditEvents() {
        auditRetentionService.cleanup();
        verify(auditService, times(1)).deleteAllBefore(any(LocalDateTime.class));
    }

}