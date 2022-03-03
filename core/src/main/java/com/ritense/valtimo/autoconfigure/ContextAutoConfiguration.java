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

package com.ritense.valtimo.autoconfigure;

import com.ritense.valtimo.repository.ContextRepository;
import com.ritense.valtimo.repository.UserContextRepository;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.ContextService;
import com.ritense.valtimo.service.CurrentUserService;
import com.ritense.valtimo.web.rest.ContextResource;
import org.camunda.bpm.engine.RepositoryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackageClasses = ContextRepository.class)
@EntityScan("com.ritense.valtimo.domain.contexts")
public class ContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ContextService.class)
    public ContextService contextService(
        final CurrentUserService currentUserService,
        final ContextRepository contextRepository,
        final UserContextRepository userContextRepository,
        final RepositoryService repositoryService
    ) {
        return new ContextService(currentUserService, contextRepository, userContextRepository, repositoryService);
    }

    @Bean
    @ConditionalOnMissingBean(ContextResource.class)
    public ContextResource ContextResource(
        final ContextService contextService,
        final CamundaProcessService camundaProcessService
    ) {
        return new ContextResource(contextService, camundaProcessService);
    }

}