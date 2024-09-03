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

package com.ritense.valtimo.web.client

import com.ritense.valtimo.web.config.ValtimoHttpRestClientConfigurationProperties
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.core5.http.io.SocketConfig
import org.apache.hc.core5.util.Timeout
import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestClient

class ApacheRequestFactoryCustomizer(
    private val valtimoHttpRestClientConfigurationProperties: ValtimoHttpRestClientConfigurationProperties
) : RestClientCustomizer {

    override fun customize(restClientBuilder: RestClient.Builder) {
        // Connect timeout
        val connectionConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofSeconds(valtimoHttpRestClientConfigurationProperties.connectionTimeout))
            .build()

        // Socket timeout
        val socketConfig = SocketConfig.custom()
            .setSoTimeout(Timeout.ofSeconds(valtimoHttpRestClientConfigurationProperties.socketTimeout))
            .build()

        // Connection request timeout
        val requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofSeconds(valtimoHttpRestClientConfigurationProperties.readTimeout))
            .build()

        val connectionManager = PoolingHttpClientConnectionManager()
        connectionManager.defaultSocketConfig = socketConfig
        connectionManager.setDefaultConnectionConfig(connectionConfig)

        val httpClient = HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build()

        val apacheRequestFactory = HttpComponentsClientHttpRequestFactory(httpClient)
        restClientBuilder.requestFactory(apacheRequestFactory)
    }

}