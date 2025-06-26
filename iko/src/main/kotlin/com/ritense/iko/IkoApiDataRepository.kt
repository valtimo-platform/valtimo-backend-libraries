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
import com.fasterxml.jackson.databind.node.MissingNode
import com.ritense.iko.client.IkoApiClient
import com.ritense.valtimo.contract.iko.DataFilter
import com.ritense.valtimo.contract.iko.IkoConnector
import com.ritense.valtimo.contract.iko.PropertyField
import com.ritense.valtimo.contract.iko.PropertyField.Companion.PROPERTY_FIELD_TYPE_URL
import org.springframework.data.domain.Pageable

class IkoApiDataRepository(
    private val ikoApiClient: IkoApiClient,
) : IkoConnector {

    override fun getType() = "iko"

    override fun getIkoConnectorPropertyFields(): List<PropertyField> = listOf(PropertyField(BASE_URL, PROPERTY_FIELD_TYPE_URL))

    override fun getDataAggregatePropertyFields(): List<PropertyField> = listOf(PropertyField(DATA_AGGREGATE_PATH))

    override fun getDataRequestPropertyFields(): List<PropertyField> = listOf(PropertyField(DATA_REQUEST_PATH))

    override fun findAll(
        config: Map<String, Any?>,
        filters: List<DataFilter>,
        pageable: Pageable
    ): JsonNode {
        val url = config[BASE_URL]
        val dataAggregate = config[DATA_AGGREGATE_PATH]
        //return ikoApiClient.get(
        //    url,
        //    dataAggregate,
        //    filters
        //)
        return MissingNode.getInstance()
    }

    override fun findAll(
        config: Map<String, Any?>,
        filters: List<DataFilter>
    ): JsonNode {
        return findAll(config, filters, Pageable.unpaged())
    }

    companion object {
        private const val BASE_URL = "baseUrl"
        private const val DATA_AGGREGATE_PATH = "dataAggregatePath"
        private const val DATA_REQUEST_PATH = "dataRequestPath"
    }
}