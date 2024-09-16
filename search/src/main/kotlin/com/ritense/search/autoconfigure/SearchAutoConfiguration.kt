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

package com.ritense.search.autoconfigure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.ritense.search.ObjectMapperConfigurer
import com.ritense.search.autodeployment.SearchListColumnDefinitionDeploymentService
import com.ritense.search.domain.DateFormatDisplayTypeParameter
import com.ritense.search.domain.EnumDisplayTypeParameter
import com.ritense.search.mapper.LegacySearchFieldV2Mapper
import com.ritense.search.mapper.SearchFieldV2Mapper
import com.ritense.search.repository.SearchFieldV2Repository
import com.ritense.search.repository.SearchListColumnRepository
import com.ritense.search.security.config.SearchHttpSecurityConfigurer
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.search.service.SearchListColumnService
import com.ritense.search.web.rest.SearchFieldV2Resource
import com.ritense.search.web.rest.SearchListColumnResource
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(basePackages = ["com.ritense.search.repository"])
@EntityScan("com.ritense.search.domain")
class SearchAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SearchListColumnService::class)
    fun searchListColumnService(
        searchListColumnRepository: SearchListColumnRepository
    ): SearchListColumnService {
        return SearchListColumnService(
            searchListColumnRepository
        )
    }

    @Bean
    @ConditionalOnMissingBean(SearchListColumnResource::class)
    fun searchListColumnResource(
        searchListColumnService: SearchListColumnService
    ): SearchListColumnResource {
        return SearchListColumnResource(
            searchListColumnService
        )
    }

    @Bean
    @ConditionalOnMissingBean(SearchFieldV2Service::class)
    fun searchFieldV2Service(
        searchFieldV2Repository: SearchFieldV2Repository,
        searchFieldMappers: List<SearchFieldV2Mapper>
    ): SearchFieldV2Service {
        return SearchFieldV2Service(
            searchFieldV2Repository,
            searchFieldMappers
        )
    }

    @Bean
    @ConditionalOnMissingBean(SearchFieldV2Resource::class)
    fun searchFieldV2Resource(
        searchFieldV2Service: SearchFieldV2Service
    ): SearchFieldV2Resource {
        return SearchFieldV2Resource(
            searchFieldV2Service
        )
    }

    @Order(302)
    @Bean
    @ConditionalOnMissingBean(SearchHttpSecurityConfigurer::class)
    fun searchHttpSecurityConfigurer(): SearchHttpSecurityConfigurer {
        return SearchHttpSecurityConfigurer()
    }

    @Bean
    fun searchEnumDisplayTypeParameterType(): NamedType {
        return NamedType(EnumDisplayTypeParameter::class.java, "enum")
    }

    @Bean
    fun searchDateFormatDisplayTypeParameterType(): NamedType {
        return NamedType(DateFormatDisplayTypeParameter::class.java, "date")
    }

    @Bean
    fun searchObjectMapper(
        objectMapper: ObjectMapper,
        displayTypeParameterTypes: Collection<NamedType>
    ): ObjectMapperConfigurer {
        return ObjectMapperConfigurer(objectMapper, displayTypeParameterTypes)
    }

    @Bean
    @ConditionalOnMissingBean(LegacySearchFieldV2Mapper::class)
    fun legacySearchFieldV2Mapper(
        objectMapper: ObjectMapper
    ): LegacySearchFieldV2Mapper {
        return LegacySearchFieldV2Mapper(objectMapper)
    }

    @Bean
    fun searchListColumnDefinitionDeploymentService(
        resourceLoader: ResourceLoader,
        applicationEventPublisher: ApplicationEventPublisher,
        searchListColumnService: SearchListColumnService,
        objectMapper: ObjectMapper
    ) = SearchListColumnDefinitionDeploymentService(
        resourceLoader,
        applicationEventPublisher,
        searchListColumnService,
        objectMapper
    )
}
