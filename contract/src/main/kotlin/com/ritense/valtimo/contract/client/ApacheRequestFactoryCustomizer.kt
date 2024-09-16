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

import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

class ApacheRequestFactoryCustomizer(
    private val valtimoHttpRestClientConfigurationProperties: ValtimoHttpRestClientConfigurationProperties
) : RestClientCustomizer {

    override fun customize(restClientBuilder: RestClient.Builder) {
        val apacheRequestFactory = HttpComponentsClientHttpRequestFactory()
        valtimoHttpRestClientConfigurationProperties.connectTimeout.let {
            apacheRequestFactory.setConnectTimeout(Duration.ofSeconds(it))
        }
        valtimoHttpRestClientConfigurationProperties.connectionRequestTimeout.let {
            apacheRequestFactory.setConnectionRequestTimeout(Duration.ofSeconds(it))
        }
        restClientBuilder.requestFactory(BufferingClientHttpRequestFactory(apacheRequestFactory))
    }

}