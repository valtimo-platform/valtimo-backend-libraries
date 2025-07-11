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

package com.ritense.iko

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.ritense.iko.client.IkoApiClient
import com.ritense.valtimo.contract.iko.Comparator
import com.ritense.valtimo.contract.iko.DataFilter
import com.ritense.valtimo.contract.iko.IkoConnector
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_URL
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.net.URI

class IkoApiConnector(
    private val ikoApiClient: IkoApiClient,
) : IkoConnector {

    override fun getType() = "iko"

    override fun getIkoConnectorPropertyFields(): List<PropertyField> =
        listOf(PropertyField(BASE_URL, PROPERTY_FIELD_TYPE_URL))

    override fun getDataAggregatePropertyFields(): List<PropertyField> = listOf(PropertyField(SEARCH_PATH))

    override fun getDataRequestPropertyFields(): List<PropertyField> = listOf(PropertyField(SEARCH_TYPE))

    override fun findAll(
        config: Map<String, Any?>,
        filters: List<DataFilter>,
        pageable: Pageable
    ): Page<JsonNode> {
        require(filters.all { it.comparator == Comparator.EQUAL_TO })
        val filterMap = filters.associate {
            it.property
                .substringAfterLast(':')
                .replace("/", "__")
                .trim('_') to it.value.toString()
        }
        val data = ikoApiClient.search(
            baseUrl = URI(config[BASE_URL].toString()),
            searchPath = config[SEARCH_PATH].toString(),
            searchType = config[SEARCH_TYPE].toString(),
            filters = filterMap,
        )

        val dataList: List<JsonNode> = if (data is ArrayNode) {
            data.toList()
        } else {
            val lists = data.filter { it is ArrayNode }
            if (lists.size == 1) {
                lists[0].toList()
            } else {
                listOf(data)
            }
        }

        return PageImpl(dataList, pageable, dataList.size.toLong())
    }

    override fun findById(config: Map<String, Any?>, id: Any): JsonNode {
        return ikoApiClient.getById(
            baseUrl = URI(config[BASE_URL].toString()),
            searchPath = config[SEARCH_PATH].toString(),
            id = id.toString()
        )
    }

    companion object {
        private const val BASE_URL = "baseUrl"
        private const val SEARCH_PATH = "searchPath"
        const val SEARCH_TYPE = "searchType"
    }
}