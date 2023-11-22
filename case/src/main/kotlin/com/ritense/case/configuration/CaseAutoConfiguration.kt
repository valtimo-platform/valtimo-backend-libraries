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

package com.ritense.case.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.ritense.authorization.AuthorizationService
import com.ritense.case.deployment.CaseTabDeploymentService
import com.ritense.case.domain.BooleanDisplayTypeParameter
import com.ritense.case.domain.DateFormatDisplayTypeParameter
import com.ritense.case.domain.EnumDisplayTypeParameter
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.repository.CaseTabSpecificationFactory
import com.ritense.case.security.config.CaseHttpSecurityConfigurer
import com.ritense.case.service.CaseDefinitionDeploymentService
import com.ritense.case.service.CaseDefinitionExportService
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.service.CaseInstanceService
import com.ritense.case.service.CaseListDeploymentService
import com.ritense.case.service.CaseTabExportService
import com.ritense.case.service.CaseTabService
import com.ritense.case.service.ObjectMapperConfigurer
import com.ritense.case.web.rest.CaseDefinitionResource
import com.ritense.case.web.rest.CaseInstanceResource
import com.ritense.case.web.rest.CaseTabManagementResource
import com.ritense.case.web.rest.CaseTabResource
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.DocumentSearchService
import com.ritense.document.service.JsonSchemaDocumentDefinitionExportService
import com.ritense.valtimo.changelog.service.ChangelogService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valueresolver.ValueResolverService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
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
        service: CaseDefinitionService,
        exportService: CaseDefinitionExportService,
    ): CaseDefinitionResource {
        return CaseDefinitionResource(service, exportService)
    }

    @Bean
    @ConditionalOnMissingBean(name = ["caseInstanceResource"]) // because integration tests fail to initialise in portaaltaak
    fun caseInstanceResource(
        service: CaseInstanceService
    ): CaseInstanceResource {
        return CaseInstanceResource(service)
    }

    @ConditionalOnMissingBean(CaseTabResource::class)
    @Bean
    fun caseTabResource(
        caseTabService: CaseTabService
    ): CaseTabResource {
        return CaseTabResource(caseTabService)
    }

    @ConditionalOnMissingBean(CaseTabManagementResource::class)
    @Bean
    fun caseTabManagementResource(
        caseTabService: CaseTabService
    ): CaseTabManagementResource {
        return CaseTabManagementResource(caseTabService)
    }

    @Bean
    fun caseDefinitionService(
        repository: CaseDefinitionSettingsRepository,
        caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
        documentDefinitionService: DocumentDefinitionService,
        valueResolverService: ValueResolverService,
        authorizationService: AuthorizationService,
    ): CaseDefinitionService {
        return CaseDefinitionService(
            repository,
            caseDefinitionListColumnRepository,
            documentDefinitionService,
            valueResolverService,
            authorizationService
        )
    }

    @ConditionalOnMissingBean(CaseTabService::class)
    @Bean
    fun caseTabService(
        caseTabRepository: CaseTabRepository,
        @Lazy authorizationService: AuthorizationService,
        documentDefinitionService: DocumentDefinitionService
    ): CaseTabService {
        return CaseTabService(caseTabRepository, documentDefinitionService, authorizationService)
    }

    @Bean
    fun caseInstanceService(
        caseDefinitionService: CaseDefinitionService,
        caseDefinitionListColumnRepository: CaseDefinitionListColumnRepository,
        documentSearchService: DocumentSearchService,
        valueResolverService: ValueResolverService,
    ): CaseInstanceService {
        return CaseInstanceService(
            caseDefinitionService,
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

    @Bean
    @ConditionalOnMissingBean(ResourcePatternResolver::class)
    fun resourcePatternResolver(resourceLoader: ResourceLoader): ResourcePatternResolver {
        return PathMatchingResourcePatternResolver(resourceLoader)
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    fun caseListDeploymentService(
        resourcePatternResolver: ResourcePatternResolver,
        objectMapper: ObjectMapper,
        caseDefinitionService: CaseDefinitionService
    ): CaseListDeploymentService {
        return CaseListDeploymentService(
            resourcePatternResolver,
            objectMapper,
            caseDefinitionService
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
    fun booleanDisplayTypeParameterType(): NamedType {
        return NamedType(BooleanDisplayTypeParameter::class.java, "boolean")
    }

    @Bean
    fun caseObjectMapper(
        objectMapper: ObjectMapper,
        displayTypeParameterTypes: Collection<NamedType>
    ): ObjectMapperConfigurer {
        return ObjectMapperConfigurer(objectMapper, displayTypeParameterTypes)
    }

    @Bean
    fun caseTabDeployer(
        objectMapper: ObjectMapper,
        caseTabRepository: CaseTabRepository,
        changelogService: ChangelogService,
        caseTabService: CaseTabService,
        @Value("\${valtimo.changelog.case-tabs.clear-tables:false}") clearTables: Boolean
    ): CaseTabDeploymentService {
        return CaseTabDeploymentService(
            objectMapper,
            caseTabRepository,
            changelogService,
            caseTabService,
            clearTables
        )
    }

    @Bean
    @ConditionalOnMissingBean(CaseTabSpecificationFactory::class)
    fun caseTabSpecificationFactory(
        @Lazy caseTabService: CaseTabService,
        queryDialectHelper: QueryDialectHelper
    ): CaseTabSpecificationFactory {
        return CaseTabSpecificationFactory(
            caseTabService,
            queryDialectHelper
        )
    }

    @Bean
    @ConditionalOnMissingBean(CaseDefinitionExportService::class)
    fun caseDefinitionExportService(documentDefinitionExportService: JsonSchemaDocumentDefinitionExportService): CaseDefinitionExportService {
        return CaseDefinitionExportService(
            documentDefinitionExportService
        )
    }

    @Bean
    @ConditionalOnMissingBean(CaseTabExportService::class)
    fun caseTabExportService(objectMapper: ObjectMapper, caseTabService: CaseTabService) =
        CaseTabExportService(
            objectMapper,
            caseTabService
        )
}
