package com.ritense.openproduct.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.tokenauthentication.plugin.TokenAuthenticationPlugin
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class OpenProductClient() {

    fun createProduct(
        baseUrl: String,
        authenticationPlugin: TokenAuthenticationPlugin,
        request: ProductRequest
    ): String? {
        val restClient = getRestclient(baseUrl, authenticationPlugin)
        val objectMapper = jacksonObjectMapper()
        val requestJson = objectMapper.writeValueAsString(request)

        val response = restClient.post()
            .uri("/producten")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(requestJson)
            .retrieve()

        val result = response.toEntity(String::class.java)
            ?: throw IllegalStateException("Failed to create product")

        return result.body
    }

    fun updateProduct(
        baseUrl: String,
        authenticationPlugin: TokenAuthenticationPlugin,
        request: MutableMap<String, Any>
    ): String? {
        val restClient = getRestclient(baseUrl, authenticationPlugin)
        val objectMapper = jacksonObjectMapper()

        val uuid = request["uuid"] as String

        val requestJson = objectMapper.writeValueAsString(request)

        val response = restClient.patch()
            .uri("/producten/$uuid")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(requestJson)
            .retrieve()

        val result = response.toEntity(String::class.java)
            ?: throw IllegalStateException("Failed to update product")

        return result.body
    }

    fun deleteProduct(
        baseUrl: String,
        authenticationPlugin: TokenAuthenticationPlugin,
        uuid: String
    ): String? {
        val restClient = getRestclient(baseUrl, authenticationPlugin)

        val response = restClient.delete()
            .uri("/producten/$uuid")
            .retrieve()

        val result = response.toEntity(String::class.java)
            ?: throw IllegalStateException("Failed to delete product")

        return result.body
    }

    fun getRestclient(baseUrl: String, authenticationPlugin: TokenAuthenticationPlugin): RestClient {
        return authenticationPlugin.applyAuth(RestClient.builder())
            .baseUrl(baseUrl)
            .build()
    }

}
