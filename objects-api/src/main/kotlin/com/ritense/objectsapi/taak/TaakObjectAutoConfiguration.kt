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

import com.ritense.objectsapi.taak.initiator.BsnProvider
import com.ritense.objectsapi.taak.initiator.DocumentInitiatorProvider
import com.ritense.objectsapi.taak.initiator.KvkProvider
import com.ritense.objectsapi.taak.resolve.DocumentValueResolver
import com.ritense.objectsapi.taak.resolve.PlaceHolderValueResolver
import com.ritense.objectsapi.taak.resolve.PlaceHolderValueResolverService
import com.ritense.objectsapi.taak.resolve.ProcessVariableValueResolver
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.processdocument.service.ProcessDocumentService
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class TaakObjectAutoConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun taakObjectConnector(
        taakProperties: TaakProperties,
        placeHolderValueResolverService: PlaceHolderValueResolverService,
        bsnProvider: BsnProvider,
        kvkProvider: KvkProvider
    ): TaakObjectConnector {
        return TaakObjectConnector(taakProperties, placeHolderValueResolverService, bsnProvider, kvkProvider)
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

    @Bean
    fun documentValueResolver(
        processDocumentService: ProcessDocumentService
    ): PlaceHolderValueResolver {
        return DocumentValueResolver(processDocumentService)
    }

    @Bean
    fun processVariableValueResolver(): PlaceHolderValueResolver {
        return ProcessVariableValueResolver()
    }

    @Bean
    fun bsnProvider(
        processDocumentService: ProcessDocumentService,
        zaakInstanceLinkService: ZaakInstanceLinkService,
        zaakRolService: ZaakRolService
    ): BsnProvider {
        return DocumentInitiatorProvider(processDocumentService, zaakInstanceLinkService, zaakRolService)
    }

    @Bean
    fun kvkProvider(
        processDocumentService: ProcessDocumentService,
        zaakInstanceLinkService: ZaakInstanceLinkService,
        zaakRolService: ZaakRolService
    ): KvkProvider {
        return DocumentInitiatorProvider(processDocumentService, zaakInstanceLinkService, zaakRolService)
    }

}