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

package com.ritense.valtimo.camunda;

import com.ritense.authorization.AuthorizationContext;
import com.ritense.valtimo.contract.event.ProcessDefinitionAvailableEvent;
import com.ritense.valtimo.service.CamundaProcessService;
import org.camunda.bpm.spring.boot.starter.event.ProcessApplicationStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

public class ProcessApplicationStartedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ProcessApplicationStartedEventListener.class);
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CamundaProcessService camundaProcessService;

    public ProcessApplicationStartedEventListener(ApplicationEventPublisher applicationEventPublisher, CamundaProcessService camundaProcessService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.camundaProcessService = camundaProcessService;
    }

    @EventListener(ProcessApplicationStartedEvent.class)
    public void engineStarted(ProcessApplicationStartedEvent event) {
        logger.debug("{} - handle - processApplicationStartedEvent", Thread.currentThread().getName());
        AuthorizationContext.runWithoutAuthorization(camundaProcessService::getDeployedDefinitions)
            .forEach(processDefinition -> applicationEventPublisher.publishEvent(new ProcessDefinitionAvailableEvent(processDefinition.getId())));
    }

}