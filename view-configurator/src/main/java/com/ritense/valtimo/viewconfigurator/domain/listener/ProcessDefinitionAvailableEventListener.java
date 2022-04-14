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

package com.ritense.valtimo.viewconfigurator.domain.listener;

import com.ritense.valtimo.contract.event.ProcessDefinitionAvailableEvent;
import com.ritense.valtimo.viewconfigurator.service.ViewConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

public class ProcessDefinitionAvailableEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionAvailableEventListener.class);
    private final ViewConfigService viewConfigService;

    public ProcessDefinitionAvailableEventListener(ViewConfigService viewConfigService) {
        this.viewConfigService = viewConfigService;
    }

    @EventListener(ProcessDefinitionAvailableEvent.class)
    public void handle(ProcessDefinitionAvailableEvent event) throws Exception {
        logger.debug("{} - handle - processDefinitionAvailableEvent - {}", Thread.currentThread().getName(), event.getProcessDefinitionId());
        viewConfigService.createViewConfiguration(event.getProcessDefinitionId());
    }

}