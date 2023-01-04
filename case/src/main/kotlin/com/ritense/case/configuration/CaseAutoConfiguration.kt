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

package com.ritense.case.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.ritense.case.domain.DateFormatDisplayTypeParameter
import com.ritense.case.domain.EnumDisplayTypeParameter
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.case.security.config.CaseHttpSecurityConfigurer
import com.ritense.case.service.CaseDefinitionDeploymentService
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.service.CaseInstanceService
import com.ritense.case.service.ObjectMapperConfigurer
import com.ritense.case.web.rest.CaseDefinitionResource
import com.ritense.case.web.rest.CaseInstanceResource
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.DocumentSearchService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import com.ritense.valueresolver.ValueResolverService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(
    basePackageClasses = [
        CaseDefinitionSettingsRepository::class
    ]
)
@EntityScan(basePackages = ["com.ritense.case.domain"])
class CaseAutoConfiguration {

    @ConditionalOnMissingBean(name = ["caseDefinitionResource"])
    @Bean
    fun caseDefinitionResource(
        service: CaseDefinitionService
    ): CaseDefinitionResource {
        return CaseDefinitionResource(service)
    }

    @Bean
    fun caseInstanceResource(
        service: CaseInstanceService
    ): CaseInstanceResource {
        return CaseInstanceResource(service)
    }

    @Bean
    fun caseDefinitionService(
        repository: CaseDefinitionSettingsRepository,
        caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
        documentDefinitionService: DocumentDefinitionService,
        valueResolverService: ValueResolverService,
        ): CaseDefinitionService {
        return CaseDefinitionService(
            repository,
            caseDefinitionListColumnRepository,
            documentDefinitionService,
            valueResolverService
        )
    }

    @Bean
    fun casInstanceService(
        caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
        documentSearchService: DocumentSearchService,
        valueResolverService: ValueResolverService,
    ): CaseInstanceService {
        return CaseInstanceService(
            caseDefinitionListColumnRepository,
            documentSearchService,
            valueResolverService,
        )
    }

    @Bean
    fun caseDefinitionDeploymentService(
        resourceLoader: ResourceLoader,
        objectMapper: ObjectMapper,
        caseDefinitionSettingsRepository: CaseDefinitionSettingsRepository
    ): CaseDefinitionDeploymentService {
        return CaseDefinitionDeploymentService(
            resourceLoader,
            objectMapper,
            caseDefinitionSettingsRepository
        )
    }

    @Order(300)
    @Bean
    @ConditionalOnMissingBean(CaseHttpSecurityConfigurer::class)
    fun caseHttpSecurityConfigurer(): CaseHttpSecurityConfigurer {
        return CaseHttpSecurityConfigurer()
    }

    @Order(Ordered.HIGHEST_PRECEDENCE + 20)
    @ConditionalOnMissingBean(name = ["caseLiquibaseMasterChangeLogLocation"])
    @Bean
    fun caseLiquibaseMasterChangeLogLocation(): LiquibaseMasterChangeLogLocation {
        return LiquibaseMasterChangeLogLocation("config/liquibase/case-master.xml")
    }

    @Bean
    fun enumDisplayTypeParameterType(): NamedType {
        return NamedType(EnumDisplayTypeParameter::class.java, "enum")
    }

    @Bean
    fun dateFormatDisplayTypeParameterType(): NamedType {
        return NamedType(DateFormatDisplayTypeParameter::class.java, "date")
    }

    @Bean
    fun caseObjectMapper(
        objectMapper: ObjectMapper,
        displayTypeParameterTypes: Collection<NamedType>
    ): ObjectMapperConfigurer {
        return ObjectMapperConfigurer(objectMapper, displayTypeParameterTypes)
    }
}
