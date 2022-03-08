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

import com.ritense.valtimo.choicefield.repository.ChoiceFieldRepository;
import com.ritense.valtimo.choicefield.repository.ChoiceFieldValueRepository;
import com.ritense.valtimo.service.ChoiceFieldService;
import com.ritense.valtimo.service.ChoiceFieldValueService;
import com.ritense.valtimo.web.rest.ChoiceFieldResource;
import com.ritense.valtimo.web.rest.ChoiceFieldValueResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackageClasses = {
    ChoiceFieldRepository.class,
    ChoiceFieldValueRepository.class
})
@EntityScan(value = "com.ritense.valtimo.domain.choicefield")
public class ChoiceFieldAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ChoiceFieldService.class)
    public ChoiceFieldService choiceFieldService(
        final ChoiceFieldRepository choiceFieldRepository,
        final ChoiceFieldValueService choiceFieldValueService
    ) {
        return new ChoiceFieldService(choiceFieldRepository, choiceFieldValueService);
    }

    @Bean
    @ConditionalOnMissingBean(ChoiceFieldValueService.class)
    public ChoiceFieldValueService choiceFieldValueService(
        final ChoiceFieldValueRepository choiceFieldValueRepository
    ) {
        return new ChoiceFieldValueService(choiceFieldValueRepository);
    }

    @Bean
    @ConditionalOnMissingBean(ChoiceFieldResource.class)
    public ChoiceFieldResource choiceFieldResource(ChoiceFieldService choiceFieldService) {
        return new ChoiceFieldResource(choiceFieldService);
    }

    @Bean
    @ConditionalOnMissingBean(ChoiceFieldValueResource.class)
    public ChoiceFieldValueResource choiceFieldValueResource(
        final ChoiceFieldValueService choiceFieldValueService,
        final ChoiceFieldRepository choiceFieldRepository
    ) {
        return new ChoiceFieldValueResource(choiceFieldValueService, choiceFieldRepository);
    }

}