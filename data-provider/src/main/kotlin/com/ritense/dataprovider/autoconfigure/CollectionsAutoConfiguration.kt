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

package com.ritense.dataprovider.autoconfigure

import com.ritense.dataprovider.domain.DataProvider
import com.ritense.dataprovider.security.config.DataProviderHttpSecurityConfigurer
import com.ritense.dataprovider.service.DataProviderService
import com.ritense.dataprovider.web.rest.DataProviderResource
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
class DataProviderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DataProviderService::class)
    fun dataProviderService(
        dataProviders: Map<String, DataProvider<*>>
    ): DataProviderService {
        return DataProviderService(
            dataProviders,
        )
    }

    @Bean
    @ConditionalOnMissingBean(DataProviderResource::class)
    fun dataProviderResource(
        dataProviderService: DataProviderService,
    ): DataProviderResource {
        return DataProviderResource(
            dataProviderService,
        )
    }

    @Order(301)
    @Bean
    @ConditionalOnMissingBean(DataProviderHttpSecurityConfigurer::class)
    fun dataProviderHttpSecurityConfigurer(): DataProviderHttpSecurityConfigurer {
        return DataProviderHttpSecurityConfigurer()
    }

}
