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

import com.ritense.authorization.AuthorizationService
import com.ritense.case.repository.CaseTabRepository
import com.ritense.case_.domain.tab.CaseWidgetTabWidget
import com.ritense.case_.repository.CaseWidgetTabRepository
import com.ritense.case_.rest.CaseWidgetTabManagementResource
import com.ritense.case_.rest.CaseWidgetTabResource
import com.ritense.case_.rest.dto.CaseWidgetTabWidgetDto
import com.ritense.case_.service.CaseWidgetTabService
import com.ritense.case_.widget.CaseWidgetAnnotatedClassResolver
import com.ritense.case_.widget.CaseWidgetDataProvider
import com.ritense.case_.widget.CaseWidgetJacksonModule
import com.ritense.case_.widget.CaseWidgetMapper
import com.ritense.case_.widget.fields.FieldsCaseWidgetDataProvider
import com.ritense.case_.widget.fields.FieldsCaseWidgetMapper
import com.ritense.document.service.DocumentService
import com.ritense.valueresolver.ValueResolverService
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
}
