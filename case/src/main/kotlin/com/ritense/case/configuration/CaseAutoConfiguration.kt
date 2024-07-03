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

package com.ritense.case.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.ritense.authorization.AuthorizationService
import com.ritense.case.deployment.CaseTabDeploymentService
import com.ritense.case.deployment.CaseTaskListDeploymentService
import com.ritense.case.domain.BooleanDisplayTypeParameter
import com.ritense.case.domain.DateFormatDisplayTypeParameter
import com.ritense.case.domain.EnumDisplayTypeParameter
import com.ritense.case.repository.CaseDefinitionListColumnRepository
import com.ritense.case.repository.CaseDefinitionSettingsRepository
import com.ritense.case.repository.CaseTabDocumentDefinitionMapper
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.repository.CaseTabSpecificationFactory
import com.ritense.case.repository.TaskListColumnRepository
import com.ritense.case.security.config.CaseHttpSecurityConfigurer
import com.ritense.case.service.CaseDefinitionDeploymentService
import com.ritense.case.service.CaseDefinitionService
import com.ritense.case.service.CaseDefinitionSettingsExporter
import com.ritense.case.service.CaseDefinitionSettingsImporter
import com.ritense.case.service.CaseInstanceService
import com.ritense.case.service.CaseListDeploymentService
import com.ritense.case.service.CaseListExporter
import com.ritense.case.service.CaseListImporter
import com.ritense.case.service.CaseTabExporter
import com.ritense.case.service.CaseTabImporter
import com.ritense.case.service.CaseTabService
import com.ritense.case.service.CaseTaskListExporter
import com.ritense.case.service.CaseTaskListImporter
import com.ritense.case.service.ObjectMapperConfigurer
import com.ritense.case.service.TaskColumnService
import com.ritense.case.web.rest.CaseDefinitionResource
import com.ritense.case.web.rest.CaseInstanceResource
import com.ritense.case.web.rest.CaseTabManagementResource
import com.ritense.case.web.rest.CaseTabResource
import com.ritense.case.web.rest.TaskListResource
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.document.service.DocumentSearchService
import com.ritense.document.service.DocumentService
import com.ritense.exporter.ExportService
import com.ritense.importer.ImportService
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import com.ritense.valtimo.changelog.service.ChangelogService
import com.ritense.valtimo.contract.authentication.UserManagementService
import com.ritense.valtimo.contract.config.LiquibaseMasterChangeLogLocation
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valueresolver.ValueResolverService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
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
        exportService: ExportService,
        importService: ImportService
    ): CaseDefinitionResource {
        return CaseDefinitionResource(service, exportService, importService)
    }

    @Bean
    @ConditionalOnMissingBean(name = ["caseInstanceResource"]) // because integration tests fail to initialise in portaaltaak
    fun caseInstanceResource(
        service: CaseInstanceService
    ): CaseInstanceResource {
        return CaseInstanceResource(service)
    }

    @Bean
    @ConditionalOnMissingBean(TaskListResource::class) // because integration tests fail to initialise in portaaltaak
    fun taskListResource(
        service: TaskColumnService
    ): TaskListResource {
        return TaskListResource(service)
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
        caseTabService: CaseTabService,
        userManagementService: UserManagementService,
    ): CaseTabManagementResource {
        return CaseTabManagementResource(
            caseTabService,
            userManagementService,
        )
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
        documentDefinitionService: DocumentDefinitionService,
        applicationEventPublisher: ApplicationEventPublisher,
        userManagementService: UserManagementService,
        documentService: DocumentService
    ): CaseTabService {
        return CaseTabService(
            caseTabRepository,
            documentDefinitionService,
            authorizationService,
            applicationEventPublisher,
            userManagementService,
            documentService
        )
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
    fun taskColumnService(
        repository: TaskListColumnRepository,
        documentDefinitionService: DocumentDefinitionService,
        valueResolverService: ValueResolverService,
        authorizationService: AuthorizationService,
    ): TaskColumnService {
        return TaskColumnService(
            repository,
            documentDefinitionService,
            valueResolverService,
            authorizationService
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
    fun TaskListDeployer(
        objectMapper: ObjectMapper,
        taskListColumnRepository: TaskListColumnRepository,
        changelogService: ChangelogService,
        taskColumnService: TaskColumnService,
        @Value("\${valtimo.changelog.case-task-list.clear-tables:false}") clearTables: Boolean
    ): CaseTaskListDeploymentService {
        return CaseTaskListDeploymentService(
            objectMapper,
            taskListColumnRepository,
            changelogService,
            taskColumnService,
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
    @ConditionalOnMissingBean(CaseTabExporter::class)
    fun caseTabExporter(
        objectMapper: ObjectMapper,
        caseTabService: CaseTabService,
    ) = CaseTabExporter(
        objectMapper,
        caseTabService
    )

    @Bean
    @ConditionalOnMissingBean(CaseListExporter::class)
    fun caseListExporter(
        caseDefinitionService: CaseDefinitionService,
        objectMapper: ObjectMapper,
    ) = CaseListExporter(
        caseDefinitionService,
        objectMapper
    )

    @Bean
    @ConditionalOnMissingBean(CaseListImporter::class)
    fun caseListImporter(
        caseListDeploymentService: CaseListDeploymentService
    ) = CaseListImporter(caseListDeploymentService)

    @Bean
    @ConditionalOnMissingBean(CaseTabImporter::class)
    fun caseTabImporter(
        caseTabDeploymentService: CaseTabDeploymentService,
        changelogDeployer: ChangelogDeployer
    ) = CaseTabImporter(caseTabDeploymentService, changelogDeployer)

    @Bean
    @ConditionalOnMissingBean(CaseTaskListExporter::class)
    fun caseTaskListExporter(
        objectMapper: ObjectMapper,
        service: TaskColumnService,
    ) = CaseTaskListExporter(
        objectMapper,
        service
    )

    @Bean
    @ConditionalOnMissingBean(CaseTaskListImporter::class)
    fun caseTaskListImporter(
        caseTaskListDeploymentService: CaseTaskListDeploymentService,
        changelogDeployer: ChangelogDeployer
    ) = CaseTaskListImporter(caseTaskListDeploymentService, changelogDeployer)

    @Bean
    @ConditionalOnMissingBean(CaseDefinitionSettingsExporter::class)
    fun caseDefinitionSettingsExporter(
        objectMapper: ObjectMapper,
        caseDefinitionService: CaseDefinitionService,
    ) = CaseDefinitionSettingsExporter(
        objectMapper,
        caseDefinitionService
    )

    @Bean
    @ConditionalOnMissingBean(CaseDefinitionSettingsImporter::class)
    fun caseDefinitionSettingsImporter(
        deploymentService: CaseDefinitionDeploymentService
    ) = CaseDefinitionSettingsImporter(
        deploymentService
    )

    @Bean
    fun caseTabDocumentDefinitionMapper(
        @Lazy documentDefinitionService: DocumentDefinitionService,
    ): CaseTabDocumentDefinitionMapper {
        return CaseTabDocumentDefinitionMapper(documentDefinitionService)
    }
}
