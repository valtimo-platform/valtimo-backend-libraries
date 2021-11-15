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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

@Slf4j
@RequiredArgsConstructor
public class ProcessDefinitionAvailableEventListener {

    private final ViewConfigService viewConfigService;

    @EventListener(ProcessDefinitionAvailableEvent.class)
    public void handle(ProcessDefinitionAvailableEvent event) throws Exception {
        logger.debug("{} - handle - processDefinitionAvailableEvent - {}", Thread.currentThread().getName(), event.getProcessDefinitionId());
        viewConfigService.createViewConfiguration(event.getProcessDefinitionId());
    }

}