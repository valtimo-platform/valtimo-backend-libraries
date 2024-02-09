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

package com.ritense.valtimo.camunda.repository;

import com.ritense.valtimo.contract.audit.utils.AuditHelper;
import com.ritense.valtimo.contract.utils.RequestHelper;
import com.ritense.valtimo.domain.process.event.ProcessDefinitionDeletedEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.context.ApplicationEventPublisher;

public class CustomRepositoryServiceImpl extends RepositoryServiceImpl {

    private final ApplicationEventPublisher applicationEventPublisher;

    public CustomRepositoryServiceImpl(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void deleteProcessDefinition(String processDefinitionId, boolean cascade, boolean skipCustomListeners) {
        ProcessDefinition processDefinition = getProcessDefinition(processDefinitionId);
        super.deleteProcessDefinition(processDefinitionId, cascade, skipCustomListeners);
        applicationEventPublisher.publishEvent(
            new ProcessDefinitionDeletedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                processDefinition.getKey()
            )
        );
    }

}