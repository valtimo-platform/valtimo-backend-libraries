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

package com.ritense.dataprovider.defaultdataproviders.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.dataprovider.autoconfigure.DataProviderAutoConfiguration
import com.ritense.dataprovider.defaultdataproviders.providers.dropdown.DropdownDatabaseDataProvider
import com.ritense.dataprovider.defaultdataproviders.providers.dropdown.DropdownJsonFileDataProvider
import com.ritense.dataprovider.defaultdataproviders.repository.DropdownListRepository
import com.ritense.dataprovider.defaultdataproviders.providers.translation.TranslationJsonFileDataProvider
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfigureBefore(DataProviderAutoConfiguration::class)
@Configuration
@EnableJpaRepositories(basePackages = ["com.ritense.dataprovider.defaultdataproviders.repository"])
@EntityScan("com.ritense.dataprovider.defaultdataproviders.domain")
class DefaultDataProviderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DropdownDatabaseDataProvider::class)
    fun dropdownDatabaseDataProvider(
        dropdownListRepository: DropdownListRepository,
    ): DropdownDatabaseDataProvider {
        return DropdownDatabaseDataProvider(dropdownListRepository)
    }

    @Bean
    @ConditionalOnMissingBean(DropdownJsonFileDataProvider::class)
    fun dropdownJsonFileDataProvider(
        resourceLoader: ResourceLoader,
        objectMapper: ObjectMapper,
    ): DropdownJsonFileDataProvider {
        return DropdownJsonFileDataProvider(
            resourceLoader,
            objectMapper,
        )
    }

    @Bean
    @ConditionalOnMissingBean(TranslationJsonFileDataProvider::class)
    fun translationJsonFileDataProvider(
        resourceLoader: ResourceLoader,
        objectMapper: ObjectMapper,
    ): TranslationJsonFileDataProvider {
        return TranslationJsonFileDataProvider(
            resourceLoader,
            objectMapper,
        )
    }

}
