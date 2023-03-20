package com.ritense.besluitenapi.client

import com.ritense.besluitenapi.BesluitenApiAuthentication
import com.ritense.besluitenapi.domain.BesluitInformatieObject
import com.ritense.besluitenapi.domain.CreateBesluitInformatieObject
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity
import java.net.URI

class BesluitenApiClient(
    private val webClientBuilder: WebClient.Builder
) {
    fun createBesluitInformatieObject(
        authentication: BesluitenApiAuthentication,
        url: URI,
        besluitInformatieObject: CreateBesluitInformatieObject
    ): BesluitInformatieObject? {
        return webClientBuilder.clone()
            .filter(authentication)
            .build()
            .post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(besluitInformatieObject)
            .retrieve()
            .toEntity<BesluitInformatieObject>()
            .block()?.body
    }
}