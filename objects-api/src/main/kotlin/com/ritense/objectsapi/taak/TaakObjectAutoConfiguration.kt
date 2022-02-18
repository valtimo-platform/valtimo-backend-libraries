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

import com.ritense.connector.service.ConnectorService
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.objectsapi.taak.resolve.DocumentValueResolverFactory
import com.ritense.objectsapi.taak.resolve.FixedValueResolverFactory
import com.ritense.objectsapi.taak.resolve.ProcessVariableValueResolverFactory
import com.ritense.objectsapi.taak.resolve.ValueResolverFactory
import com.ritense.objectsapi.taak.resolve.ValueResolverService
import com.ritense.openzaak.provider.BsnProvider
import com.ritense.processdocument.service.ProcessDocumentService
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import kotlin.contracts.ExperimentalContracts

@Configuration
class TaakObjectAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TaakObjectListener::class)
    fun taakObjectListener(
        openNotificatieService: OpenNotificatieService,
        taskService: TaskService
    ): TaakObjectListener {
        return TaakObjectListener(
            openNotificatieService,
            taskService
        )
    }

    @OptIn(ExperimentalContracts::class)
    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun taakObjectConnector(
        taakProperties: TaakProperties,
        valueResolverService: ValueResolverService,
        connectorService: ConnectorService,
        bsnProvider: BsnProvider
    ): TaakObjectConnector {
        return TaakObjectConnector(
            taakProperties,
            valueResolverService,
            connectorService,
            bsnProvider,
            null
        )
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun taakProperties(): TaakProperties {
        return TaakProperties()
    }

    @Bean
    @ConditionalOnMissingBean(ValueResolverService::class)
    fun valueResolverService(
        valueResolverFactories: List<ValueResolverFactory>
    ): ValueResolverService {
        return ValueResolverService(valueResolverFactories)
    }

    @Bean
    @ConditionalOnMissingBean(FixedValueResolverFactory::class)
    fun fixedValueResolver(): ValueResolverFactory {
        return FixedValueResolverFactory()
    }

    @Bean
    @ConditionalOnMissingBean(DocumentValueResolverFactory::class)
    fun documentValueResolver(
        processDocumentService: ProcessDocumentService
    ): ValueResolverFactory {
        return DocumentValueResolverFactory(processDocumentService)
    }

    @Bean
    @ConditionalOnMissingBean(ProcessVariableValueResolverFactory::class)
    fun processVariableValueResolver(): ValueResolverFactory {
        return ProcessVariableValueResolverFactory()
    }
}