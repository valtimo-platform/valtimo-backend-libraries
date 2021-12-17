/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.service.impl

import com.ritense.openzaak.domain.connector.OpenZaakConfig
import com.ritense.openzaak.service.impl.model.ResultWrapper
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.ResolvableType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

data class OpenZaakRequestBuilder(
    private val restTemplate: RestTemplate,
    private val openZaakConfigService: OpenZaakConfigService,
    private val tokenGeneratorService: OpenZaakTokenGeneratorService,
    private var config: OpenZaakConfig? = null,
    var path: String? = null,
    var method: HttpMethod = HttpMethod.GET,
    private var queryParams: Map<String, String>? = null,
    private var body: Any? = null,
    private var acceptHeader: List<MediaType>? = null,
) {
    lateinit var url: String
    lateinit var requestEntity: HttpEntity<Any>

    fun config(config: OpenZaakConfig) = apply { this.config = config }

    fun path(path: String) = apply { this.path = path }

    fun get() = apply { this.method = HttpMethod.GET }

    fun post() = apply { this.method = HttpMethod.POST }

    fun put() = apply { this.method = HttpMethod.PUT }

    fun delete() = apply { this.method = HttpMethod.DELETE }

    fun queryParams(queryParams: Map<String, String>) = apply { this.queryParams = queryParams }

    fun body(body: Any) = apply { this.body = body }

    fun acceptHeader(acceptHeader: List<MediaType>) = apply { this.acceptHeader = acceptHeader }

    fun build() = apply {
        if (this.config == null) {
            this.config = openZaakConfigService.getOpenZaakConfig() ?: throw IllegalStateException("OpenZaak config is not found")
        }
        val builder = UriComponentsBuilder
            .fromUriString(this.config!!.url)
            .path(this.path!!)

        queryParams?.forEach {
            builder.queryParam(it.key, it.value)
        }
        url = builder.build().normalize().toUriString()
        requestEntity = if (body == null || (body is Map<*, *> && (body as Map<*, *>).isEmpty()))
            HttpEntity(buildHeaders(this.config!!))
        else
            HttpEntity(Mapper.get().writeValueAsString(body), buildPostHeaders(this.config!!))
    }

    fun <T> execute(responseClass: Class<out T>): T {
        val responseEntity = restTemplate.exchange(
            url,
            method,
            requestEntity,
            getType(responseClass)
        )
        when {
            responseEntity.statusCode.isError ->
                throw IllegalStateException("OpenZaak error with http call")
        }
        return responseEntity.body!!
    }

    fun <T> executeWrapped(responseClass: Class<out T>): ResultWrapper<T> {
        val responseEntity = restTemplate.exchange(
            url,
            method,
            requestEntity,
            getTypeWrapped(responseClass)
        )
        when {
            responseEntity.statusCode.isError ->
                throw IllegalAccessException("OpenZaak invalid http call")
        }
        return responseEntity.body!!
    }

    fun <T> executeForCollection(responseClass: Class<out T>): Collection<T> {
        val responseEntity = restTemplate.exchange(
            url,
            method,
            requestEntity,
            getTypeCollection(responseClass)
        )
        when {
            responseEntity.statusCode.isError ->
                throw IllegalAccessException("OpenZaak invalid http call")
        }
        return responseEntity.body!!
    }

    fun <T> getType(responseClass: Class<out T>): ParameterizedTypeReference<T> {
        val type: ParameterizedTypeReference<T> = ParameterizedTypeReference.forType(
            ResolvableType.forClass(responseClass).type
        )
        return type
    }

    fun <T> getTypeWrapped(responseClass: Class<out T>): ParameterizedTypeReference<ResultWrapper<T>> {
        val type: ParameterizedTypeReference<ResultWrapper<T>> = ParameterizedTypeReference.forType(
            ResolvableType.forClassWithGenerics(ResultWrapper::class.java, responseClass).type
        )
        return type
    }

    fun <T> getTypeCollection(responseClass: Class<out T>): ParameterizedTypeReference<Collection<T>> {
        val type: ParameterizedTypeReference<Collection<T>> = ParameterizedTypeReference.forType(
            ResolvableType.forClassWithGenerics(Collection::class.java, responseClass).type
        )
        return type
    }

    private fun buildHeaders(openZaakConfig: OpenZaakConfig): HttpHeaders {
        val generatedToken = tokenGeneratorService.generateToken(
            openZaakConfig.secret,
            openZaakConfig.clientId
        )
        val headers = HttpHeaders()
        if (this.acceptHeader == null) {
            headers.accept = listOf(MediaType.APPLICATION_JSON)
        } else {
            headers.accept = this.acceptHeader
        }
        headers.set("Accept-Crs", HEADER_DATA)
        headers.set("Content-Crs", HEADER_DATA)

        headers.set("Accept-Crs", "EPSG:4326")
        headers.set("Content-Crs", "EPSG:4326")
        headers.setBearerAuth(generatedToken)
        return headers
    }

    private fun buildPostHeaders(openZaakConfig: OpenZaakConfig): HttpHeaders {
        val generatedToken = tokenGeneratorService.generateToken(
            openZaakConfig.secret,
            openZaakConfig.clientId
        )
        val headers = HttpHeaders()
        if (this.acceptHeader == null) {
            headers.accept = listOf(MediaType.APPLICATION_JSON)
        } else {
            headers.accept = this.acceptHeader
        }
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Accept-Crs", HEADER_DATA)
        headers.set("Content-Crs", HEADER_DATA)
        headers.setBearerAuth(generatedToken)

        return headers
    }

    companion object {
        const val HEADER_DATA = "EPSG:4326"
    }

}