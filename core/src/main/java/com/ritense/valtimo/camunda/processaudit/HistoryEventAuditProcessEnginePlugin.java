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

package com.ritense.valtimo.camunda.processaudit;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.history.handler.CompositeHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.DbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.springframework.context.ApplicationEventPublisher;
import java.util.ArrayList;
import java.util.List;

public class HistoryEventAuditProcessEnginePlugin implements ProcessEnginePlugin {

    private final ApplicationEventPublisher applicationEventPublisher;

    public HistoryEventAuditProcessEnginePlugin(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        List<HistoryEventHandler> handlers = new ArrayList<>(3);

        if (processEngineConfiguration.getHistoryEventHandler() != null) {
            handlers.add(processEngineConfiguration.getHistoryEventHandler());
        } else {
            // This one is only added by default if no other historyEventHandler is set.
            // And since we are setting our own custom historyEventHandlers,
            // we need to add this one manually so we don't break things.
            handlers.add(new DbHistoryEventHandler());
        }

        handlers.add(new ProcessStartedEventHandler(applicationEventPublisher));
        handlers.add(new ProcessEndedEventHandler(applicationEventPublisher));

        processEngineConfiguration.setHistoryEventHandler(
            new CompositeHistoryEventHandler(handlers)
        );
    }

    @Override
    public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    }

    @Override
    public void postProcessEngineBuild(ProcessEngine processEngine) {
    }

}
