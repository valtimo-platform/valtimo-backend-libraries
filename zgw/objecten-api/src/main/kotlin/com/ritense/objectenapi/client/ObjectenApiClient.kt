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

package com.ritense.objectenapi.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.objectenapi.ObjectenApiAuthentication
import com.ritense.objectenapi.event.ObjectCreated
import com.ritense.objectenapi.event.ObjectDeleted
import com.ritense.objectenapi.event.ObjectPatched
import com.ritense.objectenapi.event.ObjectUpdated
import com.ritense.objectenapi.event.ObjectViewed
import com.ritense.objectenapi.event.ObjectsListed
import com.ritense.outbox.OutboxService
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

class ObjectenApiClient(
    private val restClientBuilder: RestClient.Builder,
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper
) {

    fun getObject(
        authentication: ObjectenApiAuthentication,
        objectUrl: URI
    ): ObjectWrapper {
        val result = buildRestClient(authentication)
            .get()
            .uri(objectUrl)
            .retrieve()
            .body<ObjectWrapper>()!!

        val response = if (result.type.host == HOST_DOCKER_INTERNAL)
            result.copy(
                type = URI.create(
                    result.type.toString().replace(HOST_DOCKER_INTERNAL, "localhost")
                )
            ) else result

        outboxService.send {
            ObjectViewed(
                response.url.toString(),
                objectMapper.valueToTree(response)
            )
        }
        return result
    }

    fun getObjectsByObjecttypeUrl(
        authentication: ObjectenApiAuthentication,
        objecttypesApiUrl: URI,
        objectsApiUrl: URI,
        objectypeId: String,
        ordering: String? = "",
        pageable: Pageable
    ): ObjectsList {
        val host = if (objecttypesApiUrl.host == "localhost") {
            HOST_DOCKER_INTERNAL
        } else {
            objecttypesApiUrl.host
        }
        val objectTypeUrl = UriComponentsBuilder.newInstance()
            .uri(objecttypesApiUrl)
            .host(host)
            .pathSegment("objecttypes")
            .pathSegment(objectypeId)
            .toUriString()

        val result = buildRestClient(authentication, objectsApiUrl.toASCIIString())
            .get()
            .uri { builder ->
                builder.path("objects")
                    .queryParam("type", objectTypeUrl)
                    .queryParam("pageSize", pageable.pageSize)
                    .queryParam("page", pageable.pageNumber + 1) //objects api pagination starts at 1 instead of 0
                    .queryParam("ordering", ordering)
                    .build()
            }
            .header(ACCEPT_CRS, EPSG_4326)
            .retrieve()
            .body<ObjectsList>()!!

        outboxService.send {
            ObjectsListed(
                objectMapper.valueToTree(result.results)
            )
        }
        return result
    }

    fun getObjectsByObjecttypeUrlWithSearchParams(
        authentication: ObjectenApiAuthentication,
        objecttypesApiUrl: URI,
        objectsApiUrl: URI,
        objectypeId: String,
        searchString: String,
        ordering: String? = "",
        pageable: Pageable
    ): ObjectsList {
        val host = if (objecttypesApiUrl.host == "localhost") {
            HOST_DOCKER_INTERNAL
        } else {
            objecttypesApiUrl.host
        }
        val objectTypeUrl = UriComponentsBuilder.newInstance()
            .uri(objecttypesApiUrl)
            .host(host)
            .pathSegment("objecttypes")
            .pathSegment(objectypeId)
            .toUriString()

        val result = buildRestClient(authentication, objectsApiUrl.toASCIIString())
            .get()
            .uri { builder ->
                builder.path("objects")
                    .queryParam("type", objectTypeUrl)
                    .queryParam("pageSize", pageable.pageSize)
                    .queryParam("page", pageable.pageNumber + 1) //objects api pagination starts at 1 instead of 0
                    .queryParam("data_attrs", searchString)
                    .queryParam("ordering", ordering)
                    .build()
            }
            .header(ACCEPT_CRS, EPSG_4326)
            .retrieve()
            .body<ObjectsList>()!!

        outboxService.send {
            ObjectsListed(
                objectMapper.valueToTree(result.results)
            )
        }
        return result
    }

    fun createObject(
        authentication: ObjectenApiAuthentication,
        objectsApiUrl: URI,
        objectRequest: ObjectRequest
    ): ObjectWrapper {
        val objectRequestCorrectedHost = if (objectRequest.type.host == "localhost") {
            objectRequest.copy(
                type = UriComponentsBuilder
                    .fromUri(objectRequest.type)
                    .host(HOST_DOCKER_INTERNAL)
                    .build()
                    .toUri()
            )
        } else {
            objectRequest
        }

        val result = buildRestClient(authentication, objectsApiUrl.toASCIIString())
            .post()
            .uri("objects")
            .header(ACCEPT_CRS, EPSG_4326)
            .header(CONTENT_CRS, EPSG_4326)
            .body(objectRequestCorrectedHost)
            .retrieve()
            .body<ObjectWrapper>()!!

        outboxService.send {
            ObjectCreated(
                result.url.toString(),
                objectMapper.valueToTree(result)
            )
        }
        return result
    }

    fun objectPatch(
        authentication: ObjectenApiAuthentication,
        objectUrl: URI,
        objectRequest: ObjectRequest
    ): ObjectWrapper {
        val objectRequestCorrectedHost = if (objectRequest.type.host == "localhost") {
            objectRequest.copy(
                type = UriComponentsBuilder
                    .fromUri(objectRequest.type)
                    .host(HOST_DOCKER_INTERNAL)
                    .build()
                    .toUri()
            )
        } else {
            objectRequest
        }
        val result = buildRestClient(authentication)
            .patch()
            .uri(objectUrl)
            .header(CONTENT_CRS, EPSG_4326)
            .body(objectRequestCorrectedHost)
            .retrieve()
            .body<ObjectWrapper>()!!

        outboxService.send {
            ObjectPatched(
                result.url.toString(),
                objectMapper.valueToTree(result)
            )
        }
        return result
    }

    fun objectUpdate(
        authentication: ObjectenApiAuthentication,
        objectUrl: URI,
        objectRequest: ObjectRequest
    ): ObjectWrapper {
        val objectRequestCorrectedHost = if (objectRequest.type.host == "localhost") {
            objectRequest.copy(
                type = UriComponentsBuilder
                    .fromUri(objectRequest.type)
                    .host(HOST_DOCKER_INTERNAL)
                    .build()
                    .toUri()
            )
        } else {
            objectRequest
        }
        val result = buildRestClient(authentication)
            .put()
            .uri(objectUrl)
            .header(CONTENT_CRS, EPSG_4326)
            .body(objectRequestCorrectedHost)
            .retrieve()
            .body<ObjectWrapper>()!!

        outboxService.send {
            ObjectUpdated(
                result.url.toString(),
                objectMapper.valueToTree(result)
            )
        }
        return result
    }

    fun deleteObject(authentication: ObjectenApiAuthentication, objectUrl: URI): HttpStatus {
        val result = buildRestClient(authentication)
            .delete()
            .uri(objectUrl)
            .header(CONTENT_CRS, EPSG_4326)
            .retrieve()
            .toBodilessEntity()

        outboxService.send {
            ObjectDeleted(objectUrl.toString())
        }
        return HttpStatus.valueOf(result.statusCode.value())
    }

    private fun buildRestClient(authentication: ObjectenApiAuthentication, baseURL: String? = null): RestClient {
        return restClientBuilder
            .clone()
            .apply {
                authentication.applyAuth(it)
                baseURL?.let { url -> it.baseUrl(url) }
            }
            .build()
    }

    companion object {
        private const val HOST_DOCKER_INTERNAL = "host.docker.internal"
        private const val CONTENT_CRS = "Content-Crs"
        private const val ACCEPT_CRS = "Accept-Crs"
        private const val EPSG_4326 = "EPSG:4326"
    }
}
