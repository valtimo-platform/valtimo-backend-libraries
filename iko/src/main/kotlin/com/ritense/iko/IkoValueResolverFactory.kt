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
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.ritense.iko.service.IkoDataAggregateService
import com.ritense.iko.service.IkoDataRequestService
import com.ritense.iko.service.IkoSearchFieldService
import com.ritense.valtimo.contract.iko.DataFilter
import com.ritense.valueresolver.ValueResolverFactory
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.DOCUMENT_ID
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.ID
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.IKO_DATA_AGGREGATE_KEY
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.IKO_DATA_REQUEST_KEY
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.PAGEABLE
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.PROCESS_INSTANCE_ID
import com.ritense.valueresolver.ValueResolverPropertyKey.Companion.VARIABLE_SCOPE
import org.operaton.bpm.engine.delegate.VariableScope
import org.springframework.data.domain.Pageable
import java.util.function.Function

class IkoValueResolverFactory(
    private val ikoDataAggregateService: IkoDataAggregateService,
    private val ikoDataRequestService: IkoDataRequestService,
    private val ikoSearchFieldService: IkoSearchFieldService,
    private val objectMapper: ObjectMapper,
) : ValueResolverFactory {

    override fun supportedPrefix(): String {
        return "iko"
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        return createResolver(mapOf(DOCUMENT_ID to documentId))
    }

    override fun createResolver(processInstanceId: String, variableScope: VariableScope): Function<String, Any?> {
        return createResolver(mapOf(PROCESS_INSTANCE_ID to processInstanceId, VARIABLE_SCOPE to variableScope))
    }

    override fun createResolver(properties: Map<String, Any>): Function<String, Any?> {
        val ikoDataAggregateKey = properties[IKO_DATA_AGGREGATE_KEY]?.toString()
        val id = properties[ID]?.toString()
        if (ikoDataAggregateKey != null && id != null) {
            val data = ikoDataAggregateService.getDataById(ikoDataAggregateKey, id)
            return toValueFunction(data)
        }

        val ikoDataRequestKey = properties[IKO_DATA_REQUEST_KEY]?.toString()
        require(ikoDataAggregateKey != null || ikoDataRequestKey != null) { "Missing ValueResolver context." }
        val (dataRequest, searchFields) = ikoDataRequestService.findAll(
            key = ikoDataRequestKey,
            ikoDataAggregateKey = ikoDataAggregateKey,
        ).map { dataRequest ->
            dataRequest to ikoSearchFieldService.findAllSearchFieldsByIkoDataRequest(
                ikoDataAggregateKey = dataRequest.id.ikoDataAggregate.key,
                ikoDataRequestKey = dataRequest.id.key
            )
        }
            .filter { (_, searchFields) -> searchFields.all { properties.contains(it.key) || !it.required } }
            .map { (dataRequest, searchFields) -> dataRequest to searchFields.filter { properties.contains(it.key) } }
            .firstOrNull() ?: return Function { null }

        val filters = searchFields.map { searchField -> DataFilter(searchField.path, properties[searchField.key]) }
        val pageable = properties[PAGEABLE] as Pageable? ?: Pageable.unpaged()
        val dataPaged = ikoDataRequestService.searchData(
            key = dataRequest.id.key,
            ikoDataAggregateKey = dataRequest.id.ikoDataAggregate.key,
            filters = filters,
            pageable = pageable
        )
        val data = objectMapper.valueToTree<ArrayNode>(dataPaged.content)
        return toValueFunction(data)
    }

    private fun toValueFunction(data: JsonNode): Function<String, Any?> {
        return Function { jsonPointer ->
            objectMapper.treeToValue<Any?>(data.at(jsonPointer))
        }
    }
}