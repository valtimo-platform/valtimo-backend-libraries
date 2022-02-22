/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.objectsapi.productaanvraag

import com.ritense.connector.domain.Connector
import com.ritense.connector.service.ConnectorService
import com.ritense.document.service.DocumentService
import com.ritense.klant.service.BurgerService
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.openzaak.service.ZaakInstanceLinkService
import com.ritense.openzaak.service.ZaakRolService
import com.ritense.processdocument.service.ProcessDocumentService
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class ProductAanvraagAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ProductAanvraagConnector::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun productAanvraagConnector(
        connectorService: ConnectorService,
        productAanvraagProperties: ProductAanvraagProperties,
    ): Connector {
        return ProductAanvraagConnector(connectorService, productAanvraagProperties)
    }

    @Bean
    @ConditionalOnMissingBean(ProductAanvraagProperties::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun productAanvraagProperties(): ProductAanvraagProperties {
        return ProductAanvraagProperties()
    }


    @Bean
    @ConditionalOnMissingBean(ProductAanvraagListener::class)
    fun productAanvraagListener(
        productAanvraagService: ProductAanvraagService,
        openNotificatieService: OpenNotificatieService,
    ): ProductAanvraagListener {
        return ProductAanvraagListener(productAanvraagService, openNotificatieService)
    }

    @Bean
    @ConditionalOnMissingBean(ProductAanvraagService::class)
    fun productAanvraagService(
        processDocumentService: ProcessDocumentService,
        documentService: DocumentService,
        openNotificatieService: OpenNotificatieService,
        zaakRolService: ZaakRolService,
        zaakInstanceLinkService: ZaakInstanceLinkService,
        burgerService: BurgerService?
    ): ProductAanvraagService {
        return ProductAanvraagService(
            processDocumentService,
            documentService,
            openNotificatieService,
            zaakRolService,
            zaakInstanceLinkService,
            burgerService
        )
    }
}
