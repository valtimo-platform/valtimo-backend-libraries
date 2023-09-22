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

package com.ritense.haalcentraal.brp.autoconfigure

import com.ritense.connector.service.ConnectorService
import com.ritense.haalcentraal.brp.client.HaalCentraalBrpClient
import com.ritense.haalcentraal.brp.connector.HaalCentraalBrpConnector
import com.ritense.haalcentraal.brp.connector.HaalCentraalBrpProperties
import com.ritense.haalcentraal.brp.web.rest.HaalCentraalBrpResource
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.web.reactive.function.client.WebClient

@Configuration
internal class HaalCentraalAutoConfiguration {

    // Connector
    @Bean
    @ConditionalOnMissingBean(HaalCentraalBrpConnector::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun haalCentraalBrpConnector(
        haalCentraalBrpProperties: HaalCentraalBrpProperties,
        haalCentraalBrpClient: HaalCentraalBrpClient
    ) : HaalCentraalBrpConnector {
        return HaalCentraalBrpConnector(haalCentraalBrpProperties, haalCentraalBrpClient)
    }

    @Bean
    @ConditionalOnMissingBean(HaalCentraalBrpProperties::class)
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun haalCentraalBrpProperties() : HaalCentraalBrpProperties {
        return HaalCentraalBrpProperties()
    }

    @Bean
    @ConditionalOnMissingBean(HaalCentraalBrpClient::class)
    fun haalCentraalBrpClient(
        webclientBuilder: WebClient.Builder
    ) : HaalCentraalBrpClient {
        return HaalCentraalBrpClient(webclientBuilder)
    }

    // Resource

    @Bean
    @ConditionalOnMissingBean(HaalCentraalBrpResource::class)
    fun haalCentraalBrpResource(
        connectorService: ConnectorService
    ) : HaalCentraalBrpResource {
        return HaalCentraalBrpResource(connectorService)
    }
}
