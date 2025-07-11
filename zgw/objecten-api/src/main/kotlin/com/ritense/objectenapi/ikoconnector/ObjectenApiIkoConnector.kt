/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.objectenapi.ikoconnector

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.objectenapi.ObjectenApiAuthentication
import com.ritense.objectenapi.client.ObjectenApiClient
import com.ritense.valtimo.contract.iko.DataFilter
import com.ritense.valtimo.contract.iko.IkoConnector
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_SECRET
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_URL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI

class ObjectenApiIkoConnector(
    private val objectenApiClient: ObjectenApiClient,
    private val objectMapper: ObjectMapper,
) : IkoConnector {

    override fun getType() = "objectenApi"

    override fun getIkoConnectorPropertyFields(): List<PropertyField> = listOf(
        PropertyField(BASE_URL, PROPERTY_FIELD_TYPE_URL),
        PropertyField(TOKEN, PROPERTY_FIELD_TYPE_SECRET)
    )

    override fun getDataAggregatePropertyFields(): List<PropertyField> = listOf(
        PropertyField(VALTIMO_OBJECTTYPEN_API_URL, PROPERTY_FIELD_TYPE_URL)
    )

    override fun findAll(
        config: Map<String, Any?>,
        filters: List<DataFilter>,
        pageable: Pageable
    ): Page<JsonNode> {
        val searchString = filters.joinToString(",") { filter ->
            var property = filter.property.substringAfterLast(':')
            require(property.startsWith("/record/data/"))
            property = property.substringAfter("/record/data/")
                .replace("/", "__")
            val operator = com.ritense.objectenapi.client.Comparator.entries
                .single { it.name == filter.comparator.name }
            "${property}__${operator.value}__${filter.value}"
        }
        val (objecttypesApiUrl, objectypeId) = config[VALTIMO_OBJECTTYPEN_API_URL].toString().split("/objecttypes/")
        val objectList = objectenApiClient.getObjectsByObjecttypeUrlWithSearchParams(
            authentication = getAuthentication(config[TOKEN].toString()),
            objecttypesApiUrl = URI(objecttypesApiUrl),
            objectsApiUrl = URI(config[BASE_URL].toString()),
            objectypeId = objectypeId,
            searchString = searchString,
            pageable = pageable,
        )

        val jsonObjectList = objectMapper.valueToTree<ArrayNode>(objectList.results)
        return PageImpl(jsonObjectList.toList(), pageable, objectList.count.toLong())
    }

    override fun findById(config: Map<String, Any?>, id: Any): JsonNode {
        val objectUrl = UriComponentsBuilder.newInstance()
            .uri(URI(config[BASE_URL].toString()))
            .pathSegment("objects")
            .pathSegment(id.toString())
            .toUriString()

        val objectWrapper = objectenApiClient.getObject(
            authentication = getAuthentication(config[TOKEN].toString()),
            objectUrl = URI(objectUrl),
        )

        return objectMapper.valueToTree(objectWrapper)
    }

    protected fun getAuthentication(token: String): ObjectenApiAuthentication {
        return object : ObjectenApiAuthentication {
            override fun applyAuth(builder: RestClient.Builder): RestClient.Builder {
                return builder.defaultHeaders { headers ->
                    headers[HttpHeaders.AUTHORIZATION] = "Token $token"
                }
            }

            override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
                return next.exchange(request)
            }
        }
    }

    companion object {
        private const val BASE_URL = "objectenApiBaseUrl"
        private const val TOKEN = "token"
        private const val VALTIMO_OBJECTTYPEN_API_URL = "objecttypenApiUrl"
    }
}