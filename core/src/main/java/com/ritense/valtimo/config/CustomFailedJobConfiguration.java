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

package com.ritense.valtimo.config;

import java.util.ArrayList;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaFailedJobConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
public class CustomFailedJobConfiguration implements CamundaFailedJobConfiguration {

    @Override
    public void preInit(ProcessEngineConfigurationImpl configuration) {
        if (configuration.getCustomPostBPMNParseListeners() == null) {
            configuration.setCustomPostBPMNParseListeners(new ArrayList<>());
        }
    }

}