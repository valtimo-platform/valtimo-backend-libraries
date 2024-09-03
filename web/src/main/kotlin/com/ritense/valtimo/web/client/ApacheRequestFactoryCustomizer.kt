package com.ritense.valtimo.web.client

import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestClient

class ApacheRequestFactoryCustomizer : RestClientCustomizer {

    override fun customize(restClientBuilder: RestClient.Builder) {
        restClientBuilder.requestFactory(HttpComponentsClientHttpRequestFactory())
    }

}