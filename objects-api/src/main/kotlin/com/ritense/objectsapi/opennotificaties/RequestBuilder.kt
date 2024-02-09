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

package com.ritense.objectsapi.opennotificaties

import com.ritense.valtimo.contract.json.MapperSingleton
import mu.KotlinLogging
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.ResolvableType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

class RequestBuilder {

    companion object {
        fun builder() = Builder(restTemplate = RestTemplateBuilder().build())
    }

    data class Builder(
        val restTemplate: RestTemplate,
        var baseUrl: String? = null,
        var token: String? = null,
        var url: String? = null,
        var path: String? = null,
        var method: HttpMethod? = null,
        var queryParams: MutableMap<String, Any> = mutableMapOf(),
        var requestEntity: HttpEntity<Any>? = null,
        var body: Any? = null
    ) {
        fun token(token: String) = apply { this.token = token }

        fun baseUrl(baseUrl: String) = apply { this.baseUrl = baseUrl }

        fun path(path: String) = apply { this.path = path }

        fun get() = apply { this.method = HttpMethod.GET }

        fun post() = apply { this.method = HttpMethod.POST }

        fun delete() = apply { this.method = HttpMethod.DELETE }

        fun body(body: Any) = apply { this.body = body }

        private fun prepare() = apply {
            val builder = UriComponentsBuilder
                .fromUriString(baseUrl!!)
                .path(path!!)

            queryParams.forEach {
                builder.queryParam(it.key, it.value)
            }
            url = builder.build().normalize().toUriString()
            requestEntity = if (body != null)
                HttpEntity(MapperSingleton.get().writeValueAsString(body), buildPostHeaders())
            else
                HttpEntity(buildHeaders())
        }

        fun execute() {
            try {
                prepare()
                val responseEntity = restTemplate.exchange(
                    url!!,
                    method!!,
                    requestEntity,
                    Void::class.java
                )
                when {
                    responseEntity.statusCode.isError ->
                        throw IllegalStateException("Error with http call")
                }
            } catch (ex: Exception) {
                logger.error { ex.message }
                throw ex
            }
        }

        fun <T> execute(responseClass: Class<out T>): T {
            return execute(ParameterizedTypeReference.forType(responseClass))
        }

        fun <T> execute(responseType: ParameterizedTypeReference<T>): T {
            try {
                prepare()
                val responseEntity = restTemplate.exchange(
                    url!!,
                    method!!,
                    requestEntity,
                    responseType
                )
                when {
                    responseEntity.statusCode.isError ->
                        throw IllegalStateException("Error with http call")
                }
                return responseEntity.body!!
            } catch (ex: Exception) {
                logger.error { ex.message }
                throw ex
            }
        }

        fun <T> executeForCollection(responseClass: Class<out T>): Collection<T> {
            try {
                prepare()
                val responseEntity = restTemplate.exchange(
                    url!!,
                    method!!,
                    requestEntity,
                    getTypeCollection(responseClass)
                )
                when {
                    responseEntity.statusCode.isError ->
                        throw IllegalStateException("Invalid http call")
                }
                return responseEntity.body!!
            } catch (ex: Exception) {
                logger.error { ex.message }
                throw ex
            }
        }

        private fun <T> getTypeCollection(responseClass: Class<out T>): ParameterizedTypeReference<Collection<T>> {
            val type: ParameterizedTypeReference<Collection<T>> = ParameterizedTypeReference.forType(
                ResolvableType.forClassWithGenerics(Collection::class.java, responseClass).type
            )
            return type
        }

        private fun buildHeaders(): HttpHeaders {
            val headers = HttpHeaders()
            headers.accept = listOf(MediaType.APPLICATION_JSON)
            applyAuth(headers)
            return headers
        }

        private fun buildPostHeaders(): HttpHeaders {
            val headers = HttpHeaders()
            headers.accept = listOf(MediaType.APPLICATION_JSON)
            headers.contentType = MediaType.APPLICATION_JSON
            applyAuth(headers)
            return headers
        }

        private fun applyAuth(headers: HttpHeaders): HttpHeaders {
            headers.set("Authorization", "Bearer ${token!!}")
            return headers
        }

        companion object {
            val logger = KotlinLogging.logger {}
        }
    }
}