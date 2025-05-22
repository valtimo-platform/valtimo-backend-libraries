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
package com.ritense.case_.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case.service.CaseTabService
import com.ritense.case_.deployment.CaseWidgetTabDeployer
import com.ritense.case_.domain.tab.CaseWidgetTabWidget
import com.ritense.case_.repository.CaseWidgetTabRepository
import com.ritense.case_.repository.CaseWidgetTabWidgetSpecificationFactory
import com.ritense.case_.rest.CaseWidgetTabManagementResource
import com.ritense.case_.rest.CaseWidgetTabResource
import com.ritense.case_.rest.dto.CaseWidgetTabWidgetDto
import com.ritense.case_.service.CaseWidgetTabExporter
import com.ritense.case_.service.CaseWidgetTabImporter
import com.ritense.case_.service.CaseWidgetTabService
import com.ritense.case_.widget.CaseWidgetAnnotatedClassResolver
import com.ritense.case_.widget.CaseWidgetDataProvider
import com.ritense.case_.widget.CaseWidgetJacksonModule
import com.ritense.case_.widget.CaseWidgetMapper
import com.ritense.case_.widget.collection.CollectionCaseWidgetDataProvider
import com.ritense.case_.widget.collection.CollectionCaseWidgetMapper
import com.ritense.case_.widget.custom.CustomCaseWidgetMapper
import com.ritense.case_.widget.fields.FieldsCaseWidgetDataProvider
import com.ritense.case_.widget.fields.FieldsCaseWidgetMapper
import com.ritense.case_.widget.table.TableCaseWidgetDataProvider
import com.ritense.case_.widget.table.TableCaseWidgetMapper
import com.ritense.document.service.DocumentService
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import com.ritense.valtimo.changelog.service.ChangelogService
import com.ritense.valtimo.contract.database.QueryDialectHelper
import com.ritense.valueresolver.ValueResolverService
import jakarta.validation.Validator
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@AutoConfiguration
@EnableJpaRepositories(
    basePackageClasses = [
        CaseWidgetTabRepository::class
    ]
)
@EntityScan(basePackages = ["com.ritense.case_.domain", "com.ritense.case_.widget"])
class CaseWidgetAutoConfiguration {

    @Suppress("UNCHECKED_CAST")
    @Bean
    @ConditionalOnMissingBean(CaseWidgetTabService::class)
    fun caseWidgetTabService(
        caseWidgetTabRepository: CaseWidgetTabRepository,
        caseTabRepository: CaseTabRepository,
        authorizationService: AuthorizationService,
        caseWidgetMappers: List<CaseWidgetMapper<*, *>>,
        caseWidgetDataProviders: List<CaseWidgetDataProvider<*>>,
        documentService: DocumentService
    ) = CaseWidgetTabService(
        documentService,
        caseWidgetTabRepository,
        caseTabRepository,
        authorizationService,
        caseWidgetMappers as List<CaseWidgetMapper<CaseWidgetTabWidget, CaseWidgetTabWidgetDto>>,
        caseWidgetDataProviders as List<CaseWidgetDataProvider<CaseWidgetTabWidget>>
    )

    @Suppress("UNCHECKED_CAST")
    @Bean
    @ConditionalOnMissingBean(CaseWidgetTabDeployer::class)
    fun caseWidgetTabDeployer(
        objectMapper: ObjectMapper,
        caseWidgetTabRepository: CaseWidgetTabRepository,
        caseWidgetMappers: List<CaseWidgetMapper<*, *>>,
        changelogService: ChangelogService,
        @Value("\${valtimo.changelog.case-widget-tab.clear-tables:false}") clearTables: Boolean,
        validator: Validator
    ) = CaseWidgetTabDeployer(
        objectMapper,
        caseWidgetTabRepository,
        caseWidgetMappers as List<CaseWidgetMapper<CaseWidgetTabWidget, CaseWidgetTabWidgetDto>>,
        changelogService,
        clearTables,
        validator
    )

    @ConditionalOnMissingBean(CaseWidgetTabWidgetSpecificationFactory::class)
    @Bean
    fun caseWidgetTabWidgetSpecificationFactory(
        queryDialectHelper: QueryDialectHelper
    ) = CaseWidgetTabWidgetSpecificationFactory(queryDialectHelper)

    @Bean
    @ConditionalOnMissingBean(CaseWidgetTabExporter::class)
    fun caseWidgetTabExporter(
        objectMapper: ObjectMapper,
        caseTabService: CaseTabService,
        caseWidgetTabService: CaseWidgetTabService
    ) = CaseWidgetTabExporter(objectMapper, caseTabService, caseWidgetTabService)

    @Bean
    @ConditionalOnMissingBean(CaseWidgetTabImporter::class)
    fun caseWidgetTabImporter(
        caseWidgetTabDeployer: CaseWidgetTabDeployer,
        changelogDeployer: ChangelogDeployer
    ) = CaseWidgetTabImporter(caseWidgetTabDeployer, changelogDeployer)

    @ConditionalOnMissingBean(CaseWidgetTabResource::class)
    @Bean
    fun caseWidgetTabResource(
        caseWidgetTabService: CaseWidgetTabService
    ) = CaseWidgetTabResource(caseWidgetTabService)

    @ConditionalOnMissingBean(CaseWidgetTabManagementResource::class)
    @Bean
    fun caseWidgetTabManagementResource(
        caseWidgetTabService: CaseWidgetTabService
    ) = CaseWidgetTabManagementResource(caseWidgetTabService)

    @ConditionalOnMissingBean(CaseWidgetAnnotatedClassResolver::class)
    @Bean
    fun caseWidgetAnnotatedClassResolver(
        context: ApplicationContext
    ) = CaseWidgetAnnotatedClassResolver(context)

    @ConditionalOnMissingBean(CaseWidgetJacksonModule::class)
    @Bean
    fun caseWidgetJacksonModule(
        annotatedClassResolver: CaseWidgetAnnotatedClassResolver
    ) = CaseWidgetJacksonModule(annotatedClassResolver)

    @ConditionalOnMissingBean(FieldsCaseWidgetMapper::class)
    @Bean
    fun fieldsCaseWidgetMapper() = FieldsCaseWidgetMapper()

    @ConditionalOnMissingBean(FieldsCaseWidgetDataProvider::class)
    @Bean
    fun fieldsCaseWidgetDataProvider(
        valueResolverService: ValueResolverService
    ) = FieldsCaseWidgetDataProvider(valueResolverService)

    @ConditionalOnMissingBean(TableCaseWidgetMapper::class)
    @Bean
    fun tableCaseWidgetMapper() = TableCaseWidgetMapper()

    @ConditionalOnMissingBean(TableCaseWidgetDataProvider::class)
    @Bean
    fun tableCaseWidgetDataProvider(
        objectMapper: ObjectMapper,
        valueResolverService: ValueResolverService
    ) = TableCaseWidgetDataProvider(objectMapper, valueResolverService)

    @ConditionalOnMissingBean(CollectionCaseWidgetMapper::class)
    @Bean
    fun collectionCaseWidgetMapper() = CollectionCaseWidgetMapper()

    @ConditionalOnMissingBean(CollectionCaseWidgetDataProvider::class)
    @Bean
    fun collectionCaseWidgetDataProvider(
        objectMapper: ObjectMapper,
        valueResolverService: ValueResolverService
    ) = CollectionCaseWidgetDataProvider(objectMapper, valueResolverService)

    @ConditionalOnMissingBean(CustomCaseWidgetMapper::class)
    @Bean
    fun customCaseWidgetMapper() = CustomCaseWidgetMapper()
}
