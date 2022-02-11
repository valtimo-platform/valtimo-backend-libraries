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

package com.ritense.objectsapi.taak

import com.ritense.objectsapi.taak.resolve.DocumentValueResolver
import com.ritense.objectsapi.taak.resolve.FixedValueResolver
import com.ritense.objectsapi.taak.resolve.PlaceHolderValueResolver
import com.ritense.objectsapi.taak.resolve.PlaceHolderValueResolverService
import com.ritense.objectsapi.taak.resolve.ProcessVariableValueResolver
import com.ritense.openzaak.provider.BsnProvider
import com.ritense.processdocument.service.ProcessDocumentService
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import kotlin.contracts.ExperimentalContracts

@Configuration
class TaakObjectAutoConfiguration {

    @OptIn(ExperimentalContracts::class)
    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun taakObjectConnector(
        taakProperties: TaakProperties,
        placeHolderValueResolverService: PlaceHolderValueResolverService,
        bsnProvider: BsnProvider
    ): TaakObjectConnector {
        return TaakObjectConnector(taakProperties, placeHolderValueResolverService, bsnProvider, null)
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun taakProperties(): TaakProperties {
        return TaakProperties()
    }

    @Bean
    @ConditionalOnMissingBean(PlaceHolderValueResolverService::class)
    fun placeHolderValueResolverService(
        placeHolderValueResolvers: List<PlaceHolderValueResolver>
    ): PlaceHolderValueResolverService {
        return PlaceHolderValueResolverService(placeHolderValueResolvers)
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @Bean
    @ConditionalOnMissingBean(FixedValueResolver::class)
    fun fixedValueResolver(): PlaceHolderValueResolver {
        return FixedValueResolver()
    }

    @Order(1)
    @Bean
    @ConditionalOnMissingBean(DocumentValueResolver::class)
    fun documentValueResolver(
        processDocumentService: ProcessDocumentService
    ): PlaceHolderValueResolver {
        return DocumentValueResolver(processDocumentService)
    }

    @Order(0)
    @Bean
    @ConditionalOnMissingBean(ProcessVariableValueResolver::class)
    fun processVariableValueResolver(): PlaceHolderValueResolver {
        return ProcessVariableValueResolver()
    }

}