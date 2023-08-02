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

package com.ritense.objectsapi.taak

import com.ritense.connector.service.ConnectorService
import com.ritense.document.domain.impl.JsonSchemaDocument
import com.ritense.document.service.DocumentService
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.openzaak.provider.BsnProvider
import com.ritense.openzaak.provider.KvkProvider
import com.ritense.openzaak.service.ZaakService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.OpenZaakService
import com.ritense.valtimo.service.BpmnModelService
import com.ritense.valtimo.service.CamundaTaskService
import com.ritense.valueresolver.ValueResolverService
import kotlin.contracts.ExperimentalContracts
import org.camunda.bpm.engine.RuntimeService
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class TaakObjectAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TaakObjectListener::class)
    fun taakObjectListener(
        openNotificatieService: OpenNotificatieService,
        camundaTaskService: CamundaTaskService,
        valueResolverService: ValueResolverService,
        bpmnModelService: BpmnModelService,
        runtimeService: RuntimeService,
        documentService: DocumentService<JsonSchemaDocument>,
        processDocumentService: ProcessDocumentService,
        zaakService: ZaakService,
        openZaakService: OpenZaakService,
    ): TaakObjectListener {
        return TaakObjectListener(
            openNotificatieService,
            camundaTaskService,
            valueResolverService,
            bpmnModelService,
            runtimeService,
            documentService,
            processDocumentService,
            zaakService,
            openZaakService,
        )
    }

    @OptIn(ExperimentalContracts::class)
    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun taakObjectConnector(
        taakProperties: TaakProperties,
        valueResolverService: ValueResolverService,
        connectorService: ConnectorService,
        bsnProvider: BsnProvider,
        kvkProvider: KvkProvider
    ): TaakObjectConnector {
        return TaakObjectConnector(
            taakProperties,
            valueResolverService,
            connectorService,
            bsnProvider,
            kvkProvider
        )
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun taakProperties(): TaakProperties {
        return TaakProperties()
    }
}