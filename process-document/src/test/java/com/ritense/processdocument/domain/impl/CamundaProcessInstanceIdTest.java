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

package com.ritense.processdocument.domain.impl;

import com.ritense.processdocument.domain.ProcessInstanceId;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CamundaProcessInstanceIdTest {

    private static final String PROCESS_INSTANCE_ID = UUID.randomUUID().toString();
    private ExecutionEntity execution;

    @BeforeEach
    public void setUp() {
        execution = mock(ExecutionEntity.class, RETURNS_DEEP_STUBS);
        when(execution.getProcessInstanceId()).thenReturn(PROCESS_INSTANCE_ID);
    }

    @Test
    public void shouldConstructFromExecution() {
        final CamundaProcessInstanceId camundaProcessInstanceId = ProcessInstanceId.fromExecution(execution, CamundaProcessInstanceId.class);
        assertThat(camundaProcessInstanceId).isNotNull();
        assertThat(camundaProcessInstanceId.toString()).isEqualTo(PROCESS_INSTANCE_ID);
    }

}