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

package com.ritense.valtimo.contract.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile

@AutoConfiguration
@EnableConfigurationProperties(ValtimoHttpRestClientConfigurationProperties::class)
class RestClientAutoConfiguration {

    @Bean
    fun requestFactoryCustomizer(
        valtimoHttpRestClientConfigurationProperties: ValtimoHttpRestClientConfigurationProperties
    ): ApacheRequestFactoryCustomizer {
        return ApacheRequestFactoryCustomizer(valtimoHttpRestClientConfigurationProperties)
    }

    @Bean
    @ConditionalOnMissingBean(HostDockerInternalRestClientCustomizer::class)
    @ConditionalOnProperty(value = ["valtimo.docker.filter.enabled"], havingValue = "true", matchIfMissing = false)
    fun hostDockerInternalRestClientCustomizer(
        @Value("\${valtimo.docker.filter.ports:8001,8002,8003,8006,8010,8011}") dockerPorts: List<String>,
        @Value("\${valtimo.docker.filter.rewriteRequestHost:false}") rewriteRequestHost: Boolean,
        @Value("\${server.port:8080}") webServerPort: Int,
    ): HostDockerInternalRestClientCustomizer {
        return HostDockerInternalRestClientCustomizer(
            dockerPorts.map { it.toInt() },
            rewriteRequestHost,
            webServerPort,
        )
    }

    @Bean
    @ConditionalOnMissingBean(HostDockerInternalRestClientCustomizer::class)
    @Profile("dev")
    @ConditionalOnProperty(value = ["valtimo.docker.filter.enabled"], havingValue = "true", matchIfMissing = true)
    fun devHostDockerInternalRestClientCustomizer(
        @Value("\${valtimo.docker.filter.ports:8001,8002,8003,8006,8010,8011}") dockerPorts: List<String>,
        @Value("\${valtimo.docker.filter.rewriteRequestHost:false}") rewriteRequestHost: Boolean,
        @Value("\${server.port:8080}") webServerPort: Int,
    ): HostDockerInternalRestClientCustomizer {
        return HostDockerInternalRestClientCustomizer(
            dockerPorts.map { it.toInt() },
            rewriteRequestHost,
            webServerPort,
        )
    }
}