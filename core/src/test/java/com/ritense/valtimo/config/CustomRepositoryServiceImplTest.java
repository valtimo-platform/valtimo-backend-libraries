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

package com.ritense.valtimo.config;

import com.ritense.valtimo.camunda.repository.CustomRepositoryServiceImpl;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.domain.process.event.ProcessDefinitionDeletedEvent;
import org.camunda.bpm.engine.impl.cmd.GetDeployedProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomRepositoryServiceImplTest {

    private CustomRepositoryServiceImpl customRepositoryServiceImpl;
    private ApplicationEventPublisher applicationEventPublisher;
    private CommandExecutor commandExecutor;
    private final String PROCESS_DEFINITION_KEY = "e6025d07-555b-11e9-9747-acde48001122";

    @BeforeEach
    void setUp() {
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        commandExecutor = mock(CommandExecutor.class);

        when(commandExecutor.execute(any(GetDeployedProcessDefinitionCmd.class))).thenReturn(getProcessDefinitionEntityFromId(PROCESS_DEFINITION_KEY));
        // When commandExecutor is called on commands DeleteProcessDefinitionsByIdsCmd or DeleteProcessDefinitionsByKeyCmd, it must do nothing.

        customRepositoryServiceImpl = new CustomRepositoryServiceImpl(applicationEventPublisher);
        customRepositoryServiceImpl.setCommandExecutor(commandExecutor);
    }

    @Test
    void shouldPublishProcessDefinitionDeletedEventWhenProcessDefinitionIsDeleted() throws IOException {
        customRepositoryServiceImpl.deleteProcessDefinition(PROCESS_DEFINITION_KEY);

        ArgumentCaptor<AuditEvent> argumentCaptor = ArgumentCaptor.forClass(AuditEvent.class);

        verify(applicationEventPublisher, times(1)).publishEvent(argumentCaptor.capture());
        AuditEvent capturedArgument = argumentCaptor.getValue();
        assertTrue(capturedArgument instanceof ProcessDefinitionDeletedEvent);
    }

    private ProcessDefinitionEntity getProcessDefinitionEntityFromId(String key) {
        ProcessDefinitionEntity processDefinitionEntity = new ProcessDefinitionEntity();
        processDefinitionEntity.setKey(key);
        return processDefinitionEntity;
    }

}