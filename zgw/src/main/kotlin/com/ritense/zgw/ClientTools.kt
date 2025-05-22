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

package com.ritense.zgw

import com.ritense.valtimo.contract.json.MapperSingleton
import com.ritense.zgw.domain.ZgwErrorResponse
import com.ritense.zgw.exceptions.ClientErrorException
import com.ritense.zgw.exceptions.RequestFailedException
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.ResolvableType
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.net.URI

class ClientTools {
    companion object {
        val logger = mu.KotlinLogging.logger {}

        @Deprecated("Use of WebClient is deprecated, this was used before")
        fun <T> getTypedPage(responseClass: Class<out T>): ParameterizedTypeReference<Page<T>> {
            return ParameterizedTypeReference.forType(
                ResolvableType.forClassWithGenerics(Page::class.java, responseClass).type
            )
        }

        @Deprecated("Use of WebClient is deprecated, this was used before")
        fun zgwErrorHandler(): ExchangeFilterFunction {
            return ExchangeFilterFunction.ofResponseProcessor { clientResponse: ClientResponse ->
                if (clientResponse.statusCode().is2xxSuccessful) {
                    return@ofResponseProcessor Mono.just<ClientResponse>(
                        clientResponse
                    )
                } else if (clientResponse.statusCode().is4xxClientError) {
                    clientResponse.bodyToMono(String::class.java)
                        .switchIfEmpty(Mono.just(""))
                        .flatMap { errorBody: String ->
                            try {
                                val zgwError = MapperSingleton.get().readValue(errorBody, ZgwErrorResponse::class.java)
                                Mono.error(ClientErrorException(zgwError, clientResponse.statusCode()))
                            } catch (e: Exception) {
                                Mono.error(RequestFailedException(errorBody, clientResponse.statusCode()))
                            }
                        }
                } else {
                    return@ofResponseProcessor clientResponse.bodyToMono<String>(String::class.java)
                        .flatMap<ClientResponse>({ errorBody: String ->
                            Mono.error(RequestFailedException(errorBody, clientResponse.statusCode()))
                        })
                }
            }
        }

        fun baseUrlToBuilder(builder: UriBuilder, uri: URI): UriBuilder {
            return builder.scheme(uri.scheme)
                .host(uri.host)
                .path(uri.path)
                .port(uri.port)
        }

        fun UriBuilder.optionalQueryParam(name: String, value: Any?): UriBuilder {
            value?.let { this.queryParam(name, it) }
            return this
        }
    }
}