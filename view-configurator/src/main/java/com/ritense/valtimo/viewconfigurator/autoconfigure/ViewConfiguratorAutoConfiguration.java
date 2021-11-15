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

package com.ritense.valtimo.viewconfigurator.autoconfigure;

import com.ritense.valtimo.viewconfigurator.domain.listener.ProcessDefinitionAvailableEventListener;
import com.ritense.valtimo.viewconfigurator.domain.listener.TaskCompletedEventListener;
import com.ritense.valtimo.viewconfigurator.repository.ViewConfigRepository;
import com.ritense.valtimo.viewconfigurator.service.ProcessDefinitionService;
import com.ritense.valtimo.viewconfigurator.service.ProcessDefinitionVariableService;
import com.ritense.valtimo.viewconfigurator.service.ViewConfigService;
import com.ritense.valtimo.viewconfigurator.service.impl.ProcessDefinitionServiceImpl;
import com.ritense.valtimo.viewconfigurator.service.impl.ProcessDefinitionVariableServiceImpl;
import com.ritense.valtimo.viewconfigurator.service.impl.ViewConfigServiceImpl;
import com.ritense.valtimo.viewconfigurator.web.rest.ViewConfiguratorResource;
import org.camunda.bpm.engine.RepositoryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.ritense.valtimo.viewconfigurator.repository")
@EntityScan("com.ritense.valtimo.viewconfigurator.domain")
public class ViewConfiguratorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ViewConfigService.class)
    public ViewConfigService viewConfigService(
        final ProcessDefinitionVariableService processDefinitionVariableService,
        final ViewConfigRepository viewConfigRepository,
        final ProcessDefinitionService processDefinitionService
    ) {
        return new ViewConfigServiceImpl(processDefinitionVariableService, viewConfigRepository, processDefinitionService);
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDefinitionService.class)
    public ProcessDefinitionService processDefinitionService(RepositoryService repositoryService) {
        return new ProcessDefinitionServiceImpl(repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDefinitionVariableService.class)
    public ProcessDefinitionVariableService processDefinitionVariableService(RepositoryService repositoryService) {
        return new ProcessDefinitionVariableServiceImpl(repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean(ProcessDefinitionAvailableEventListener.class)
    public ProcessDefinitionAvailableEventListener processDefinitionAvailableEventListener(
        final ViewConfigService viewConfigService
    ) {
        return new ProcessDefinitionAvailableEventListener(viewConfigService);
    }

    @Bean
    @ConditionalOnMissingBean(TaskCompletedEventListener.class)
    public TaskCompletedEventListener taskCompletedEventListener(
        final ViewConfigService viewConfigService
    ) {
        return new TaskCompletedEventListener(viewConfigService);
    }

    @Bean
    @ConditionalOnMissingBean(ViewConfiguratorResource.class)
    public ViewConfiguratorResource viewConfiguratorResource(ViewConfigService viewConfigService) {
        return new ViewConfiguratorResource(viewConfigService);
    }

}