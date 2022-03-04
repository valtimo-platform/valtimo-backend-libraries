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

package com.ritense.objectsapi.opennotificaties

import com.ritense.connector.domain.Connector
import com.ritense.connector.service.ConnectorService
import com.ritense.objectsapi.repository.AbonnementLinkRepository
import com.ritense.objectsapi.web.rest.OpenNotificatieResource
import com.ritense.objectsapi.web.rest.impl.OpenNotificatieResourceImpl
import com.ritense.openzaak.service.ZaakService
import com.ritense.resource.repository.OpenZaakResourceRepository
import com.ritense.resource.service.OpenZaakService
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class OpenNotificatieAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OpenNotificatieService::class)
    fun openNotificationService(
        connectorService: ConnectorService,
        applicationEventPublisher: ApplicationEventPublisher,
        abonnementLinkRepository: AbonnementLinkRepository,
        zaakService: ZaakService,
        openZaakService: OpenZaakService,
    ): OpenNotificatieService {
        return OpenNotificatieService(
            connectorService,
            applicationEventPublisher,
            abonnementLinkRepository,
            zaakService,
            openZaakService
        )
    }

    @Bean
    @ConditionalOnMissingBean(OpenNotificatieClient::class)
    fun openNotificatieClient(openNotificatieProperties: OpenNotificatieProperties): OpenNotificatieClient {
        return OpenNotificatieClient(openNotificatieProperties)
    }

    @Bean
    @ConditionalOnMissingBean(OpenNotificatieResource::class)
    fun openNotificatieResource(openNotificatieService: OpenNotificatieService): OpenNotificatieResource {
        return OpenNotificatieResourceImpl(openNotificatieService)
    }

    @Bean
    @ConditionalOnMissingBean(OpenNotificatieConnector::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun openNotificatieConnector(
        openNotificatieProperties: OpenNotificatieProperties,
        abonnementLinkRepository: AbonnementLinkRepository,
        openNotificatieClient: OpenNotificatieClient,
    ): Connector {
        return OpenNotificatieConnector(
            openNotificatieProperties,
            abonnementLinkRepository,
            openNotificatieClient
        )
    }

    @Bean
    @ConditionalOnMissingBean(OpenNotificatieProperties::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun openNotificatieProperties(
    ): OpenNotificatieProperties {
        return OpenNotificatieProperties()
    }
}
