/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.klant.autoconfiguration

import com.ritense.klant.client.OpenKlantClient
import com.ritense.klant.client.OpenKlantClientProperties
import com.ritense.klant.client.OpenKlantTokenGenerator
import com.ritense.klant.service.impl.BurgerService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenKlantClientProperties::class)
class KlantAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BurgerService::class)
    fun burgerService(
        openKlantClientProperties: OpenKlantClientProperties,
        openKlantClient: OpenKlantClient
    ): BurgerService {
        return BurgerService(openKlantClientProperties, openKlantClient)
    }

    @Bean
    fun openKlantTokenGenerator(): OpenKlantTokenGenerator {
        return OpenKlantTokenGenerator()
    }

    @Bean
    fun openKlantClient(
        openKlantClientProperties: OpenKlantClientProperties,
        openKlantTokenGenerator: OpenKlantTokenGenerator
    ): OpenKlantClient {
        return OpenKlantClient(openKlantClientProperties, openKlantTokenGenerator)
    }

}