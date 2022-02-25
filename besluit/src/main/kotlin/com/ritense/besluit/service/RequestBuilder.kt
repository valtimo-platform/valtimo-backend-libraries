package com.ritense.besluit.service

import com.ritense.valtimo.contract.json.Mapper
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
        var path: String? = null,
        var token: String? = null,
        var url: String? = null,
        var method: HttpMethod? = null,
        var queryParams: MutableMap<String, Any> = mutableMapOf(),
        var requestEntity: HttpEntity<Any>? = null,
        var body: Any? = null
    ) {
        fun baseUrl(baseUrl: String) = apply { this.baseUrl=baseUrl }

        fun path(path: String) = apply { this.path = path }

        fun token(token: String) = apply { this.token = token }

        fun get() = apply { this.method = HttpMethod.GET }

        fun post() = apply { this.method = HttpMethod.POST }

        fun put() = apply { this.method = HttpMethod.PUT }

        fun delete() = apply { this.method = HttpMethod.DELETE }

        fun queryParams(queryParams: MutableMap<String, Any>?) = apply {
            if (queryParams != null) {
                this.queryParams = queryParams
            }
        }

        fun queryParam(key: String, value: Any?) = apply {
            if (value != null) {
                queryParams[key] = value
            }
        }

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
                HttpEntity(Mapper.INSTANCE.get().writeValueAsString(body), buildPostHeaders())
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
                        throw IllegalAccessException("Invalid http call")
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
            headers.setAll(defaultHeaders())
            applyAuth(headers)
            return headers
        }

        private fun buildPostHeaders(): HttpHeaders {
            val headers = HttpHeaders()
            headers.accept = listOf(MediaType.APPLICATION_JSON)
            headers.contentType = MediaType.APPLICATION_JSON
            headers.setAll(defaultHeaders())
            applyAuth(headers)
            return headers
        }

        private fun applyAuth(headers: HttpHeaders): HttpHeaders {
            headers.set("Authorization", "Token ${token!!}")
            return headers
        }

        companion object {
            val logger = KotlinLogging.logger {}
            private fun defaultHeaders(): Map<String, String> {
                return mapOf(
                    "Accept-Crs" to "EPSG:4326",
                    "Content-Crs" to "EPSG:4326"
                )
            }
        }
    }
}