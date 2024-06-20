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

package com.ritense.valtimo.contract.http

import com.ritense.valtimo.contract.json.MapperSingleton
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class ValtimoHttpAutoConfiguration {

    @ConditionalOnMissingBean(RestTemplateBuilder::class)
    @Bean
    fun restTemplateBuilderHolder(
        restTemplateBuilder: RestTemplateBuilder,
        valtimoHttpRestTemplatesConfigurationProperties: ValtimoHttpRestTemplateConfigurationProperties
    ): RestTemplateBuilderHolder {
        val valtimoRestTemplateBuilder =
            RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(valtimoHttpRestTemplatesConfigurationProperties.connectionTimeout))
                .setReadTimeout(Duration.ofSeconds(valtimoHttpRestTemplatesConfigurationProperties.readTimeout))

        RestTemplateBuilderHolder.set(valtimoRestTemplateBuilder)

        return RestTemplateBuilderHolder
    }

    @Bean
    @ConditionalOnMissingBean(WebClientBuilderHolder::class)
    fun valtimoWebclientBuilderHolder(
        valtimoHttpWebClientConfigurationProperties: ValtimoHttpWebClientConfigurationProperties,
        webClientBuilder: WebClient.Builder
    ): WebClientBuilderHolder {
        val objectMapper = MapperSingleton.get()
        val httpClient = HttpClient
            .create()
            .responseTimeout(
                Duration.ofSeconds(
                    valtimoHttpWebClientConfigurationProperties.connectionTimeout.toLong()
                )
            )
            .doOnConnected { conn: Connection ->
                conn.addHandlerLast(
                    ReadTimeoutHandler(valtimoHttpWebClientConfigurationProperties.readTimeout)
                )
            }

        webClientBuilder
            .clientConnector(
                ReactorClientHttpConnector(httpClient)
            )
            .codecs { configurer ->
                with(configurer.defaultCodecs()) {
                    jackson2JsonEncoder(
                        Jackson2JsonEncoder(objectMapper)
                    )
                    jackson2JsonDecoder(
                        Jackson2JsonDecoder(objectMapper)
                    )
                }
            }

        WebClientBuilderHolder.set(webClientBuilder)
        return WebClientBuilderHolder
    }
}