/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.search.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.search.repository.SearchConfigurationRepository
import com.ritense.search.service.SearchConfigurationDeploymentService
import com.ritense.search.service.SearchConfigurationService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.search.repository"])
@EntityScan("com.ritense.search.domain")
class SearchConfigurationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SearchConfigurationDeploymentService::class)
    fun searchConfigurationDeploymentService(
        resourceLoader: ResourceLoader,
        searchConfigurationService: SearchConfigurationService,
        objectMapper: ObjectMapper,
    ): SearchConfigurationDeploymentService {
        return SearchConfigurationDeploymentService(
            resourceLoader,
            searchConfigurationService,
            objectMapper,
        )
    }

    @Bean
    @ConditionalOnMissingBean(SearchConfigurationService::class)
    fun searchConfigurationService(
        searchConfigurationRepository: SearchConfigurationRepository,
    ): SearchConfigurationService {
        return SearchConfigurationService(
            searchConfigurationRepository,
        )
    }

}
